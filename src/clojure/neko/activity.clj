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

(ns neko.activity
  "Utilities to aid in working with an activity."
  {:author "Daniel Solano Gómez"}
  (:import android.app.Activity
           android.view.View)
  (:require [neko.context :as context])
  (:use neko.-protocols.resolvable
        neko.-utils))

(def 
  ^{:doc "The current activity to operate on."
    :dynamic true}
  *activity*)

(defmacro with-activity
  "Evaluates body such that both *activity* and *context* are bound to the given activiy."
  [activity & body]
  `(let [activity# ~activity]
     (binding [context/*context* activity#
               *activity* activity#]
       ~@body)))

(defn activity?
  "Determines whether the argument is an instance of Activity."
  [x]
  (instance? Activity x))

(defn has-*activity*?
  "Ensures that the calling context has a valid *activity* var."
  []
  (and (bound? #'*activity*)
       (activity? *activity*)))

(defn set-content-view!
  "Sets the content for the activity.  The view may be one of:

  + A view object, which will be used directly
  + An integer presumed to be a valid layout ID
  + A keyword used to resolve to a layout ID using
    (neko.context/resolve-resource)"
  ([view]
   {:pre [(or (instance? View view)
              (resolvable? view))]}
   (set-content-view! *activity* view))
  ([^Activity activity view]
   {:pre [(activity? activity)
          (or (instance? View view)
              (resolvable? view))]}
   (cond
     (instance? View view)
       (.setContentView activity ^View view)
     (integer? view)
       (.setContentView activity ^Integer view)
     :else
       (.setContentView activity ^Integer (resolve-layout view activity)))))

(defn request-window-features!
  "Requests the given features for the activity.  The features should be
  keywords such as :no-title or :indeterminate-progress corresponding
  FEATURE_NO_TITLE and FEATURE_INDETERMINATE_PROGRESS, respectively.  Returns a
  sequence of boolean values corresponding to each feature, where a true value
  indicates the requested feature is supported and now enabled.

  If within a with-activity form, supplying an activity as the first argument
  is not necessary.

  This function should be called before set-content-view!."
  {:arglists '([& features] [activity & features])}
  [activity & features]
  {:pre  [(or (activity? activity)
              (and (keyword? activity)
                   (has-*activity*?)))
          (every? keyword? features)]
   :post [%
          (every? (fn [x] (instance? Boolean x)) %)]}
  (let [[^Activity activity features]
          (if (instance? Activity activity)
            [activity features]
            [*activity* (cons activity features)])
        keyword->int (fn [k]
                       (static-field-value android.view.Window
                                           k
                                           #(str "FEATURE_" %)))
        request-feature  (fn [k]
                           (try
                             (.requestWindowFeature activity (keyword->int k))
                             (catch NoSuchFieldException _
                               (throw (IllegalArgumentException.
                                        (format "‘%s’ is not a valid feature."
                                                k))))))]
    (doall (map request-feature features))))
