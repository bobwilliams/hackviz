(ns hackviz.global
  (:require [overtone.at-at :as at]))

(def github-token (atom ""))
(def turbinedb-url (atom "http://localhost:8080"))
(def turbinedb-database (atom "hackviz"))
(def turbinedb-collection (atom "commits"))
(def update-delay (atom (* 1000 60)))
(def server-base (atom "http://localhost:8080"))
(def server-port (atom 9000))
(def scheduler-pool (at/mk-pool))
(def repositories (atom []))
(def event-listeners (atom []))

(defn update-atom [atom value]
  (if value (reset! atom value)))

(defn or-default [conf key default]
  (or (key conf) default))

(defn initialize-atoms [conf]
  (update-atom server-base (:server-base conf))
  (update-atom server-port (or (:server-port conf) 9000))
  (update-atom github-token (:github-token conf))
  (update-atom turbinedb-url (or (:turbinedb-url conf) "http://localhost:8080"))
  (update-atom turbinedb-database (or (:turbinedb-database conf) "hackviz"))
  (update-atom turbinedb-collection (or (:turbinedb-collection conf) "commits"))
  (update-atom update-delay (or (:update-delay conf) 60000)))