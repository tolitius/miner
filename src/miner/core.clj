(ns miner.core
  (:require [miner.bits :as bits])
  (:import [javax.xml.bind DatatypeConverter]
           [java.security MessageDigest]))

(defn sha-256 [bs]
  (.digest (java.security.MessageDigest/getInstance "SHA-256") bs))

(defn found? [n bs]
  (let [bhash (-> bs sha-256 sha-256)]
    (<= n (bits/total-zero-bits bhash))))

(defn hexify [bs]
  (DatatypeConverter/printHexBinary bs))

(defn found-match [block difficulty counter]
  (let [bs (.getBytes (str block counter))]
    (when (found? difficulty bs)
      {:block-hash (hexify (sha-256 (sha-256 bs)))
       :counter counter})))

(defn mine [{:keys [block difficulty]}]
  (some (partial found-match block difficulty)
        (range)))

(defn verify [{:keys [block counter]}]
  (let [bhash (-> (.getBytes (str block counter))
                  sha-256
                  sha-256)]
    {:block-hash (hexify bhash)
     :bits (bits/bytes->bits bhash)}))

;; playground

;; "miner address" would be in the first transacaction in this block
;; hence the merkle root is unique
;; "mine address" here is for demo purposes to show presence of "x" in "H(s,x,c) < 2^(n-k)"

(def block-header (str {:version 42
                        :hash-prev-block "00000000000000001e8d6829a8a21adc5d38d0a473b144b6765798e61f98bd1d"
                        :hash-merkle-root "51d37bdd871c9e1f4d5541be67a6ab625e32028744d7d4609d0c37747b40cd2"
                        :time (/ (System/currentTimeMillis) 1000)
                        :miner-adddress "1c1tAaz5x1HUXrCNLbtMDqc46o5GNn4xqX"}))
