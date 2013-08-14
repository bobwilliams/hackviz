(ns hackviz.realtime
  (:require [org.httpkit.server :refer [send! on-close]]
            [cheshire.core :as json]
            [hackviz.global :as g]
            [hackviz.github :as gh]))

(defn convert-event [commit {:keys [owner team name]}]
  (assoc (gh/convert-event commit owner team name) :msg (:message commit)))

(defn get-repo [{{name :name} :repository}]
  (first (filter #(= name (:repo %)) @g/repositories)))

(defn broadcast [events]
  (doseq [listener @g/event-listeners]
    (send! (key listener) (json/generate-string events) false)))

(defn handle-github-callback [callback]
  (when-let [repo (get-repo callback)]
    (let [commits (:commits callback)
          events (map #(convert-event % repo) commits)]
      (broadcast events))))

(defn on-close-handler [con]
  (on-close con (fn [status]
                  (swap! g/event-listeners dissoc con))))

(defn register-event-listener [con]
  (swap! g/event-listeners assoc con true)
  (on-close-handler con))