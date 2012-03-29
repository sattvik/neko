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

(ns neko.threading
  "Utilities used to manage multiple threads on Android."
  {:author "Daniel Solano Gómez"}
  (:import android.app.Activity
           android.view.View
           clojure.lang.IFn
           java.util.concurrent.TimeUnit
           neko.threading.AsyncTask))

(defn run-on-ui-thread*
  "Runs the given nullary function on the UI thread.  If this function is
  called on the UI thread, it will evaluate immediately."
  [^Activity activity ^IFn f]
  (.runOnUiThread activity (reify Runnable (run [this] (f)))))

(defmacro run-on-ui-thread
  "Runs the macro body on the UI thread.  If this macro is called on the UI
  thread, it will evaluate immediately."
  [activity & body]
  `(run-on-ui-thread* ~activity (fn [] ~@body)))

(defn post*
  "Causes the function to be added to the message queue.  The function will
  execute on the UI thread.  Returns true if successfully placed in the message
  queue."
  [^View view ^IFn f]
  (.post view (reify Runnable (run [this] (f)))))

(defmacro post
  "Causes the macro body to be added to the message queue.  It will execute on
  the UI thread.  Returns true if successfully placed in the message queue."
  [^View view & body]
  `(post* ~view (fn [] ~@body)))

(defn post-delayed*
  "Causes the function to be added to the message queue, to be run after the
  specified amount of time elapses.  The function will execute on the UI
  thread.  Returns true if successfully placed in the message
  queue."
  [^View view ^long millis  ^IFn f]
  (.postDelayed view (reify Runnable (run [this] (f))) millis ))

(defmacro post-delayed
  "Causes the macro body to be added to the message queue.  It will execute on
  the UI thread.  Returns true if successfully placed in the message queue."
  [view millis & body]
  `(post-delayed* ~view ~millis (fn [] ~@body)))

(defn on-ui-thread?
  "Returns a logical true value if the current thread is a UI thread."
  []
  (android.os.Looper/myLooper))

(defrecord Task
  [^IFn bg-fn
   ^IFn pre-fn
   ^IFn post-fn
   ^IFn progress-fn
   ^IFn cancel-fn
   ^AsyncTask real-task])

(defn new-task
  "Creates a new asynchronous task that will execute the given function in the background."
  [f]
  (Task. f nil nil nil nil nil))

(defn with-pre-execute
  [task f]
  (assoc task :pre-fn f))

(defn with-post-execute
  [task f]
  (assoc task :post-fn f))

(defn with-on-progress-update
  [task f]
  (assoc task :progress-fn f))

(defn with-on-cancelled
  [task f]
  (assoc task :cancel-fn f))

(def ^{:private true
       :dynamic true}
  *async-task*)

(defn publish-progress
  [& values]
  {:pre [(bound? #'*async-task*)]}
  (.superPublishProgress ^AsyncTask *async-task* (to-array values)))

(defn execute!
  ""
  ([^Task task & params]
   (let [real-task (proxy [AsyncTask] []
                     (doInBackground [_]
                       (binding [*async-task* this]
                         (let [bg-fn (.bg_fn task)]
                           (if (= [::no-args] params)
                             (bg-fn)
                             (apply bg-fn params)))))

                     (onPreExecute []
                       (when-let [pre-fn (.pre_fn task)]
                         (pre-fn)))

                     (onPostExecute [result]
                       (when-let [post-fn (.post_fn task)]
                         (post-fn result)))

                     (onProgressUpdate [values]
                       (when-let [progress-fn (.progress_fn task)]
                         (apply progress-fn values)))

                     (onCancelled []
                       (when-let [cancel-fn (.cancel_fn task)]
                         (cancel-fn))))]
     (assoc task :real-task (.execute real-task nil))))
  ([task]
   (execute! task ::no-args)))

(def ^{:doc "A map of unit keywords to TimeUnit instances."
       :private true}
  unit-map
  {; days/hours/minutes added in API level 9
   ;:days    TimeUnit/DAYS
   ;:hours   TimeUnit/HOURS
   ;:minutes TimeUnit/MINUTES
   :seconds TimeUnit/SECONDS
   :millis  TimeUnit/MILLISECONDS
   :micros  TimeUnit/MICROSECONDS
   :nanos   TimeUnit/NANOSECONDS})

(defn result-of
  "Gets the result of an executed task.  The bare version will block until the
  task is complete.  If provided a time with no unit, the unit is assumed to be
  milliseconds.  The unit may be a an instance of java.util.concurrent.TimeUnit
  or one of the following keywords: :seconds, :millis, :micros, or :nanos.

  Note that if you use a TimeUnit instance, units larger than seconds are not
  supported before API level 9."
  ([task]
   (result-of task ::ignored ::ignored))
  ([task time]
   (result-of task time TimeUnit/MILLISECONDS))
  ([^Task task time units]
   {:pre [; must be executed
          (.real_task task)
          ; time must be ::ignored or a non-negative number
          (or (= ::ignored time)
              (and (number? time)
                   (not (neg? time))))
          ; units must be a valid keyword or a TimeUnit
          (or (= ::ignored units)
              (and (keyword? units)
                   (units unit-map))
              (instance? TimeUnit units))]}
   (let [^AsyncTask real-task (.real_task task)]
     (if (= ::ignored time)
       (.get real-task)
       (.get real-task time (cond
                              (keyword? units) (units unit-map)
                              :else units))))))

(defn cancel
  ([^Task task may-interrupt?]
   {:pre [; must be executed
          (.real_task task)]}
   (let [^AsyncTask real-task (.real_task task)]
     (.cancel real-task (boolean may-interrupt?))))
  ([task]
   (cancel task true))
  )

