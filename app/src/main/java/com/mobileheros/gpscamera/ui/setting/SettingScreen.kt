package com.mobileheros.gpscamera.ui.setting

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileheros.gpscamera.R
import com.mobileheros.gpscamera.ui.common.TitleBar
import com.mobileheros.gpscamera.utils.Utils

@Composable
fun SettingScreen(viewModel: SettingViewModel = viewModel()) {

    Scaffold(topBar = {
        TitleBar(title = stringResource(id = R.string.settings))
    }) { innerPadding ->
        val uiState = viewModel.uiState.collectAsStateWithLifecycle()
        var inputDialogShow = remember { mutableStateOf(false) }
        var rateDialogShow = remember { mutableStateOf(false) }
        var inputType = remember {
            mutableIntStateOf(0)
        }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                PathPanel(
                    uiState.value.path,
                    uiState.value.photoPrefix,
                    uiState.value.videoPrefix
                ) { type ->
                    run {
                        inputType.value = type
                        inputDialogShow.value = true
                    }
                }
                Spacer(modifier = Modifier.size(12.dp))
                RateItem(rateDialogShow)
                Spacer(modifier = Modifier.size(12.dp))
                PrivacyItem()
                Spacer(modifier = Modifier.size(12.dp))
                VersionItem()
            }

            InputDialog(
                show = inputDialogShow,
                type = inputType.value,
                if (inputType.value == 0) uiState.value.path else if (inputType.value == 1) uiState.value.photoPrefix else uiState.value.videoPrefix,
                viewModel::updateState
            )

            RateDialog(show = rateDialogShow)
        }
    }
}

@Composable
fun PathPanel(
    path: String,
    photoPrefix: String,
    videoPrefix: String,
    onClick: (type: Int) -> Unit
) {
    Card(colors = CardDefaults.cardColors().copy(containerColor = Color.White)) {
        Column {
            PathItem(
                imageId = R.mipmap.ic_storage_path,
                title = stringResource(id = R.string.storage_path),
                path = path
            ) {
                onClick(0)
            }
            HorizontalDivider(color = colorResource(id = R.color.gray_line), thickness = 0.5.dp)
            PathItem(
                imageId = R.mipmap.ic_setting_photo,
                title = stringResource(id = R.string.save_photo_prefix),
                path = photoPrefix
            ) {
                onClick(1)
            }
            HorizontalDivider(color = colorResource(id = R.color.gray_line), thickness = 0.5.dp)
            PathItem(
                imageId = R.mipmap.ic_setting_video,
                title = stringResource(id = R.string.save_video_prefix),
                path = videoPrefix
            ) {
                onClick(2)
            }
        }
    }
}


@Composable
fun PathItem(imageId: Int, title: String, path: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(start = 15.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painter = painterResource(imageId), contentDescription = null)
        Spacer(modifier = Modifier.width(10.dp))
        SettingText(title)
        Text(
            text = path,
            modifier = Modifier.weight(1f),
            style = TextStyle.Default.copy(
                textAlign = TextAlign.End,
                color = colorResource(id = R.color.yellow_main)
            )
        )
    }
}

@Composable
fun SettingText(title: String) {
    Text(
        text = title,
        style = TextStyle(color = colorResource(id = R.color.text_222222), fontSize = 16.sp)
    )
}

@Composable
fun RateItem(show: MutableState<Boolean>) {
    Card(
        colors = CardDefaults.cardColors().copy(containerColor = Color.White),
        onClick = { show.value = true }) {
        Row(
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painter = painterResource(R.mipmap.ic_rate), contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            SettingText(stringResource(id = R.string.rate_us))
            Spacer(modifier = Modifier.weight(1f))
            Image(painter = painterResource(id = R.mipmap.ic_date_arrow), contentDescription = null)
        }
    }
}

@Composable
fun PrivacyItem() {
    Card(
        colors = CardDefaults.cardColors().copy(containerColor = Color.White),
        onClick = { /*TODO*/ }) {
        Row(
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painter = painterResource(R.mipmap.ic_privacy), contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            SettingText(stringResource(id = R.string.privacy_and_policy))
            Spacer(modifier = Modifier.weight(1f))
            Image(painter = painterResource(id = R.mipmap.ic_date_arrow), contentDescription = null)
        }
    }
}

@Composable
fun VersionItem() {
    Card(
        colors = CardDefaults.cardColors().copy(containerColor = Color.White),
        onClick = { /*TODO*/ }) {
        Row(
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painter = painterResource(R.mipmap.ic_version), contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            SettingText(
                stringResource(id = R.string.version) + " " + Utils.getVersionName(
                    LocalContext.current
                )
            )
        }
    }
}

@Composable
fun InputDialog(
    show: MutableState<Boolean>,
    type: Int,
    value: String,
    update: (type: Int, value: String) -> Unit
) {
    if (show.value) {
        var content by remember {
            mutableStateOf(value)
        }
        Dialog(onDismissRequest = { show.value = false }) {
            Card {
                Column(
                    modifier = Modifier.padding(
                        top = 20.dp,
                        start = 15.dp,
                        end = 15.dp,
                        bottom = 5.dp
                    )
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = content,
                        onValueChange = { content = it })
                    Spacer(modifier = Modifier.height(15.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            modifier = Modifier.weight(1f),
                            onClick = { show.value = false }) {
                            Text(text = stringResource(id = R.string.cancel).toUpperCase(locale = Locale.current))
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        TextButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                update(type, content)
                                show.value = false
                            }) {
                            Text(text = stringResource(id = R.string.save).toUpperCase(locale = Locale.current))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RateDialog(show: MutableState<Boolean>) {
    if (show.value) {
        val star = remember {
            mutableFloatStateOf(5.0f)
        }
        val context = LocalContext.current
        Dialog(onDismissRequest = { show.value = false }) {
            Card {
                Column(
                    modifier = Modifier
                        .padding(
                            top = 20.dp,
                            start = 15.dp,
                            end = 15.dp,
                            bottom = 15.dp
                        )
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        text = stringResource(id = R.string.rate_title),
                        style = TextStyle(
                            color = colorResource(id = R.color.text_222222),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(id = R.string.rate_tip),
                        style = TextStyle(
                            color = colorResource(id = R.color.text_222222),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(15.dp))
                    StarRatingBar(rating = star.floatValue) {
                        star.floatValue = it
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalButton(
                            colors = ButtonDefaults.buttonColors().copy(
                                containerColor = colorResource(
                                    id = R.color.gray_switch
                                )
                            ),
                            modifier = Modifier.weight(1f),
                            onClick = { show.value = false }) {
                            Text(
                                text = stringResource(id = R.string.cancel).toUpperCase(locale = Locale.current),
                                style = TextStyle(color = colorResource(id = R.color.text_666666))
                            )
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        FilledTonalButton(
                            colors = ButtonDefaults.buttonColors().copy(
                                containerColor = colorResource(
                                    id = R.color.yellow_main
                                )
                            ),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (star.floatValue >= 5) {
                                    //todo 跳转商店
                                    Toast.makeText(context, "5星好评", Toast.LENGTH_LONG).show()
                                }
                                show.value = false }) {
                            Text(text = stringResource(id = R.string.rate_us).toUpperCase(locale = Locale.current))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StarRatingBar(
    maxStars: Int = 5,
    rating: Float,
    onRatingChanged: (Float) -> Unit
) {
    val density = LocalDensity.current.density
    val starSize = (18f * density).dp
    val starSpacing = (0.5f * density).dp

    Row(
        modifier = Modifier
            .selectableGroup()
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.SpaceBetween
    ) {
        for (i in 1..maxStars) {
            val isSelected = i <= rating
            val icon = if (isSelected) Icons.Filled.Star else Icons.Default.Star
            val iconTintColor = if (isSelected) Color(0xFFFFC700) else Color(0xFFC5C5C5)
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier
                    .selectable(
                        selected = isSelected,
                        onClick = {
                            onRatingChanged(i.toFloat())
                        }
                    )
                    .width(starSize)
                    .height(starSize)
            )

            if (i < maxStars) {
                Spacer(modifier = Modifier.width(starSpacing))
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
fun TestScreen() {
//    SettingScreen()
    val show = remember { mutableStateOf(true) }
    RateDialog(show = show)
}