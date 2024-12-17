package com.mobileheros.gpscamera.ui.privacy

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.mobileheros.gpscamera.R
import com.mobileheros.gpscamera.ui.common.TitleBar
import com.mobileheros.gpscamera.utils.Constants.PRIVACY_URL

@Composable
fun PrivacyScreen(navBack: () -> Unit) {
    Scaffold(topBar = {
        TitleBar(
            title = stringResource(id = R.string.privacy_and_policy),
            navBack = navBack
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            WebViewScreen()
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen() {
    AndroidView(
        factory = { context ->
            return@AndroidView WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()

                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.setSupportZoom(false)
            }
        },
        update = {
            it.loadUrl(PRIVACY_URL)
        }
    )
}
