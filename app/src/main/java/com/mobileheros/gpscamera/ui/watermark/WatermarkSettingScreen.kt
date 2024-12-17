package com.mobileheros.gpscamera.ui.watermark

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.mobileheros.gpscamera.R
import com.mobileheros.gpscamera.utils.Constants
import com.mobileheros.gpscamera.utils.Global
import com.orhanobut.logger.Logger


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatermarkSettingScreen(
    navBack: () -> Unit,
    navSetting: () -> Unit,
    navSubscribe: () -> Unit,
    viewModel: WatermarkSettingViewModel = hiltViewModel()
) {
    Scaffold(topBar = {
        TopAppBar(
            modifier = Modifier.background(Color.White),
            title = { Text(text = stringResource(id = R.string.watermark_settings)) },
            navigationIcon = {
                Image(
                    painter = painterResource(id = R.mipmap.ic_back),
                    contentDescription = null,
                    modifier = Modifier
                        .clickable { navBack() }
                        .padding(horizontal = 10.dp)
                )
            },
            actions = {
                Image(
                    painter = painterResource(id = R.mipmap.setting),
                    contentDescription = null,
                    modifier = Modifier
                        .clickable { navSetting() }
                        .padding(end = 12.dp)
                )
            }
        )

    }) { innerPadding ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        Logger.e(uiState.tagListString + "---------------111")
        val showDateFormatDialog = remember {
            mutableStateOf(false)
        }
        val showAddTagDialog = remember {
            mutableStateOf(false)
        }
        val showDeleteTagDialog = remember {
            mutableStateOf(false)
        }
        val tag = remember {
            mutableStateOf("")
        }
        Column(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                TopPanel(viewModel)
                Spacer(modifier = Modifier.height(10.dp))
                DateItem(format = uiState.dataFormat, showDateFormatDialog)
                Spacer(modifier = Modifier.height(10.dp))
                LogoItem(
                    checked = uiState.logo,
                    viewModel::updateLogo,
                    uiState.scale,
                    viewModel::updateScale,
                    navSubscribe
                )
                Spacer(modifier = Modifier.height(10.dp))
                TextItem(
                    checked = uiState.textSwitch,
                    uiState.text,
                    viewModel::updateTextSwitch,
                    viewModel::updateText
                )
                Spacer(modifier = Modifier.height(10.dp))
                TagItem(
                    checked = uiState.tag,
                    showAddTagDialog,
                    showDeleteTagDialog,
                    tag,
                    uiState.tagListString,
                    viewModel::updateTag
                )
            }
        }

        DataFormatDialog(
            show = showDateFormatDialog,
            curFormat = uiState.dataFormat,
            onSelected = viewModel::updateDateFormat
        )
        InputDialog(show = showAddTagDialog, uiState.tagListString, viewModel::updateTagList)
        DeleteDialog(
            show = showDeleteTagDialog,
            value = tag.value,
            uiState.tagListString,
            viewModel::updateTagList
        )

    }

}


val bgModifier = Modifier
    .clip(RoundedCornerShape(10.dp))
    .background(Color.White)

@Composable
fun TopPanel(viewModel: WatermarkSettingViewModel) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    Box(
        modifier = bgModifier
    ) {
        Column {
            SwitchItem(
                stringResource(id = R.string.compass),
                uiState.value.compass,
                viewModel::updateCompass
            )
            HorizontalDivider(color = colorResource(id = R.color.gray_line), thickness = 0.5.dp)
            SwitchItem(stringResource(id = R.string.gps), uiState.value.gps, viewModel::updateGps)
            HorizontalDivider(color = colorResource(id = R.color.gray_line), thickness = 0.5.dp)
            SwitchItem(
                stringResource(id = R.string.altitude),
                uiState.value.altitude,
                viewModel::updateAltitude
            )
            HorizontalDivider(color = colorResource(id = R.color.gray_line), thickness = 0.5.dp)
            SwitchItem(
                stringResource(id = R.string.address),
                uiState.value.address,
                viewModel::updateAddress
            )
            HorizontalDivider(color = colorResource(id = R.color.gray_line), thickness = 0.5.dp)
//            SwitchItem(stringResource(id = R.string.map), uiState.value.map, viewModel::updateMap)
//            HorizontalDivider(color = colorResource(id = R.color.gray_line), thickness = 0.5.dp)
            SwitchItem(
                stringResource(id = R.string.weather),
                uiState.value.weather,
                viewModel::updateWeather
            )
        }
    }
}

@Composable
fun SwitchItem(title: String, checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(15.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(fontSize = 16.sp, color = colorResource(id = R.color.text_222222))
        )
        YellowSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun YellowSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?) {
    Switch(
        checked = checked, onCheckedChange = {
            if (onCheckedChange != null) {
                onCheckedChange(it)
            }
        }, colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = colorResource(id = R.color.yellow_main),
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = colorResource(id = R.color.gray_switch),
            uncheckedBorderColor = colorResource(id = R.color.gray_switch)
        )
    )
}

@Composable
fun DateItem(format: String, showDialog: MutableState<Boolean>) {
    Box(
        modifier = bgModifier.then(Modifier.clickable { showDialog.value = true })
    ) {
        Row(
            modifier = Modifier
                .height(52.dp)
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.date_amp_time),
                style = TextStyle(fontSize = 16.sp, color = colorResource(id = R.color.text_222222))
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = format.ifEmpty { stringResource(id = R.string.display_off) },
                style = TextStyle(color = colorResource(id = R.color.yellow_main))
            )
            Spacer(modifier = Modifier.width(5.dp))
            Image(painter = painterResource(id = R.mipmap.ic_date_arrow), contentDescription = null)
        }
    }
}

@Composable
fun LogoItem(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit),
    scale: Float,
    update: (Float) -> Unit,
    navSubscribe: () -> Unit
) {
    Box(
        modifier = bgModifier
    ) {
        Column {
            val style = TextStyle(
                color = colorResource(
                    id = R.color.text_222222
                )
            )
            Row(
                modifier = Modifier
                    .height(52.dp)
                    .padding(15.dp)
                    .clickable(enabled = !Global.isVip.value) {
                        navSubscribe()
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.logo),
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.text_222222)
                    )
                )
                Spacer(modifier = Modifier.size(8.dp))
                Image(painter = painterResource(id = R.mipmap.ic_pro), contentDescription = null)
                Spacer(modifier = Modifier.weight(1f))
                YellowSwitch(
                    checked = if (Global.isVip.value) checked else false,
                    onCheckedChange = if (Global.isVip.value) onCheckedChange else fun(_: Boolean) {
                        navSubscribe()
                    })
            }
            if (checked) {
                var imageUri by remember { mutableStateOf<Uri?>(Global.imageUri) }
                val launcher =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                        if (uri != null) {
                            imageUri = uri
                            Global.imageUri = uri
                        }
                    }
                Row(
                    modifier = Modifier
                        .padding(15.dp, 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(id = R.string.logo), style = style)
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        Modifier
                            .height(40.dp)
                            .width(40.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    TextButton(
                        onClick = { launcher.launch("image/*") },
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(
                                id = R.color.yellow_main
                            )
                        ),
                        contentPadding = PaddingValues(10.dp, 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.select),
                            style = TextStyle(color = Color.White)
                        )
                    }
                }

            }
            if (checked) {
                val factor = remember {
                    mutableFloatStateOf(scale)
                }
                Row(
                    modifier = Modifier
                        .padding(15.dp, 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(id = R.string.small), style = style)
                    Spacer(modifier = Modifier.width(15.dp))
                    Slider(
                        value = factor.floatValue, onValueChange = {
                            factor.floatValue = it
                            update(it)
                        }, colors = SliderDefaults.colors(
                            thumbColor = colorResource(
                                id = R.color.yellow_thumb,
                            ),
                            activeTrackColor = colorResource(id = R.color.yellow_main),
                            inactiveTrackColor = colorResource(
                                id = R.color.yellow_main
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TextItem(
    checked: Boolean,
    curText: String,
    onCheckedChange: ((Boolean) -> Unit)?,
    update: (value: String) -> Unit
) {
    var text by remember { mutableStateOf(curText) }
    Box(
        modifier = bgModifier
    ) {
        Column {
            Row(
                modifier = Modifier
                    .height(52.dp)
                    .padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.text),
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.text_222222)
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                YellowSwitch(checked = checked, onCheckedChange = onCheckedChange)
            }
            if (checked) {
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = if (it.trim().length > 50) it.trim().take(50) else it.trim()
                        update(text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.please_enter_content),
                            style = TextStyle(
                                color = colorResource(
                                    id = R.color.gray_switch
                                )
                            )
                        )
                    }

                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagItem(
    checked: Boolean,
    showAddDialog: MutableState<Boolean>,
    showDeleteDialog: MutableState<Boolean>,
    tag: MutableState<String>,
    tagList: String,
    onCheckedChange: ((Boolean) -> Unit)?
) {
    Box(
        modifier = bgModifier
    ) {
        Column {
            Row(
                modifier = Modifier
                    .height(52.dp)
                    .padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.tag),
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.text_222222)
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                YellowSwitch(checked = checked, onCheckedChange = onCheckedChange)
            }
            if (checked) {
                FlowRow(
                    modifier = Modifier.padding(15.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy((-8).dp)
                ) {
                    FilterChip(
                        onClick = { showAddDialog.value = true },
                        label = {
                            Text("Add")
                        },
                        selected = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Done icon",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colorResource(
                                id = R.color.yellow_main
                            ),
                            selectedContainerColor = colorResource(
                                id = R.color.yellow_main
                            ),
                            selectedLeadingIconColor = Color.White,
                            labelColor = Color.White,
                            selectedLabelColor = Color.White,
                        )
                    )
                    if (tagList.isNotEmpty()) {
                        tagList.split(",").forEach {
                            FilterChip(
                                onClick = {
                                    tag.value = it
                                    showDeleteDialog.value = true
                                },
                                label = {
                                    Text(it)
                                },
                                selected = true,
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = colorResource(
                                        id = R.color.yellow_main
                                    ),
                                    selectedContainerColor = colorResource(
                                        id = R.color.yellow_main
                                    ),
                                    labelColor = Color.White,
                                    selectedLabelColor = Color.White,
                                )
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun DataFormatDialog(
    show: MutableState<Boolean>,
    curFormat: String,
    onSelected: (value: String) -> Unit
) {
    if (show.value) {
        Dialog(onDismissRequest = { show.value = false }) {
            Card(colors = CardDefaults.cardColors().copy(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        text = stringResource(id = R.string.format),
                        style = TextStyle(
                            fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorResource(
                                id = R.color.text_222222
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        textAlign = TextAlign.Center
                    )
                    Column(modifier = Modifier.selectableGroup()) {
                        val displayOff = stringResource(id = R.string.display_off)
                        FormatRow(displayOff, selected = displayOff == curFormat) {
                            onSelected(displayOff)
                            show.value = false
                        }
                        Constants.FORMAT_LIST.forEach {
                            FormatRow(it, selected = it == curFormat) {
                                onSelected(it)
                                show.value = false
                            }
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun FormatRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = TextStyle(color = colorResource(id = R.color.text_222222), fontSize = 16.sp)
        )
        RadioButton(
            selected = selected, onClick = null, colors = RadioButtonDefaults.colors().copy(
                selectedColor = colorResource(
                    id = R.color.yellow_main
                )
            )
        )
    }
}

@Composable
fun InputDialog(
    show: MutableState<Boolean>,
    list: String,
    update: (value: String) -> Unit
) {
    if (show.value) {
        var content by remember {
            mutableStateOf("")
        }
        Dialog(onDismissRequest = { show.value = false }) {
            Card(colors = CardDefaults.cardColors().copy(containerColor = Color.White)) {
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
                                if (list.isEmpty()) {
                                    update(list.plus(content))
                                } else {
                                    update(list.plus(",").plus(content))
                                }
                                show.value = false
                            }) {
                            Text(text = stringResource(id = R.string.add).toUpperCase(locale = Locale.current))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteDialog(
    show: MutableState<Boolean>,
    value: String,
    tagList: String,
    update: (value: String) -> Unit
) {
    if (show.value) {

        Dialog(onDismissRequest = { show.value = false }) {
            Card(colors = CardDefaults.cardColors().copy(containerColor = Color.White)) {
                Column(
                    modifier = Modifier.padding(
                        top = 20.dp,
                        start = 15.dp,
                        end = 15.dp,
                        bottom = 5.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = stringResource(id = R.string.notice))
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(text = stringResource(id = R.string.delete_tag))
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
                                val list = tagList.split(",").toMutableList()
                                list.remove(value)
                                update(list.joinToString(","))
                                show.value = false
                            }) {
                            Text(text = stringResource(id = R.string.delete).toUpperCase(locale = Locale.current))
                        }
                    }
                }
            }
        }
    }
}
