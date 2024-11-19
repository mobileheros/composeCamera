package com.mobileheros.gpscamera.utils


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mobileheros.gpscamera.utils.Constants.LOCAL_CONFIG
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.localConfig: DataStore<Preferences> by preferencesDataStore(
    name = LOCAL_CONFIG
)

/**
 * 存数据
 */
fun <T> DataStore<Preferences>.putData(key: String, value: T) {
    runBlocking {
        when (value) {
            is Int -> putIntData(this@putData, key, value)
            is Long -> putLongData(this@putData, key, value)
            is String -> putStringData(this@putData, key, value)
            is Boolean -> putBooleanData(this@putData, key, value)
            is Float -> putFloatData(this@putData, key, value)
            is Double -> putDoubleData(this@putData, key, value)
            else -> throw IllegalArgumentException("This type cannot be saved to the Data Store")
        }
    }
}

/**
 * 取数据
 */
fun <T> DataStore<Preferences>.getData(key: String, defaultValue: T): T {
    val data = when (defaultValue) {
        is Int -> getIntData(this, key, defaultValue)
        is Long -> getLongData(this, key, defaultValue)
        is String -> getStringData(this, key, defaultValue)
        is Boolean -> getBooleanData(this, key, defaultValue)
        is Float -> getFloatData(this, key, defaultValue)
        is Double -> getDoubleData(this, key, defaultValue)
        else -> throw IllegalArgumentException("This type cannot be saved to the Data Store")
    }
    return data as T
}

/**
 * 清空数据
 */
fun clearData(dataStore: DataStore<Preferences>) = runBlocking { dataStore.edit { it.clear() } }

/**
 * 存放Int数据
 */
private suspend fun putIntData(dataStore: DataStore<Preferences>, key: String, value: Int) =
    dataStore.edit {
        it[intPreferencesKey(key)] = value
    }

/**
 * 存放Long数据
 */
private suspend fun putLongData(dataStore: DataStore<Preferences>, key: String, value: Long) =
    dataStore.edit {
        it[longPreferencesKey(key)] = value
    }

/**
 * 存放String数据
 */
private suspend fun putStringData(dataStore: DataStore<Preferences>, key: String, value: String) =
    dataStore.edit {
        it[stringPreferencesKey(key)] = value
    }

/**
 * 存放Boolean数据
 */
private suspend fun putBooleanData(dataStore: DataStore<Preferences>, key: String, value: Boolean) =
    dataStore.edit {
        it[booleanPreferencesKey(key)] = value
    }

/**
 * 存放Float数据
 */
private suspend fun putFloatData(dataStore: DataStore<Preferences>, key: String, value: Float) =
    dataStore.edit {
        it[floatPreferencesKey(key)] = value
    }

/**
 * 存放Double数据
 */
private suspend fun putDoubleData(dataStore: DataStore<Preferences>, key: String, value: Double) =
    dataStore.edit {
        it[doublePreferencesKey(key)] = value
    }

/**
 * 取出Int数据
 */
private fun getIntData(dataStore: DataStore<Preferences>, key: String, default: Int = 0): Int =
    runBlocking {
        return@runBlocking dataStore.data.map {
            it[intPreferencesKey(key)] ?: default
        }.first()
    }

/**
 * 取出Long数据
 */
private fun getLongData(dataStore: DataStore<Preferences>, key: String, default: Long = 0): Long =
    runBlocking {
        return@runBlocking dataStore.data.map {
            it[longPreferencesKey(key)] ?: default
        }.first()
    }

/**
 * 取出String数据
 */
private fun getStringData(
    dataStore: DataStore<Preferences>,
    key: String,
    default: String? = null
): String = runBlocking {
    return@runBlocking dataStore.data.map {
        it[stringPreferencesKey(key)] ?: default
    }.first()!!
}

/**
 * 取出Boolean数据
 */
private fun getBooleanData(
    dataStore: DataStore<Preferences>,
    key: String,
    default: Boolean = false
): Boolean = runBlocking {
    return@runBlocking dataStore.data.map {
        it[booleanPreferencesKey(key)] ?: default
    }.first()
}

/**
 * 取出Float数据
 */
private fun getFloatData(
    dataStore: DataStore<Preferences>,
    key: String,
    default: Float = 0.0f
): Float = runBlocking {
    return@runBlocking dataStore.data.map {
        it[floatPreferencesKey(key)] ?: default
    }.first()
}

/**
 * 取出Double数据
 */
private fun getDoubleData(
    dataStore: DataStore<Preferences>,
    key: String,
    default: Double = 0.00
): Double = runBlocking {
    return@runBlocking dataStore.data.map {
        it[doublePreferencesKey(key)] ?: default
    }.first()
}

