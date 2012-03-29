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

(ns neko.find-view-test
  "Tests for the ViewFinder protocol."
  {:author "Daniel Solano Gómez"}
  (:gen-class :main false
              :init init
              :constructors {[] [Class]}
              :extends android.test.ActivityUnitTestCase
              :methods [[testActivity [] void]
                        [testWindow [] void]
                        [testView [] void]
                        [testDialog [] void]
                        [testOneArg [] void]])
  (:import [com.sattvik.neko.tests R$id R$layout])
  (:use [neko activity
              find-view
              test-utils]
        junit.assert))

(defn -init []
  [[com.sattvik.neko.tests.TestActivity] nil])

(defn -testActivity
  "Tests the ViewFinder protocol with an Activity."
  [this]
  (let [activity (start-activity this)
        output-view (.findViewById activity R$id/output)]
    ; keyword
    (is-same output-view (find-view activity :output))
    ; id
    (is-same output-view (find-view activity R$id/output))
    ; views not available
    (is-nil (find-view activity (int 0)))
    (is-nil (find-view activity :android/extractArea))
    ; check one-arg function fails
    (does-throw IllegalArgumentException
                (find-view activity))
    ; fails with bad arguments
    (does-throw AssertionError
                (find-view activity 'neko))
    (does-throw AssertionError
                (find-view activity "neko"))
    (does-throw AssertionError
                (find-view activity []))))

(defn -testWindow
  "Tests the ViewFinder protocol with a Window."
  [this]
  (let [window      (.getWindow (start-activity this))
        output-view (.findViewById window R$id/output)]
    ; keyword
    (is-same output-view (find-view window :output))
    ; id
    (is-same output-view (find-view window R$id/output))
    ; views not available
    (is-nil (find-view window (int 0)))
    (is-nil (find-view window :android/extractArea))
    ; check one-arg function fails
    (does-throw IllegalArgumentException
                (find-view window))
    ; fails with bad arguments
    (does-throw AssertionError
                (find-view window 'neko))
    (does-throw AssertionError
                (find-view window "neko"))
    (does-throw AssertionError
                (find-view window []))))

(defn -testView
  "Tests the ViewFinder protocol with a View."
  [this]
  (let [view        (.. (start-activity this) getWindow getDecorView)
        output-view (.findViewById view R$id/output)]
    ; keyword
    (is-same output-view (find-view view :output))
    ; id
    (is-same output-view (find-view view R$id/output))
    ; views not available
    (is-nil (find-view view (int 0)))
    (is-nil (find-view view :android/extractArea))
    ; check one-arg function fails
    (does-throw IllegalArgumentException
                (find-view view))
    ; fails with bad arguments
    (does-throw AssertionError
                (find-view view 'neko))
    (does-throw AssertionError
                (find-view view "neko"))
    (does-throw AssertionError
                (find-view view []))))

(defn -testDialog
  "Tests the ViewFinder protocol with a Dialog."
  [this]
  (let [activity    (start-activity this)
        custom-view (.. activity getLayoutInflater (inflate R$layout/main nil))
        dialog      (.create (doto (android.app.AlertDialog$Builder. activity)
                               (.setView custom-view)))
        output-view (.findViewById dialog R$id/output)]
    ; keyword
    (is-same output-view (find-view dialog :output))
    ; id
    (is-same output-view (find-view dialog R$id/output))
    ; views not available
    (is-nil (find-view dialog (int 0)))
    (is-nil (find-view dialog :android/extractArea))
    ; check one-arg function fails
    (does-throw IllegalArgumentException
                (find-view dialog))
    ; fails with bad arguments
    (does-throw AssertionError
                (find-view dialog 'neko))
    (does-throw AssertionError
                (find-view dialog "neko"))
    (does-throw AssertionError
                (find-view dialog []))))

(defn -testOneArg
  "Tests the one-argument version of the protocol."
  [this]
  ; does not work on illegal types
  (does-throw IllegalArgumentException
              (find-view 'neko))
  (does-throw IllegalArgumentException
              (find-view "neko"))
  (does-throw IllegalArgumentException
              (find-view []))
  ; fails outside of a valid context
  (does-throw AssertionError
              (find-view :output))
  (does-throw AssertionError
              (find-view R$id/output))
  (with-activity (start-activity this)
    (let [output-view (.findViewById *activity* R$id/output)]
      ; keyword
      (is-same output-view (find-view :output))
      ; id
      (is-same output-view (find-view R$id/output))
      ; views not available
      (is-nil (find-view (int 0)))
      (is-nil (find-view :android/extractArea)))))
