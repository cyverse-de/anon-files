(use '[clojure.java.shell :only (sh)])
(require '[clojure.string :as string])

(defn git-ref
  []
  (or (System/getenv "GIT_COMMIT")
      (string/trim (:out (sh "git" "rev-parse" "HEAD")))
      ""))

(defproject org.cyverse/anon-files "2.12.0-SNAPSHOT"
  :description "Serves up files and directories that are shared with the anonymous user in iRODS."
  :url "https://github.com/cyverse-de/anon-files"
  :license {:name "BSD"}
  :manifest {"Git-Ref" ~(git-ref)}
  :uberjar-name "anon-files-standalone.jar"
  :main ^:skip-aot anon-files.core
  :profiles {:uberjar {:aot :all}}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.cyverse/clj-jargon "2.8.3"
                  :exclusions [[org.slf4j/slf4j-log4j12]
                               [log4j]]]
                 [org.cyverse/service-logging "2.8.0"]
                 [org.cyverse/common-cli "2.8.1"]
                 [org.cyverse/clojure-commons "2.8.2"]
                 [org.cyverse/event-messages "0.0.1"]
                 [com.novemberain/langohr "3.5.1"]
                 [com.cemerick/url "0.1.1" :exclusions [com.cemerick/clojurescript.test]]
                 [medley "0.6.0"]
                 [compojure "1.5.0"]
                 [ring "1.5.0"]
                 [slingshot "0.10.3"]]
  :plugins [[lein-ring "0.9.3"]
            [jonase/eastwood "0.2.3"]
            [test2junit "1.2.2"]]
  :jvm-opts ["-Dlogback.configurationFile=/etc/iplant/de/logging/anon-files-logging.xml"]
  :eastwood {:exclude-namespaces [:test-paths]
             :linters [:wrong-arity :wrong-ns-form :wrong-pre-post :wrong-tag :misplaced-docstrings]})
