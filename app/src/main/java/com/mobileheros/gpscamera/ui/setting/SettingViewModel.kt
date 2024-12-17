package com.mobileheros.gpscamera.ui.setting

import android.content.Context
import androidx.lifecycle.ViewModel
import com.mobileheros.gpscamera.utils.Constants
import com.mobileheros.gpscamera.utils.getData
import com.mobileheros.gpscamera.utils.localConfig
import com.mobileheros.gpscamera.utils.putData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SettingUiState(
    val path: String = Constants.CUSTOM_DIR_DEFAULT,
    val photoPrefix: String = Constants.PHOTO_PREFIX_DEFAULT,
    val videoPrefix: String = Constants.VIDEO_PREFIX_DEFAULT,
)

@HiltViewModel
class SettingViewModel @Inject constructor(@ApplicationContext private val context: Context) :
    ViewModel() {
    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { _ ->
            context.localConfig.let {
                SettingUiState(
                    path = it.getData(Constants.CUSTOM_DIR, Constants.CUSTOM_DIR_DEFAULT),
                    photoPrefix = it.getData(
                        Constants.PHOTO_PREFIX,
                        Constants.PHOTO_PREFIX_DEFAULT
                    ),
                    videoPrefix = it.getData(Constants.VIDEO_PREFIX, Constants.VIDEO_PREFIX_DEFAULT)
                )
            }
        }
    }

    fun updateState(type: Int, value: String) {
        _uiState.update { currentState ->
            when (type) {
                0 -> {
                    context.localConfig.putData(Constants.CUSTOM_DIR, value)
                    currentState.copy(path = value)
                }

                1 -> {
                    context.localConfig.putData(Constants.PHOTO_PREFIX, value)
                    currentState.copy(photoPrefix = value)
                }

                2 -> {
                    context.localConfig.putData(Constants.VIDEO_PREFIX, value)
                    currentState.copy(videoPrefix = value)
                }

                else -> currentState
            }

        }
    }
}