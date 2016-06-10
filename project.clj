(defproject com.yetanalytics/ring-jwt-session "0.1.0-SNAPSHOT"
  :description "Provides JWT token-based sessions for ring."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj" "src/cljs"]
  :dependencies [[org.clojure/clojure "1.8.0"
                  :scope "provided"]
                 [org.clojure/clojurescript "1.8.51"
                  :scope "provided"]
                 [ring/ring-core "1.4.0"
                  :scope "provided"]
                 [buddy/buddy-sign "1.0.0"
                  :exclusions [commons-codec]]])
