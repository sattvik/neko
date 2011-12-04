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

(ns neko.listeners.dialog-test
  "Tests for the neko.listeners.dialog namespace."
  {:author "Daniel Solano Gómez"}
  (:gen-class :main false
              :extends junit.framework.TestCase
              :methods [[testOnCancel [] void]
                        [testOnClick [] void]
                        [testOnDismiss [] void]
                        [testOnKey [] void]
                        [testOnMultiChoiceClick [] void]]
              :exposes-methods {setUp superSetUp})
  (:import android.content.DialogInterface
           android.view.KeyEvent)
  (:use neko.listeners.dialog
        junit.assert))

(def test-count (atom 0))
(def test-dialog (reify DialogInterface))

(defn -setUp
  [this]
  (.superSetUp this)
  (reset! test-count 0))

(defn -testOnCancel
  "Tests the on-cancel macro and on-cancel-call function."
  [this]
  (let [fn-listener (on-cancel-call (fn [d]
                                      (when (= d test-dialog)
                                        (swap! test-count inc))))
        macro-listener (on-cancel (when (= dialog test-dialog)
                                    (swap! test-count inc)))]
    (.onCancel fn-listener test-dialog)
    (.onCancel macro-listener test-dialog)
    (is-eq 2 @test-count)))

(defn -testOnClick
  "Tests the on-click macro and on-click-call function."
  [this]
  (let [buttons (atom [])
        fn-listener (on-click-call (fn [d w]
                                     (when (= d test-dialog)
                                       (swap! buttons conj w))))
        macro-listener (on-click (when (= dialog test-dialog)
                                    (swap! buttons conj which)))]
    (.onClick fn-listener test-dialog DialogInterface/BUTTON_NEUTRAL)
    (.onClick macro-listener test-dialog DialogInterface/BUTTON_NEGATIVE)
    (.onClick fn-listener test-dialog DialogInterface/BUTTON_POSITIVE)
    (.onClick macro-listener test-dialog 0)
    (.onClick fn-listener test-dialog 1)
    (.onClick macro-listener test-dialog 2)
    (.onClick fn-listener test-dialog 3)
    (.onClick macro-listener test-dialog 5)
    (is-eq [:neutral :negative :positive (int 0) (int 1) (int 2) (int 3) (int 5)] @buttons)))

(defn -testOnDismiss
  "Tests the on-dismiss macro and on-dismiss-call function."
  [this]
  (let [fn-listener (on-dismiss-call (fn [d]
                                       (when (= d test-dialog)
                                         (swap! test-count inc))))
        macro-listener (on-dismiss (when (= dialog test-dialog)
                                     (swap! test-count inc)))]
    (.onDismiss fn-listener test-dialog)
    (.onDismiss macro-listener test-dialog)
    (is-eq 2 @test-count)))

(defn -testOnKey
  "Tests the on-key macro and on-key-call function."
  [this]
  (let [keys (atom [])
        key-up (fn [listener code]
                 (.onKey listener test-dialog code
                         (KeyEvent. KeyEvent/ACTION_UP code)))
        key-down (fn [listener code]
                   (.onKey listener test-dialog code
                           (KeyEvent. KeyEvent/ACTION_DOWN code)))
        fn-listener (on-key-call (fn [d c e]
                                   (swap! keys conj c)
                                   (= KeyEvent/ACTION_UP (.getAction e))))
        macro-listener (on-key (swap! keys conj key-code)
                               (= KeyEvent/ACTION_UP (.getAction event)))
        N KeyEvent/KEYCODE_N
        E KeyEvent/KEYCODE_E
        K KeyEvent/KEYCODE_K
        O KeyEvent/KEYCODE_O]
    (is (key-up fn-listener N))
    (is-not (key-down fn-listener E))
    (is (key-up macro-listener K))
    (is-not (key-down macro-listener O))
    (is-eq [N E K O] @keys)))

(defn -testOnMultiChoiceClick
  "Tests the on-multi-choice-click macro and on-multi-choice-click-call function."
  [this]
  (let [choices (atom [])
        fn-listener (on-multi-choice-click-call (fn [d w c?]
                                                  (swap! choices conj [w c?])))
        macro-listener (on-multi-choice-click (swap! choices conj [which checked?]))]
    (.onClick fn-listener test-dialog 0 true)
    (.onClick macro-listener test-dialog 3 false)
    (.onClick fn-listener test-dialog 0 false)
    (.onClick macro-listener test-dialog 4 true)
    (is-eq [[(int 0) true]
            [(int 3) false]
            [(int 0) false]
            [(int 4) true]]
           @choices)))
