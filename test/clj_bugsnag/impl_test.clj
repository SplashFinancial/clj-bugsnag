(ns clj-bugsnag.impl-test
  (:require [bond.james :as bond]
            [clojure.test :as t]
            [clj-bugsnag.impl :as impl]))

(t/deftest load-bugsnag-api-key!-test
  (t/testing "Test bugsnag api key provider chain"
    (bond/with-stub [[impl/getenv (fn [_] "my-env-key")]
                     [impl/getProperty (fn [_] "my-properties-key")]]
      (t/is (= "my-api-key" (impl/load-bugsnag-api-key! {:api-key "my-api-key"}))))
    (bond/with-stub [[impl/getenv (fn [_] "my-env-key")]
                     [impl/getProperty (fn [_] "my-properties-key")]]
      (t/is (= "my-env-key" (impl/load-bugsnag-api-key! {}))))
    (bond/with-stub [[impl/getenv (fn [_] nil)]
                     [impl/getProperty (fn [_] "my-properties-key")]]
      (t/is (= "my-properties-key" (impl/load-bugsnag-api-key! {}))))
    (bond/with-stub [[impl/getenv (fn [_] nil)]
                     [impl/getProperty (fn [_] nil)]]
      (t/is (thrown-with-msg? IllegalArgumentException #"could not locate your Bugsnag API key" (impl/load-bugsnag-api-key! {}))))))
