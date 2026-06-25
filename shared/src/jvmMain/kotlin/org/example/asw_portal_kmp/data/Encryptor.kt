package org.example.asw_portal_kmp.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import java.io.File
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@OptIn(ExperimentalSerializationApi::class)
actual class Encryptor {

    private companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val GCM_NONCE_SIZE = 12
        private const val GCM_TAG_SIZE = 16
    }

    private val key: SecretKey by lazy {
        val keyFile = File(System.getProperty("user.home"), ".config/yourapp/encryption.key")
        if (keyFile.exists()) {
            // Load existing key
            val keyBytes = keyFile.readBytes()
            SecretKeySpec(keyBytes, "AES")
        } else {
            // Generate new key
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(KEY_SIZE, SecureRandom())
            val newKey = keyGenerator.generateKey()

            // Store key securely
            keyFile.parentFile.mkdirs()
            keyFile.writeBytes(newKey.encoded)
            newKey
        }
    }

    actual fun encrypt(data: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)

        // Generate random nonce
        val nonce = ByteArray(GCM_NONCE_SIZE)
        SecureRandom().nextBytes(nonce)

        val gcmSpec = GCMParameterSpec(GCM_TAG_SIZE * 8, nonce)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

        // Combine: nonce + encrypted data
        val combined = nonce + encryptedBytes
        return Base64.getEncoder().encodeToString(combined)
    }

    actual fun decrypt(encryptedData: String): String {
        val combined = Base64.getDecoder().decode(encryptedData)

        // Extract nonce and encrypted data
        val nonce = combined.copyOfRange(0, GCM_NONCE_SIZE)
        val encryptedBytes = combined.copyOfRange(GCM_NONCE_SIZE, combined.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val gcmSpec = GCMParameterSpec(GCM_TAG_SIZE * 8, nonce)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}

// Helper for storing data
@Serializable
data class EncryptedData(
    val encrypted: String,
    val salt: String
)