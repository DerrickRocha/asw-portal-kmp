package org.example.asw_portal_kmp.data

expect class Encryptor() {
    fun encrypt(data: String): String
    fun decrypt(encryptedData: String): String
}