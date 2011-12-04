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
  (:import android.content.Context)
  (:use neko.-protocols.resolvable
        neko.-utils))

(def 
  ^{:doc "The current context object to operate on."
    :dynamic true}
  *context*)

(defn context?
  "Predict to determine if the is an instance of Context."
  [x]
  (instance? Context x))

(defn has-*context*?
  "Tests that the caller has a valid binding for *context*."
  []
  (and (bound? #'*context*)
       (context? *context*)))

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
  ([^Context context type name]
   {:pre  [(context? context)
           (keyword? type)
           (resolvable? name)]
    :post [(integer? %)]}
   (resolve-it name context type))
  ([type name]
   {:pre  [(has-*context*?)
           (keyword? type)
           (resolvable? name)]
    :post [(integer? %)]}
   (resolve-it name *context* type)))

(defn get-id
  "Finds the ID for the XML item with the given name.  This is simply a
  convenient way of calling (resolve-resource context :id name)."
  ([name]
   {:pre  [(has-*context*?)
           (resolvable? name)]
    :post [(integer? %)]}
   (resolve-id name *context*))
  ([context name]
   {:pre [(context? context)
          (resolvable? name)]
    :post [(integer? %)]}
   (resolve-id name context)))

(defn get-string
  "Gets the localized string with the given ID or name from the context.  If an
  integer ID is given, it will be used directly; otherwise, the name will be
  resolved using resolve-resource.
  
  If additional arguments are supplied, the string will be interpreted as a
  format and the arguments will be applied to the format.
  
  If within a with-context form, the context parameter may be omitted."
  {:arglists '([context? id-or-name & args])}
  ([id-or-name]
   {:pre  [(has-*context*?)
           (resolvable? id-or-name)]
    :post [(string? %)]}
   (get-string *context* id-or-name))
  ([context id-or-name]
   {:pre  [(or (and (context? context)
                    (resolvable? id-or-name))
               (and (has-*context*?)
                    (resolvable? context)))]
    :post [(string? %)]}
   (if (instance? Context context)
     (.getString ^Context context (resolve-string id-or-name context))
     (get-string *context* context id-or-name)))
  ([context id-or-name & args]
   {:pre  [(or (and (context? context)
                    (resolvable? id-or-name))
               (and (has-*context*?)
                    (resolvable? context)))]
    :post [(string? %)]}
   (if (instance? Context context)
     (.getString ^Context context
                 (resolve-string id-or-name context)
                 (to-array args))
     (apply get-string *context* context id-or-name args))))

(defn get-layout
  "Finds the resource ID for the layout with the given name.  This is simply a
  convenient way of calling (resolve-resource context :layout name)."
  ([name]
   {:pre  [(has-*context*?)
           (resolvable? name)]
    :post [(integer? %)]}
   (resolve-layout name *context*))
  ([context name]
   {:pre [(context? context)
          (resolvable? name)]
    :post [(integer? %)]}
   (resolve-layout name context)))

(defn get-system-service
  "Gets a system service from the context.  The type argument is a keyword that
  names the service type.  Examples include :alarm for the alarm service and
  :layout-inflater for the layout inflater service.  If within a with-context
  form, the context may be omitted."
  ([type]
   {:pre [(has-*context*?)]}
   (get-system-service *context* type))
  ([^Context context type]
   {:pre [(keyword? type)
          (context? context)]}
   ; using reflection here allows forward-compatibility
   (try
     (.getSystemService context (static-field-value Context type #(str % "_SERVICE")))
     (catch NoSuchFieldException _
       (throw (IllegalArgumentException.
                (format "No service type '%s' exists." (name type))))))))

(defn inflate-layout
  "Inflates the layout with the given ID or name.  The id-or-name argument must
  be an integer ID or a keyword name for the desired layout.  If within a
  with-context form, the context may be omitted."
  ([id-or-name]
   {:pre  [(has-*context*?)
           (resolvable? id-or-name)]
    :post [(instance? android.view.View %)]}
   (get-layout *context* id-or-name))
  ([context id-or-name]
   {:pre [(context? context)
          (resolvable? id-or-name)]
    :post [(instance? android.view.View %)]}
   (.. android.view.LayoutInflater
     (from context)
     (inflate ^Integer (resolve-layout id-or-name context)
              nil))))
