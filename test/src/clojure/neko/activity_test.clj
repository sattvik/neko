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
                        [testFindView [] void]
                        [testSetContentViewWithID [] void]
                        [testSetContentViewWithIDNoContext [] void]
                        [testSetContentViewWithName [] void]
                        [testSetContentViewWithNameNoContext [] void]
                        [testSetContentViewWithView [] void]
                        [testSetContentViewWithViewNoContext [] void]
                        ]
              :exposes-methods {setUp superSetUp})
  (:import com.sattvik.neko.test_app.R$id)
  (:use [neko activity
              [context :only [*context*]]
              test-utils]
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

(defn -testFindView
  "Tests the find-view function."
  [this]
  (let [output-view (.findViewById @activity R$id/output)]
    ; keyword
    (is-same output-view (find-view @activity :output))
    ; id
    (is-same output-view (find-view @activity R$id/output))
    (with-activity @activity
      (is-same output-view (find-view :output))
      (is-same output-view (find-view R$id/output)))))

(defn -testSetContentViewWithID
  "Tests the set-content-view! function using an ID within a with-activiity
  context."
  [this]
  (with-activity @activity
    (is-nil (find-view :android/text1))
    (set-content-view! android.R$layout/simple_list_item_1)
    (is-not-nil (find-view :android/text1))))

(defn -testSetContentViewWithIDNoContext
  "Tests the set-content-view! function using an ID."
  [this]
  (is-nil (find-view @activity :android/text1))
  (set-content-view! @activity android.R$layout/simple_list_item_1)
  (is-not-nil (find-view @activity :android/text1)))

(defn -testSetContentViewWithName
  "Tests the set-content-view! function using a keyword name within a
  with-activiity context."
  [this]
  (with-activity @activity
    (is-nil (find-view :android/text1))
    (set-content-view! :android/simple_list_item_1)
    (is-not-nil (find-view :android/text1))))

(defn -testSetContentViewWithNameNoContext
  "Tests the set-content-view! function using a keyword name."
  [this]
  (is-nil (find-view @activity :android/text1))
  (set-content-view! @activity :android/simple_list_item_1)
  (is-not-nil (find-view @activity :android/text1)))

(defn -testSetContentViewWithView
  "Tests the set-content-view! function using a view object within a
  with-activiity context."
  [this]
  (with-activity @activity
    (is-nil (find-view :android/text1))
    (set-content-view! (.. @activity
                           getLayoutInflater
                           (inflate android.R$layout/simple_list_item_1 nil)))
    (is-not-nil (find-view :android/text1))))

(defn -testSetContentViewWithViewNoContext
  "Tests the set-content-view! function using a view object."
  [this]
  (is-nil (find-view @activity :android/text1))
  (set-content-view! @activity
                     (.. @activity
                         getLayoutInflater
                         (inflate android.R$layout/simple_list_item_1 nil)))
  (is-not-nil (find-view @activity :android/text1)))
