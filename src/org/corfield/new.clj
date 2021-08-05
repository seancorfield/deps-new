;; copyright (c) 2021 sean corfield, all rights reserved

(ns org.corfield.new
  "The next generation of clj-new. Uses tools.build and
  tools.deps.alpha heavily to provide a simpler 'shim'
  around template processing."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.tools.build.api :as b])
  (:import (java.nio.file Files)
           (java.nio.file.attribute FileAttribute)))

(set! *warn-on-reflection* true)

(s/def ::root string?)
(s/def ::files (s/map-of string? string?))
(s/def ::dir-spec (s/tuple string? string? ::files))
(s/def ::transform (s/coll-of ::dir-spec :min-count 1))
(s/def ::template (s/keys :opt-un [::root ::transform]))

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

(defn- find-root
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

(defn- ->subst-map
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

(defn- copy-template-dir
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

(defn- preprocess-options
  "Given the raw options hash map, preprocess, parse, and
  validate certain values, and derive defaults for others."
  [{:keys [template target-dir name] :as opts}]
  (when-not (and template name)
    (throw (ex-info "Both :template and :name are required." opts)))
  (let [;; clean up input parameters:
        template   (symbol template) ; allow for string or symbol
        template   (if (namespace template)
                     template
                     ;; default ns for short template names:
                     (symbol "org.corfield.new" (name template)))
        target-dir (str target-dir)
        defaults   {:template   template
                    :target-dir target-dir
                    :name       name}]
    (merge defaults (dissoc opts :template :target-dir :name))))

(defn create
  "Exec function to create a new project from a template.
  :template -- a symbol identifying the template.
  :target-dir -- a string identifying the directory to
      create the new project in.
  :overwrite -- a boolean indicating whether to delete
      and recreate an existing directory or not."
  [opts]
  (let [{:keys [template target-dir overwrite]}
        (preprocess-options opts)
        [dir ednf] (find-root template)
        _
        (when-not dir
          (throw (ex-info (str "Unable to find template.edn for " template) {})))
        ;; this may throw for invalid EDN:
        edn        (-> ednf (slurp) (edn/read-string))
        data       (->subst-map {:top 'myname :main 'myapp})]

    (when-not (s/valid? ::template edn)
      (throw (ex-info (str ednf " is not a valid template file\n\n"
                           (s/explain-str ::template edn))
                      (s/explain-data ::template edn))))

    (when (.exists (io/file target-dir))
      (if overwrite
        (b/delete {:path target-dir})
        (throw (ex-info (str target-dir " already exists (and :overwrite was not true).") {}))))

    (println "Creating project from" template "in" target-dir)

    (copy-template-dir dir target-dir [(:root edn "root")] data)
    (run! #(copy-template-dir dir target-dir % data) (:transform edn))))

(comment
  (let [[_dir edn] (find-root 'org.corfield.new/app)]
    (s/conform ::template (edn/read-string (slurp edn))))
  (create {:template   'org.corfield.new/app
           :target-dir "new-out"
           :overwrite  true})
  (find-root 'org.corfield.new/lib)
  (substitute "{{foo/file}}.clj"
              (->subst-map {:top/ns "org.corfield"
                            :foo    "org.corfield-ns"
                            :bar    "org/corfield_file"}))
  (b/copy-dir))
