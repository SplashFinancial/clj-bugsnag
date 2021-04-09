(ns clj-bugsnag.impl)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Wrappers - here to support testing with-redefs since
;;;;            we can't redef static methods

(defn- getenv
  ([] (System/getenv))
  ([k] (System/getenv k)))

(defn- getProperty [k]
  (System/getProperty k))

(defn load-bugsnag-api-key!
  [opts]
  (if-let [api-key  (or (:api-key opts)
                        (getenv "BUGSNAG_KEY")
                        (getProperty "bugsnagKey"))]
    api-key
    (throw (IllegalArgumentException. "clj-bugsnag.impl/load-bugsnag-api-key! could not locate your Bugsnag API key"))))
