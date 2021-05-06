# clj-bugsnag

[![Clojars Project](https://img.shields.io/clojars/v/com.splashfinancial/clj-bugsnag.svg)](https://clojars.org/com.splashfinancial/clj-bugsnag)
[![cljdoc badge](https://cljdoc.org/badge/com.splashfinancial/clj-bugsnag)](https://cljdoc.org/d/com.splashfinancial/clj-bugsnag/CURRENT)
![GitHub Runner](https://github.com/Wall-Brew-Co/clj-bugsnag/workflows/Clojure%20CI/badge.svg)

A fully fledged [Bugsnag](https://bugsnag.com) exception reporting client for Clojure.

Originally forked from [MicrosoftArchive](https://github.com/microsoftarchive/clj-bugsnag)

## Features

- Automatically exposes ex-info data as metadata
- Ring middleware included, attaches ring request map as metadata
- Include snippet of code around stack trace lines
- Mark in-project stack traces to hide frameworks
- Pass along user IDs to Bugsnag

## Releases and Dependency Information

A deployed copy of the most recent version of [clj-bugsnag can be found on clojars.](https://clojars.org/com.splashfinancial/clj-bugsnag)
To use it, add the following as a dependency in your project.clj or deps.edn file:

[![Clojars Project](https://clojars.org/com.splashfinancial/clj-bugsnag/latest-version.svg)](https://clojars.org/com.splashfinancial/clj-bugsnag)

The next time you build your application, [Leiningen](https://leiningen.org/) or [tools.deps](https://clojure.org/guides/deps_and_cli) should pull it automatically.
Alternatively, you may clone or fork the repository to work with it directly.

## Example Usage

```clojure
(require '[clj-bugsnag.core :as bugsnag]
         '[clj-bugsnag.ring :as bugsnag.ring])

;; Ring middleware, all keys besides :api-key are optional:

(bugsnag.ring/wrap-bugsnag
  handler
  {:api-key "Project API key"
   ;; Defaults to "production"
   :environment "production"
   ;; Project namespace prefix, used to hide irrelevant stack trace elements
   :project-ns "your-project-ns-prefix"
   ;; A optional version for your app, this is displayed in bugsnag.
   ;; If not provided the latest git sha will be used - this means that
   ;; the git repo is available when you run your app.
   :version "your-app-version"
   ;; A optional function to extract a user object from a ring request map
   ;; Used to count how many users are affected by a crash
   :user-from-request (constantly {:id "shall return a map"})})

;; Manual reporting using the notify function:

(try
  (some-function-that-could-crash some-input)
  (catch Exception exception

    ;; Notify with options map, all keys are optional:
    (bugsnag/notify
      exception
      {:api-key "Project API key"
       ;; Attach custom metadata to create tabs in Bugsnag:
       :meta {:input some-input}
       ;; Pass a user object to Bugsnag for better stats
       :user {:id ... :email ...}})

    ;; If no api-key is provided, clj-bugsnag
    ;; will fall back to BUGSNAG_KEY environment variable
    (bugsnag/notify exception)))
```

## License

Distributed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), the same as Clojure and the predecessor of this fork.
