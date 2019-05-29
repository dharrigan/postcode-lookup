(ns postcode-lookup.core
  (:require [cheshire.core :as json]
            [taoensso.timbre :as logger]
            [config.core :refer [env]]
            [clj-http.client :as http-client])
  (:gen-class))

(def api-endpoint (-> env :craftyclicks :base-url))
(def api-key (-> env :craftyclicks :api-key))

(defn string->map
  [message]
  (json/parse-string message true))

(defn response->body->map
  [response]
  (string->map (:body response)))

(defn lookup
  [postcode]
  (response->body->map (http-client/get api-endpoint {:query-params {"key" api-key "postcode" postcode}})))

(defn delivery_point->address
  [dp street town county postcode]
  {:name (:organisation_name dp)
   :number (:building_number dp)
   :street street
   :town town
   :county county
   :postcode postcode})

(defn parse
  [json]
  (let [{:keys [thoroughfares postcode town traditional_county]} json
        {:keys [thoroughfare_name thoroughfare_descriptor delivery_points]} (first thoroughfares)
        street (str thoroughfare_name " " thoroughfare_descriptor)]
    (mapv #(delivery_point->address % street town traditional_county postcode) delivery_points)))

(defn strip-empty-values
  [coll]
  (reduce-kv (fn [m k v] (if (empty? v) m (assoc m k v))) {} coll))

(defn clean-up
  [json]
  (mapv strip-empty-values (parse json)))

(defn -main
  [& args]
  (doseq [postcode ["ABCD123" "ABCD456"]]
    (logger/info (clean-up (lookup postcode)))))
