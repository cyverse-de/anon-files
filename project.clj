(use '[clojure.java.shell :only (sh)])
(require '[clojure.string :as string])

(defn git-ref
  []
  (or (System/getenv "GIT_COMMIT")
      (string/trim (:out (sh "git" "rev-parse" "HEAD")))
      ""))

(defproject org.iplantc/anon-files "4.2.5"
  :description "Serves up files and directories that are shared with the anonymous user in iRODS."
  :url "http://github.com/iPlantCollaborativeOpenSource/DiscoveryEnvironmentBackend/"
  :license {:name "BSD"}
  :manifest {"Git-Ref" ~(git-ref)}
  :uberjar-name "anon-files-standalone.jar"
  :main ^:skip-aot anon-files.core
  :profiles {:uberjar {:aot :all}}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.iplantc/clj-jargon "4.2.5"]
                 [org.iplantc/common-cli "4.2.5"]
                 [org.iplantc/common-cfg "4.2.5"]
                 [medley "0.1.5"]
                 [compojure "1.1.6"]
                 [ring "1.2.1"]]
  :iplant-rpm {:summary "Serves up files and directories that are shared with the anonymous user in iRODS."
               :provides "anon-files"
               :dependencies ["iplant-service-config >= 0.1.0-5" "java-1.7.0-openjdk"]
               :config-files ["log4j.properties"]
               :config-path "resources/main"}
  :plugins [[lein-ring "0.8.10"]
            [org.iplantc/lein-iplant-rpm "4.2.5"]])
