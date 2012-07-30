(ns
    #^{:author "Pelle Braendgaard"
       :doc "OAuth server library for Clojure."}
  oauth-server.server
  (:require [oauth-server.digest :as digest]
            [oauth-server.signature :as sig])
  (:use [clojure.contrib.string :only [upper-case as-str]]))

(defn parse-oauth-header
  "Parses the oauth http header"
  [auth]
  (if (or (= auth nil)
          (not (re-find #"^OAuth" auth)))
    nil
    (reduce (fn [v c] (conj c v)) {}  ; I know there has to be a simpler way of doing this
            (map (fn [x] {(keyword ( x 1)) (sig/url-decode (x 2))})
                 (re-seq #"(oauth_[^=, ]+)=\"([^\"]*)\"" auth)))))

(defn oauth-params [request]
  (parse-oauth-header ((or (request :headers) {}) "authorization")))

(defn request-method [request] (upper-case (as-str (request :request-method))))

(defn signature-request-port
  [{:keys [scheme server-port]}]
  (condp = [scheme (Integer. server-port)]
    [:https 443] ""
    [:http 80] ""
    (str ":" server-port)))

(defn request-uri [request & {:as overrides}]
  (let [request (into request (filter second overrides))]
    (str (or (as-str (request :scheme)) "http")
         "://"
         (request :server-name)
         (signature-request-port request)
         (request :uri))))

(defn request-parameters [request]
  (merge (dissoc (oauth-params request) :oauth_signature) (request :params)))

(defn request-base-string
  "creates a signature base string from a ring request"
  [request & overrides]
  (sig/base-string (request-method request) (apply request-uri request overrides) (request-parameters request)))

(defn wrap-oauth
  "Middleware to handle OAuth authentication of requests. If the request is oauth
   authenticated it adds the following to the request:
    :oauth-token - The oauth token used
    :oauth-consumer - The consumer key used
  Takes a function which will be used to find a token. This accepts the consumer
  and token parameters and should return the responding consumer secret and token
  secret."
  [handler token-finder & overrides]
  (fn [request]
    (let
        [op (oauth-params request)]
      (if (not (empty? op))
        (let
            [oauth-consumer (op :oauth_consumer_key)
             oauth-token (op :oauth_token)
             [consumer-secret token-secret] (token-finder oauth-consumer oauth-token)]
          (if (and consumer-secret token-secret (sig/verify
                                                 (op :oauth_signature)
                                                 {:secret consumer-secret :signature-method :hmac-sha1}
                                                 (apply request-base-string request overrides)
                                                 token-secret))
            (handler (assoc request :oauth-consumer oauth-consumer :oauth-token oauth-token))
            (handler request)))
        (handler request)))))
