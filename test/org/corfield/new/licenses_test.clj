;; copyright (c) 2026 Francois Rey, all rights reserved

(ns org.corfield.new.licenses-test
  (:require
   [clojure.string :as str]
   [lazytest.core :refer [defdescribe it should throws?]]
   [org.corfield.new.licenses :as licenses]))

(defdescribe provide-licenses
  (it "should provide a default value for licenses"
      (should (-> nil
                  licenses/id->license
                  :licenses))
      (should (-> {}
                  licenses/id->license
                  :licenses))
      (should (-> {:no-licenses :value}
                  licenses/id->license
                  :licenses)))
  (it "should use :jar as default license source"
      (should (= :jar
                 (-> {:no-licenses :value}
                     licenses/id->license
                     :licenses))))
  (it "should throw with an invalid license source"
      (should (throws? Exception
                (fn [] (-> {:licenses :invalid-value}
                           licenses/id->license
                           :licenses))))))

(defdescribe accept-symbol-or-string
  (it "should accept symbol or string as license id"
      (should (= "MIT"
                 (-> {:license/id "MIT"}
                     licenses/id->license
                     :license/id)
                 (-> {:license/id 'MIT}
                     licenses/id->license
                     :license/id)))))

(defdescribe provide-default-license
  (it "should provide a default license"
      (should (-> nil
                  licenses/id->license
                  :license/id))
      (should (-> {}
                  licenses/id->license
                  :license/id))
      (should (-> {:no-license :id}
                  licenses/id->license
                  :license/id)))
  (it "should use EPL-1.0 as default license"
      (should (= "EPL-1.0"
                 (-> {:no-license :id}
                     licenses/id->license
                     :license/id))))
  (it "should throw if license was not found"
      (should (throws? Exception
                       (fn [] (-> {:license/id "invalid-value"}
                                  licenses/id->license
                                  :license/id))))))

(defdescribe return-full-map
  (it "should return a map with all entries"
      (should (-> {:license/id "SchemeReport"} ;; SchemeReport has no URL
                  licenses/id->license
                  (#(and (= 5 (count %))
                         (every? #{:licenses
                                   :license/id
                                   :license/name
                                   :license/url
                                   :license/text}
                                 (keys %))))))))
;; SchemeReport has no URL
(defdescribe indicate-missing-attributes
  (it "should indicate missing license attributes"
      (should (some #(and (string? %)
                          (str/starts-with? % "*")
                          (str/ends-with? % "*"))
                    (-> {:license/id "SchemeReport"}
                        licenses/id->license
                        vals)))))
