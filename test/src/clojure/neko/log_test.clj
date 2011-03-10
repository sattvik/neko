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

(ns neko.log-test
  "Tests the functionality of the neko.log namespace"
  {:author "Daniel Solano Gómez"}
  (:gen-class :main false
              :extends junit.framework.TestCase
              :methods [[testLogDebug [] void]
                        [testLogVerbose [] void]
                        [testLogInfo [] void]
                        [testLogWarn [] void]
                        [testLogError [] void]]
              :exposes-methods {setUp superSetUp})
  (:import android.os.SystemClock)
  (:use neko.log
        clojure.java.io
        clojure.java.shell
        junit.assert))

(def tag "LogTest")

(deflog tag)

(def start-signal (atom false))

(defn- collect
  [text]
  (future
    (let [process (..
                    (ProcessBuilder. ["logcat" "-s" tag])
                    (redirectErrorStream true)
                    start)
          pattern (re-pattern text)
          start   (SystemClock/elapsedRealtime)
          timed-out? (fn [] (< 500 (- (SystemClock/elapsedRealtime) start)))]
      (try
        (binding [*in* (reader (.getInputStream process))]
          (loop [line (read-line)]
            (cond
              (or (nil? line)
                  (timed-out?)) nil
              (and @start-signal
                   (re-find pattern line)) line
              :else (recur (read-line)))))
        (finally
          (.destroy process))))))

(defn- start-collecting []
  (compare-and-set! start-signal false true))

(defn logged-with-prefix?
  [text prefix]
  (is (re-find (re-pattern (str \^ prefix \/ tag)) text)))

(defn -setUp [this]
  (.superSetUp this)
  (reset! start-signal false))

(defn -testLogDebug [this]
  (let [test-text "This is a test debug message."
        collector (collect test-text)]
    (start-collecting)
    (log-d test-text)
    (logged-with-prefix? @collector "D")))

(defn -testLogVerbose [this]
  (let [test-text "This is a test verbose message."
        collector (collect test-text)]
    (start-collecting)
    (log-v test-text)
    (logged-with-prefix? @collector "V")))

(defn -testLogInfo [this]
  (let [test-text "This is a test info message."
        collector (collect test-text)]
    (start-collecting)
    (log-i test-text)
    (logged-with-prefix? @collector "I")))

(defn -testLogWarn [this]
  (let [test-text "This is a test warn message."
        collector (collect test-text)]
    (start-collecting)
    (log-w test-text)
    (logged-with-prefix? @collector "W")))

(defn -testLogError [this]
  (let [test-text "This is a test error message."
        collector (collect test-text)]
    (start-collecting)
    (log-e test-text)
    (logged-with-prefix? @collector "E")))
