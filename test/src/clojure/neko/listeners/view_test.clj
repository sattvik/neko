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

(ns neko.listeners.view-test
  "Tests for the neko.listeners.view namespace."
  {:author "Daniel Solano Gómez"}
  (:gen-class :main false
              :extends android.test.AndroidTestCase
              :methods [[testOnClick [] void]
                        [testOnCreateContextMenu [] void]
                        [testOnFocusChange [] void]
                        [testOnKey [] void]
                        [testOnLongClick [] void]
                        [testOnTouch [] void]]
              :exposes-methods {setUp superSetUp})
  (:import android.view.View)
  (:use neko.listeners.view
        junit.assert))

(def test-view (atom nil))
(def clicks (atom 0))

(defn -setUp
  "Creates new view for testing."
  [this]
  (.superSetUp this)
  (reset! test-view (View. (.getContext this)))
  (reset! clicks 0))

(defn -testOnClick
  "Tests the on-click macro and the on-click-call function."
  [this]
  (let [fn-listener (on-click-call (fn [v]
                                 (is-eq @test-view v)
                                 (swap! clicks inc)))
        macro-listener (on-click (is-eq @test-view view)
                                 (swap! clicks inc))]
    (.onClick fn-listener @test-view)
    (.onClick macro-listener @test-view)
    (is-eq 2 @clicks)))

(defn -testOnCreateContextMenu
  "Tests the on-create-context-menu macro and the on-create-context-menu-call
  function."
  [this]
  (let [test-menu (reify android.view.ContextMenu)
        test-info (reify android.view.ContextMenu$ContextMenuInfo)
        fn-listener (on-create-context-menu-call (fn [m v i]
                                               (is-eq test-menu m)
                                               (is-eq @test-view v)
                                               (is-eq test-info i)
                                               (swap! clicks inc)))
        macro-listener (on-create-context-menu (is-eq test-menu menu)
                                               (is-eq @test-view view)
                                               (is-eq test-info info)
                                               (swap! clicks inc))]
    (.onCreateContextMenu fn-listener test-menu @test-view test-info)
    (.onCreateContextMenu macro-listener test-menu @test-view test-info)
    (is-eq 2 @clicks)))

(defn -testOnFocusChange
  "Tests the on-focus-change macro and the on-focus-change-call function."
  [this]
  (let [test-focus (atom false)
        fn-listener (on-focus-change-call (fn [v f?]
                                            (is-eq @test-view v)
                                            (is-eq @test-focus f?)
                                            (swap! clicks inc)))
        macro-listener (on-focus-change (is-eq @test-view view)
                                        (is-eq @test-focus focused?)
                                        (swap! clicks inc))]
    (.onFocusChange fn-listener @test-view false)
    (.onFocusChange macro-listener @test-view false)
    (reset! test-focus true)
    (.onFocusChange fn-listener @test-view true)
    (.onFocusChange macro-listener @test-view true)
    (is-eq 4 @clicks)))

(defn -testOnKey
  "Tests the on-key macro and the on-key-call function."
  [this]
  (let [test-code  (int android.view.KeyEvent/KEYCODE_EQUALS)
        test-event (android.view.KeyEvent. android.view.KeyEvent/ACTION_DOWN
                                           test-code)
        fn-listener (on-key-call (fn [v c e]
                                   (is-eq @test-view v)
                                   (is-eq test-code c)
                                   (is-eq test-event e)
                                   true))
        macro-listener (on-key (is-eq @test-view view)
                               (is-eq test-code key-code)
                               (is-eq test-event event)
                               false)]
    (is (.onKey fn-listener @test-view test-code test-event))
    (is-not (.onKey macro-listener @test-view test-code test-event))))

(defn -testOnLongClick
  "Tests the on-long-click macro and the on-long-click-call function."
  [this]
  (let [fn-listener (on-long-click-call (fn [v]
                                      (is-eq @test-view v)
                                      true))
        macro-listener (on-long-click (is-eq @test-view view)
                                      false)]
    (is (.onLongClick fn-listener @test-view))
    (is-not (.onLongClick macro-listener @test-view))))

(defn -testOnTouch
  "Tests the on-touch macro and the on-touch-call function."
  [this]
  (let [down-time (android.os.SystemClock/uptimeMillis)
        event-time (+ down-time 250)
        action android.view.MotionEvent/ACTION_UP
        test-event (android.view.MotionEvent/obtain down-time event-time
                                                    action 50 50 0)
        fn-listener (on-touch-call (fn [v e]
                                     (is-eq @test-view v)
                                     (is-eq test-event e)
                                     true))
        macro-listener (on-touch (is-eq @test-view view)
                                 (is-eq test-event event)
                                 false)]
    (is (.onTouch fn-listener @test-view test-event))
    (is-not (.onTouch macro-listener @test-view test-event))))
