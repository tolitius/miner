# miner

Bitcoin relies on [Hashcash](https://en.wikipedia.org/wiki/Hashcash) to find (mine) the next block in a chain. The overall flow is fairly simple:

* create a `block header` that includes:

  - block version
  - transaction merkle tree hash (i.e. a hash of all transactions in a block)
  - hash of the previous block (i.e. a potential parent)
  - current timestamp in seconds
  - difficulty (how many leading zero bits there should be in a new block hash)
  - nonce (starts with 0 and increments until the hash is found)

* run a `sha256( sha256( block_header ) )`

  - if the hash _does not_ have a `difficulty` number of leading zero bits, increment `nonce` try again
  - if the hash _does_ have a `difficulty` number of leading zero bits, stop and celebrate

A couple of omitted details from the above flow:

* the first transaction in the block has a miner's address which makes a unique seed for hashing
* [difficulty](https://blockexplorer.com/api/status?q=getDifficulty) is not exactly an integer since bitcoin algorithm needs to adjust difficulty to still maintain 10 minute block intervals. so it is usually represented as a 256-bit number and all the valid block hashes need to be lower than or equal to it.
* whever `nonce` overflows, an `extraNonce` field of the very first transaction in the block is changed (which changes the merkle root hash), and the `nonce` is then reset back to `0`.

## "I Me Mine"

> Coming on strong all the time
All through' the day I me mine
_(the Beatles / "Let It Be" / 8 May 1970)_

We'll work with a slightly different version of a block header that explicitly includes miner's addess and does _not_ include the difficulty, since we'll use it as a function argument to have a more dynamic feel to it:

```clojure
;; "miner address" would be in the first transacaction in this block
;; hence the merkle root is unique
;; "mine address" here is for demo purposes to show presence of "x" in "H(s,x,c) < 2^(n-k)"

{:version 42
 :hash-prev-block "00000000000000001e8d6829a8a21adc5d38d0a473b144b6765798e61f98bd1d"
 :hash-merkle-root "51d37bdd871c9e1f4d5541be67a6ab625e32028744d7d4609d0c37747b40cd2"
 :time (/ (System/currentTimeMillis) 1000)
 :miner-adddress "1c1tAaz5x1HUXrCNLbtMDqc46o5GNn4xqX"}
```

`:time` will be different every time this header is created / compiled to have some lively mining properties.

Let's mine a proper hash for this block with a difficulty of 9 leading zero bits:

```bash
[miner]$ boot dev
```
```clojure
=> (mine {:block m/block-header :difficulty 9})

{:block-hash "0000C4881FB571CB7FDEEC94FB07968B3822E28F48B86CF76B5D619F5C59741E", :counter 48833}
```

we can visually verify it:

```clojure
=> (verify {:block m/block-header :counter 48833})

{:block-hash "0000C4881FB571CB7FDEEC94FB07968B3822E28F48B86CF76B5D619F5C59741E",
 :bits       "0000000000000000111111111111111111111111110001001111111111111111111111111000100000011111111111111111111111111111101101010111000111111111111111111111111111001011011111111111111111111111111111111101111011111111111111111111111111101100111111111111111111111111100101001111111111111111111111111111101100000111111111111111111111111111100101101111111111111111111111111000101100111000001000101111111111111111111111111110001011111111111111111111111110001111010010001111111111111111111111111011100001101100111111111111111111111111111101110110101101011101011000011111111111111111111111111001111101011100010110010111010000011110"}
```

Let's see how long mining vs. verification takes:

```clojure
=> (time (mine {:block m/block-header :difficulty 9}))

"Elapsed time: 203.523561 msecs"
{:block-hash "0000C4881FB571CB7FDEEC94FB07968B3822E28F48B86CF76B5D619F5C59741E", :counter 48833}

=> (time (verify {:block m/block-header :counter 48833}))

"Elapsed time: 0.265915 msecs"
{:block-hash "0000C4881FB571CB7FDEEC94FB07968B3822E28F48B86CF76B5D619F5C59741E",
 :bits       "0000000000000000111111111111111111111111110001001111111111111111111111111000100000011111111111111111111111111111101101010111000111111111111111111111111111001011011111111111111111111111111111111101111011111111111111111111111111101100111111111111111111111111100101001111111111111111111111111111101100000111111111111111111111111111100101101111111111111111111111111000101100111000001000101111111111111111111111111110001011111111111111111111111110001111010010001111111111111111111111111011100001101100111111111111111111111111111101110110101101011101011000011111111111111111111111111001111101011100010110010111010000011110"}
```

Now let's up our game by increasing the difficulty to 24 leading zero bits:

```clojure
=> (time (mine {:block m/block-header :difficulty 24}))

"Elapsed time: 28347.474171 msecs"
{:block-hash "000000D3AF97325446B46CA64B0640BFC79031B734180F4A7B61571A3D247832", :counter 8125108}

=> (time (verify {:block m/block-header :counter 8125108}))

"Elapsed time: 0.28443 msecs"
{:block-hash "000000D3AF97325446B46CA64B0640BFC79031B734180F4A7B61571A3D247832",
 :bits       "0000000000000000000000001111111111111111111111111101001111111111111111111111111110101111111111111111111111111111100101110011001001010100010001101111111111111111111111111011010001101100111111111111111111111111101001100100101100000110010000001111111111111111111111111011111111111111111111111111111111000111111111111111111111111111100100000011000111111111111111111111111110110111001101000001100000001111010010100111101101100001010101110001101000111101001001000111100000110010"}
```

## Prove and verify

to verify someone did the work: is constant time (i.e. inexpensive)

to _prove_ you did the work:    is exponentially hard depending on the current algorithm's difficulty

## License

Copyright © 2018 tolitius

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
