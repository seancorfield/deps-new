;; copyright (c) 2021 sean corfield, all rights reserved

(ns org.corfield.new.impl-test
  (:require [expectations.clojure.test
             :refer [defexpect expect expecting from-each more more-of]]
            [org.corfield.new.impl :as sut]))

(defexpect test->ns
  (expect "org.corfield.new.impl-test"
          (#'sut/->ns "org/corfield/new/impl_test")))

(defexpect test->file
  (expect "org/corfield/new/impl_test"
          (#'sut/->file "org.corfield.new.impl-test")))

(defexpect test-find-root
  (expecting "existing templates"
             (expect some? (sut/find-root 'org.corfield.new/app))
             (expect some? (sut/find-root 'org.corfield.new/app)))
  (expecting "missing templates"
             (expect nil?  (sut/find-root 'org.corfield.new/no-such-template))))

(defexpect test->subst-map
  (expect (more (comp string? key)
                (comp string? val)
                #(re-find #"^\{\{.*\}\}" (key %)))
          (from-each [kv (sut/->subst-map {:a 42 :b "bee" :c true})]
                     kv))
  (expect (more-of {a "{{a}}" b "{{b}}" c "{{c}}"}
                   "42"   a
                   "bee"  b
                   "true" c)
          (sut/->subst-map {:a 42 :b "bee" :c true})))

;; #'sut/substitute
;; sut/copy-template-dir
;; #'sut/deconstruct-project-name
;; sut/preprocess-options
;; sut/apply-template-fns
