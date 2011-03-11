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

(ns neko.activity
  "Utilities to aid in working with an activity."
  {:author "Daniel Solano Gómez"}
  (:import android.app.Activity)
  (:require [neko.context :as context]))

(def 
  ^{:doc "The current activity to operate on."}
  *activity*)

(defmacro with-activity
  "Evaluates body such that both *activity* and *context* are bound to the given activiy."
  [activity & body]
  `(binding [context/*context* ~activity
             *activity* ~activity]
     ~@body))
