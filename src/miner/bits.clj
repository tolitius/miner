(ns miner.bits
  (:require [clojure.string :as s]))

;; if a byte value is less then one of the powers of two
;; it has a certain number of leading bits
(defn byte-leading-zero-bits [b]
  (condp > b
    0 0
    1 8
    2 7
    4 6
    8 5
    16 4
    32 3
    64 2
    128 1
    0))

(defn total-zero-bits [bhash]
  (reduce
    (fn [current b]
      (let [zbits (byte-leading-zero-bits b)]
        (if (< zbits 8)
          (reduced (+ current zbits))
          (+ current zbits))))
    0
    bhash))

(defn byte->sbits [b]
  (as-> (Integer/toBinaryString b) $
    (format "%8s" $)
    (s/replace $ #" " "0")))

(defn bytes->bits [barr]
  (reduce (fn [bits b]
            (str bits (byte->sbits b)))
          "" barr))
