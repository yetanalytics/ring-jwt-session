# ring-jwt-session

Provides a stateless ring session cookie store using JSON Web Tokens. As with encrypted cookies, the 'key' is the session itself. The client can read the session (unless you make it `:http-only`), but if it is modified it will fail verification on the server.

Please note that this lib is highly experimental and is NOT for production use just yet.


## Installation
Leiningen:

``` clojure
[com.yetanalytics/ring-jwt-session "0.1.1"]
```

## Usage

``` clojure
(ns your.handler
  (:require
    ...
    [ring.middleware.defaults :refer [wrap-defaults
                                      site-defaults]]
    [ring.middleware.session.jwt-cookie
     :refer [jwt-cookie-store
             wrap-token-errors
             wrap-jwt-origin]]))

(def app
  (-> app-routes
      wrap-jwt-origin ;; optional origin verification for CORS
      (wrap-defaults
       (-> site-defaults
           (assoc-in [:session :store]
                     ;; chose a better key, preferably at least 128-bit
                     (jwt-cookie-store "seekrit"))
           (assoc-in
            [:session :cookie-attrs :http-only] false))) ;; let the client see
      ;; catch jwt validation errors
      wrap-token-errors))
```

### Cljs

A helper is provided to get/decode the session in the browser.

``` clojure
(require 'ring-jwt-session.core :refer [get-session])

(get-session) ;; => {...}
;; or with a custom session key:
(get-session "my-session-key")

```

## License

Copyright © 2016 Yet Analytics

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
