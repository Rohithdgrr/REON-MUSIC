package com.reon.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration

enum class CacheType(val ttl: Duration) {
    SEARCH(Duration.ofHours(1)),
    SUGGEST(Duration.ofMinutes(30)),
    HOME(Duration.ofMinutes(30)),
    ALBUM(Duration.ofHours(24)),
    ARTIST(Duration.ofHours(24)),
    PLAYLIST(Duration.ofHours(6)),
    RADIO(Duration.ofHours(1)),
    STREAM(Duration.ofMinutes(5)),
    LYRICS(Duration.ofDays(7)),
    PLAYER(Duration.ofMinutes(5)),
}

/**
 * Cache-aside store. Keys are `"$prefix:{type}:{key}"` — bump `CACHE_PREFIX`
 * env to invalidate everything when InnerTube changes shape.
 */
class ReonCache(val prefix: String, maxSize: Long = 10_000) {
    private val buckets: Map<CacheType, Cache<String, String>> =
        CacheType.entries.associateWith { type ->
            Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(type.ttl)
                .build<String, String>()
        }

    fun key(type: CacheType, id: String): String = "$prefix:${type.name.lowercase()}:$id"

    fun get(type: CacheType, id: String): String? = buckets[type]?.getIfPresent(key(type, id))

    fun put(type: CacheType, id: String, value: String) {
        buckets[type]?.put(key(type, id), value)
    }
}
