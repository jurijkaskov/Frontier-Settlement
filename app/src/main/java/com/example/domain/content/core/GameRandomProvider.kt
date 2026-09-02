package com.example.domain.content.core

import kotlin.random.Random

/**
 * Deterministic Random Provider for procedural content generation.
 * Generates isolated, namespaced PRNG streams from a master game seed and contextual parameters.
 * Guarantees that calling a generator in one domain does not advance or contaminate the seed
 * sequence of unrelated systems.
 */
object GameRandomProvider {

    /**
     * Derives a deterministic 64-bit seed from a master seed, namespace string, and optional numeric tokens.
     */
    fun deriveSeed(masterSeed: Long, namespace: String, vararg tokens: Any): Long {
        var hash = masterSeed xor 0x5DEECE66DL

        // Mix in namespace chars with large prime
        for (ch in namespace) {
            hash = (hash * 31L) + ch.code
            hash = hash xor (hash ushr 16)
        }

        // Mix in contextual tokens (days, indices, coordinates, etc.)
        for (token in tokens) {
            val tokenHash = token.hashCode().toLong()
            hash = (hash * 6364136223846793005L) + tokenHash + 1442695040888963407L
            hash = hash xor (hash ushr 27)
        }

        return hash
    }

    /**
     * Creates a Kotlin [Random] instance for the specified namespace and parameters.
     */
    fun createRandom(masterSeed: Long, namespace: String, vararg tokens: Any): Random {
        val seed = deriveSeed(masterSeed, namespace, *tokens)
        return Random(seed)
    }

    /**
     * Convenience method to extract a bounded integer in [min..max] deterministically.
     */
    fun getIntInRange(masterSeed: Long, namespace: String, min: Int, max: Int, vararg tokens: Any): Int {
        if (min >= max) return min
        val random = createRandom(masterSeed, namespace, *tokens)
        return random.nextInt(min, max + 1)
    }

    /**
     * Convenience method to extract a bounded float in [min..max] deterministically.
     */
    fun getFloatInRange(masterSeed: Long, namespace: String, min: Float, max: Float, vararg tokens: Any): Float {
        if (min >= max) return min
        val random = createRandom(masterSeed, namespace, *tokens)
        return min + (random.nextFloat() * (max - min))
    }
}
