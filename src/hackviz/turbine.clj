(ns hackviz.turbine
  (:require [clj-http.client :as client]
            [clojure.string :as str]
            [cheshire.core :as json]
            [hackviz.global :as g]
            [clj-time.coerce :refer :all]))

(defn base-url []
  (str @g/turbinedb-url "/db"))

(defn db-url [db]
  (str (base-url) "/" db))

(defn coll-url [db coll]
  (str (db-url db) "/" coll))

(defn retrieve-meta [url]
  (-> url client/get :body (json/parse-string true)))

(defn get-databases []
  (retrieve-meta (base-url)))

(defn get-collections [db]
  (retrieve-meta (db-url db)))

(defn get-segments [db coll]
  (retrieve-meta (coll-url db coll)))

(defn insert-event [commit-event db coll]
  (let [event {:timestamp (:time commit-event), :data commit-event}
        e-json (json/generate-string event)
        url (coll-url db coll)]
    (client/post url {:body e-json})))

(defn add-commit-events [events]
  (doseq [e events] (insert-event e @g/turbinedb-database @g/turbinedb-collection)))

(defn query [q db coll]
  (let [q-json (json/generate-string q)
        url (coll-url db coll)
        res (client/get url {:query-params {"q" q-json}})
        body (:body res)]
    (json/parse-string body true)))

(defn query-commits [q]
  (query q @g/turbinedb-database @g/turbinedb-collection))

(defn newest-commit-ts [owner repo]
  (let [matches [{:owner {:eq owner}} {:repo {:eq repo}}]
        q {:match matches :reduce [{:newest {:max "time"}}]}
        results (query-commits q)
        ts-dbl (-> results first :data first :data first :newest)]
    (long (or ts-dbl 0))))

(def str-operators #{:eq :neq})

(defn parse-double [s]
  (Double/parseDouble s))

(defn convert-dbl [match]
  (let [[seg expr] (first match)
        [op value] (first expr)
        new-value (if (str-operators op) value (parse-double value))]
    {seg {op new-value}}))

(defn validate-query [query]
  (let [updated (assoc query :match (->> query :match (map convert-dbl)))]
    updated))

(defn create-valid-query [query start end]
  (merge
    (validate-query query)
    (when start {:start start})
    (when end {:end end})))

(defn long-or-nil [str]
  (when str
    (Long/parseLong str)))

(defn create-query-from-params [{:keys [q start end]}]
  (let [query (json/parse-string q true)
        start (long-or-nil start)
        end (long-or-nil end)]
    (create-valid-query query start end)))