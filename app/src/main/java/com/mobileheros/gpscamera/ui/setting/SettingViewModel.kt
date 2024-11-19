package com.mobileheros.gpscamera.ui.setting

import androidx.lifecycle.ViewModel
import com.mobileheros.gpscamera.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingUiState(
    val path: String = Constants.CUSTOM_DIR_DEFAULT,
    val photoPrefix: String = Constants.PHOTO_PREFIX_DEFAULT,
    val videoPrefix: String = Constants.VIDEO_PREFIX_DEFAULT,
)
class SettingViewModel : ViewModel(){
    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState = _uiState.asStateFlow()

    fun updateState(type: Int, value: String) {
        _uiState.update { currentState ->
            when(type) {
                0 -> {
                    currentState.copy(path = value)
                }
                1 -> {
                    currentState.copy(photoPrefix = value)
                }
                2 -> {
                    currentState.copy(videoPrefix = value)
                }

                else -> currentState
            }

        }
    }
}