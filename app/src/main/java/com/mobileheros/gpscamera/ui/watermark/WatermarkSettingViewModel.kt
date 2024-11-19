package com.mobileheros.gpscamera.ui.watermark

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.mobileheros.gpscamera.utils.Constants
import com.mobileheros.gpscamera.utils.Global
import com.mobileheros.gpscamera.utils.getData
import com.mobileheros.gpscamera.utils.localConfig
import com.mobileheros.gpscamera.utils.putData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WatermarkSettingUiState(
    val compass: Boolean = Global.compass,
    val gps: Boolean = Global.gps,
    val altitude: Boolean = Global.altitude,
    val address: Boolean = Global.address,
    val map: Boolean = Global.map,
    val weather: Boolean = Global.weather,
    val dataFormat: String = Global.dateFormat,
    val logo: Boolean = Global.logo,
    val tag: Boolean = Global.tag,
    val textSwitch: Boolean = Global.textSwitch,
    val tagList: MutableList<String> = mutableListOf(),
    val tagListString: String = "",
    val text: String = Global.text,
    val scale: Float = Global.scale
)

class WatermarkSettingViewModel(private val config: DataStore<Preferences>) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                return WatermarkSettingViewModel(
                    application.localConfig
                ) as T
            }
        }
    }

    private val _uiState = MutableStateFlow(
        WatermarkSettingUiState(
            tagList = if (config.getData(Constants.TAG_LIST, "").isEmpty()) {
                mutableListOf()
            } else {
                config.getData(Constants.TAG_LIST, "").split(",").toMutableList()
            }
        )
    )
    val uiState = _uiState.asStateFlow()


    fun updateCompass(flag: Boolean) {
        _uiState.update { currentState ->
            Global.compass = flag
            config.putData(Constants.SWITCH_COMPASS, flag)
            currentState.copy(compass = flag)
        }
    }

    fun updateGps(flag: Boolean) {
        _uiState.update { currentState ->
            Global.gps = flag
            config.putData(Constants.SWITCH_GPS, flag)
            currentState.copy(gps = flag)
        }
    }

    fun updateAltitude(flag: Boolean) {
        _uiState.update { currentState ->
            Global.altitude = flag
            config.putData(Constants.SWITCH_ALTITUDE, flag)
            currentState.copy(altitude = flag)
        }
    }

    fun updateAddress(flag: Boolean) {
        _uiState.update { currentState ->
            Global.address = flag
            config.putData(Constants.SWITCH_ADDRESS, flag)
            currentState.copy(address = flag)
        }
    }

    fun updateMap(flag: Boolean) {
        _uiState.update { currentState ->
            Global.map = flag
            config.putData(Constants.SWITCH_MAP, flag)
            currentState.copy(map = flag)
        }
    }

    fun updateWeather(flag: Boolean) {
        _uiState.update { currentState ->
            Global.weather = flag
            config.putData(Constants.SWITCH_WEATHER, flag)
            currentState.copy(weather = flag)
        }
    }

    fun updateDateFormat(flag: String) {
        _uiState.update { currentState ->
            Global.dateFormat = flag
            config.putData(Constants.DATE_FORMAT, flag)
            currentState.copy(dataFormat = flag)
        }
    }

    fun updateLogo(flag: Boolean) {
        _uiState.update { currentState ->
            Global.logo = flag
            config.putData(Constants.SWITCH_LOGO, flag)
            currentState.copy(logo = flag)
        }
    }
    fun updateTextSwitch(flag: Boolean) {
        _uiState.update { currentState ->
            Global.textSwitch = flag
            config.putData(Constants.SWITCH_TEXT, flag)
            currentState.copy(textSwitch = flag)
        }
    }

    fun updateTag(flag: Boolean) {
        _uiState.update { currentState ->
            Global.tag = flag
            config.putData(Constants.SWITCH_TAG, flag)
            currentState.copy(tag = flag)
        }
    }

    fun addTag(tag: String) {
        _uiState.update { currentState ->
            currentState.tagList.add(tag)
            config.putData(Constants.TAG_LIST, currentState.tagList.joinToString(","))
            val list = mutableListOf<String>()
            list.addAll(currentState.tagList)
            currentState.copy(tagList = list)
        }
    }

    fun deleteTag(tag: String) {
        _uiState.update { currentState ->
            currentState.tagList.remove(tag)
            config.putData(Constants.TAG_LIST, currentState.tagList.joinToString(","))
            currentState.copy(tagList = mutableListOf<String>().apply { addAll(currentState.tagList) })
        }
    }
    fun updateTagList(tag: String) {
        _uiState.update { currentState ->
            config.putData(Constants.TAG_LIST, tag)
            currentState.copy(tagListString = tag)
        }
    }

    fun updateText(text: String) {
        _uiState.update { currentState ->
            config.putData(Constants.TEXT_CONTENT, text)
            Global.text = text
            currentState.copy(text = text)
        }
    }
    fun updateScale(scale: Float) {
        _uiState.update { currentState ->
            config.putData(Constants.SCALE, scale)
            Global.scale = scale
            currentState.copy(scale = scale)
        }
    }

}