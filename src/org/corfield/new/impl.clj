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

(defn- adjust-subst-map
  "Given a substitution hash map and new open/close tags,
  return an updated substitution hash map with the keys
  adjusted to use the new open/close tags instead."
  [data open close]
  (reduce-kv (fn [m k v]
               (let [k' (-> k
                            (str/replace #"^\{\{" open)
                            (str/replace #"\}\}$" close))]
                 (assoc m k' v)))
             {}
             data))

(comment
  (adjust-subst-map (->subst-map {:a 1 :b "two"}) "<<" ">>")
  )

(defn copy-template-dir
  "Given a template directory, a target directory, a tuple
  of source directory, target subdirectory (with possible
  substitutions), and optional map of files to rename, and
  a substitution hash map, perform the copy with all those
  substitutions.

  If files is provided, any files found in the source directory
  that are not explicitly mentioned are copied directly."
  [template-dir target-dir {:keys [src target files delims opts]} data]
  (let [target    (when target (str "/" (substitute target data)))
        opts      (set opts)
        raw       (:raw opts)
        file      (:file opts)
        file-data (let [[open close] delims]
                    (if (and open close)
                      (adjust-subst-map data open close)
                      data))]
    (if (seq files)
      ;; TODO: can't have files and single file
      (let [intermediate (-> (Files/createTempDirectory
                              "deps-new" (into-array FileAttribute []))
                             (.toFile)
                             (doto .deleteOnExit)
                             (.getCanonicalPath))
            inter-target (str intermediate target)]
        ;; first we just copy the raw files with no substitutions:
        (b/copy-dir {:target-dir inter-target
                     :src-dirs   [(str template-dir "/" src)]})
        ;; now we process the named files, substituting paths:
        (run! (fn [[from to]]
                (b/delete {:path (str inter-target "/" from)})
                (b/copy-file {:src    (str template-dir "/" src "/" from)
                              :target (str inter-target "/"
                                           (substitute to data))}))
              files)
        ;; finally we copy the prepared folder (with substitutions):
        (b/copy-dir (cond-> {:target-dir target-dir
                             :src-dirs   [intermediate]}
                      (not raw)
                      (assoc :replace file-data))))
      (if file
        (b/copy-file {:src (str template-dir "/" src)
                      :target (str target-dir "/" target)})
        (b/copy-dir (cond-> {:target-dir (str target-dir target)
                             :src-dirs   [(str template-dir "/" src)]}
                      (not raw)
                      (assoc :replace file-data)))))))

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
     :name        (str project-name)
     :scm/domain  (let [[_ scm-tld scm-host]
                        (re-matches (re-pattern (str known-scms ".*$")) qualifier)]
                    (if scm-host
                      (str scm-host "."
                           (if (= "io" scm-tld)
                             "com"
                             scm-tld))
                      "github.com"))
     :scm/user    (str/replace top #"(com|org)\." "")
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
            :raw-name   (str project-name)
            :template   (str template)
            :target-dir target-dir
            :user       username
            :version    "0.1.0-SNAPSHOT"}
           ;; remove options we cleaned up:
           (dissoc opts :template :target-dir :name))))

(defn apply-template-fns
  "Given the template directory, the options hash map, and
  the template hash map (EDN), apply any data manipulation
  and template manipulation functions specified in the template.

  `:template-dir` is added to the options hash map first, and
  `:template` is already available."
  [template-dir basic-opts basic-edn]
  (let [basic-opts (assoc basic-opts :template-dir template-dir)
        opts (if-let [data-fn (:data-fn basic-edn)]
               ;; :data-fn result is additive:
               (merge basic-opts ((requiring-resolve data-fn) basic-opts))
               basic-opts)
        edn  (if-let [template-fn (:template-fn basic-edn)]
               ;; :template-fn result is replacement:
               ((requiring-resolve template-fn) basic-edn opts)
               basic-edn)]
    ;; this allows any defaults from the template to
    ;; be part of the data used for substitution:
    [(merge {:description (str "FIXME: my new"
                               (when-let [template-name (:template opts)]
                                 (str " " template-name))
                               " project.")} edn opts) edn]))

(comment
  (find-root 'org.corfield.new/app)
  (find-root 'org.corfield.new/lib)
  (substitute "{{foo/file}}.clj"
              (->subst-map {:top/ns "org.corfield"
                            :foo    "org.corfield-ns"
                            :bar    "org/corfield_file"}))
  )
