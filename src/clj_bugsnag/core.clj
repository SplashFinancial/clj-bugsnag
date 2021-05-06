(ns clj-bugsnag.core
  (:require [clj-bugsnag.impl :as impl]
            [clj-stacktrace.core :refer [parse-exception]]
            [clj-stacktrace.repl :refer [method-str]]
            [clojure.java.shell :refer [sh]]
            [clj-http.client :as http]
            [clojure.repl :as repl]
            [clojure.string :as string]
            [clojure.walk :as walk]))

(defn get-git-rev
  []
  (try
    (string/trim (:out (sh "git" "rev-parse" "HEAD")))
    (catch Throwable _t "git revision not available")))

(def git-rev (memoize get-git-rev))

(defn- find-source-snippet
  [around function-name]
  (try
    (let [fn-sym (symbol function-name)
          fn-var (find-var fn-sym)
          source (repl/source-fn fn-sym)
          start (-> fn-var meta :line)
          indexed-lines (map-indexed (fn [i line]
                                        [(+ i start) (string/trimr line)])
                                     (string/split-lines source))]
      (into {} (filter #(<= (- around 3) (first %) (+ around 3)) indexed-lines)))
    (catch Exception _ex
      nil)))

(defn- transform-stacktrace
  [trace-elems project-ns]
  (try
    (vec (for [{:keys [file line ns] :as elem} trace-elems
               :let [project? (string/starts-with? (or ns "_") project-ns)
                     method (method-str elem)
                     code (when (string/ends-with? (or file "") ".clj")
                            (find-source-snippet line (string/replace (or method "") "[fn]" "")))]]
            {:file file
             :lineNumber line
             :method method
             :inProject project?
             :code code}))
    (catch Exception ex
      [{:file "clj-bugsnag/core.clj"
        :lineNumber 1
        :code {1 (str ex)
               2 "thrown while building stack trace."}}])))

(defn- stringify
  [thing]
  (if (or (map? thing) (string? thing) (number? thing) (sequential? thing))
    thing
    (str thing)))

(defn exception->json
  [exception {:keys [project-ns context group severity user version environment meta] :as options}]
  (let [ex            (parse-exception exception)
        message       (:message ex)
        class-name    (.getName ^Class (:class ex))
        project-ns    (or project-ns "\000")
        stacktrace    (transform-stacktrace (:trace-elems ex) project-ns)
        base-meta     (if-let [d (ex-data exception)]
                        {"ex–data" d}
                        {})
        api-key       (impl/load-bugsnag-api-key! options)
        grouping-hash (or group
                          (if (isa? (type exception) clojure.lang.ExceptionInfo)
                            message
                            class-name))]
    {:apiKey   api-key
     :notifier {:name    "com.splashfinancial/clj-bugsnag"
                :version "1.0.0"
                :url     "https://github.com/SplashFinancial/clj-bugsnag"}
     :events   [{:payloadVersion "2"
                 :exceptions     [{:errorClass class-name
                                   :message    message
                                   :stacktrace stacktrace}]
                 :context        context
                 :groupingHash   grouping-hash
                 :severity       (or severity "error")
                 :user           user
                 :app            {:version      (or version (git-rev))
                                  :releaseStage (or environment "production")}
                 :device         {:hostname (.. java.net.InetAddress getLocalHost getHostName)}
                 :metaData       (walk/postwalk stringify (merge base-meta meta))}]}))

(defn notify
  "Main interface for manually reporting exceptions.
   When not :api-key is provided in options,
   tries to load BUGSNAG_KEY var from enviroment."
  ([exception]
    (notify exception nil))

  ([exception options]
    (let [params (exception->json exception options)
          url "https://notify.bugsnag.com/"]
      (http/post url {:form-params params
                      :content-type :json}))))
