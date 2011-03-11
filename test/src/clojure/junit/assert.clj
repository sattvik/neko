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

(ns junit.assert
  "A small namespace to help with the tediousness of having to use JUnit
  assertions."
  {:author "Daniel Solano Gómez"}
  (:import junit.framework.Assert))

(defn is
  [actual]
  (Assert/assertTrue (boolean actual)))

(defn is-not
  [actual]
  (Assert/assertFalse (boolean actual)))

(defn is-eq
  [expected actual]
  (Assert/assertEquals expected actual))

(defn is-nil
  [actual]
  (Assert/assertNull actual))

(defn is-not-nil
  [actual]
  (Assert/assertNotNull actual))

(defn is-not-same
  [expected actual]
  (Assert/assertNotSame expected actual))

(defn is-same
  [expected actual]
  (Assert/assertSame expected actual))

(defmacro does-throw
  [ex-class form]
  `(try
     ~form
     (Assert/fail (str "Exception " ~ex-class " not thrown."))
     (catch ~ex-class _#
       nil)
     (catch Throwable e#
       (Assert/fail (str "Wrong exception " (class e#) " caught.")))))
