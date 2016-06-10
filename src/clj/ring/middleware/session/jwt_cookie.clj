(ns ring.middleware.session.jwt-cookie
  (:require [ring.middleware.session.store :refer [SessionStore]]
            [buddy.sign.jwt :as jwt]))

(deftype JWTCookieStore [secret-key]
  SessionStore
  (read-session [_ data]
    (when data
      (try
        (jwt/unsign data secret-key)
        (catch Exception ex
          (throw (ex-info
                  "Invalid Token"
                  {:type ::invalid-token
                   :cause (ex-data ex)}))))))
  (write-session [_ _ data]
    (jwt/sign data secret-key))
  (delete-session [_ _]
    (jwt/sign {} secret-key)))

(defn jwt-cookie-store
  "Creates a signed cookie storage engine. Requires a key.
   Note that this session key will contain the ring session
   map and will be readable to clients."
  [secret-key]
  (JWTCookieStore. secret-key))


;; Middleware

(def forbidden-response
  {:status 403
   :body "Forbidden"})

(defn wrap-token-errors
  "If there is something wrong with the token, return a 403.
  Run this outside middleware that decodes/encodes the session."
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception ex
        (if (= (some-> ex ex-data :type) ::invalid-token)
          forbidden-response
          (throw ex))))))

(defn wrap-jwt-origin
  "When origin and user-agent info is present in the session,
   check it against the corresponding headers. Return 403 on mismatch.
  Otherwise, set the origin and user agent.
  Make sure this runs inside middleware that uses the cookie store."
  [handler]
  (fn [{:keys [session headers] :as request}]
    (try
      (let [{req-origin "origin"} headers]
        (when-let [{:keys [origin]} (:client session)]
          (assert (= req-origin origin) "Session origin does not match."))
        (handler (cond-> request
                   ;; when the session is new, add the client info
                   (and
                    req-origin
                    (= session {})) (assoc-in [:session :client]
                                            {:origin req-origin}))))
      (catch java.lang.AssertionError e
        forbidden-response))))
