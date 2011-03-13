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

(ns neko.listeners.text-view
  "Uility functions and macros for creating listeners corresponding to the
  android.widget.TextView class."
  {:author "Daniel Solano Gómez"})

(defn on-editor-action-call
  "Takes a function and yields a TextView.OnEditorActionListener object that
  will invoke the function.  This function must take the following three
  arguments:

  view       the view that was clicked
  action-id  identifier of the action, this will be either the identifier you
             supplied or EditorInfo/IME_NULL if being called to the enter key
             being pressed
  key-event  if triggered by an enter key, this is the event; otherwise, this
             is nil

  The function should evaluate to a logical true value if it has consumed the
  action, otherwise logical false."
  [handler-fn]
  {:pre  [(fn? handler-fn)]
   :post [(instance? android.widget.TextView$OnEditorActionListener %)]}
  (reify android.widget.TextView$OnEditorActionListener
    (onEditorAction [this view action-id key-event]
      (boolean (handler-fn view action-id key-event)))))

(defmacro on-editor-action
  "Takes a body of expressions and yields a TextView.OnEditorActionListener
  object that will invoke the body.  The body takes the following implicit
  arguments:

  view       the view that was clicked
  action-id  identifier of the action, this will be either the identifier you
             supplied or EditorInfo/IME_NULL if being called to the enter key
             being pressed
  key-event  if triggered by an enter key, this is the event; otherwise, this
             is nil

  The body should evaluate to a logical true value if it has consumed the
  action, otherwise logical false."
  [& body]
  `(on-editor-action-call (fn [~'view ~'action-id ~'key-event] ~@body)))
