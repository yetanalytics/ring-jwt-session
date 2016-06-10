(ns ring.middleware.session.jwt-cookie-test
  (:require [clojure.test :refer :all]
            [ring.middleware.session.jwt-cookie :refer :all]
            [ring.middleware.session.store :refer [SessionStore
                                                   read-session
                                                   write-session
                                                   delete-session]]
            [buddy.sign.jwt :as jwt])
  (:import [ring.middleware.session.jwt_cookie JWTCookieStore]))

(deftest cookie-store-test
  (let [store (jwt-cookie-store "seekrit")
        valid-key (jwt/sign {:foo "bar"} "seekrit")]

    (testing "jwt-cookie-store returns a new cookie store"
      (is (instance? JWTCookieStore store))
      (testing "that is a ring SessionStore"
        (is (satisfies? SessionStore store))))

    (testing "read-session"
      (testing "decodes valid session keys"
        (is (= {:foo "bar"}
               (read-session store valid-key))))

      (testing "throws an error for invalid keys"
        (testing "with a bad signing key"
          (try
            (read-session store (jwt/sign {:foo "bar"} "someotherseekrit"))
            (catch Exception ex
              (let [{:keys [type cause]} (ex-data ex)]
                (is (= type :ring.middleware.session.jwt-cookie/invalid-token))
                (is (= cause {:type :validation :cause :signature}))))))

        (testing "with a malformed key"
          (try
            (read-session store (subs valid-key 3))
            (catch Exception ex
              (let [{:keys [type cause]} (ex-data ex)]
                (is (= type :ring.middleware.session.jwt-cookie/invalid-token))
                (is (= cause {:type :validation :cause :signature}))))))
        (testing "with a passed :exp key"
          (try
            (read-session store (jwt/sign {:foo "bar"
                                           ;; expires NOW!
                                           :exp (java.util.Date.)}
                                          "seekrit"))
            (catch Exception ex
              (let [{:keys [type cause]} (ex-data ex)]
                (is (= type :ring.middleware.session.jwt-cookie/invalid-token))
                (is (= cause {:type :validation :cause :exp})))))))

      (testing "returns nil for nil keys"
        (is (nil? (read-session store nil)))))
    (testing "write-session returns a signed token"
      (is (= valid-key
             (write-session store nil {:foo "bar"}))))
    (testing "delete-session returns a signed token with an empty session"
      (is (= {}
             (jwt/unsign (delete-session store nil) "seekrit"))))))

(deftest wrap-token-errors-test
  (testing "wrap-token-errors middleware catches token errors and returns 401"
    (let [store (jwt-cookie-store "seekrit")
          handler-token-error (fn [_] (read-session
                                      store
                                      (jwt/sign {:foo "bar"
                                                 :exp (java.util.Date.)}
                                                "seekrit")))
          handler-other-error (fn [_] (throw (Exception. "Some other error")))]
      (is (= {:status 401 :body "Unauthorized"}
             ((wrap-token-errors handler-token-error) {})))

      (testing "but lets other errors through"
        (try
          ((wrap-token-errors handler-other-error) {})
          (catch Exception ex
            (is (= "Some other error"
                   (.getMessage ex)))))))))
