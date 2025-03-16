;; copyright (c) 2021-2025 sean corfield, all rights reserved

(ns org.corfield.new.impl-test
  (:require
   [clojure.string :as str]
   [clojure.tools.build.api :as b]
   [lazytest.core :refer [defdescribe describe it should]]
   [lazytest.extensions.expectations
             :refer [defexpect expect expecting from-each more more-> more-of
                     side-effects]]
   [org.corfield.new.impl :as sut]))

(defdescribe ns-file-conversions
  (it "should produce a valid ns from a file"
    (should (= "org.corfield.new.impl-test"
               (#'sut/->ns "org/corfield/new/impl_test"))))
  (it "should produce a valid file from a ns"
    (should (= "org/corfield/new/impl_test"
               (#'sut/->file "org.corfield.new.impl-test"))))
  (it "should round-trip in both directions"
    (should (= "org.corfield.new.impl-test"
               (#'sut/->ns (#'sut/->file "org.corfield.new.impl-test"))))
    (should (= "org/corfield/new/impl_test"
               (#'sut/->file (#'sut/->ns "org/corfield/new/impl_test"))))))

(defdescribe test-find-root
  (it "should find built-in templates"
    (should (some? (sut/find-root [] 'org.corfield.new/app)))
    (should (some? (sut/find-root [] 'org.corfield.new/lib))))
  (it "should not find a non-existent templates"
    (should (nil? (sut/find-root [] 'org.corfield.new/no-such-template))))
  (describe "without a root directory"
    (it "should not find local templates"
      (should (nil? (sut/find-root [] 'data/impl)))))
  (describe "with a root directory"
    (it "should find local templates"
      (should (some? (sut/find-root ["."] 'data/impl))))))

(defdescribe test->subst-map
  (describe "generic key and val string tests"
    (it "produces strings for keys and vals"
      (expect (more (comp string? key)
                    (comp string? val))
              (from-each [kv (sut/->subst-map {:a 42 :b "bee" :c true})]
                         kv)))
    (it "produces keys that are substitutions"
      (expect #(re-find #"^\{\{.*\}\}" (key %))
              (from-each [kv (sut/->subst-map {:a 42 :b "bee" :c true})]
                         kv))))
  (it "produces a simple substitution map"
    (expect (more-of {a "{{a}}" b "{{b}}" c "{{c}}"}
                     "42"   a
                     "bee"  b
                     "true" c)
            (sut/->subst-map {:a 42 :b "bee" :c true})))
  (it "produces ns and file substitutions"
    (expect (more-of {a "{{a/ns}}"
                      b "{{b}}" bns "{{b/ns}}" bfile "{{b/file}}"
                      c "{{c/file}}"}
                     nil?    a
                     "b_e-e" b
                     "b-e-e" bns
                     "b_e_e" bfile
                     nil?    c)
            (sut/->subst-map {:a 42 :b "b_e-e" :c true}))))

;; #'sut/substitute
;; sut/copy-template-dir

(defexpect test-copy-template-dir
  (expect (more-of [[folder-copy] [{:keys [src target]}] [{:keys [replace src-dirs target-dir]}]]
                   ;; step 1: copy dir with no replace, to temporary folder:
                   nil?       (:replace    folder-copy)
                   ["/tmp/x"] (:src-dirs   folder-copy)
                   #"/y$"     (:target-dir folder-copy)
                   ;; step 2: copy only file a to temporary structure:
                   "/tmp/x/a" src
                   #"/y/b$"   target
                   true?      (str/starts-with? target (:target-dir folder-copy))
                   ;; step 3: copy dir with no replace, from temporary folder to target:
                   nil?       replace
                   vector?    src-dirs
                   true?      (str/starts-with? (:target-dir folder-copy) (first src-dirs))
                   "/tmp/y"   target-dir)
          (side-effects [b/copy-dir b/copy-file]
                        (sut/copy-template-dir "/tmp" "/tmp/y"
                                               {:src "x" :target "y" :files {"a" "b"} :opts [:raw]}
                                               {"q" "r"})))
  (expect (more-of [[{:keys [src target]}]
                    [{:keys [replace src-dirs target-dir]}]]
                   ;; step 1: copy only file a to temporary structure:
                   "/tmp/x/a" src
                   #"/y/b$"   target
                   ;; step 2: copy dir with replace, from temporary folder to target:
                   {"q" "r"}  replace
                   vector?    src-dirs
                   true?      (str/starts-with? target (first src-dirs))
                   "/tmp/y"   target-dir)
          (side-effects [b/copy-dir b/copy-file]
                        (sut/copy-template-dir "/tmp" "/tmp/y"
                                               {:src "x" :target "y" :files {"a" "b"} :opts [:only]}
                                               {"q" "r"}))))

;; #'sut/deconstruct-project-name

(defexpect test-preprocess-options
  (expect (more-> clojure.lang.ExceptionInfo type
                  "Both :template and :name are required." ex-message
                  {:foo 42} ex-data)
          (sut/preprocess-options {:foo 42}))

  (expect (more-of {:keys [main name target-dir template top]
                    :scm/keys [domain repo user]}
                   "example"      main
                   "quux/example" name
                   "example"      target-dir
                   "foo/bar"      template
                   "quux"         top
                   "github.com"   domain
                   "example"      repo
                   "quux"         user)
          (sut/preprocess-options {:template 'foo/bar :name 'quux/example}))

  (expect (more-of {:keys [main name target-dir template top]
                    :scm/keys [domain repo user]}
                   "my-bar"       main
                   "io.github.my-name/my-bar" name
                   "my-bar"       target-dir
                   "foo/bar"      template
                   "my-name"      top
                   "github.com"   domain
                   "my-bar"       repo
                   "my-name"      user)
          (sut/preprocess-options {:template 'foo/bar :name 'io.github.my-name/my-bar}))

  (expect (more-of {:keys [main name target-dir template top]
                    :scm/keys [domain repo user]}
                   "cool-lib"     main
                   "com.acme/cool-lib" name
                   "cool-lib"     target-dir
                   "foo/bar"      template
                   "com.acme"     top
                   "github.com"   domain
                   "cool-lib"     repo
                   "acme"         user)
          (sut/preprocess-options {:template 'foo/bar :name 'com.acme/cool-lib})))

;; sut/apply-template-fns
