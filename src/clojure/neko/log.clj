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

(ns neko.log
  "Utility for logging in Android.  To use this utility simply use the deflog
  macro as follows:

    (deflog \"MyTag\")

  This will intern a number of functions in your namespace, namely: log-d,
  log-v, log-i, log-w, log-e, and log-wtf.  These functions will perform the
  equivalent of calling the corresponding method on the android.util.Log
  class with the tag given in the deflog macro call.

  For example, given the above deflog call,
  
    (log-d \"Some log string\")

  is equivalent to:

    (android.util.Log/d \"MyTag\" \"Some log string\")

  Calls to log-wtf on platforms that do not support Log.wtf() will be
  downgraded to Log.e() calls."
  {:author "Daniel Solano Gómez"}
  (:import android.util.Log))

(defmacro deflogfn
  "Macro for generating log functions."
  {:private true}
  [fn-name doc-string method-name]
  `(defn ~fn-name
     ~doc-string
     ([tag# message#]
      (. Log (~method-name tag# message#)))
     ([tag# message# throwable#]
      (. Log (~method-name tag# message# throwable#)))))

(deflogfn log-debug "Sends a DEBUG log message." d)

(deflogfn log-error "Sends a ERROR log message." e)

(deflogfn log-info "Sends a INFO log message." i)

(deflogfn log-verbose "Sends a VERBOSE log message." v)

(deflogfn log-warn "Sends a WARN log message." w)

(defmacro deflog
  "Creates a number of logging functions for the given tag."
  [tag]
  `(do
     (intern *ns* (with-meta (symbol "log-d") {:private true}) (partial log-debug ~tag))
     (intern *ns* (with-meta (symbol "log-e") {:private true}) (partial log-error ~tag))
     (intern *ns* (with-meta (symbol "log-i") {:private true}) (partial log-info ~tag))
     (intern *ns* (with-meta (symbol "log-v") {:private true}) (partial log-verbose ~tag))
     (intern *ns* (with-meta (symbol "log-w") {:private true}) (partial log-warn ~tag))))
