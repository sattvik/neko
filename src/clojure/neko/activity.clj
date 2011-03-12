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
  (:import android.app.Activity)
  (:require [neko.context :as context])
  (:use neko.-utils))

(def 
  ^{:doc "The current activity to operate on."}
  *activity*)

(defmacro with-activity
  "Evaluates body such that both *activity* and *context* are bound to the given activiy."
  [activity & body]
  `(let [activity# ~activity]
     (binding [context/*context* activity#
               *activity* activity#]
       ~@body)))

(defn find-view
  "Finds a view that identified by the given ID.  Returns the view if found or
  nil otherwise.  The id-or-name argument may either be the integer ID or a
  keyword name.

  If processed within a with-activity form, the activity argument may be
  omitted."
  ([id-or-name]
   (find-view *activity* id-or-name))
  ([^Activity activity id-or-name]
   (.findViewById activity (context/resolve-resource activity :id id-or-name))))

(defn set-content-view!
  "Sets the content for the activity.  The view may be one of:

  + A view object, which will be used directly
  + An integer presumed to be a valid layout ID
  + A keyword used to resolve to a layout ID using
    (neko.context/resolve-resource)"
  ([view]
   (set-content-view! *activity* view))
  ([^Activity activity view]
   {:pre [(or (instance? android.view.View view)
              (integer? view)
              (keyword? view))]}
   (cond
     (instance? android.view.View view)
       (.setContentView activity ^android.view.View view)
     (integer? view)
       (.setContentView activity ^Integer view)
     :else
       (.setContentView activity ^Integer (context/resolve-resource activity :layout view)))))

(defn request-window-features!
  "Requests the given features for the activity.  The features should be
  keywords such as :no-title or :indeterminate-progress corresponding
  FEATURE_NO_TITLE and FEATURE_INDETERMINATE_PROGRESS, respectively.  Returns a
  sequence of boolean values corresponding to each feature, where a true value
  indicates the requested feature is supported and now enabled.

  If within a with-activity form, supplying an activity as the first argument
  is not necessary.

  This function should be called before set-content-view!."
  [activity? & features]

  (let [[^Activity activity features]
          (if (instance? Activity activity?)
            [activity? features]
            [*activity* (cons activity? features)])
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
