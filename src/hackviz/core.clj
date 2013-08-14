(ns hackviz.core
  (:gen-class)
  (:require [org.httpkit.server :refer :all]
            [ring.util.response :refer :all]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.java.io :as io]
            [ring.middleware.reload :as reload]
            [cheshire.core :as json]
            [hackviz.global :as g]
            [hackviz.github :as gh]
            [hackviz.turbine :as turbine]
            [hackviz.updater :as updater]
            [hackviz.views :as views]
            [hackviz.realtime :as realtime]))

(def resource-conf (-> "config.json" io/resource))

(defn read-conf [file]
  (json/parse-string (slurp (or file resource-conf)) true))

(defn newest-ts [{:keys [owner name]}]
  (+ (turbine/newest-commit-ts owner name) 1000))

(defn init-repo [repo]
  (assoc repo :ts (newest-ts repo)))

(defn load-repo [repo]
  (let [repo-atom (-> repo init-repo atom)]
    (swap! g/repositories #(conj % repo-atom))
    (updater/schedule-continual-updates repo-atom)))

(defn load-repos [conf]
  (doseq [repo (:repos conf)]
    (load-repo repo)))

(defn register-event-listener [req]
  (with-channel req con
    (realtime/register-event-listener con)))

(defn query-turbine [params]
  (-> params 
      turbine/create-query-from-params 
      turbine/query-commits))

(defn register-github-pubsub [repos]
  (doseq [r repos] (gh/register-github-pubsub r)))

(defroutes routes
  (GET "/alo" [] "alo guvna")
  (GET "/commits" {params :params} (-> params query-turbine json/generate-string))
  (GET "/testpage" [] (views/page (map #(:team @%) @g/repositories)))
  (GET "/realtime" [] (views/realtime-page (map #(:team @%) @g/repositories)))
  (GET "/commit-events" [] register-event-listener)
  (POST "/github-pubsub" {body :body} (realtime/handle-github-callback (json/parse-string body true)))
  (route/resources "/"))

(defn app-routes [{mode :mode}]
  (if (= mode "prod")
    (handler/site routes)
    (-> #'routes handler/site reload/wrap-reload)))

(defn -main [& [conf-file]]
  (let [conf (read-conf conf-file)
        app (app-routes conf)]
    (g/initialize-atoms conf)
    (load-repos conf)
    (when (:realtime-enabled conf)
      (register-github-pubsub @g/repositories))
    (run-server app {:port @g/server-port :join? false})))