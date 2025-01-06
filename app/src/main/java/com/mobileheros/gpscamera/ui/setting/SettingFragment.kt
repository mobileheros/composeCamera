package com.mobileheros.gpscamera.ui.setting

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

class SettingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SettingScreen(navBack = {
                    findNavController().navigateUp()
                }, navPrivacy = {
                    findNavController().safeNavigate(R.id.action_setting_to_web)
                })
            }
        }

    }
}