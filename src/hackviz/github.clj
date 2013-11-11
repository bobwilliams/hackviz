(ns hackviz.github
  (:require [tentacles.repos :as repos]
            [clj-http.client :as client]
            [tentacles.core :as tcore]
            [cheshire.core :as json]
            [clj-time.format :refer :all]
            [clj-time.coerce :refer :all]
            [hackviz.global :as g]))

(def iso-formatter (formatter "yyyy-MM-dd'T'HH:mm:ss'Z'"))

(defn callback-url []
  (str @g/server-base "/github-pubsub"))

(defn auth []
  {:oauth-token @g/github-token})

(defrecord CommitEvent [time owner author team repo additions deletions])

(defn ts-to-iso [ts]
  (unparse iso-formatter (from-long ts)))

(defn iso-to-ts [iso]
  (to-long (parse iso-formatter iso)))

(defn has-keys? [c]
  (seq (keys c)))

(defn get-rate-limit-remaining []
  (-> (tcore/rate-limit (auth)) :rate :remaining)) ;; TODO: Error Handling

(defn update-api-limit-remaining []
  (reset! g/api-calls-left (get-rate-limit-remaining)))

(defn should-make-call? []
  (> @g/api-calls-left 500))

(defn decrement-calls-left []
  (swap! g/api-calls-left dec))

(defn retrieve-raw-commits [owner repo ts]
  (decrement-calls-left)
  (repos/commits owner repo (merge (auth) {:since (ts-to-iso ts) :all-pages true})))

(defn commit-sha [{sha :sha id :id}]
  (or sha id))

(defn get-commit-details [owner repo commit]
  (decrement-calls-left)
  (repos/specific-commit owner repo (commit-sha commit) (auth)))

(defn get-date [commit-details]
  (or 
    (-> commit-details :commit :author :date)
    (-> commit-details :commit :committer :date)))

(defn get-name [commit-details]
  (or
    (-> commit-details :author :login)
    (-> commit-details :committer :login)
    (-> commit-details :commit :author :name)
    (-> commit-details :commit :committer :name)))

(defn convert-event [commit owner team repo]
  (let [details (get-commit-details owner repo commit)
        author (get-name details)
        ts (iso-to-ts (get-date details))
        ups (-> details :stats :additions)
        downs (-> details :stats :deletions)]
    (CommitEvent. ts owner author team repo ups downs)))

(defn commit-events-since [owner team repo ts]
  (try
    (->> (retrieve-raw-commits owner repo ts)
         (filter has-keys?)
         (reverse)
         (take-while (fn [e] (should-make-call?)))
         (map #(convert-event % owner team repo))
         (doall))
    (catch RuntimeException e (do
                                (.printStackTrace e)
                                (println "Failed retrieve commits (" owner " - " team " - " repo " - " ts "), Exception: " e)))))

(defn github-pubsub [owner name mode]
  (repos/pubsubhubub owner name mode "push" (callback-url) (auth)))

(defn subscribe-github-pubsub [{:keys [owner name]}]
  (github-pubsub owner name "subscribe")) ;; TODO: HMAC Secret

(defn unsubscribe-github-pubsub [{:keys [owner name]}]
  (github-pubsub owner name "unsubscribe"))