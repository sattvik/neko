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

(ns neko.listeners.adapter-view
  "Uility functions and macros for creating listeners corresponding to the
  android.widget.AdapterView class."
  {:author "Daniel Solano Gómez"})

(defn on-item-click-call
  "Takes a function and yields an AdapterView.OnItemClickListener object that
  will invoke the function.  This function must take the following four
  arguments:

  parent    the AdapterView where the click happened
  view      the view within the AdapterView that was clicked
  position  the position of the view in the adapter
  id        the row id of the item that was clicked"
  [handler-fn]
  {:pre  [(fn? handler-fn)]
   :post [(instance? android.widget.AdapterView$OnItemClickListener %)]}
  (reify android.widget.AdapterView$OnItemClickListener
    (onItemClick [this parent view position id]
      (handler-fn parent view position id))))

(defmacro on-item-click
  "Takes a body of expressions and yields an AdapterView.OnItemClickListener
  object that will invoke the body.  The body takes the following implicit
  arguments:

  parent    the AdapterView where the click happened
  view      the view within the AdapterView that was clicked
  position  the position of the view in the adapter
  id        the row id of the item that was clicked"
  [& body]
  `(on-item-click-call (fn [~'parent ~'view ~'position ~'id] ~@body)))

(defn on-item-long-click-call
  "Takes a function and yields an AdapterView.OnItemLongClickListener object
  that will invoke the function.  This function must take the following four
  arguments:

  parent    the AdapterView where the click happened
  view      the view within the AdapterView that was clicked
  position  the position of the view in the adapter
  id        the row id of the item that was clicked

  The function should evaluate to a logical true value if it has consumed the
  long click; otherwise logical false."
  [handler-fn]
  {:pre  [(fn? handler-fn)]
   :post [(instance? android.widget.AdapterView$OnItemLongClickListener %)]}
  (reify android.widget.AdapterView$OnItemLongClickListener
    (onItemLongClick [this parent view position id]
      (boolean (handler-fn parent view position id)))))

(defmacro on-item-long-click
  "Takes a body of expressions and yields an
  AdapterView.OnItemLongClickListener object that will invoke the body.  The
  body takes the following implicit arguments:

  parent    the AdapterView where the click happened
  view      the view within the AdapterView that was clicked
  position  the position of the view in the adapter
  id        the row id of the item that was clicked

  The body should evaluate to a logical true value if it has consumed the long
  click; otherwise logical false."
  [& body]
  `(on-item-long-click-call (fn [~'parent ~'view ~'position ~'id] ~@body)))

(defn on-item-selected-call
  "Takes one or two functions and yields an AdapterView.OnItemSelectedListener object
  that will invoke the functions.  The first function will be called to handle the
  onItemSelected(…) method and must take the following four arguments:

  parent    the AdapterView where the selection happened
  view      the view within the AdapterView that was clicked
  position  the position of the view in the adapter
  id        the row id of the item that was selected

  If a second function is provided, it will be called when the selection
  disappears from the view.  It takes a single argument, the AdapterView that
  now contains no selected item."
  ([item-fn]
   {:pre  [(fn? item-fn)]
    :post [(instance? android.widget.AdapterView$OnItemSelectedListener %)]}
   (on-item-selected-call item-fn nil))
  ([item-fn nothing-fn]
   {:pre  [(fn? item-fn)
           (or (nil? nothing-fn)
               (fn? nothing-fn))]
    :post [(instance? android.widget.AdapterView$OnItemSelectedListener %)]}
   (reify android.widget.AdapterView$OnItemSelectedListener
     (onItemSelected [this parent view position id]
       (item-fn parent view position id))
     (onNothingSelected [this parent]
       (when nothing-fn
         (nothing-fn parent))))))

(defmacro on-item-selected
  "Takes a body of expressions and yields an AdapterView.OnItemSelectedListener
  object that will invoke the body  The body takes the following implicit
  arguments:

  type      either :item corresponding an onItemSelected(…) call or :nothing
            corresponding to an onNothingSelected(…) call
  parent    the AdapterView where the selection happened or now contains no
            selected item
  view      the view within the AdapterView that was clicked.  If type is
            :nothing, this will be nil
  position  the position of the view in the adapter.  If type is :nothing, this
            will be nil.
  id        the row id of the item that was selected.  If type is :nothing,
            this will be nil."
  [& body]
  `(let [handler-fn# (fn [~'type ~'parent ~'view ~'position ~'id]
                       ~@body)]
     (on-item-selected-call
       (fn ~'item-handler [parent# view# position# id#]
         (handler-fn# :item parent# view# position# id#))
       (fn ~'nothing-handler [parent#]
         (handler-fn# :nothing parent# nil nil nil)))))
