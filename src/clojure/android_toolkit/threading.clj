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

(ns android-toolkit.threading
  "Utilities used to manage multiple threads on Android."
  {:author "Daniel Solano Gómez"}
  (:import android.app.Activity
           android.view.View
           android_toolkit.threading.AsyncTask
           clojure.lang.IFn
           java.util.concurrent.TimeUnit))

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

(defn post
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

(defn post-delayed
  "Causes the macro body to be added to the message queue.  It will execute on
  the UI thread.  Returns true if successfully placed in the message queue."
  [^View view ^long millis & body]
  `(post-delayed* ~view ~millis (fn [] ~@body)))

(defn on-ui-thread?
  "Returns a truthy value if the current thread is a UI thread."
  []
  (android.os.Looper/myLooper))

(defrecord Task
  [^IFn bg-fn
   ^IFn pre-fn
   ^IFn post-fn
   ^IFn progress-fn
   ^IFn cancel-fn
   ^android.os.AsyncTask real-task])

(defn new-task
  "Creates a new asynchronous task that will execute the given function in the background."
  [f]
  (Task. f nil nil nil nil nil))

(defn with-pre-execute
  [^Task task f]
  (assoc task :pre-fn f))

(defn with-post-execute
  [^Task task f]
  (assoc task :post-fn f))

(defn with-on-progress-update
  [^Task task f]
  (assoc task :progress-fn f))

(defn with-on-cancelled
  [^Task task f]
  (assoc task :cancel-fn f))

(def *async-task* nil)


(use 'android-toolkit.log)
(deflog "ARGH!")
(defn publish-progress
  [& values]
  (if *async-task*
    (.superPublishProgress *async-task* (to-array values))
    (throw (IllegalStateException.
             "Not within the scope of a background function of a running asynchronous task."))))

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
  ([^Task task]
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
  ([^Task task]
   (result-of task ::ignored ::ignored))
  ([^Task task time]
   (result-of task time TimeUnit/MILLISECONDS))
  ([^Task task time units]
   (if-let [real-task (.real_task task)]
     (if (= ::ignored time)
       (.get real-task)
       (.get real-task
             time
             (cond 
               (nil? units) (throw (NullPointerException. "Null time unit type"))
               (instance? TimeUnit units) units
               (and (keyword? units)
                    (units unit-map)) (units unit-map)
               :else (throw (IllegalArgumentException. "Invalid time unit type")))))
     (throw (IllegalArgumentException. "Cannot get the result of an unexecuted task.")))))

(defn cancel
  ([^Task task may-interrupt?]
   (if-let [real-task (.real_task task)]
     (.cancel real-task may-interrupt?)
     (throw (IllegalArgumentException. "Cannot cancel an unexecuted task."))))
  ([^Task task]
   (cancel task true))
  )

