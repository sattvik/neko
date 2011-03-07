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

(use 'clojure.java.io)
(import '[java.io File PushbackReader]
        '[java.util.regex Matcher Pattern])

(defn find-clojure-files
  "Finds all Clojure source files in a given directory."
  [path]
  (let [file (as-file path)]
    (cond
      (.isDirectory file) (mapcat find-clojure-files (.listFiles file))
      (and (.isFile file)
           (.. file getName (endsWith ".clj"))) [file]
      :else nil)))

(defn file->ns
  "Looks for the namespace in a Clojure source file."
  [file]
  (binding [*in* (PushbackReader. (reader file))]
    (let [next-form (fn [] (read *in* false ::eof))]
      (loop [form (next-form)]
        (cond
          (= ::eof form)
            nil
          (and (list? form)
               (= 'ns (first form)))
            (second form)
          :else
            (recur (next-form)))))))

(defn compile-ns
  "Compiles a namespace."
  [ns]
  (println (format "Compiling %s…" ns))
  (compile ns))


(defn compile-dir
  "Compiles all Clojure files in the given directory."
  [dir]
  (binding [*compile-path* (System/getProperty "clojure.compile.path")]
    (->> (find-clojure-files dir)
         (map file->ns)
         (remove nil?)
         (map compile-ns)
         (dorun))))

(defn args->dirs
  "Splits any path arguments into directories"
  [args]
  (let [split-path (fn [arg] (seq (.split arg (Pattern/quote File/pathSeparator))))]
    (flatten (map split-path args))))

(dorun (map compile-dir (args->dirs *command-line-args*)))
