(ns hackviz.views
  (:require [hiccup.core :refer :all]))

(defn team-link [team]
  [:li 
    [:a {:href (str "/" team)} team]])

(defn nav-bar []
  [:div.navbar
    [:a.navbar-brand {:href "#"} "Hackviz"]
    [:ul.nav.navbar-nav
      [:li.active
        [:a {:href "#"} "Home"]]
      [:li
        [:a {:href "#"} "Teams"]]
      [:li
        [:a {:href "#"} "Repositories"]]]])

(defn overview [teams realtime?]
  (html
    [:html
      [:head
        [:link {:href "bootstrap/css/bootstrap.min.css" :rel "stylesheet" :media "screen"}]
        [:script {:src "http://ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js"}]
        [:script {:src "bootstrap/js/bootstrap.min.js"}]
        [:script {:src "http://code.highcharts.com/highcharts.js"}]
        [:link {:href "css/hackviz.css" :rel "stylesheet" :media "screen"}]
        [:script {:src "js/hackviz.js"}]
        (when realtime? [:script {:src "js/hackviz-realtime.js"}])
        [:script {:src "js/moment.min.js"}]]
      [:body
        (nav-bar)
        [:div.btn-group
          [:button.btn.btn-default.dropdown-toggle {:type "button" :data-toggle "dropdown"} "All Teams"
            [:span.caret]]
          [:ul.dropdown-menu
            (map team-link teams)]]
        [:div#spline-commits {:style "width 100%; height:400px;"}]
        [:div#pie-commits {:style "min-width: 310px; height: 400px; margin: 0 auto;"}]
        [:div#spline-adds {:style "width 100%; height:400px;"}]
        [:div#pie-adds {:style "min-width: 310px; height: 400px; margin: 0 auto;"}]
        [:div#spline-commits-author {:style "width 100%; height:400px;"}]
        [:div#pie-commits-author {:style "min-width: 310px; height: 400px; margin: 0 auto;"}]
        [:div#spline-adds-author {:style "width 100%; height:400px;"}]
        [:div#pie-adds-author {:style "min-width: 310px; height: 400px; margin: 0 auto;"}]]]))

(defn page [teams]
  (overview teams false))

(defn realtime-page [teams]
  (overview teams true))