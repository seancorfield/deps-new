;; copyright (c) 2021 sean corfield, all rights reserved

(ns ^:no-doc org.corfield.new.impl
  "The implementation helpers for `org.corfield.new/create`."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.build.api :as b])
  (:import (java.nio.file Files)
           (java.nio.file.attribute FileAttribute)
           (java.text SimpleDateFormat)
           (java.util Calendar Date)))

(set! *warn-on-reflection* true)

(defn- ->ns
  "Given a string or symbol, presumably representing a
  file path, return a string that represents the
  equivalent namespace."
  [f]
  (-> f (str) (str/replace "/" ".") (str/replace "_" "-")))

(defn- ->file
  "Given a string or symbol, presumably representing a
  namespace, return a string that represents the
  equivalent file system path."
  [n]
  (-> n (str) (str/replace "." "/") (str/replace "-" "_")))

(defn find-root
  "Given a qualified symbol that represents a template,
  return a pair of the directory path to the template's
  files and the path of the `template.edn` file that
  describes how to produce a project from it."
  [template-sym]
  (let [poss-dir (->file template-sym)
        edn-file (str poss-dir "/template.edn")
        paths    (str/split (System/getProperty "java.class.path")
                            (re-pattern (System/getProperty "path.separator")))]
    (some #(let [file (io/file (str % "/" edn-file))]
             (when (.exists file)
               [(.getCanonicalPath (io/file (str % "/" poss-dir)))
                (.getCanonicalPath file)]))
          paths)))

(defn ->subst-map
  "Given a hash map of substitution data, return a hash map of
  string substitutions, suitable for `tools.build.api/copy-dir`.

  For any unqualified keys that have string or symbol values,
  compute a `/ns` version that could be used as a namespace and
  a `/file` version that could be used as a filename. These are
  done fairly simply as seen above."
  [data]
  (reduce-kv (fn [m k v]
               (let [n (namespace k)
                     s (str (when n (str n "/")) (name k))]
                 (cond-> (assoc m (str "{{" s "}}") (str v))
                   (and (nil? n) (or (string? v) (symbol? v)))
                   (assoc (str "{{" s "/ns}}")   (->ns   v)
                          (str "{{" s "/file}}") (->file v)))))
             {}
             data))

(defn- substitute
  "Given a string and a substitution hash map, return the
  string with all substitutions performed."
  [s data]
  (reduce (fn [s [from to]] (str/replace s from to)) s data))

(defn copy-template-dir
  "Given a template directory, a target directory, a tuple
  of source directory, target subdirectory (with possible
  substitutions), and optional map of files to rename, and
  a substitution hash map, perform the copy with all those
  substitutions.

  If files is provided, any files found in the source directory
  that are not explicitly mentioned are copied directly."
  [dir target-dir [src target files] data]
  (let [target (when target (str "/" (substitute target data)))]
    (if (seq files)
      (let [intermediate (-> (Files/createTempDirectory
                              "deps-new" (into-array FileAttribute []))
                             (.toFile)
                             (doto .deleteOnExit)
                             (.getCanonicalPath))]
        ;; first we just copy the raw files with no substitutions:
        (b/copy-dir {:target-dir intermediate
                     :src-dirs   [(str dir "/" src)]})
        ;; now we process the named files, substituting paths:
        (run! (fn [[from to]]
                (b/delete {:path (str intermediate "/" from)})
                (b/copy-file {:src    (str dir "/" src "/" from)
                              :target (str intermediate target "/"
                                           (substitute to data))}))
              files)
        ;; finally we copy the prepared folder (with substitutions):
        (b/copy-dir {:target-dir target-dir
                     :src-dirs   [intermediate]
                     :replace    data}))
      (b/copy-dir {:target-dir (str target-dir target)
                   :src-dirs   [(str dir "/" src)]
                   :replace    data}))))

(def ^:private known-scms
  "A string to be used as part of a regex, identifying
  known SCM providers that we special case."
  "^(io|com|org)\\.(github|gitlab|bitbucket)\\.")

(defn- deconstruct-project-name
  "Given a symbol, make it canonical, and break down the
  various things we derive from it."
  [project-name]
  (let [project-name (if (namespace project-name)
                       project-name
                       (symbol (name project-name)
                               (name project-name)))
        qualifier    (namespace project-name)
        base-name    (name project-name)
        top          (str/replace qualifier (re-pattern known-scms) "")]
    {:artifact/id base-name
     :group/id    (if (str/includes? qualifier ".")
                    qualifier
                    (str "net.clojars." qualifier))
     :main        base-name
     :name        project-name
     :scm/domain  (let [[_ scm-tld scm-host]
                        (re-matches (re-pattern (str known-scms ".*$")) qualifier)]
                    (if scm-host
                      (str scm-host "."
                           (if (= "io" scm-tld)
                             "com"
                             scm-tld))
                      "github.com"))
     :scm/user    top
     :scm/repo    base-name
     :top         top}))

(defn preprocess-options
  "Given the raw options hash map, preprocess, parse, and
  validate certain values, and derive defaults for others."
  [{:keys [template target-dir], project-name :name, :as opts}]
  (when-not (and template project-name)
    (throw (ex-info "Both :template and :name are required." opts)))
  (let [template   (symbol template) ; allow for string or symbol
        template   (if (namespace template)
                     template
                     ;; default ns for short template names:
                     (symbol "org.corfield.new" (name template)))
        {:keys [main] :as name-data}
        (deconstruct-project-name (symbol project-name)) ; allow for string or symbol
        target-dir (str (or target-dir main))
        username   (or (System/getenv "USER")
                       (System/getProperty "user.name"))]
    (merge name-data
           {:developer  (str/capitalize username)
            :now/date   (.format (SimpleDateFormat. "yyyy-MM-dd") (Date.))
            :now/year   (.get (Calendar/getInstance) Calendar/YEAR)
            :raw-name   project-name
            :template   template
            :target-dir target-dir
            :user       username
            :version    "0.1.0-SNAPSHOT"}
           ;; remove options we cleaned up:
           (dissoc opts :template :target-dir :name))))

(comment
  (find-root 'org.corfield.new/app)
  (find-root 'org.corfield.new/lib)
  (substitute "{{foo/file}}.clj"
              (->subst-map {:top/ns "org.corfield"
                            :foo    "org.corfield-ns"
                            :bar    "org/corfield_file"}))
  )
