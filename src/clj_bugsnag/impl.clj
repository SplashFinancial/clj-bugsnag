(ns clj-bugsnag.impl)

(defn getenv
  "Wrapper - here to support testing since we can't redef static methods"
  [k]
  (System/getenv k))

(defn getProperty
  "Wrapper - here to support testing since we can't redef static methods"
  [k]
  (System/getProperty k))

(defn load-bugsnag-api-key!
  [opts]
  (if-let [api-key  (or (:api-key opts)
                        (getenv "BUGSNAG_KEY")
                        (getProperty "bugsnagKey"))]
    api-key
    (throw (IllegalArgumentException. "clj-bugsnag.impl/load-bugsnag-api-key! could not locate your Bugsnag API key"))))
