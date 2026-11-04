package com.fieldcam.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fieldcam.core.Config
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fieldcam")

/** 설정 전체를 JSON 문자열 하나로 DataStore 에 보관한다. DB 없음. */
class ConfigStore(private val context: Context) {
    private val key = stringPreferencesKey("config_json")
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    val config: Flow<Config> = context.dataStore.data.map { prefs ->
        prefs[key]?.let { runCatching { json.decodeFromString<Config>(it) }.getOrNull() } ?: Config()
    }

    suspend fun update(transform: (Config) -> Config) {
        context.dataStore.edit { prefs ->
            val current = prefs[key]?.let { runCatching { json.decodeFromString<Config>(it) }.getOrNull() } ?: Config()
            prefs[key] = json.encodeToString(Config.serializer(), transform(current))
        }
    }
}
