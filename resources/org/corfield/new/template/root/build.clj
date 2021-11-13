(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b] ; for b/git-count-revs
            [org.corfield.build :as bb]))

(def lib '{{group/id}}/{{artifact/id}})
(def version "{{version}}")
#_ ; alternatively, use MAJOR.MINOR.COMMITS:
(def version (format "1.0.%s" (b/git-count-revs nil)))

(defn test "Run the tests." [opts]
  (bb/run-tests opts))
