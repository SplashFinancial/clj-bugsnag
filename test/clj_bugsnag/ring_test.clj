(ns clj-bugsnag.ring-test
  (:require [bond.james :as bond]
            [clojure.test :as t]
            [clj-bugsnag.core :as core]
            [clj-bugsnag.impl :as impl]
            [clj-bugsnag.ring :as ring]))

(t/deftest wrap-bugsnag-test
  (bond/with-stub! [[impl/load-bugsnag-api-key! (fn [_] "some-api-key")]
                    [core/notify (fn [& _] nil)]] ;; don't POST to bugsnag while testing
    (bond/with-spy [core/notify]
      (let [handler (fn [_req] (throw (ex-info "BOOM" {})))
            wrapped (ring/wrap-bugsnag handler {})]
        (t/testing "Ring middleware re-throws exceptions after notifying bugsnag"
          (t/is (thrown-with-msg? clojure.lang.ExceptionInfo #"BOOM" (wrapped {})))
          (t/is (= 1 (-> core/notify bond/calls count))))))))
