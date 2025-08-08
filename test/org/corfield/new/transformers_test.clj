;; copyright (c) 2025 sean corfield, all rights reserved

(ns org.corfield.new.transformers-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [lazytest.extensions.expectations :refer [expect more-of]]
   [org.corfield.new.transformers :as sut]))

(defdescribe test-choose-test-runner
  (it "returns lazytest config when :lazytest selected"
    (expect (more-of {:test-runner/keys [coordinate main exec-fn namespace deftest is testing]}
                     #"noahtheduke/lazytest" coordinate
                     "lazytest.main"         main
                     "lazytest.main/run-impl" exec-fn
                     "lazytest.core"         namespace
                     "defdescribe"           deftest
                     "expect"                is
                     "it"                    testing)
            (sut/choose-test-runner {:test-runner :lazytest})))
  (it "returns clojure.test config by default"
    (expect (more-of {:test-runner/keys [coordinate main exec-fn namespace deftest is testing]}
                     #"cognitect-labs/test-runner" coordinate
                     "cognitect.test-runner"       main
                     "cognitect.test-runner.api/test" exec-fn
                     "clojure.test"                namespace
                     "deftest"                     deftest
                     "is"                          is
                     "testing"                     testing)
            (sut/choose-test-runner {}))))

(defdescribe test-maybe-add-bb
  (it "does nothing when :build is not :bb"
    (let [edn {:transform [["build" ""
                             {"build.tmpl" "build.clj"
                              "deps.tmpl"  "deps.edn"}
                             :only]]}
          res (sut/maybe-add-bb edn {:build :clojure})]
      (expect edn res)))
  (it "updates template to use build-bb and adds bb.edn mapping when :build is :bb"
    (let [edn {:transform [["build" ""
                             {"build.tmpl" "build.clj"
                              "deps.tmpl"  "deps.edn"}
                             :only]]}
          res (sut/maybe-add-bb edn {:build :bb})]
      (expect "build-bb" (get-in res [:transform 0 0]))
      (expect "bb.edn"   (get-in res [:transform 0 2 "bb.tmpl"]))
      ;; ensure existing mappings remain intact
      (expect "build.clj" (get-in res [:transform 0 2 "build.tmpl"]))
      (expect "deps.edn"  (get-in res [:transform 0 2 "deps.tmpl"])))))
