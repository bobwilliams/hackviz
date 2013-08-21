(ns hackviz.realtime
  (:require [org.httpkit.server :refer [send! on-close]]
            [cheshire.core :as json]
            [hackviz.global :as g]
            [hackviz.github :as gh]))

(defn convert-event [commit {:keys [owner team name]}]
  (assoc (gh/convert-event commit owner team name) :msg (:message commit) :ts (System/currentTimeMillis)))

(defn get-repo [{{name :name} :repository}]
  (first (filter #(= name (:name @%)) @g/repositories))) ;; Todo: Verify Owners

(defn send-to-listener [listener events]
  (send! listener (json/generate-string events) false))

(defn broadcast [events]
  (doseq [listener @g/event-listeners]
    (send-to-listener listener events)))

(defn take-amount [lst cnt]
  (if (> (count lst) cnt)
    (drop (- (count lst) cnt) lst)
    lst))

(defn buffer [events]
  (swap! g/event-buffer #(concat (take-amount % @g/buffer-count) events)))

(defn handle-github-callback [callback]
  (when-let [repo (get-repo callback)]
    (let [commits (:commits callback)
          events (map #(convert-event % @repo) commits)]
      (broadcast events)
      (buffer events))))

(defn remove-event-listener [con]
  (swap! g/event-listeners (fn [listeners]
                             (remove #(= % con) listeners))))

(defn on-close-handler [con]
  (on-close con (fn [status]
                  (prn "Removing event listener!")
                  (remove-event-listener con))))

(defn register-event-listener [con]
  (prn "Registering event listener!")
  (swap! g/event-listeners #(conj % con))
  (on-close-handler con)
  (send-to-listener con @g/event-buffer))