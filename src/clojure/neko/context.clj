; Copyright © 2011 Sattvik Software & Technology Resources, Ltd. Co.
; All rights reserved.
;
; This program and the accompanying materials are made available under the
; terms of the Eclipse Public License v1.0 which accompanies this distribution,
; and is available at <http://www.eclipse.org/legal/epl-v10.html>.
;
; By using this software in any fashion, you are agreeing to be bound by the
; terms of this license.  You must not remove this notice, or any other, from
; this software.

(ns neko.context
  "Utilities to aid in working with a context."
  {:author "Daniel Solano Gómez"}
  (:import android.content.Context))

(def 
  ^{:doc "The current context object to operate on."}
  *context*)

(defmacro with-context
  "Evaluates body such that *context* is bound to the given context."
  [context & body]
  `(binding [*context* ~context]
     ~@body))

(defn resolve-resource
  "Resolves the resource ID of a given type with the given name.  For example,
  to refer to what in Java would be R.string.my_string, you can use:

    (resolve-resource context :string :my_string)

  The type should be a keyword corresponding to a resource type such as
  :layout, :attr, or :id.
  
  The name should be a keyword.  If the keyword has a namespace, it will be
  used as the package from which to retrieve the resources.  Generally, this is
  not required as the default will be the context’s package.  However, this can
  be used to access the resources from the platform.  For example, the
  equivalent to android.R.layout.simple_list_item_1 is:

    (resolve-resource context :layout :android/simple_list_item_1)

  The name portion of the name argument will be converted to a string and any
  hyphens or periods will be transformed to underscores.  Note that hyphens are
  not valid in Android names, but are allowed here to be Clojure friendly.
  
  If the name argument is an integer, it is assumed to be a valid resource ID
  and will be returned as is without any processing.

  If used within a with-context form, the context may be left out.

  Note that this method is much slower than using the constant directly, but
  results in more readable code."
  ([context type name]
   (if (integer? name)
     name
     (let [package (or (namespace name) (.getPackageName context))
           type  (clojure.core/name type)
           name  (.. (clojure.core/name name) (replace \- \_) (replace \. \_))]
       (try
         (let [class   (Class/forName (str package ".R$" type))
               field   (.getField class name)]
           (.getInt field nil))
         (catch ClassNotFoundException _
           (throw (IllegalArgumentException.
                    (format "Could not find class corresponding to '%s.R.%s'"
                            package
                            type))))
         (catch NoSuchFieldException _
           (throw (IllegalArgumentException.
                    (format "Resource not found: '%s.R.%s.%s'"
                            package
                            type
                            name))))))))
  ([type name]
   (resolve-resource *context* type name)))

(defn get-string
  "Gets the localized string with the given ID or name from the context.  If an
  integer ID is given, it will be used directly; otherwise, the name will be
  resolved using resolve-resource.
  
  If additional arguments are supplied, the string will be interpreted as a
  format and the arguments will be applied to the format.
  
  If within a with-context form, the context parameter may be omitted."
  {:arglists '([context? id-or-name & args])}
  ([id-or-name]
   (get-string *context* id-or-name))
  ([context id-or-name]
   (if (instance? Context context)
     (.getString context (resolve-resource context :string id-or-name))
     (get-string *context* context id-or-name)))
  ([context id-or-name & args]
   (if (instance? Context context)
     (.getString context
                 (resolve-resource context :string id-or-name)
                 (to-array args))
     (apply get-string *context* context id-or-name args))))

(defn get-layout
  "Finds the resource ID for the layout with the given name.  This is simply a
  convenient way of calling (resolve-resource context :layout name)."
  ([name]
   (get-layout *context* name))
  ([context name]
   (resolve-resource context :layout name)))

(defn get-system-service
  "Gets a system service from the context.  The type argument is a keyword that
  names the service type.  Examples include :alarm for the alarm service and
  :layout-inflater for the layout inflater service."
  ([type]
   (get-system-service *context* type))
  ([context type]
   ; using reflection here allows forward-compatibility
   (try
     (let [field-name (.. (name type) (replace \- \_) toUpperCase
                        (concat "_SERVICE"))
           field      (.getField Context field-name)
           value      (.get field nil)]
       (.getSystemService context value))
     (catch NoSuchFieldException _
       (throw (IllegalArgumentException.
                (format "No service type '%s' exists." (name type))))))))
