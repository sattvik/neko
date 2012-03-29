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

(ns neko.-protocols.resolvable
  "Home of the Resolvable protocol and related utilities."
  {:author "Daniel Solano Gómez"}
  (:import android.content.Context))

(defn- resolve-from-keyword
  "Resolves a resource reprensented as a keyword."
  [context type name]
  {:pre  [(instance? Context context)
          (keyword? type)
          (keyword? name)]
   :post [(integer? %)]}
  (let [package (or (namespace name) (.getPackageName ^Context context))
        type    (clojure.core/name type)
        name    (.. (clojure.core/name name) (replace \- \_) (replace \. \_))]
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
                         name)))))))

(defprotocol Resolvable
  "Protocol for resolving a resource given some sort of id, a context, and a
  type."
  (resolve-it [id context type])
  (resolve-id [id context])
  (resolve-string [id context])
  (resolve-layout [id context]))

(extend-protocol Resolvable
  Integer
  (resolve-it [id _1 _2] id)
  (resolve-id [id _] id)
  (resolve-string [id _] id)
  (resolve-layout [id _] id)

  Long
  (resolve-it [id _1 _2] (.intValue id))
  (resolve-id [id _] (.intValue id))
  (resolve-string [id _] (.intValue id))
  (resolve-layout [id _] (.intValue id))

  clojure.lang.Keyword
  (resolve-it
    [name context type] (resolve-from-keyword context type name))
  (resolve-id
    [name context] (resolve-from-keyword context :id name))
  (resolve-string
    [name context] (resolve-from-keyword context :string name))
  (resolve-layout
    [name context] (resolve-from-keyword context :layout name)))

(defn resolvable?
  "Determines whether the argument represents an argument that can be resolved
  as a resource."
  [x]
  (satisfies? Resolvable x))
