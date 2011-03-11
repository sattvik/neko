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

(ns neko.compilation
  "Utility functions for managing the compilation environment when using
  version of Clojure that supports dynamic compilation on the Dalvik virtual
  machine.

  To use this namespace, you need to call init with a context, such as an
  activity object.  This will create a cache directory where temporary files
  will be placed and will set the 'clojure.compile.path' system property and
  the '*compile-path*' var.  If the cache directory already exists, it will be
  cleaned out.

  Note that additional invocations to init within the same process will not have
  any effect."
  {:author "Daniel Solano Gómez"}
  (:import android.content.Context
           java.io.File))

(def #^{:doc "Whether or not compilation has been initialised"
        :private true}
  cache-dir (atom nil))

(def #^{:doc "The default name of the cache directory."}
  default-cache-dir "clojure_cache")

(defn- cache-file?
  "Predicate for determining if a given file name is a cache file."
  [^String name]
  (and (.startsWith name "repl-")
       (or (.endsWith name ".dex")
           (.endsWith name ".jar"))))

(defn clear-cache
  "Clears all DEX and JAR files from the cache directory."
  []
  (locking cache-dir
    (let [^File dir @cache-dir
          delete-file (fn [^String name] (.delete (File. dir name)))]
      (when dir
        (->>
          (.list dir)
          (filter cache-file?)
          (map delete-file)
          (dorun))))))

(defn init
  "Initialises the compilation path, creating or cleaning cache directory as
  necessary."
  ([^Context context ^String dir-name]
   (locking cache-dir
     (when-not @cache-dir
       (let [dir  (.getDir context dir-name Context/MODE_PRIVATE)
             path (.getAbsolutePath dir)]
         (reset! cache-dir dir)
         (System/setProperty "clojure.compile.path" path)
         (alter-var-root #'clojure.core/*compile-path* (constantly path))))))
  ([^Context context]
   (init context default-cache-dir)))
