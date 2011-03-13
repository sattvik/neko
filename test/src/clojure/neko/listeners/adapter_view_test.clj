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

(ns neko.listeners.adapter-view-test
  "Tests for the neko.listeners.adapter-view namespace."
  {:author "Daniel Solano Gómez"}
  (:gen-class :main false
              :extends android.test.AndroidTestCase
              :methods [[testOnItemClick [] void]
                        [testOnItemLongClick [] void]
                        [testOnItemSelected [] void]]
              :exposes-methods {setUp superSetUp})
  (:import [android.widget ArrayAdapter ListView])
  (:use neko.context
        neko.listeners.adapter-view
        junit.assert))

(def test-parent (atom nil))
(def test-adapter (atom nil))
(def test-pos 3)
(def test-view (atom nil))
(def test-id (atom nil))

(defn -setUp
  "Creates new view for testing."
  [this]
  (.superSetUp this)
  (with-context (.getContext this)
    (reset! test-parent (ListView. *context*))
    (reset! test-adapter
            (ArrayAdapter. *context*
                           (get-layout :android/simple_list_item_1)
                           (get-id :android/text1)
                           (into-array ["One" "Two" "Three" "Four"]))))
  (reset! test-view (.getView @test-adapter test-pos nil nil))
  (reset! test-id (.getItemId @test-adapter test-pos)))

(defn -testOnItemClick
  "Tests the on-item-click macro and on-item-click-call macro."
  [this]
  (let [count       (atom 0)
        fn-listener (on-item-click-call (fn [p v pos id]
                                          (when (and (= v @test-view)
                                                     (= p @test-parent)
                                                     (= pos test-pos)
                                                     (= id @test-id))
                                            (swap! count inc))))
        macro-listener (on-item-click (when (and (= view @test-view)
                                                 (= parent @test-parent)
                                                 (= position test-pos)
                                                 (= id @test-id))
                                        (swap! count inc)))]
    (.onItemClick fn-listener @test-parent @test-view test-pos @test-id)
    (.onItemClick macro-listener @test-parent @test-view test-pos @test-id)
    (is-eq 2 @count)))

(defn -testOnItemLongClick
  "Tests the on-item-long-click macro and on-item-long-click-call macro."
  [this]
  (let [fn-listener (on-item-long-click-call
                      (fn [p v pos id]
                        (if (and (= v @test-view)
                                 (= p @test-parent)
                                 (= pos test-pos)
                                 (= id @test-id))
                          "foo!"
                          false)))
        macro-listener (on-item-long-click
                         (if (and (= view @test-view)
                                  (= parent @test-parent)
                                  (= position test-pos)
                                  (= id @test-id))
                           nil
                           true))]
    (is (.onItemLongClick fn-listener @test-parent @test-view test-pos
                          @test-id))
    (is-not (.onItemLongClick macro-listener @test-parent @test-view test-pos
                              @test-id))))

(defn -testOnItemSelected
  "Tests the on-item-selected macro and on-item-selected-call macro."
  [this]
  (let [results (atom [])
        item-fn (fn [parent view pos id]
                  (when (and (= parent @test-parent)
                             (= view @test-view)
                             (= pos test-pos)
                             (= id @test-id))
                    (swap! results conj :item-fn)))
        item-listener (on-item-selected-call item-fn)
        both-listener (on-item-selected-call
                        item-fn
                        (fn [parent]
                          (when (= parent @test-parent)
                            (swap! results conj :nothing-fn))))
        macro-listener (on-item-selected
                         (cond
                           (and (= type :item)
                                (= parent @test-parent)
                                (= view @test-view)
                                (= position test-pos)
                                (= id @test-id))
                             (swap! results conj :item-macro)
                           (and (= type :nothing)
                                (= parent @test-parent)
                                (= view nil)
                                (= position nil)
                                (= id nil))
                             (swap! results conj :nothing-macro)))
        listeners [item-listener both-listener macro-listener]]
    (dorun (map #(.onItemSelected % @test-parent @test-view test-pos @test-id)
                listeners))
    (dorun (map #(.onNothingSelected % @test-parent)
                listeners))
    (is-eq [:item-fn :item-fn :item-macro :nothing-fn :nothing-macro]
           @results)))
