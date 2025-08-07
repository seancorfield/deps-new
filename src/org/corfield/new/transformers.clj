;; copyright (c) 2025 sean corfield, all rights reserved

(ns org.corfield.new.transformers
  "Built-in transformers to support conditional template generation,
   based on CLI options:

   :data-fn transformers:

   * choose-test-runner -- if `:test-runner :lazytest` is specified, add the
     coordinates etc for LazyTest, else add Cognitect's `test-runner`

   :template-fn transformers:

   * maybe-add-bb -- if `:build :bb` is specified, use the `build-bb`
     templates folder (instead of the default `build` folder)")

(def ^:private clojure-test-runner
  {:test-runner/coordinate "io.github.cognitect-labs/test-runner {:git/tag \"v0.5.1\" :git/sha \"dfb30dd\"}"
   :test-runner/main       "cognitect.test-runner"
   :test-runner/exec-fn    "cognitect.test-runner.api/test"
   :test-runner/namespace  "clojure.test"
   :test-runner/deftest    "deftest"
   :test-runner/is         "is"
   :test-runner/testing    "testing"})

(def ^:private lazytest-runner
  {:test-runner/coordinate "io.github.noahtheduke/lazytest {:mvn/version \"1.8.0\"}"
   :test-runner/main       "lazytest.main"
   :test-runner/exec-fn    "lazytest.main/run-impl"
   :test-runner/namespace  "lazytest.core"
   :test-runner/deftest    "defdescribe"
   :test-runner/is         "expect"
   :test-runner/testing    "it"})

(defn choose-test-runner
  "Return the relevant coordinates and namespace etc for the user's choice
   of `:test-runner`."
  [data]
  (if (= :lazytest (:test-runner data))
    lazytest-runner
    clojure-test-runner))

(defn maybe-add-bb
  "If the user has asked for a Babashka template, update the template.edn
  to include `bb.edn` and use the simpler `build.clj` file."
  [edn data]
  (if (= :bb (:build data))
    (-> edn
        (assoc-in  [:transform 0 0] "build-bb")
        (update-in [:transform 0 2] assoc "bb.tmpl" "bb.edn"))
    edn))
