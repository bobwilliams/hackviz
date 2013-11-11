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
            [hackviz.turbine :as t]
            [hackviz.updater :as updater]
            [hackviz.views :as views]
            [hackviz.realtime :as realtime]))

(def resource-conf (-> "config.json" io/resource))

(defn read-conf [file]
  (json/parse-string (slurp (or file resource-conf)) true))

(defn newest-ts [{:keys [owner name]}]
  (+ (t/newest-commit-ts owner name) 1000))

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
  (let [query (t/create-query-from-params params)
        db @g/turbinedb-database
        coll @g/turbinedb-collection
        results (t/query query db coll)]
    {:query query :results results}))

(defn subscribe-github-pubsub [repos]
  (doseq [r repos] (gh/subscribe-github-pubsub @r)))

(defn buffer-return [events]
  (realtime/buffer events)
  events)

(defn sort-leaders [leaders]
  (sort #(compare (second %2) (second %1)) leaders))

(defn convert-group [grp]
  [(-> grp :group) (-> grp :data first :data first first second)])

(defn convert-leaders [results]
  (take 10 (sort-leaders (map convert-group results))))

(defn create-reducer [[k v]]
  {(str k "-" v) {v k}})

(defn leaders [grouping reducer]
  (let [group [{:segment grouping}]
        reduce [(create-reducer reducer)]
        query {:group group :reduce reduce}]
    (-> query t/query-commits convert-leaders)))

(defn user-commit-leaders []
  (leaders "author" ["repo" "count"]))

(defn user-code-leaders []
  (leaders "author" ["additions" "sum"]))

(defn team-commit-leaders []
  (leaders "team" ["repo" "count"]))

(defn team-code-leaders []
  (leaders "team" ["additions" "sum"]))

(defroutes routes
  (GET "/alo" [] "alo guvna")
  (GET "/query" {params :params} (-> params query-turbine json/generate-string))
  (GET "/" [] (views/team-page))
  (GET "/teams" [] (views/team-page))
  (GET "/users" [] (views/user-page))
  (GET "/realtime" [] (views/realtime-page))
  (GET "/event-stream" [] register-event-listener)
  (GET "/query-builder" [] (views/commit-query-builder))
  (GET "/team-leaderboards" [] (views/leaderboards (team-commit-leaders) (team-code-leaders) :team-leaderboards))
  (GET "/user-leaderboards" [] (views/leaderboards (user-commit-leaders) (user-code-leaders) :user-leaderboards))
  (POST "/github-pubsub" {{payload :payload} :params} (realtime/handle-github-callback (json/parse-string payload true)))
  (route/resources "/static/"))

(defn app-routes [{mode :mode}]
  (if (= mode "prod")
    (handler/site routes)
    (-> #'routes handler/site reload/wrap-reload)))

(defn -main [& [conf-file]]
  (let [conf (read-conf conf-file)
        app (app-routes conf)]
    (g/initialize-atoms conf)
    (gh/update-api-limit-remaining)
    (updater/schedule-update-api-calls-left)
    (load-repos conf)
    (when (:realtime-enabled conf)
      (subscribe-github-pubsub @g/repositories))
    (run-server app {:port @g/server-port :join? false})))