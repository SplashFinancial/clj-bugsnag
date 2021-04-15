(ns clj-bugsnag.core-test
  (:require [bond.james :as bond]
            [clojure.string :as cs]
            [clojure.test :as t]
            [clj-bugsnag.core :as core]
            [clj-bugsnag.impl :as impl]))

(defn make-crash
  "A function that will crash"
  []
  (let [closure (fn []
                  (.crash nil))]

  ;;
  ;; /end to check for 3 lines before and after

    (closure)))

(t/deftest exception->json-test
  (bond/with-stub! [[impl/load-bugsnag-api-key! (fn [_] "some-api-key")]]
    (t/testing "The resulting json map includes ExceptionInfo's ex-data"
      (let [json (core/exception->json (ex-info "BOOM" {:wat "?!"}) {:meta {:reason println}})]
        (t/is (= "?!" (-> json :events first (get-in [:metaData "ex–data" ":wat"]))))
        (t/is (= "BOOM" (-> json :events first :groupingHash)))
        (t/is (= "some-api-key" (:apiKey json)))
        (t/is (cs/starts-with? (-> json :events first (get-in [:metaData ":reason"])) "clojure.core$println@"))))
    (t/testing "Stacktraces include the source code that threw"
      (let [crash (try
                    (make-crash)
                    (catch Exception ex
                      (core/exception->json ex nil)))]
        (t/is (= (-> crash :events first :exceptions first :stacktrace second :code)
                 {9  "  \"A function that will crash\""
                  10 "  []"
                  11 "  (let [closure (fn []"
                  12 "                  (.crash nil))]"
                  13 ""
                  14 "  ;;"
                  15 "  ;; /end to check for 3 lines before and after"}))
        (t/is (= (-> crash :events first :exceptions first :stacktrace (nth 2) :code)
                 {14 "  ;;"
                  15 "  ;; /end to check for 3 lines before and after"
                  16 ""
                  17 "    (closure)))"}))))))
