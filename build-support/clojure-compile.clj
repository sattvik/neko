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
(use 'clojure.stacktrace)
(import '[java.io File PushbackReader]
        '[java.util.regex Matcher Pattern])

(set! *warn-on-reflection*
      (Boolean/valueOf (System/getProperty "clojure.warn.reflection" "false")))

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

(def compiled-ns (atom #{}))

(defn compile-ns
  "Compiles a namespace."
  ([ns failed-ns]
   (when-not (@compiled-ns ns)
     (try
       (println (format "Compiling %s…" ns))
       (compile ns)
       (swap! compiled-ns conj ns)
       true
       (catch Exception e
         (let [msg (.getMessage e)
               cnfe-pattern #".*java\.lang\.ClassNotFoundException: ([a-zA-z0-9-_.]+)(, compiling:| )\(.*:\d+\)$"]
           (if-let [matches (re-matches cnfe-pattern msg)]
             (let [not-found-ns (symbol (matches 1))]
               (println (format "Dependency failure detected, will try to compile %s." not-found-ns))
               (if (failed-ns not-found-ns)
                 (throw e)
                 (if (compile-ns not-found-ns (conj failed-ns ns))
                   (do
                     (println "Failure resolved.")
                     (compile-ns ns failed-ns))
                   (throw e))))
             (throw e)))))))
  ([ns]
   (compile-ns ns #{})))

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
  (let [split-path (fn [^String arg] (seq (.split arg (Pattern/quote File/pathSeparator))))]
    (flatten (map split-path args))))

(dorun (map compile-dir (args->dirs *command-line-args*)))
