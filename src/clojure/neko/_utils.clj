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

(ns neko.-utils
  "Internal utilities used by Neko, not intended for external consumption."
  {:author "Daniel Solano Gómez"})

(defn static-field-value
  "Takes a keyword and converts it to a field name by getting the name from the
  keyword, converting all hypens to underscores, capitalizing all letters, and
  applying the transformation function."
  ([^Class class field xform]
   {:pre [(class? class)
          (keyword? field)
          (fn? xform)]}
   (let [field (.. (name field) (replace \- \_) toUpperCase)
         field ((fn [x] {:post [(string? %)]} (xform x)) field)
         field (.getField class ^String field)]
     (.get field nil)))
  ([class field]
   (static-field-value class field identity)))

(defn integer-or-keyword?
  "Convenient method for testing if the argument is an integer or a keyword."
  [x]
  (or (integer? x)
      (keyword? x)))
