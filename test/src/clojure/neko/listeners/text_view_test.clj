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

(ns neko.listeners.text-view-test
  "Tests for the neko.listeners.text-view namespace."
  {:author "Daniel Solano Gómez"}
  (:gen-class :main false
              :extends android.test.AndroidTestCase
              :methods [[testOnEditorAction [] void]])
  (:import android.view.KeyEvent
           android.widget.TextView)
  (:use neko.listeners.text-view
        junit.assert))

(defn -testOnEditorAction
  "Tests the on-editor-action-call function and on-editor-action macro."
  [this]
  (let [test-view   (TextView. (.getContext this))
        test-id     android.view.inputmethod.EditorInfo/IME_NULL
        fn-listener (on-editor-action-call
                      (fn [v a e]
                        (if (and (= test-view v)
                                 (= test-id a)
                                 (nil? e))
                          "boo!"
                          false)))
        macro-listener (on-editor-action
                         (if (and (= test-view view)
                                  (= test-id action-id)
                                  (nil? key-event))
                           nil
                           true))]
    (is (.onEditorAction fn-listener test-view test-id nil))
    (is-not (.onEditorAction macro-listener test-view test-id nil))))
