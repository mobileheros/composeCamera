package com.mobileheros.gpscamera.ui.watermark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.mobileheros.gpscamera.R
import com.mobileheros.gpscamera.utils.CommonUtils.safeNavigate

class WatermarkFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WatermarkSettingScreen(navBack = {
                    findNavController().navigateUp()
                }, navSubscribe = {
                    findNavController().safeNavigate(R.id.action_tag_to_subscribe, Bundle())
                }, navSetting = {
                    findNavController().safeNavigate(R.id.action_tag_to_setting,Bundle())
                })
            }
        }

    }
}