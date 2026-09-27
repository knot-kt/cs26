package com.knotkt.cs26.server

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.UUID

data class StoredMedia(
    val objectKey: String,
    val mimeType: String,
    val sizeBytes: Long,
)

interface MediaStorage {
    fun store(bytes: ByteArray, mimeType: String): StoredMedia
}

class LocalMediaStorage(
    private val root: Path = Path.of(System.getenv("CS26_MEDIA_DIR") ?: "build/media"),
) : MediaStorage {
    override fun store(bytes: ByteArray, mimeType: String): StoredMedia {
        Files.createDirectories(root)
        val extension = mimeType.substringAfter('/', "bin").replace(Regex("[^a-zA-Z0-9]"), "")
        val objectKey = "uploads/${UUID.randomUUID()}.$extension"
        val destination = root.resolve(objectKey.removePrefix("uploads/"))
        Files.write(destination, bytes, StandardOpenOption.CREATE_NEW)
        return StoredMedia(objectKey, mimeType, bytes.size.toLong())
    }
}