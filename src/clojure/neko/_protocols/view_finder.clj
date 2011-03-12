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

(ns neko.-protocols.view-finder
  "Home of the ViewFinder protocol and related utilities."
  {:author "Daniel Solano Gómez"})

(defprotocol ViewFinder
  "Protocol for types that support finding child views by an ID."
  (-find-view [finder id]))
