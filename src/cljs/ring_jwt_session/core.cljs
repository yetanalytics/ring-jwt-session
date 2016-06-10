(ns ring-jwt-session.core
  (:require [goog.net.cookies]
            [clojure.string :as str]))

(defn- parse-json [s]
  (.parse js/JSON s))

(defn- get-cookie [k]
  (.get goog.net.cookies k))

(defn get-session
  "Get the ring session payload."
  [& [?key]]
  (some-> (or ?key
              "ring-session")
          get-cookie
          (str/split #"\.")
          (get 1)
          js/atob
          parse-json
          (js->clj :keywordize-keys true)))
