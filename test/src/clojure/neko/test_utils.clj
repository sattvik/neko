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

(ns neko.test-utils
  "Utilities for the Neko test suite."
  {:author "Daniel Solano Gómez"}
  (:import android.content.Intent))

(defn- start-intent
  "Returns a suitable start intent for activity unit test cases."
  []
  (doto (Intent.)
    (.addCategory Intent/CATEGORY_LAUNCHER)
    (.addFlags Intent/FLAG_ACTIVITY_NEW_TASK)
    (.setClassName "com.sattvik.neko.tests"
                   "com.sattvik.neko.tests.TestActivity")
    (.setAction Intent/ACTION_MAIN)))

(defn start-activity
  "Starts the activity for a test."
  [test]
  (.startActivity test (start-intent) nil nil))
