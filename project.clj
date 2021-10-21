(defproject com.splashfinancial/clj-bugsnag "1.0.0"
  :description "Fully fledged Bugsnag client. Supports ex-data and ring middleware."
  :url "https://github.com/SplashFinancial/clj-bugsnag"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.3.0"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [clj-stacktrace "0.2.8"]
                 [clj-http "2.0.0"]
                 [cheshire "5.5.0"]]
  :profiles {:dev {:dependencies [[circleci/bond "0.6.0"]
                                  [clj-http-fake "1.0.3"]]}})
