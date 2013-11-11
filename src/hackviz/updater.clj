(ns hackviz.updater
  (:require [hackviz.github :as gh]
            [hackviz.turbine :as turbine]
            [overtone.at-at :as at]
            [hackviz.global :as g]))

(defn new-commit-events-for-repo [{:keys [name owner team ts]}]
  (gh/commit-events-since owner team name (+ ts 1000)))

(defn update-repo-atom [repo values]
  (swap! repo #(merge % values)))

(defn newest-timestamp [commit-events]
  (when (seq commit-events)
    (let [times (map :time commit-events)]
      (apply max times))))

(defn update-repo [repo]
  (when (gh/should-make-call?)
    (when-let [commit-events (new-commit-events-for-repo @repo)]
      (println "Adding " (count commit-events) " new events to " (:name @repo))
      ;(println (:time (first commit-events)) " -- " (:time (last commit-events)))
      (turbine/add-commit-events commit-events)
      (let [newest-ts (newest-timestamp commit-events)]
        (update-repo-atom repo {:ts newest-ts})))))

;(update-repo-atom repo {:ts (System/currentTimeMillis)}))))

(defn try-update-repo [repo]
  (try
    (update-repo repo)
    (catch java.lang.Throwable t (do
                                   (.printStackTrace t)
                                   (println "Error Updating Repo (" (:name @repo) ": " t)))))

(defn schedule-continual-updates [repo]
  (at/interspaced @g/update-delay #(try-update-repo repo) g/scheduler-pool :initial-delay 5000))

(defn try-update-api-limit []
  (try
    (gh/update-api-limit-remaining)
    (println "API Calls Left: " @g/api-calls-left)
    (catch java.lang.Throwable t (do
                                   (.printStackTrace t)
                                   (println "Error Updating Api Limit: " t)))))

(defn schedule-update-api-calls-left []
  (at/interspaced @g/check-api-calls-delay #(try-update-api-limit) g/rate-limit-update-pool :initial-delay 5000))