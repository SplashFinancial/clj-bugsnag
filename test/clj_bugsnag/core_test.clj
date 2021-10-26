(ns clj-bugsnag.core-test
  (:require [bond.james :as bond]
            [clj-http.fake :refer [with-fake-routes-in-isolation]]
            [clj-bugsnag.core :as core]
            [clj-bugsnag.impl :as impl]
            [clojure.string :as cs]
            [clojure.test :as t]))

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
        (t/is (= "?!" (-> json :events first (get-in [:metaData "ex-data" ":wat"]))))
        (t/is (= "BOOM" (-> json :events first :groupingHash)))
        (t/is (= "some-api-key" (:apiKey json)))
        (t/is (cs/starts-with? (-> json :events first (get-in [:metaData ":reason"])) "clojure.core$println@"))))
    (t/testing "Stacktraces include the source code that threw"
      (let [crash (try
                    (make-crash)
                    (catch Exception ex
                      (core/exception->json ex nil)))]
        (t/is (= (-> crash :events first :exceptions first :stacktrace second :code)
                 {10  "  \"A function that will crash\""
                  11 "  []"
                  12 "  (let [closure (fn []"
                  13 "                  (.crash nil))]"
                  14 ""
                  15 "  ;;"
                  16 "  ;; /end to check for 3 lines before and after"}))
        (t/is (= (-> crash :events first :exceptions first :stacktrace (nth 2) :code)
                 {15 "  ;;"
                  16 "  ;; /end to check for 3 lines before and after"
                  17 ""
                  18 "    (closure)))"}))))))

(t/deftest notify-test
  (let [test-exception  (Exception. "Oh no!")
        sample-response {:status                200
                         :headers               {"Access-Control-Allow-Origin" "*"
                                                 "Bugsnag-Event-Id"            "some-event-id"
                                                 "Date"                        "Thu, 21 Oct 2021 19:31:06 GMT"
                                                 "Content-Length"              "2"
                                                 "Content-Type"                "text/plain; charset=utf-8"
                                                 "Via"                         "1.1 google"
                                                 "Alt-Svc"                     "clear"
                                                 "Connection"                  "close"}
                         :body                  "OK"
                         :trace-redirects       ["https://notify.bugsnag.com/"]
                         :orig-content-encoding nil}
        sample-client   {:api-key "my-api-key"}]
    (t/testing "Test to ensure we call the Bugsnag API, and, by default return the HTTP response from Bugsnag (Which is overridable)"
      (with-fake-routes-in-isolation
        {"https://notify.bugsnag.com/" (fn [_] sample-response)}
        (t/is (= sample-response (dissoc (core/notify test-exception sample-client) :request-time)))
        (bond/with-spy [clj-http.client/post]
          (t/is (nil? (core/notify test-exception (assoc sample-client :suppress-bugsnag-response? true))))
          (t/is (= 1 (-> clj-http.client/post bond/calls count))))))))

(t/deftest notify-v2!-test
  (let [test-exception  (Exception. "Oh no!")
        sample-response {:status                200
                         :headers               {"Access-Control-Allow-Origin" "*"
                                                 "Bugsnag-Event-Id"            "some-event-id"
                                                 "Date"                        "Thu, 21 Oct 2021 19:31:06 GMT"
                                                 "Content-Length"              "2"
                                                 "Content-Type"                "text/plain; charset=utf-8"
                                                 "Via"                         "1.1 google"
                                                 "Alt-Svc"                     "clear"
                                                 "Connection"                  "close"}
                         :body                  "OK"
                         :trace-redirects       ["https://notify.bugsnag.com/"]
                         :orig-content-encoding nil}
        sample-client   {:api-key "my-api-key"}]
    (t/testing "Test to ensure we call the Bugsnag API, and return nil (Which is not overridable)"
      (with-fake-routes-in-isolation
        {"https://notify.bugsnag.com/" (fn [_] sample-response)}
        (bond/with-spy [clj-http.client/post]
          (t/is (nil? (core/notify-v2! test-exception (assoc sample-client :suppress-bugsnag-response? false))))
          (t/is (= 1 (-> clj-http.client/post bond/calls count))))
        (bond/with-spy [clj-http.client/post]
          (t/is (nil? (core/notify-v2! test-exception sample-client)))
          (t/is (= 1 (-> clj-http.client/post bond/calls count))))))))
