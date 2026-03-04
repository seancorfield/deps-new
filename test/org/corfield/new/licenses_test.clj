;; copyright (c) 2026 Francois Rey, all rights reserved

(ns org.corfield.new.licenses-test
  (:require
   [clojure.string :as str]
   [lazytest.core :refer [defdescribe it should throws?]]
   [org.corfield.new.licenses :as licenses]))

(defdescribe provide-licenses
  (it "should use :jar as default license source"
      (should (= :jar
                 (-> nil
                     licenses/id->license
                     :licenses)
                 (-> {}
                     licenses/id->license
                     :licenses)
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
      (should (and (string? licenses/default-license-id)
                   (not (str/blank? licenses/default-license-id))))
      (should (= licenses/default-license-id
                 (-> nil
                     licenses/id->license
                     :license/id)
                 (-> {}
                     licenses/id->license
                     :license/id)
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
