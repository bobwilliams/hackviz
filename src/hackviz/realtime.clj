(ns hackviz.realtime
  (:require [org.httpkit.server :refer [send! on-close]]
            [cheshire.core :as json]
            [hackviz.global :as g]
            [hackviz.github :as gh]))

(defn convert-event [commit {:keys [owner team name]}]
  (assoc (gh/convert-event commit owner team name) :msg (:message commit)))

(defn get-repo [{{name :name} :repository}]
  (first (filter #(= name (:name @%)) @g/repositories))) ;; Todo: Verify Owners

(defn broadcast [events]
  (doseq [listener @g/event-listeners]
    (send! listener (json/generate-string events) false)))

(defn handle-github-callback [callback]
  (when-let [repo (get-repo callback)]
    (let [commits (:commits callback)
          events (map #(convert-event % @repo) commits)]
      (broadcast events))))

(defn on-close-handler [con]
  (on-close con (fn [status]
                  (prn "Removing event listener!")
                  (swap! g/event-listeners #(remove (= % con))))))

(defn register-event-listener [con]
  (prn "Registering event listener!")
  (swap! g/event-listeners #(conj % con))
  (on-close-handler con))