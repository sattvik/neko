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

(ns neko.dialog.alert-test
  "Tests for the neko.alert-dialog namespace."
  {:author "Daniel Solano Gómez"}
  (:gen-class :main false
              :extends android.test.AndroidTestCase
              :methods [[testNewBuilder [] void]
                        [testBasicCreate [] void]
                        ]
              :exposes-methods {setUp superSetUp}
              )
  (:import android.app.AlertDialog$Builder
           neko.dialog.alert.FunctionalAlertDialogBuilder)
  (:use junit.assert
        neko.context
        neko.find-view
        neko.dialog.alert
        )
  )

(def context (atom nil))

(defn -setUp
  "Sets up the context atom"
  [this]
  (.superSetUp this)
  (reset! context (.getContext this)))

(defn -testNewBuilder
  "Tests the new-builder function."
  [this]
  ; without context
  (does-throw AssertionError (new-builder))
  ; non-context argument
  (does-throw AssertionError (new-builder "neko"))
  (is (instance? FunctionalAlertDialogBuilder (new-builder @context)))
  (with-context @context
    (is (instance? FunctionalAlertDialogBuilder (new-builder)))))

(defn -testBasicCreate
  "Tests that the create function works with no other set-up."
  [this]
  (let [test-builder (fn [builder]
                       ; a simple test that just ensures a view exists
                       (let [dialog (create builder)]
                         (is-not-nil (find-view dialog :android/icon))))]
    ; test with android builder
    (test-builder (AlertDialog$Builder. @context))
    ; test with functional builder
    (test-builder (new-builder @context))))
