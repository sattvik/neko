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
  {:author "Daniel Solano Gómez"})

(defprotocol Resolvable
  "Protocol for resolving a resource given some sort of id, a context, and a
  type."
  (resolve-it [id context type])
  (resolve-id [id context])
  (resolve-string [id context])
  (resolve-layout [id context]))

(extend-type Integer
  Resolvable
  (resolve-it [id _1 _2] id)
  (resolve-id [id _] id)
  (resolve-string [id _] id)
  (resolve-layout [id _] id))

(defn resolvable?
  "Determines whether the argument represents an argument that can be resolved
  as a resource."
  [x]
  (satisfies? Resolvable x))
