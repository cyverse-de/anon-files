(ns anon-files.core
  (:gen-class)
  (:use [compojure.core]
        [ring.util.http-response])
  (:require [compojure.route :as route]
            [anon-files.amqp :as amqp]
            [anon-files.events :as events]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [clojure.string :as string]
            [common-cli.core :as ccli]
            [anon-files.config :as cfg]
            [anon-files.serve :as serve]
            [clojure.tools.logging :as log]
            [me.raynes.fs :as fs]
            [service-logging.thread-context :as tc]))

(defn cli-options
  []
  [["-p" "--port PORT" "Listen port number"
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Ports must be 0-65536"]]

   ["-c" "--config PATH" "Path to the config file"
    :default "/etc/iplant/de/anon-files.properties"
    :validate [#(fs/exists? %) "Config file must exist."
               #(fs/readable? %) "Config file must be readable."]]

   ["-v" "--version" "Print out the version number."]

   ["-h" "--help"]])

(defroutes app-routes
  (GET "/" [:as {{expecting :expecting} :params :as req}]
       (if (and expecting (not= expecting "anon-files"))
         (internal-server-error (str "Hello from anon-files. Error: expecting " expecting "."))
         "Hello from anon-files."))
  (HEAD "/*" [:as req] (log/spy (serve/handle-head-request req)))
  (GET "/*" [:as req] (log/spy (serve/handle-request req)))
  (OPTIONS "/*" [:as req] (log/spy (serve/handle-options-request req))))

(def app
  (-> #'app-routes
      wrap-keyword-params
      wrap-params))

(defn listen-for-events
  []
  (amqp/connect
   (events/exchange-config)
   (events/queue-config)
   {"events.anon-files.ping" events/ping-handler}))

(def svc-info
  {:desc "A service that serves up files shared with the iRODS anonymous user."
   :app-name "anon-files"
   :group-id "org.cyverse"
   :art-id "anon-files"
   :service "anon-files"})

(defn -main
  [& args]
  (tc/with-logging-context svc-info
    (let [{:keys [options arguments errors summary]} (ccli/handle-args svc-info args cli-options)]
      (when-not (fs/exists? (:config options))
        (ccli/exit 1 (str "The default --config file " (:config options) " does not exist.")))
      (cfg/load-config-from-file (:config options))
      (.start (Thread. listen-for-events))
      (log/info "Started listening on" (cfg/listen-port))
      (require 'ring.adapter.jetty)
      ((eval 'ring.adapter.jetty/run-jetty) app {:port (cfg/listen-port)}))))
