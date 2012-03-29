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

(ns neko.threading-test
  "Test for the neko.threading namespace."
  {:author "Daniel Solano Gómez"}
  (:gen-class :main false
              :extends android.test.ActivityUnitTestCase
              :init init
              :constructors {[] [Class]}
              :methods [[testRunOnUiThread [] void]
                        [testRunOnUiThreadMacro [] void]
                        [testPost [] void]
                        [testPostDelayed [] void]
                        [testOnUiThread [] void]
                        [testBasicTask [] void]
                        [testPreExecute [] void]
                        [testPostExecute [] void]
                        [testResultOf [] void]
                        [testExecWithArgs [] void]
                        [testOnProgressUpdate [] void]
                        [testPublishOutOfContext [] void]])
  (:import [java.util.concurrent CountDownLatch TimeoutException TimeUnit])
  (:use [neko test-utils threading]
        junit.assert))

(defn -init []
  [[com.sattvik.neko.tests.TestActivity] nil])

(defn -testRunOnUiThread [this]
  (let [ui-thread (Thread/currentThread)
        test-thread (atom nil)]
    ; should execute immediately
    (run-on-ui-thread* (start-activity this)
      (fn [] (reset! test-thread (Thread/currentThread))))
    (is-eq ui-thread @test-thread)))

(defn -testRunOnUiThreadMacro [this]
  (let [ui-thread (Thread/currentThread)
        test-thread (atom nil)]
    ; should execute immediately
    (run-on-ui-thread (start-activity this)
      (reset! test-thread (Thread/currentThread)))
    (is-eq ui-thread @test-thread)))

(defn -testPost [this]
  (let [latch  (CountDownLatch. 2)
        count  (atom 0)
        activity (start-activity this)
        run-me (reify Runnable
                 (run [this]
                   (let [view (android.widget.Button. activity)]
                     (when (post* view (fn [] (.countDown latch)))
                       (swap! count inc))
                     (when (post view (.countDown latch))
                       (swap! count inc)))))]
    (.. (Thread. run-me) start)
    (.await latch 5 TimeUnit/SECONDS)
    (is-eq 2 @count)))

(defn -testPostDelayed [this]
  (let [latch  (CountDownLatch. 2)
        count  (atom 0)
        activity (start-activity this)
        run-me (reify Runnable
                 (run [this]
                   (let [view (android.widget.Button. activity)]
                     (when (post-delayed* view 100 (fn [] (.countDown latch)))
                       (swap! count inc))
                     (when (post-delayed view 100 (.countDown latch))
                       (swap! count inc)))))]
    (.. (Thread. run-me) start)
    (.await latch 5 TimeUnit/SECONDS)
    (is-eq 2 @count)))

(defn -testOnUiThread [this]
  "Tests that the on-ui-thread? function works."
  (is (on-ui-thread?))
  (is-not @(future (on-ui-thread?))))

(defn -testBasicTask [this]
  "Tests that a basic task executes in a different thread."
  (let [task    (new-task (fn [] (Thread/currentThread)))
        task    (execute! task)]
    (is-not-same (Thread/currentThread) (result-of task))))

(defn -testPreExecute
  "Tests that setting a pre-execute function works."
  [this]
  (let [methods (atom [])
        pre-on-ui (atom nil)
        task    (new-task (fn [] (swap! methods conj "bg")))
        task    (with-pre-execute task (fn []
                                         (reset! pre-on-ui (on-ui-thread?))
                                         (swap! methods conj "pre")))
        task    (execute! task)]
    (result-of task)
    (is-eq ["pre" "bg"] @methods)
    (is @pre-on-ui)))

(defn -testResultOf
  "Tests that the result-of function works correctly."
  [this]
  (let [task (new-task (fn [] 1))]
    ; can't get result of un-executed task
    (does-throw AssertionError (result-of task))
    (does-throw AssertionError (result-of task 100))
    (does-throw AssertionError (result-of task 1 :seconds))
    (let [task (execute! task)]
      ; blocking result-of
      (is-eq 1 (result-of task))
      ; with times (default millis)
      (is-eq 1 (result-of task 100))
      ; with times and unit
      (is-eq 1 (result-of task 100 :seconds))
      (is-eq 1 (result-of task 100 :millis))
      (is-eq 1 (result-of task 100 :micros))
      (is-eq 1 (result-of task 100 :nanos))
      (is-eq 1 (result-of task 100 TimeUnit/SECONDS))
      (is-eq 1 (result-of task 100 TimeUnit/MILLISECONDS))
      (is-eq 1 (result-of task 100 TimeUnit/MICROSECONDS))
      (is-eq 1 (result-of task 100 TimeUnit/NANOSECONDS))
      ; invalid arguments
      (does-throw AssertionError (result-of task "bogus"))
      (does-throw AssertionError (result-of task :bogus))
      (does-throw AssertionError (result-of task []))
      (does-throw AssertionError (result-of task 100 :bogus))
      (does-throw AssertionError (result-of task 100 "bogus"))
      (does-throw AssertionError (result-of task nil))
      (does-throw AssertionError (result-of task 100 nil))))
  ; test timeouts
  (let [exec-task (fn [] (execute! (new-task (fn [] (Thread/sleep 2000) 2))))]
    (does-throw TimeoutException (result-of (exec-task) 100))
    (does-throw TimeoutException (result-of (exec-task) 1 :seconds))
    (does-throw TimeoutException (result-of (exec-task) 100 :millis))
    (does-throw TimeoutException (result-of (exec-task) 100 :micros))
    (does-throw TimeoutException (result-of (exec-task) 100 :nanos))
    (does-throw TimeoutException (result-of (exec-task) 1 TimeUnit/SECONDS))
    (does-throw TimeoutException (result-of (exec-task) 100 TimeUnit/MILLISECONDS))
    (does-throw TimeoutException (result-of (exec-task) 100 TimeUnit/MICROSECONDS))
    (does-throw TimeoutException (result-of (exec-task) 100 TimeUnit/NANOSECONDS)))
  (let [exec-task (fn [] (execute! (new-task (fn [] (Thread/sleep 100) 3))))]
    (is-eq 3 (result-of (exec-task)))
    (is-eq 3 (result-of (exec-task) 150))
    (is-eq 3 (result-of (exec-task) 150 :millis))))

(defn -testPostExecute
  "Tests that setting a post-execute function works."
  [this]
  (let [methods (atom [])
        post-on-ui (atom false)
        ; tasks can return result before post-execute runs
        latch   (CountDownLatch. 1)
        task    (new-task (fn []
                            (swap! methods conj "bg")
                            :finished))
        task    (with-post-execute task (fn [result]
                                          (when (= result :finished)
                                            (reset! post-on-ui (on-ui-thread?))
                                            (swap! methods conj "post")
                                            (.countDown latch))))
        task    (execute! task)]
    (.await latch)
    (is-eq ["bg" "post"] @methods)
    (is @post-on-ui)))

(defn -testExecWithArgs
  "Tests that writing a task that takes arguments works correctly."
  [this]
  ; test three simple args
  (let [task    (new-task (fn [a b c] [a b c]))
        task    (execute! task 1 2 3)]
    (is-eq [1 2 3] (result-of task)))
  ; test two simple args
  (let [task    (new-task (fn [a b] [a b]))
        task    (execute! task 1 2)]
    (is-eq [1 2] (result-of task)))
  ; test a single collection arg
  (let [task    (new-task (fn [a] a))
        task    (execute! task [1 2])]
    (is-eq [1 2] (result-of task)))
  ; test a single array arg
  (let [task    (new-task (fn [a] a))
        array   (int-array [1 2 3])
        task    (execute! task array)]
    (is-same array (result-of task))))

(defn -testOnProgressUpdate
  "Tests that the on-progress-update is called correctly."
  [this]
  (let [test-data (range 5)
        updates (atom [])
        task (new-task (fn [data]
                         (doseq [datum data]
                           (publish-progress datum))))
        task (with-on-progress-update task
                                      (fn [datum]
                                        (swap! updates conj datum)))]
    (result-of (execute! task test-data))
    (is-eq test-data @updates)))

(defn -testPublishOutOfContext
  "Tests that trying to publish pogress outside the background function fails."
  [this]
  ; with no context at all
  (does-throw AssertionError (publish-progress 1 2 3))
  ; not allowed in pre- or post-execute
  (let [fail-count (atom 0)
        latch (CountDownLatch. 1)
        task (new-task (fn [] :foo))
        task (with-pre-execute task (fn []
                                      (try
                                        (publish-progress)
                                        (catch AssertionError _
                                          (swap! fail-count inc)))))
        task (with-post-execute task (fn [_]
                                       (try
                                         (publish-progress :bar)
                                         (catch AssertionError _
                                           (swap! fail-count inc)))
                                       (.countDown latch)))]
    (execute! task)
    (.await latch)
    (is-eq 2 @fail-count))
  ; not allowed in on-progress-update
  (let [task (new-task (fn [] (publish-progress)))
        did-throw (atom false)
        task (with-on-progress-update task (fn []
                                             (try
                                               (publish-progress)
                                               (catch AssertionError _
                                                 (reset! did-throw true)))))
        task (execute! task)]
    (result-of task)
    (is @did-throw))
  ; not allowed in on-cancelled
  (let [task (new-task (fn [] (Thread/sleep 100)))
        fail-count (atom 0)
        latch (CountDownLatch. 1)
        task (with-on-cancelled task (fn []
                                       (try
                                         (publish-progress \x \y \z)
                                         (catch AssertionError _
                                           (swap! fail-count inc)
                                           (.countDown latch)))))
        task (execute! task)]
    (cancel task)
    (.await latch)
    (is-eq 1 @fail-count)))
