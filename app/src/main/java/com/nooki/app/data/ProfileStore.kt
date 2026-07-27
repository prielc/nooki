package com.nooki.app.data

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.security.MessageDigest
import java.security.SecureRandom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.profileDataStore by preferencesDataStore(name = "nooki_profile")

data class ApprovedChannel(
    val id: String,
    val title: String,
    val thumbnailUrl: String
)

/**
 * Persists the parent PIN (FR-001/FR-002) and the approved-channel whitelist (FR-004/FR-005/FR-012).
 * Per PP-005 (No Backend), this is the app's only storage — everything lives on-device (PRD §11).
 */
class ProfileStore(context: Context) {

    private val dataStore = context.applicationContext.profileDataStore

    private object Keys {
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val CHANNELS = stringPreferencesKey("approved_channels")
    }

    val isPinSet: Flow<Boolean> = dataStore.data.map { it[Keys.PIN_HASH] != null }

    val approvedChannels: Flow<List<ApprovedChannel>> = dataStore.data.map { prefs ->
        decodeChannels(prefs[Keys.CHANNELS])
    }

    suspend fun createPin(pin: String) {
        require(pin.length == 4 && pin.all(Char::isDigit)) { "PIN must be exactly 4 digits" }
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = hashPin(pin, salt)
        dataStore.edit { prefs ->
            prefs[Keys.PIN_SALT] = Base64.encodeToString(salt, Base64.NO_WRAP)
            prefs[Keys.PIN_HASH] = Base64.encodeToString(hash, Base64.NO_WRAP)
        }
    }

    suspend fun validatePin(pin: String): Boolean {
        val prefs = dataStore.data.first()
        val saltEncoded = prefs[Keys.PIN_SALT] ?: return false
        val hashEncoded = prefs[Keys.PIN_HASH] ?: return false
        val salt = Base64.decode(saltEncoded, Base64.NO_WRAP)
        val expected = Base64.decode(hashEncoded, Base64.NO_WRAP)
        val actual = hashPin(pin, salt)
        return MessageDigest.isEqual(expected, actual)
    }

    suspend fun addChannel(channel: ApprovedChannel) {
        dataStore.edit { prefs ->
            val current = decodeChannels(prefs[Keys.CHANNELS]).toMutableList()
            if (current.none { it.id == channel.id }) {
                current += channel
            }
            prefs[Keys.CHANNELS] = encodeChannels(current)
        }
    }

    suspend fun removeChannel(channelId: String) {
        dataStore.edit { prefs ->
            val current = decodeChannels(prefs[Keys.CHANNELS]).filterNot { it.id == channelId }
            prefs[Keys.CHANNELS] = encodeChannels(current)
        }
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        return digest.digest(pin.toByteArray(Charsets.UTF_8))
    }

    private fun encodeChannels(channels: List<ApprovedChannel>): String {
        val array = JSONArray()
        channels.forEach { channel ->
            array.put(
                JSONObject()
                    .put("id", channel.id)
                    .put("title", channel.title)
                    .put("thumbnailUrl", channel.thumbnailUrl)
            )
        }
        return array.toString()
    }

    private fun decodeChannels(json: String?): List<ApprovedChannel> {
        if (json.isNullOrEmpty()) return emptyList()
        val array = JSONArray(json)
        return (0 until array.length()).map { index ->
            val obj = array.getJSONObject(index)
            ApprovedChannel(
                id = obj.getString("id"),
                title = obj.getString("title"),
                thumbnailUrl = obj.getString("thumbnailUrl")
            )
        }
    }
}
