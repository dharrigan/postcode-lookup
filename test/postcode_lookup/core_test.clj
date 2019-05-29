(ns postcode-lookup.core-test
  (:require [clojure.test :as t]
            [clojure.java.io :as io]
            [postcode-lookup.core :as p]))

(defn read-json
  [filename]
  (p/string->map (slurp (io/file (io/resource filename)))))

(def abcd123 (read-json "test/abcd123.json"))
(def abcd456 (read-json "test/abcd456.json"))

(defn get-building-by-number
  [building_number delivery_points]
  (into {} (filter #(= building_number (:building_number %)) delivery_points)))

(defn get-udprn
  [building_number json]
  (:udprn (get-building-by-number building_number (:delivery_points (first (:thoroughfares json))))))

(defn filter-for-keyword
  [k coll]
  (filter #(contains? % k) coll))

(t/deftest abcd123-should-have-32-addresses
  (t/testing "parse abcd123 data"
   (t/is (= 32 (count (p/parse abcd123))))))

(t/deftest abcd123-should-have-no-name-entries
  (t/testing "abcd123 should have no name entries"
    (t/is (empty? (filter-for-keyword :name (p/clean-up abcd123))))))

(t/deftest abcd456-should-have-21-addresses
  (t/testing "parse abcd456 data"
   (t/is (= 21 (count (p/parse abcd456))))))

(t/deftest abcd456-should-have-one-name-entry
  (t/testing "abcd456 should have one name entry"
    (t/is (= 1 (count (filter-for-keyword :name (p/clean-up abcd456)))))))

(t/deftest building-102-should-have-udprn-12233111
  (t/testing "building 102 should have udprn 1223111"
    (t/is (= "12233111" (get-udprn "102" abcd456)))))
