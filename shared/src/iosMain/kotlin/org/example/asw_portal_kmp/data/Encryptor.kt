package org.example.asw_portal_kmp.data

import io.ktor.utils.io.core.toByteArray
import kotlinx.cinterop.*
import platform.Foundation.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
actual class Encryptor {

    private val key: ByteArray by lazy {
        // In production, derive this from a secure source
        "your-256-bit-encryption-key-here-32bytes".toByteArray().copyOf(32)
    }

    actual fun encrypt(data: String): String {
        val dataBytes = data.toByteArray()
        val encryptedBytes = xorEncrypt(dataBytes, key)

        // Convert to Base64 string using NSData
        return memScoped {
            // Allocate memory for the bytes and get a pointer
            val ptr = allocArray<ByteVar>(encryptedBytes.size)
            for (i in encryptedBytes.indices) {
                ptr[i] = encryptedBytes[i]
            }

            NSData.dataWithBytes(
                ptr.reinterpret(),
                encryptedBytes.size.toULong()
            ).base64EncodedStringWithOptions(0u)
        }
    }

    @OptIn(BetaInteropApi::class)
    actual fun decrypt(encryptedData: String): String {
        // Decode from Base64 using NSData
        val encryptedBytes = NSData.create(
            base64EncodedString = encryptedData,
            options = 0u
        )?.toByteArray() ?: return ""

        val decryptedBytes = xorEncrypt(encryptedBytes, key)
        return decryptedBytes.decodeToString(0, 0 + decryptedBytes.size)
    }

    // XOR encryption - simple but effective for token storage
    private fun xorEncrypt(data: ByteArray, key: ByteArray): ByteArray {
        val result = ByteArray(data.size)
        for (i in data.indices) {
            result[i] = (data[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
        return result
    }
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    val bytes = this.bytes
    val length = this.length.toInt()
    return ByteArray(length).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length.toULong())
        }
    }
}
