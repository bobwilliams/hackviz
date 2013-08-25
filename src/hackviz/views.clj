(ns hackviz.views
  (:require [hiccup.core :refer :all]))

(defn team-link [team]
  [:li 
    [:a {:href (str "/" team)} team]])

(defn nav-bar [page]
  [:div.navbar.navbar-default {:role "navigation"}
    [:a.navbar-brand {:href "#"} "Hackviz"]
    [:ul.nav.navbar-nav
      [:li (when (= page :teams) {:class "active"})
        [:a {:href "teams"} "Teams"]]
      [:li (when (= page :users) {:class "active"})
        [:a {:href "users"} "Users"]]
      [:li (when (= page :realtime) {:class "active"})
        [:a {:href "realtime"} "Realtime"]]
      [:li (when (= page :team-leaderboards) {:class "active"})
        [:a {:href "team-leaderboards"} "Team Leaderboards"]]
      [:li (when (= page :user-leaderboards) {:class "active"})
        [:a {:href "user-leaderboards"} "User Leaderboards"]]
      [:li (when (= page :query-builder) {:class "active"})
        [:a {:href "query-builder"} "Query"]]]])

(defn realtime-graphs []
  [:div 
    [:div#spline-adds {:style "width 100%; height:400px;"}]
    [:div#pie-adds {:style "min-width: 310px; height: 400px; margin: 0 auto;"}]
    [:div#spline-adds-author {:style "width 100%; height:400px;"}]
    [:div#pie-adds-author {:style "min-width: 310px; height: 400px; margin: 0 auto;"}]])

(defn team-graphs []
  [:div 
    [:div#spline-commits {:style "width 100%; height:400px;"}]
    [:div#pie-commits {:style "min-width: 310px; height: 400px; margin: 0 auto;"}]
    [:div#spline-adds {:style "width 100%; height:400px;"}]
    [:div#pie-adds {:style "min-width: 310px; height: 400px; margin: 0 auto;"}]])

(defn user-graphs []
  [:div
    [:div#spline-commits-author {:style "width 100%; height:400px;"}]
    [:div#pie-commits-author {:style "min-width: 310px; height: 400px; margin: 0 auto;"}]
    [:div#spline-adds-author {:style "width 100%; height:400px;"}]
    [:div#pie-adds-author {:style "min-width: 310px; height: 400px; margin: 0 auto;"}]])

(defn overview [realtime? author? page]
  (html
    [:html
      [:head
        [:link {:href "bootstrap/css/bootstrap.min.css" :rel "stylesheet" :media "screen"}]
        [:script {:src "http://ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js"}]
        [:script {:src "bootstrap/js/bootstrap.min.js"}]
        [:script {:src "http://code.highcharts.com/highcharts.js"}]
        [:link {:href "css/hackviz.css" :rel "stylesheet" :media "screen"}]
        (cond
          realtime? [:script {:src "js/hackviz-realtime.js"}]
          author? [:script {:src "js/hackviz-users.js"}]
          :else [:script {:src "js/hackviz-teams.js"}])
        [:script {:src "js/moment.min.js"}]]
      [:body
        (nav-bar page)
        [:div.btn-group
          [:button.btn.btn-default.dropdown-toggle {:type "button" :data-toggle "dropdown"} "All Teams"
            [:span.caret]]]
        (cond 
          realtime? (realtime-graphs)
          author? (user-graphs)
          :else (team-graphs))]]))

(defn team-page []
  (overview false false :teams))

(defn user-page []
  (overview false true :users))

(defn realtime-page []
  (overview true false :realtime))

(def match-entities ["author","team","repo","additions","deletions"])
(def match-operators ["=","!=","<","<=",">",">="])

(def group-entities ["author","team","repo"])
(def group-times ["year","month","hour","day","minute"])

(def reduce-entities ["commit","additions","deletions"])
(def reduce-operators ["count","min","max","sum","avg","stdev"])

(defn dropdown-item [item]
  [:li {:role "presentation"}
    [:a {:role "menuitem" :tabindex "-1" :href "#"} item]])

(defn dropdown 
  ([items] (dropdown (rest items) (first items)))
  ([items default]
    [:div.dropdown
      [:ul.dropdown-menu {:role "menu" :aria-labelledby "dropdownMenu1"}
        (map dropdown-item (cons default items))]]))

(defn dropdown-item-simple [item]
  [:option {:value item} item])

(defn dropdown-item-simple-default [item]
  [:option {:value item :selected "selected"} item])

(defn dropdown-simple
  ([items id] (dropdown-simple (rest items) (first items) id))
  ([items default id]
    [:select.query-select.form-control {:id id}
      (cons 
        (dropdown-item-simple-default default)
        (map dropdown-item-simple items))]))

(defn input [label]
  [:input#match-val.query-input.form-control {:type "text" :placeholder label}])

(defn wg [el]
  [:div.col-lg-2 el])

(defn query-builder []
  (html
    [:html
      [:head
        [:link {:href "bootstrap/css/bootstrap.min.css" :rel "stylesheet" :media "screen"}]
        [:script {:src "http://ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js"}]
        [:script {:src "bootstrap/js/bootstrap.min.js"}]
        [:script {:src "http://code.highcharts.com/highcharts.js"}]
        [:link {:href "css/hackviz.css" :rel "stylesheet" :media "screen"}]
        [:script {:src "js/query-builder.js"}]
        [:script {:src "js/moment.min.js"}]]
      [:body
        (nav-bar :query-builder)
        [:div.container
          [:div.row
            (wg [:label "Match: "])
            (wg (dropdown-simple match-entities "none" "match-entities"))
            (wg (dropdown-simple match-operators "none" "match-ops"))
            (wg (input "value"))]
          [:div.row
            (wg [:label "Group (Segment)"])
            (wg (dropdown-simple group-entities "none" "group-entities"))
            (wg [:label "Group (Time)"])
            (wg (dropdown-simple group-times "group-times"))]
          [:div.row
            (wg [:label "Reduce"])
            (wg (dropdown-simple reduce-entities "reduce-entities"))
            (wg (dropdown-simple reduce-operators "reduce-ops"))]
          [:div.row
            [:button#go-btn.btn.btn-primary {:type "button"} "Run Query"]]]
        [:div#dynamic-graph {:style "width 100%; height:400px;"}]]]))

(defn render-row [[name value]]
  [:tr
    [:td name]
    [:td value]])

(defn render-table [leaders msg]
  [:table.table.table-striped
    [:thead
      [:tr
        [:th "Name"]
        [:th msg]]]
    [:tbody
      (map render-row leaders)]])

(defn leaderboards [commit-leaders code-leaders page]
  (html
    [:html
      [:head
        [:link {:href "bootstrap/css/bootstrap.min.css" :rel "stylesheet" :media "screen"}]
        [:script {:src "http://ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js"}]
        [:script {:src "bootstrap/js/bootstrap.min.js"}]
        [:script {:src "http://code.highcharts.com/highcharts.js"}]
        [:link {:href "css/hackviz.css" :rel "stylesheet" :media "screen"}]
        [:script {:src "js/moment.min.js"}]]
      [:body
        (nav-bar page)
        [:h2 "Commits"]
        (render-table commit-leaders "Commits")
        [:h2 "Lines of Code"]
        (render-table code-leaders "Lines of Code")]]))