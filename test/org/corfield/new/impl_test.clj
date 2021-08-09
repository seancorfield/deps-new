;; copyright (c) 2021 sean corfield, all rights reserved

(ns org.corfield.new.impl-test
  (:require [expectations.clojure.test :refer [defexpect expect]]
            [org.corfield.new.impl :as sut]))

(defexpect test->ns
  (expect "org.corfield.new.impl-test"
          (#'sut/->ns "org/corfield/new/impl_test")))

(defexpect test->file
  (expect "org/corfield/new/impl_test"
          (#'sut/->file "org.corfield.new.impl-test")))

;; sut/find-root
;; sut/->subst-map
;; #'sut/substitute
;; sut/copy-template-dir
;; #'sut/deconstruct-project-name
;; sut/preprocess-options
;; sut/apply-template-fns
