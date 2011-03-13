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

(ns neko.listeners.view
  "Uility functions and macros for setting listeners corresponding to the
  android.view.View class."
  {:author "Daniel Solano Gómez"})

(defn on-click-call
  "Takes a function and yields a View.OnClickListener object that will invoke
  the function.  This function must take one argument, the view that was
  clicked."
  [handler-fn]
  (reify android.view.View$OnClickListener
    (onClick [this view]
      (handler-fn view))))

(defmacro on-click
  "Takes a body of expressions and yields a View.OnClickListener object that
  will invoke the body.  The body takes an implicit argument 'view' that is the
  view that was clicked."
  [& body]
  `(on-click-call (fn [~'view] ~@body)))

(defn on-create-context-menu-call
  "Takes a function and yields a View.OnCreateContextMenuListener object that
  will invoke the function.  This function must take the following three
  arguments:

  menu: the context menu that is being built
  view: the view for which the context menu is being built
  info: extra information about the item for which the context menu should be
        shown.  This information will vary depending on the class of view."
  [handler-fn]
  (reify android.view.View$OnCreateContextMenuListener
    (onCreateContextMenu [this menu view info]
      (handler-fn menu view info))))

(defmacro on-create-context-menu
  "Takes a body of expressions and yields a View.OnCreateContextMenuListener
  object that will invoke the body.  The body takes the following three
  implicit arguments:

  menu: the context menu that is being built
  view: the view for which the context menu is being built
  info: extra information about the item for which the context menu should be
        shown.  This information will vary depending on the class of view."
  [& body]
  `(on-create-context-menu-call (fn [~'menu ~'view ~'info] ~@body)))

(comment -- Introduced in SDK version 11 (Honeycomb)
(defn on-drag-call
  "Takes a function and yields a View.OnDragListener object that will invoke
  the function.  This function must take the two arguments described in
  on-drag and should return a boolean."
  [handler-fn]
  (reify android.view.View$OnDragListener
    (onDrag [this view event]
      (handler-fn view event))))

(defmacro on-drag
  "Takes a body of expressions and yields a View.OnDragListener object that
  will invoke the body.  The body takes the following two implicit arguments:

  view:  the view that received the drag event
  event: the DragEvent object for the drag event"
  [& body]
  `(on-drag-call (fn [~'view ~'event] ~@body))))

(defn on-focus-change-call
  "Takes a function and yields a View.OnFocusChangeListener object that will
  invoke the function.  This function must take the following two arguments:

  view:     the view whose state has changed
  focused?: the new focuse state for view"
  [handler-fn]
  (reify android.view.View$OnFocusChangeListener
    (onFocusChange [this view focused?]
      (handler-fn view focused?))))

(defmacro on-focus-change
  "Takes a body of expressions and yields a View.OnFocusChangeListener object
  that will invoke the body.  The body takes the following two implicit
  arguments:

  view:     the view whose state has changed
  focused?: the new focuse state for view"
  [& body]
  `(on-focus-change-call (fn [~'view ~'focused?] ~@body)))

(defn on-key-call
  "Takes a function and yields a View.OnKeyListener object that will invoke the
  function.  This function must take the following three arguments:

  view:     the view the key has been dispatched to
  key-code: the code for the physical key that was pressed
  event:    the KeyEvent object containing full information about the event

  The function should evaluate to a logical true value if it has consumed the
  event, otherwise logical false."
  [handler-fn]
  (reify android.view.View$OnKeyListener
    (onKey [this view key-code event]
      (boolean (handler-fn view key-code event)))))

(defmacro on-key
  "Takes a body of expressions and yields a View.OnKeyListener object that will
  invoke the body.  The body takes the following three implicit arguments:

  view:     the view the key has been dispatched to
  key-code: the code for the physical key that was pressed
  event:    the KeyEvent object containing full information about the event

  The body should evaluate to a logical true value if it has consumed the
  event, otherwise logical false."
  [& body]
  `(on-key-call (fn [~'view ~'key-code ~'event] ~@body)))

(comment -- as of API level 11
(defn on-layout-change-call
  "Takes a function and yields a View.OnLayoutChangeListener object that
  will invoke the function.  This function must take the arguments  described
  in on-layout-change."
  [handler-fn]
  (reify android.view.View$OnLayoutChangeListener
    (onLayoutChange [this view left top right bottom
                     old-left old-top old-right old-bottom]
      (handler-fn view left top right bottom
                  old-left olt-top old-right old-bottom))))

(defmacro on-layout-change
  "Takes a body of expressions and yields a View.OnLayoutChangeListener
  object that will invoke the body.  The body takes the following implicit
  arguments:

  view:       the view whose state has changed
  left:       the new value of the view's left property
  top:        the new value of the view's top property
  right:      the new value of the view's right property
  bottom:     the new value of the view's bottom property
  old-left:   the previous value of the view's left property
  old-top:    the previous value of the view's top property
  old-right:  the previous value of the view's right property
  old-bottom: the previous value of the view's bottom property"
  [& body]
  `(on-key-call (fn [~'view ~'left ~'top ~'right ~'bottom
                 ~'old-left ~'old-top ~'old-right ~'old-bottom] ~@body))))

(defn on-long-click-call
  "Takes a function and yields a View.OnLongClickListener object that will
  invoke the function.  This function must take one argument, the view that was
  clicked, and must evaluate to a logical true value if it has consumed the
  long click, otherwise logical false."
  [handler-fn]
  (reify android.view.View$OnLongClickListener
    (onLongClick [this view]
      (boolean (handler-fn view)))))

(defmacro on-long-click
  "Takes a body of expressions and yields a View.OnLongClickListener object
  that will invoke the body.  The body takes an implicit argument 'view' that
  is the view that was clicked and held.  The body should also evaluate to a
  logical true value if it consumes the long click, otherwise logical false."
  [& body]
  `(on-long-click-call (fn [~'view] ~@body)))

(comment -- incomplete -- also for API level 11 (Honeycomb)
(defn on-system-ui-visibility-change-call
  "Takes a function and yields a View.OnSystemUiVisibilityChangeListener object
  that will invoke the function.  This function must take one argument, the
  view that was clicked, and must evaluate to true if it has consumed the long
  click, false otherwise."
  [handler-fn]
  (reify android.view.View$OnSystemUiVisibilityChangeListener
    (onLongClick [this view]
      (handler-fn view))))

(defmacro on-system-ui-visibility-change
  "Takes a body of expressions and yields a
  View.OnSystemUiVisibilityChangeListener object that will invoke the body.
  The body takes an implicit argument 'visibility' which will be either
  :status-bar-hidden or :status-bar-visible."
  [& body]
  `(on-system-ui-visibility-change-call (fn [~'visibility] ~@body))))

(defn on-touch-call
  "Takes a function and yields a View.OnTouchListener object that will invoke
  the function.  This function must take the following two arguments:

  view:  the view the touch event has been dispatched to
  event: the MotionEvent object containing full information about the event

  The function should evaluate to a logical true value if it consumes the
  event, otherwise logical false."
  [handler-fn]
  (reify android.view.View$OnTouchListener
    (onTouch [this view event]
      (boolean (handler-fn view event)))))

(defmacro on-touch
  "Takes a body of expressions and yields a View.OnTouchListener object that
  will invoke the body.  The body takes the following implicit arguments:
  
  view:  the view the touch event has been dispatched to
  event: the MotionEvent object containing full information about the event
  
  The body should evaluate to a logical value if it consumes the event,
  otherwise logical false."
  [& body]
  `(on-touch-call (fn [~'view ~'event] ~@body)))
