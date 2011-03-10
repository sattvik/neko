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

(ns neko.activity-test
  "Tests for the neko.activity namespace."
  {:author "Daniel Solano Gómez"}
  (:gen-class :main false
              :init init
              :constructors {[] [Class]}
              :extends android.test.ActivityUnitTestCase
              :methods [[testWithActivity [] void]
                        ]
              :exposes-methods {setUp superSetUp})
  (:use neko.activity
        [neko.context :only [*context*]]
        neko.test-utils
        junit.assert))

(def activity (atom nil))

(defn -init []
  [[com.sattvik.neko.test_app.TestActivity] nil])

(defn -setUp [this]
  (.superSetUp this)
  (reset! activity (.startActivity this (start-intent) nil nil)))

(defn -testWithActivity
  "Test that the with-activity macro."
  [this]
  (is-not (bound? #'*context*))
  (is-not (bound? #'*activity*))
  (with-activity @activity
    (is-same @activity *context*)
    (is-same @activity *activity*))
  (is-not (bound? #'*context*))
  (is-not (bound? #'*activity*)))
