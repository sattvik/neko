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
              :extends android.test.InstrumentationTestCase
              :methods [[^{android.test.UiThreadTest {}} testNewBuilder [] void]
                        [^{android.test.UiThreadTest {}} testBasicCreate [] void]
                        [^{android.test.UiThreadTest {}} testGetBuilderObjectWithObject [] void]
                        [^{android.test.UiThreadTest {}} testGetBuilderObjectFunctional [] void]
                        [testCancellationDefaultWithObject [] void]
                        [testCancellationTrueWithObject [] void]
                        [testCancellationFalseWithObject [] void]
                        [testCancellationDefaultFunctional [] void]
                        [testCancellationTrueFunctional [] void]
                        [testCancellationFalseFunctional [] void]
                        ]
              :exposes-methods {runTest superRunTest
                                setUp superSetUp
                                tearDown superTearDown}
              )
  (:import android.app.AlertDialog$Builder
           android.test.UiThreadTest
           [java.util.concurrent CountDownLatch TimeUnit]
           neko.dialog.alert.FunctionalAlertDialogBuilder)
  (:use junit.assert
        [neko activity
              find-view]
        neko.dialog.alert
        neko.listeners.dialog
        )
  )

(def the-test (atom nil))
(def activity (atom nil))

(defn start-activity []
  (when-not @activity
    (let [package (.. @the-test getInstrumentation getTargetContext getPackageName) ]
      (reset! activity (.launchActivity @the-test package com.sattvik.neko.tests.TestActivity nil))))
  @activity)

(defn -setUp
  [this]
  (doto this
    (.superSetUp))
  (reset! the-test this))

(defn -tearDown
  [this]
  (when @activity
    (.finish @activity)
    (reset! activity nil))
  (reset! the-test nil)
  (.superTearDown this))

(defn -runTest
  [this]
  (let [method-name (.getName this)
        method      (.. this getClass (getMethod method-name (into-array Class [])))]
    (when (.isAnnotationPresent method UiThreadTest)
      (start-activity)))
  (.superRunTest this))

(defn -testNewBuilder
  "Tests the new-builder function."
  [this]
  ; without context
  (does-throw AssertionError (new-builder))
  ; non-context argument
  (does-throw AssertionError (new-builder "neko"))
  (is (instance? FunctionalAlertDialogBuilder (new-builder @activity)))
  (with-activity @activity
    (is (instance? FunctionalAlertDialogBuilder (new-builder)))))

(defn -testBasicCreate
  "Tests that the create function works with no other set-up."
  [this]
  (let [test-builder (fn [builder]
                       ; a simple test that just ensures a view exists
                       (let [dialog (create builder)]
                         (.show dialog)
                         (is-not-nil (find-view dialog :android/icon))
                         (.dismiss dialog)))]
    ; test with android builder
    (test-builder (AlertDialog$Builder. @activity))
    ; test with functional builder
    (test-builder (new-builder @activity))))


; ----------------------------------------------------------------------
; ---   Test get-builder-object                                      ---
; ----------------------------------------------------------------------

(defn -testGetBuilderObjectWithObject
  "Tests the get-builder-object function with the object-based builder."
  [this]
  (let [builder (AlertDialog$Builder. @activity)]
    (is (identical? builder (get-builder-object builder)))))

(defn -testGetBuilderObjectFunctional
  "Tests the get-builder-object function with the functional builder."
  [this]
  (let [builder (new-builder @activity)]
    (is (instance? AlertDialog$Builder (get-builder-object builder)))))


; ----------------------------------------------------------------------
; ---   Test with-cancellation                                       ---
; ----------------------------------------------------------------------

(defn- test-cancellation
  "Helper function used to test cancellation"
  [build-fn cancellable?]
  (let [dialog (atom nil)
        latch  (CountDownLatch. 1)
        cancel-test
          (reify Runnable
            (run [_]
              (let [builder (build-fn)]
                (reset! dialog (create builder))
                (doto @dialog
                  (.setOnCancelListener
                    (on-cancel (.countDown latch)))
                  (.show)
                  (.onBackPressed)))))]
    (.runTestOnUiThread @the-test cancel-test)
    (is-eq cancellable?
           (.await latch 250 TimeUnit/MILLISECONDS))))

(defn -testCancellationDefaultWithObject
  "Tests that by default an object-based builder is cancellable."
  [this]
  (start-activity)
  (test-cancellation #(AlertDialog$Builder. @activity)
                     true))

(defn -testCancellationTrueWithObject
  "Tests that enabling cancellation with an object works."
  [this]
  (start-activity)
  (test-cancellation #(let [builder (AlertDialog$Builder. @activity)]
                        (with-cancellation builder true))
                     true))

(defn -testCancellationFalseWithObject
  "Tests that disabling cancellation with an object works."
  [this]
  (start-activity)
  (test-cancellation #(let [builder (AlertDialog$Builder. @activity)]
                        (with-cancellation builder false))
                     false))

(defn -testCancellationDefaultFunctional
  "Tests that by default a functional builder is cancellable."
  [this]
  (start-activity)
  (test-cancellation #(new-builder @activity)
                     true))

(defn -testCancellationTrueFunctional
  "Tests that enabling cancellation on a functional builder works."
  [this]
  (start-activity)
  (test-cancellation #(let [builder (new-builder @activity)
                            builder (with-cancellation builder true)]
                        (is (:cancellable builder))
                        builder)
                     true))

(defn -testCancellationFalseFunctional
  "Tests that disabling cancellation on a functional builder works."
  [this]
  (start-activity)
  (test-cancellation #(let [builder (new-builder @activity)
                            builder (with-cancellation builder false)]
                        (is-not (:cancellable builder))
                        builder)
                     false))
