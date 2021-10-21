# clj-bugsnag

[![Clojars Project](https://img.shields.io/clojars/v/com.splashfinancial/clj-bugsnag.svg)](https://clojars.org/com.splashfinancial/clj-bugsnag)
[![cljdoc badge](https://cljdoc.org/badge/com.splashfinancial/clj-bugsnag)](https://cljdoc.org/d/com.splashfinancial/clj-bugsnag/CURRENT)
[![Clojure Tests](https://github.com/SplashFinancial/clj-bugsnag/actions/workflows/clojure_tests.yml/badge.svg)](https://github.com/SplashFinancial/clj-bugsnag/actions/workflows/clojure_tests.yml)

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

[![Clojars Project](https://img.shields.io/clojars/v/com.splashfinancial/clj-bugsnag.svg)](https://clojars.org/com.splashfinancial/clj-bugsnag)

The next time you build your application, [Leiningen](https://leiningen.org/) or [tools.deps](https://clojure.org/guides/deps_and_cli) should pull it automatically.
Alternatively, you may clone or fork the repository to work with it directly.

## Example Usage

### Ring Middleware

```clojure
(require '[clj-bugsnag.core :as bugsnag]
         '[clj-bugsnag.ring :as bugsnag.ring])

;; All keys besides :api-key are optional

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
```

### Manual Reporting

```clojure
(require '[clj-bugsnag.core :as bugsnag])
;; Manual reporting using the notify function:

(try
  (some-function-that-could-crash some-input)
  (catch Exception exception

    ;; `notify` with options map in which all keys are optional:
    ;; A full list of supported options
    (bugsnag/notify
      exception
      {:api-key "Project API key"
       :project-ns "My service"
       :context "some-http-handler"
       :environment "dev"
       :version "v1.2.3"
       :severity "error"
       :return-bugsnag-response? true
       ;; Attach custom metadata to create tabs in Bugsnag:
       :meta {:input some-input}
       ;; Pass a user object to Bugsnag for better stats
       :user {:id ... :email ...}})))

    ;; If no api-key is provided, clj-bugsnag will fall back to BUGSNAG_KEY environment variable and bugsnagKey system property
    (bugsnag/notify exception)
```

By default, `notify` will return nil and fire the side-effect of logging to Bugsnag.
If you'd like access to the `clj-http` response from Bugsnag, you may set the `:return-bugsnag-response?` key in the option map to any truthy value.

Definitions of all option map keys are below:

- `:api-key` - The BugSnag API key for your project.
  If this key is missing, the library will attempt to load the Environment variable `BUGSNAG_KEY` and the JVM Property `bugsnagKey` in this order.
  If all three values are nil, an exception will be thrown
- `:project-ns` - The BugSnag project name you'd like to report the error to.
  Typically the artifact name.
  Defaults to \000
- `:context` - The BugSnag 'context' in which an error occurred.
  Defaults to nil.
  See [Bugsnag's documentation](https://docs.bugsnag.com/platforms/java/other/customizing-error-reports/) for more details
- `:group` - The BugSnag 'group' an error occurred within.
  Defaults to the exception message for instances of `clojure.lang.ExceptionInfo` or the Class Name of the Exception
- `:severity` - The severity of the error.
  Must be one of `info`, `warning`, and `error`.
  Defaults to `error`
- `:user`  - A string or map of data representing the active end user when the error occurred.
  Defaults to nil
- `:version` - The application version running when the error was reported.
  Defaults to the git SHA when possible.
  Otherwise nil.
- `:environment` - The deployment context in which the error occurred.
  Defaults to `Production`
- `:meta` - A map of arbitrary metadata to associate to the error
- `:return-bugsnag-response?` - A boolean toggle for this function's return value.
  When truthy, return the clj-http response from calling BugSnag's API
  When falsy, return nil- consistent with other logging interfaces and `println`
  Defaults to falsy.

## License

Distributed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), the same as Clojure and the predecessor of this fork.
