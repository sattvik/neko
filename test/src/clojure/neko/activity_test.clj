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
                        [testSetContentViewWithID [] void]
                        [testSetContentViewWithIDNoContext [] void]
                        [testSetContentViewWithName [] void]
                        [testSetContentViewWithNameNoContext [] void]
                        [testSetContentViewWithView [] void]
                        [testSetContentViewWithViewNoContext [] void]
                        [testRequestWindowFeaturesNoFeatures [] void]
                        [testRequestWindowFeaturesOneFeature [] void]
                        [testRequestWindowFeaturesManyFeatures [] void]
                        [testRequestWindowFeaturesManyFeaturesInContext [] void]
                        [testRequestWindowFeaturesBadFeatures [] void]])
  (:import android.view.Window
           com.sattvik.neko.tests.R$id)
  (:require [com.sattvik.neko.tests.TestActivity :as test-activity])
  (:use [neko activity
              [context :only [*context*]]
              find-view
              test-utils]
        junit.assert))

(defn -init []
  [[com.sattvik.neko.tests.TestActivity] nil])

(defn -testWithActivity
  "Test that the with-activity macro."
  [this]
  (let [activity (start-activity this)]
    (is-not (bound? #'*context*))
    (is-not (bound? #'*activity*))
    (with-activity activity
      (is-same activity *context*)
      (is-same activity *activity*))
    (is-not (bound? #'*context*))
    (is-not (bound? #'*activity*))))

(defn -testSetContentViewWithID
  "Tests the set-content-view! function using an ID within a with-activiity
  context."
  [this]
  (with-activity (start-activity this)
    (is-nil (find-view :android/text1))
    (set-content-view! android.R$layout/simple_list_item_1)
    (is-not-nil (find-view :android/text1))))

(defn -testSetContentViewWithIDNoContext
  "Tests the set-content-view! function using an ID."
  [this]
  (let [activity (start-activity this)]
    (is-nil (find-view activity :android/text1))
    (set-content-view! activity android.R$layout/simple_list_item_1)
    (is-not-nil (find-view activity :android/text1))))

(defn -testSetContentViewWithName
  "Tests the set-content-view! function using a keyword name within a
  with-activiity context."
  [this]
  (with-activity (start-activity this)
    (is-nil (find-view :android/text1))
    (set-content-view! :android/simple_list_item_1)
    (is-not-nil (find-view :android/text1))))

(defn -testSetContentViewWithNameNoContext
  "Tests the set-content-view! function using a keyword name."
  [this]
  (let [activity (start-activity this)]
    (is-nil (find-view activity :android/text1))
    (set-content-view! activity :android/simple_list_item_1)
    (is-not-nil (find-view activity :android/text1))))

(defn -testSetContentViewWithView
  "Tests the set-content-view! function using a view object within a
  with-activiity context."
  [this]
  (let [activity (start-activity this)]
    (with-activity activity
      (is-nil (find-view :android/text1))
      (set-content-view! (.. activity
                             getLayoutInflater
                             (inflate android.R$layout/simple_list_item_1 nil)))
      (is-not-nil (find-view :android/text1)))))

(defn -testSetContentViewWithViewNoContext
  "Tests the set-content-view! function using a view object."
  [this]
  (let [activity (start-activity this)]
    (is-nil (find-view activity :android/text1))
    (set-content-view! activity
                       (.. activity
                           getLayoutInflater
                           (inflate android.R$layout/simple_list_item_1 nil)))
    (is-not-nil (find-view activity :android/text1))))

(defn -testRequestWindowFeaturesNoFeatures
  "Tests the request-window-features! without requesting any features."
  [this]
  (let [activity (start-activity this)]
    (is (= [] (request-window-features! activity)))))

(defn -testRequestWindowFeaturesOneFeature
  "Tests the request-window-features! function with only one feature."
  [this]
  (reset! test-activity/set-content-view? false)
  (let [activity (start-activity this)]
    (is (= [true] (request-window-features! activity :progress)))
    (with-activity activity
      (is (= [true] (request-window-features! :no-title))))))

(defn -testRequestWindowFeaturesManyFeatures
  "Tests the request-window-features! requesting several features."
  [this]
  (reset! test-activity/set-content-view? false)
  (let [activity (start-activity this)]
    (is (= [true true true]
           (request-window-features! activity :progress :no-title
                                     :indeterminate-progress)))))

(defn -testRequestWindowFeaturesManyFeaturesInContext
  "Tests the request-window-features! requesting several features in an
  with-activity context."
  [this]
  (reset! test-activity/set-content-view? false)
  (with-activity (start-activity this)
    (is (= [true true true]
           (request-window-features! :progress :no-title
                                     :indeterminate-progress)))))

(defn -testRequestWindowFeaturesBadFeatures
  "Tests the request-window-features! with bad features."
  [this]
  (reset! test-activity/set-content-view? false)
  (does-throw RuntimeException
    (request-window-features! (start-activity this) :neko)))
