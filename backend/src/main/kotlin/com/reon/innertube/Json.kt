package com.reon.innertube

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Null-safe tree-walking helpers. InnerTube changes shape often; never throw on drift. */
fun JsonObject.obj(name: String): JsonObject? = this[name] as? JsonObject

fun JsonObject.arr(name: String): JsonArray? = this[name] as? JsonArray

fun JsonElement?.text(): String? =
    try {
        (this as? kotlinx.serialization.json.JsonPrimitive)?.content
    } catch (_: Exception) {
        null
    }

/** `{"text": {"runs": [{"text": ...}]}}` → list of run objects. */
fun JsonObject.runs(): List<JsonObject> =
    obj("text")?.arr("runs")?.mapNotNull { it as? JsonObject } ?: emptyList()

/** Title-like node: either `{"text":{"runs":[]}}` (flex columns) or bare `{"runs":[]}` (titles, bylines). */
fun JsonObject.runsAny(): List<JsonObject> {
    (this["runs"] as? JsonArray)?.let { arr ->
        return arr.mapNotNull { it as? JsonObject }
    }
    return runs()
}

fun List<JsonObject>.joinedText(): String =
    joinToString("") { it["text"].text() ?: "" }

/** Thumbnail list `{"thumbnails": [{"url","width","height"}]}` → largest URL. */
fun JsonObject.thumbUrl(): String? {
    val thumbs = obj("thumbnail")?.obj("musicThumbnailRenderer")?.obj("thumbnail")?.arr("thumbnails")
        ?: obj("musicThumbnailRenderer")?.obj("thumbnail")?.arr("thumbnails")
        ?: arr("thumbnails")
        ?: return this["url"].text()
    return thumbs.mapNotNull { it as? JsonObject }
        .maxByOrNull { (it["width"].text()?.toIntOrNull() ?: 0) }
        ?.get("url").text()
}

fun JsonObject.watchVideoId(): String? =
    obj("navigationEndpoint")?.obj("watchEndpoint")?.get("videoId").text()

fun JsonObject.browseId(): String? =
    obj("navigationEndpoint")?.obj("browseEndpoint")?.get("browseId").text()

fun JsonObject.playlistId(): String? =
    obj("navigationEndpoint")?.obj("watchEndpoint")?.get("playlistId").text()
        ?: obj("navigationEndpoint")?.obj("browseEndpoint")?.get("browseId").text()
            ?.takeIf { it.startsWith("VL") }?.removePrefix("VL")

fun String.toDurationMs(): Long {
    val parts = split(":").mapNotNull { it.toLongOrNull() }
    if (parts.isEmpty()) return 0L
    var ms = 0L
    for (p in parts) ms = ms * 60 + p
    return ms * 1000
}

fun Long.toDurationLabel(): String {
    val s = (this / 1000).coerceAtLeast(0)
    return "%02d:%02d".format(s / 60, s % 60)
}

val DURATION_RE = Regex("""^\d+:\d{2}(?::\d{2})?$""")
