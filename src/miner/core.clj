(ns miner.core
  (:require [clojure.string :as s]
            [miner.bits :as bits]
            [org.httpkit.client :as http]
            [cheshire.core :as json])

  (:import [java.math RoundingMode]
           [javax.xml.bind DatatypeConverter]
           [java.security MessageDigest]))

(defonce difficulty-url "https://blockexplorer.com/api/status?q=getDifficulty")
(defonce highest-target "00000000FFFF0000000000000000000000000000000000000000000000000000")

(defn sha-256 [bs]
  (.digest (java.security.MessageDigest/getInstance "SHA-256") bs))

(defn found? [n bs]
  (let [bhash (-> bs sha-256 sha-256)]
    (<= n (bits/total-zero-bits bhash))))

(defn hexify [bs]
  (DatatypeConverter/printHexBinary bs))

(defn found-match [block difficulty nonce]
  (let [bs (.getBytes (str block nonce))]
    (when (found? difficulty bs)
      {:block-hash (hexify (sha-256 (sha-256 bs)))
       :nonce nonce})))

(defn mine [{:keys [block difficulty]}]
  (some (partial found-match block difficulty)
        (range)))

(defn verify [{:keys [block nonce]}]
  (let [bhash (-> (.getBytes (str block nonce))
                  sha-256
                  sha-256)]
    {:block-hash (hexify bhash)
     :bits (bits/bytes->bits bhash)}))

(defn bigdec->hexstr [bd]
  (let [hex (.toString (.toBigInteger bd) 16)]
    (-> (format "%64s" hex)
        (s/replace #" " "0"))))

(defn show-current-difficulty []
  (if-let [diff (-> @(http/get difficulty-url)
                    :body
                    (json/parse-string true)
                    :difficulty)]
    (let [bdiff (bigdec diff)
          max-target (bigdec (BigInteger. highest-target 16))
          target (-> (.divide max-target bdiff RoundingMode/HALF_EVEN)
                   bigdec->hexstr)]
      {:difficulty diff
       :target target
       :leading-zero-bits (bits/hexstr-leading-zero-bits target)})
    {:error (str "could not fetch difficulty from " difficulty-url " try again")}))

;; playground

;; "miner address" would be in the first transacaction in this block
;; hence the merkle root is unique
;; "mine address" here is for demo purposes to show presence of "x" in "H(s,x,c) < 2^(n-k)"

(def block-header (str {:version 42
                        :hash-prev-block "00000000000000001e8d6829a8a21adc5d38d0a473b144b6765798e61f98bd1d"
                        :hash-merkle-root "51d37bdd871c9e1f4d5541be67a6ab625e32028744d7d4609d0c37747b40cd2"
                        :time (/ (System/currentTimeMillis) 1000)
                        :miner-adddress "1c1tAaz5x1HUXrCNLbtMDqc46o5GNn4xqX"}))
