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

(ns neko.listeners.dialog
  "Uility functions and macros for setting listeners corresponding to the
  android.content DialogInterface interface."
  {:author "Daniel Solano Gómez"}
  (:import android.content.DialogInterface))

(defn on-cancel-call
  "Takes a function and yields a DialogInterface.OnCancelListener object that
  will invoke the function.  This function must take one argument, the dialog
  that was cancelled."
  [handler-fn]
  (reify android.content.DialogInterface$OnCancelListener
    (onCancel [this dialog]
      (handler-fn dialog))))

(defmacro on-cancel
  "Takes a body of expressions and yields a DialogInterface.OnCancelListener object that
  will invoke the body.  The body takes an implicit argument 'dialog' that is the
  dialog that was cancelled."
  [& body]
  `(on-cancel-call (fn [~'dialog] ~@body)))

(defn on-click-call
  "Takes a function and yields a DialogInterface.OnCancelListener object that
  will invoke the function.  This function must take two arguments:

  dialog: the dialog that received the click
  which:  the button that was clicked (one of :negative, :neutral, or
          :positive) or the position of the item that was clicked"
  [handler-fn]
  (reify android.content.DialogInterface$OnClickListener
    (onClick [this dialog which]
      (let [which (condp = which
                    DialogInterface/BUTTON_NEGATIVE :negative
                    DialogInterface/BUTTON_NEUTRAL  :neutral
                    DialogInterface/BUTTON_POSITIVE :positive
                    which)]
        (handler-fn dialog which)))))

(defmacro on-click
  "Takes a body of expressions and yields a DialogInterface.OnCancelListener
  object that will invoke the function.  The body will take the following two
  implicit arguments:

  dialog: the dialog that received the click
  which:  the button that was clicked (one of :negative, :neutral, or
          :positive) or the position of the item that was clicked"
  [& body]
  `(on-click-call (fn [~'dialog ~'which] ~@body)))

(defn on-dismiss-call
  "Takes a function and yields a DialogInterface.OnDismissListener object that
  will invoke the function.  This function must take one argument, the dialog
  that was dismissed."
  [handler-fn]
  (reify android.content.DialogInterface$OnDismissListener
    (onDismiss [this dialog]
      (handler-fn dialog))))

(defmacro on-dismiss
  "Takes a body of expressions and yields a DialogInterface.OnDismissListener
  object that will invoke the body.  The body takes an implicit argument
  'dialog' that is the dialog that was dismissed."
  [& body]
  `(on-dismiss-call (fn [~'dialog] ~@body)))

(defn on-key-call
  "Takes a function and yields a DialogInterface.OnKeyListener object that will
  invoke the function.  This function must take the following three arguments:

  dialog:   the dialog the key has been dispatched to
  key-code: the code for the physical key that was pressed
  event:    the KeyEvent object containing full information about the event

  The function should evaluate to a logical true value if it has consumed the
  event, otherwise logical false."
  [handler-fn]
  (reify android.content.DialogInterface$OnKeyListener
    (onKey [this dialog key-code event]
      (boolean (handler-fn dialog key-code event)))))

(defmacro on-key
  "Takes a body of expressions and yields a DialogInterface.OnKeyListener
  object that will invoke the body.  The body takes the following three
  implicit arguments:

  dialog:   the dialog the key has been dispatched to
  key-code: the code for the physical key that was pressed
  event:    the KeyEvent object containing full information about the event

  The body should evaluate to a logical true value if it has consumed the
  event, otherwise logical false."
  [& body]
  `(on-key-call (fn [~'dialog ~'key-code ~'event] ~@body)))

(defn on-multi-choice-click-call
  "Takes a function and yields a DialogInterface.OnMultiChoiceClickListener
  object that will invoke the function.  This function must take the following
  three arguments:

  dialog:   the dialog where the selection was made
  which:    the position of the item in the list that was clicked
  checked?: true if the click checked the item, else false"
  [handler-fn]
  (reify android.content.DialogInterface$OnMultiChoiceClickListener
    (onClick [this dialog which checked?]
      (handler-fn dialog which checked?))))

(defmacro on-multi-choice-click
  "Takes a body of expressions and yields a
  DialogInterface.OnMultiChoiceClickListener object that will invoke the body.
  The body takes the following three implicit arguments:

  dialog:   the dialog where the selection was made
  which:    the position of the item in the list that was clicked
  checked?: true if the click checked the item, else false"
  [& body]
  `(on-multi-choice-click-call (fn [~'dialog ~'which ~'checked?] ~@body)))

(comment -- OnShowListener is added in API level 8)
