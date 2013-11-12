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
      [:li (when (= page :commit-feed) {:class "active"})
        [:a {:href "commit-feed"} "Commit Feed"]]
      [:li (when (= page :team-leaderboards) {:class "active"})
        [:a {:href "team-leaderboards"} "Team Leaderboards"]]
      [:li (when (= page :user-leaderboards) {:class "active"})
        [:a {:href "user-leaderboards"} "User Leaderboards"]]
      [:li (when (= page :query-builder) {:class "active"})
        [:a {:href "query-builder"} "Query"]]]])

(defn common-head [title & extras]
  [:head
    [:title title]
    [:link {:href "/static/bootstrap/css/bootstrap.min.css" :rel "stylesheet" :media "screen"}]
    [:script {:src "http://ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js"}]
    [:script {:src "/static/bootstrap/js/bootstrap.min.js"}]
    [:script {:src "http://code.highcharts.com/highcharts.js"}]
    [:link {:href "/static/css/hackviz.css" :rel "stylesheet" :media "screen"}]
    [:script {:src "/static/js/turbine.js"}]
    [:script {:src "/static/js/moment.min.js"}]
    extras])

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

(defn dropdown-menu-item [entity]
  [:li [:a {:href "#" :onclick (str "renderAll(\"" entity "\")")} entity]])

(defn dropdown-menu [entities]
  [:ul.dropdown-menu {:role "menu"} (map dropdown-menu-item entities)])

(defn overview [realtime? author? page teams]
  (html
    [:html
      (common-head "HackViz" 
                   (cond
                     realtime? [:script {:src "/static/js/hackviz-realtime.js"}]
                     author? [:script {:src "/static/js/hackviz-users.js"}]
                     :else [:script {:src "/static/js/hackviz-teams.js"}]))
      [:body
        (nav-bar page)
        (when teams
          [:div.btn-group
            [:button.btn.btn-default.dropdown-toggle {:type "button" :data-toggle "dropdown"} "All Teams"
              [:span.caret]]
            (dropdown-menu teams)])
        (cond 
          realtime? (realtime-graphs)
          author? (user-graphs)
          :else (team-graphs))]]))

(defn team-page [teams]
  (overview false false :teams teams))

(defn user-page [teams]
  (overview false true :users teams))

(defn realtime-page []
  (overview true false :realtime nil))

(defn commit-feed []
  (html
    [:html
     (common-head "HackViz" [:script {:src "/static/js/hackviz-feed.js"}])
     [:body
       (nav-bar :commit-feed)
       [:div#commits]]]))

(def match-entities ["author","team","repo","additions","deletions"])
(def match-operators ["=","!=","<","<=",">",">="])

(def group-entities ["author","team","repo"])
(def group-times ["year","month","hour","day","minute"])

(def reduce-entities ["commit","additions","deletions"])
(def reduce-operators ["count","min","max","sum","avg","stdev"])

(defn dropdown-item [item]
  [:option {:value item} item])

(defn dropdown-item-default [item]
  [:option {:value item :selected "selected"} item])

(defn dropdown
  ([items id] (dropdown (rest items) (first items) id))
  ([items default id]
    [:select.query-select.form-control {:id id}
      (cons 
        (dropdown-item-default default)
        (map dropdown-item items))]))

(defn input [label]
  [:input#match-val.query-input.form-control {:type "text" :placeholder label}])

(defn wg [el]
  [:div.col-lg-2 el])

(defn el-header [txt]
  [:h3 {:style "float: none; margin-left: auto; margin-right: auto;"} txt])

(def match-operators ["=" "!=" "<" "<=" ">" ">="])
(def group-times ["year" "month" "day" "hour" "minute"])
(def reduce-operators ["count" "min" "max" "sum" "avg" "stdev"])
(def commit-segments ["owner" "author" "team" "repo" "additions" "deletions"])

(defn query-builder [segments]
  (html
    [:html
      (common-head "Query Builder" [:script {:src "/static/js/query-builder.js"}])
      [:body
        (nav-bar :query-builder)
        [:div.container
          [:div.row
            (wg [:label "Match: "])
            (wg (dropdown segments "none" "match-entities"))
            (wg (dropdown match-operators "none" "match-ops"))
            (wg (input "value"))]
          [:div.row
            (wg [:label "Group (Segment)"])
            (wg (dropdown segments "none" "group-entities"))
            (wg [:label "Group (Time)"])
            (wg (dropdown group-times "group-times"))]
          [:div.row
            (wg [:label "Reduce"])
            (wg (dropdown segments "reduce-entities"))
            (wg (dropdown reduce-operators "reduce-ops"))]
          [:div.row
            [:button#go-btn.btn.btn-primary {:type "button"} "Run Query"]]]
        [:div {:style "text-align: center;"}
          (el-header "Graph")
          [:div#dynamic-graph {:style "width 100%; height:400px;"}]
          (el-header "Query")
          [:pre#query {:style "text-align: left;"}]
          (el-header "Results")
          [:pre#results {:style "text-align: left;"}]]]]))

(defn commit-query-builder []
  (query-builder commit-segments))

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
      (common-head "Leaderboard")
      [:body
        (nav-bar page)
        [:h2 "Commits"]
        (render-table commit-leaders "Commits")
        [:h2 "Lines of Code"]
        (render-table code-leaders "Lines of Code")]]))