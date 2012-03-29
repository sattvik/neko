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

(ns neko.context-test
  "Tests for the neko.context namespace."
  {:author "Daniel Solano Gómez"}
  (:gen-class :main false
              :extends android.test.AndroidTestCase
              :methods [[testWithContext [] void]
                        [testResolveResource [] void]
                        [testGetString [] void]
                        [testGetId [] void]
                        [testGetLayoutId [] void]
                        [testGetSystemService [] void]
                        [testInflateLayout [] void]]
              :exposes-methods {setUp superSetUp})
  (:import android.content.Context
           [com.sattvik.neko.tests R$id R$layout R$string])
  (:use neko.context
        junit.assert))

(def context (atom nil))

(defn -setUp
  [this]
  (.superSetUp this)
  (reset! context (.getContext this)))

(defn -testWithContext
  "Test that the with-context macro functions."
  [this]
  (is-not (bound? #'*context*))
  (with-context @context
    (is-same @context *context*))
  (is-not (bound? #'*context*)))

(defn -testResolveResource
  "Tests the resolve-resource function."
  [this]
  (let [test-string-id com.sattvik.neko.tests.R$string/test_string
        icon-id        com.sattvik.neko.tests.R$drawable/icon
        layout-id      com.sattvik.neko.tests.R$layout/main]
    ; basic tests
    (is-eq test-string-id
           (resolve-resource @context :string :test_string))
    (is-eq icon-id
           (resolve-resource @context :drawable :icon))
    (is-eq layout-id
           (resolve-resource @context :layout :main))
    ; test using dots
    (is-eq test-string-id
           (resolve-resource @context :string :test.string))
    ; test using hyphens
    (is-eq test-string-id
           (resolve-resource @context :string :test-string))
    ; test using namespace
    (is-eq test-string-id
           (resolve-resource @context :string
                             :com.sattvik.neko.tests/test-string))
    ; basic tests with context
    (with-context @context
      (is-eq test-string-id
             (resolve-resource :string :test_string))
      (is-eq icon-id
             (resolve-resource :drawable :icon))
      (is-eq layout-id
             (resolve-resource :layout :main))))
  ; test invalid type
  (does-throw IllegalArgumentException
              (resolve-resource @context :neko :neko))
  ; test an invalid name
  (does-throw IllegalArgumentException
              (resolve-resource @context :layout :neko))
  ; test android resources
  (is-eq android.R$id/copy
         (resolve-resource @context :id :android/copy))
  (is-eq android.R$drawable/ic_menu_today
         (resolve-resource @context :drawable :android/ic_menu_today))
  (is-eq android.R$string/ok
         (resolve-resource @context :string :android/ok)))

(defn -testGetString
  [this]
  (let [; string ids
        plain-id         R$string/plain_string
        one-arg-id       R$string/one_arg_string
        two-arg-id       R$string/two_arg_string
        three-arg-id     R$string/three_arg_string
        six-arg-id       R$string/six_arg_string
        ; resolved strings
        plain-string     (.getString @context plain-id)
        one-arg-string   (.getString @context one-arg-id
                                     (to-array ["one"]))
        two-arg-string   (.getString @context two-arg-id
                                     (to-array ["one" "two"]))
        three-arg-string (.getString @context three-arg-id
                                     (to-array ["one" "two" "three"]))
        six-arg-string   (.getString @context six-arg-id
                                     (to-array ["one" "two" "three"
                                                "four" "five" "six"]))]
    ; without context (using id)
    (is-eq plain-string   (get-string @context plain-id))
    (is-eq one-arg-string (get-string @context one-arg-id "one"))
    (is-eq two-arg-string (get-string @context two-arg-id "one" "two"))
    (is-eq three-arg-string (get-string @context three-arg-id "one" "two"
                                        "three"))
    (is-eq six-arg-string (get-string @context six-arg-id "one" "two" "three"
                                      "four" "five" "six"))
    ; without context (using name)
    (is-eq plain-string   (get-string @context :plain-string))
    (is-eq one-arg-string (get-string @context :one-arg-string "one"))
    (is-eq two-arg-string (get-string @context :two-arg-string "one" "two"))
    (is-eq three-arg-string (get-string @context :three-arg-string "one" "two"
                                         "three"))
    (is-eq six-arg-string (get-string @context :six-arg-string "one" "two"
                                      "three" "four" "five" "six"))
    ; with context
    (with-context @context
      ; using id
      (is-eq plain-string   (get-string plain-id))'
      (is-eq one-arg-string (get-string one-arg-id "one"))
      (is-eq two-arg-string (get-string two-arg-id "one" "two"))
      (is-eq three-arg-string (get-string three-arg-id "one" "two" "three"))
      (is-eq six-arg-string (get-string six-arg-id "one" "two" "three" "four"
                                        "five" "six"))
      ; using name
      (is-eq plain-string   (get-string :plain-string))
      (is-eq one-arg-string (get-string :one-arg-string "one"))
      (is-eq two-arg-string (get-string :two-arg-string "one" "two"))
      (is-eq three-arg-string (get-string :three-arg-string "one" "two"
                                          "three"))
      (is-eq six-arg-string (get-string :six-arg-string "one" "two" "three"
                                        "four" "five" "six"))))
  ; test android strings
  (let [ok-string   (.getString @context android.R$string/ok)
        copy-string (.getString @context android.R$string/copy)]
    ; without context
    (is-eq ok-string
           (get-string @context :android/ok))
    (is-eq copy-string
           (get-string @context :android/copy))
    ; with context
    (with-context @context
      (is-eq ok-string (get-string :android/ok))
      (is-eq copy-string (get-string :android/copy)))))

(defn -testGetId
  "Tests the get-id function."
  [this]
  (let [output-id R$id/output]
    ; normal
    (is-eq output-id (get-id @context :output))
    (is-eq output-id (get-id @context output-id))
    ; missing context
    (does-throw AssertionError (get-id :output))
    ; bad arguments
    (does-throw AssertionError (get-id @context "foo"))
    (does-throw AssertionError (get-id "foo" :output))
    (with-context @context
      (is-eq output-id (get-id :output))
      (is-eq output-id (get-id output-id))
      ; bad arguments
      (does-throw AssertionError (get-id "foo")))))

(defn -testGetLayoutId
  "Tests the get-layout function."
  [this]
  (let [main-id R$layout/main
        dialog-item-id android.R$layout/select_dialog_item]
    (is-eq main-id
           (get-layout @context :main))
    (is-eq dialog-item-id
           (get-layout @context :android/select_dialog_item))
    (with-context @context
      (is-eq main-id
             (get-layout :main))
      (is-eq dialog-item-id
             (get-layout :android/select_dialog_item)))))

(defn -testGetSystemService
  "Tests the get-system-service function."
  [this]
  (let [alarm-service     (.getSystemService @context Context/ALARM_SERVICE)
        inflater-service  (.getSystemService @context
                                             Context/LAYOUT_INFLATER_SERVICE)
        location-service  (.getSystemService @context
                                             Context/LOCATION_SERVICE)
        telephony-service (.getSystemService @context
                                             Context/TELEPHONY_SERVICE)]
    (is-same alarm-service
             (get-system-service @context :alarm))
    (is-same inflater-service
             (get-system-service @context :layout-inflater))
    (is-same location-service
             (get-system-service @context :location))
    (is-same telephony-service
             (get-system-service @context :telephony))
    (with-context @context
      (is-same alarm-service
               (get-system-service :alarm))
      (is-same inflater-service
               (get-system-service :layout-inflater))
      (is-same location-service
               (get-system-service :location))
      (is-same telephony-service
               (get-system-service :telephony))))
  ; bad service name
  (does-throw IllegalArgumentException
              (get-system-service @context :neko)))

(defn -testInflateLayout
  "Tests the inflate-layoutfunction."
  [this]
  ; bad parameters: no id-or-name
  (does-throw AssertionError (inflate-layout @context))
  ; bad parameters: invalid id-or-name type
  (does-throw AssertionError (inflate-layout @context "foo"))
  ; non-existent resource
  (does-throw IllegalArgumentException (inflate-layout @context :neko))
  ; basic test of successful inflation
  (is (instance? android.widget.RelativeLayout
                 (inflate-layout @context :main)))
  (with-context @context
    ; bad parameters: invalid id-or-name type
    (does-throw AssertionError (inflate-layout "foo"))
    ; non-existent resource
    (does-throw IllegalArgumentException (inflate-layout :neko))
    ; basic test of successful inflation
    (is (instance? android.widget.RelativeLayout
                   (inflate-layout @context :main)))))
