package com.mobileheros.gpscamera.ui.subscribe

import android.app.Activity
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobileheros.gpscamera.R
import com.mobileheros.gpscamera.bean.ResolutionBean
import com.mobileheros.gpscamera.utils.Constants
import com.mobileheros.gpscamera.utils.Global
import java.util.Locale

@Composable
fun SubscribeScreen(navBack: () -> Unit,viewModel: SubscribeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isTrial = remember { mutableStateOf(true) }
    val context = LocalContext.current
    val renewTip = remember { mutableStateOf("") }
    uiState.list.forEach {
        it.transform(context)
    }
    if (uiState.list.isNotEmpty()) {
        val bean = uiState.list[uiState.index]
        val result = bean.product.pricingPhases.pricingPhaseList.find { it.priceAmountMicros == 0L }
        isTrial.value = result != null
        val price = bean.product.pricingPhases.pricingPhaseList.last().formattedPrice
        val priceStr = "$price per ${bean.title.substring(0, bean.title.length - 2).lowercase()}"
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000)

        renewTip.value = if (!Global.isVip.value) {
            if (isTrial.value) {
                stringResource(R.string.renew_tip_trial, bean.title.lowercase(), date, priceStr, date)
            } else {
                stringResource(R.string.renew_tip_normal, bean.title, price)
            }
        } else ""
    }

    Scaffold { innerPadding->
        ConstraintLayout(modifier = Modifier.background(color = Color(0xFF101118)).padding(innerPadding).fillMaxSize()) {
            val (table, button) = createRefs()
            TopArea(modifier = Modifier.constrainAs(table) {
                top.linkTo(parent.top)
//                bottom.linkTo(button.top, margin = 20.dp)
            }, Global.isVip.value, uiState.list,uiState.index, viewModel::checkIndex)
            BottomArea(modifier = Modifier.constrainAs(button) {
                bottom.linkTo(parent.bottom)
            }, Global.isVip.value, tip = renewTip.value) {
                if (Global.isVip.value) {
                    navBack()
                } else {
                    viewModel.joinNow(context as Activity)
                }
            }
        }
    }


}

@Composable
fun TopArea(
    modifier: Modifier,
    isVip: Boolean,
    list: List<ProductItemBean>,
    index: Int,
    onClick: (Int) -> Unit
) {
    Column(
        modifier = modifier.then(
            Modifier
                .verticalScroll(state = rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 120.dp)

        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(top = 12.dp, bottom = 20.dp)
                .fillMaxWidth()
        ) {
            Text(
                stringResource(if (isVip) R.string.congratulations else R.string.get_premium),
                style = TextStyle(
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic
                )
            )
        }
        Table(R.string.photo_size, Constants.image_resolution_list)
        Spacer(modifier = Modifier.height(15.dp))
        Table(R.string.video_size, Constants.video_resolution_list)
        Spacer(modifier = Modifier.height(15.dp))
        Column {
            HorizontalLine()
            TableTitle(stringResource(R.string.watermark_settings))
            HorizontalLine()
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .height(38.dp)
                    .fillMaxWidth()
                    .background(Color(0xFF191A23))
            ) {
                VerticalLine()
                RowItem(
                    modifier = Modifier.weight(1f),
                    stringResource(R.string.logo),
                    R.mipmap.ic_owned
                )
                VerticalLine()
            }
            HorizontalLine()
        }
        Spacer(Modifier.height(20.dp))
        if (Global.isVip.value) {
            OwnedTip()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                userScrollEnabled = false,
                modifier = Modifier.selectableGroup().height(200.dp)
            ) {
                for (i in list.indices) {
                    item {
                        SubscribeItem(i, list[i].title, list[i].subTitle, i == index, onClick)
                    }
                }
            }
        }
    }
}

@Composable
fun Table(stringId: Int, list: List<ResolutionBean>) {
    Column {
        HorizontalLine()
        TableTitle(stringResource(stringId))
        HorizontalLine()
        for (i in list.indices step 2) {
            TableRow(list[i], if (i + 1 < list.size) list[i + 1] else null)
            HorizontalLine()
        }
    }
}

@Composable
fun HorizontalLine() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = Color(0xFF46395D)
    )
}


@Composable
fun TableTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .height(38.dp)
            .fillMaxWidth()
            .background(Color(0xFF2A1541))
    ) {
        VerticalLine()
        Spacer(Modifier.weight(1f))
        star()
        Text(
            title,
            style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        star()
        Spacer(Modifier.weight(1f))
        VerticalLine()
    }
}

@Composable
fun star() {
    Icon(
        Icons.Default.Star,
        contentDescription = null,
        tint = Color(0xFFB2C4EE),
        modifier = Modifier.size(14.dp)
    )
}

@Composable
fun TableRow(bean: ResolutionBean, bean2: ResolutionBean?) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .height(38.dp)
            .fillMaxWidth()
            .background(Color(0xFF191A23))
    ) {
        VerticalLine()
        RowItem(
            modifier = Modifier.weight(1f),
            bean.title,
            if (bean.isPro) R.mipmap.ic_owned else R.mipmap.ic_free
        )
        VerticalLine()
        if (bean2 != null) {
            RowItem(
                modifier = Modifier.weight(1f),
                bean2.title,
                if (bean2.isPro) R.mipmap.ic_owned else R.mipmap.ic_free
            )
        } else {
            RowItem(modifier = Modifier.weight(1f), "", 0)
        }
        VerticalLine()
    }
}

@Composable
fun RowItem(modifier: Modifier, title: String, imageId: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.then(
            Modifier
                .height(38.dp)
                .background(Color(0xFF191A23))
        )
    ) {
        Text(title, style = TextStyle(color = Color.White, fontSize = 12.sp))
        Spacer(modifier = Modifier.width(5.dp))
        if (imageId != 0) {
            Image(
                painter = painterResource(imageId),
                contentDescription = null
            )
        }
    }
}

@Composable
fun VerticalLine() {
    VerticalDivider(
        modifier = Modifier.fillMaxHeight(),
        thickness = 1.dp,
        color = Color(0xFF46395D)
    )
}

@Composable
fun SubscribeItem(
    index: Int,
    period: String,
    price: String,
    checked: Boolean,
    onClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .height(80.dp)
            .fillMaxWidth()
            .border(
                if (checked) 2.dp else 1.dp,
                color = colorResource(if (checked) R.color.yellow_main else R.color.color_ff636a73),
                shape = RoundedCornerShape(12.dp)
            )
            .selectable(selected = checked, role = Role.RadioButton, onClick = { onClick(index) })
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            Text(
                period, style = TextStyle(
                    fontSize = 14.sp, color = colorResource(
                        if (checked) R.color.yellow_main else
                            R.color.color_ffc6c6c6
                    )
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                price, style = TextStyle(
                    fontSize = 20.sp, color = colorResource(
                        if (checked) R.color.yellow_main else
                            R.color.color_ffc6c6c6
                    )
                )
            )
        }
    }
}

@Composable
fun OwnedTip() {
    Text(
        modifier = Modifier.padding(horizontal = 35.dp, vertical = 8.dp),
        text = stringResource(R.string.owned_tip),
        style = TextStyle(
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            color = colorResource(R.color.color_ffe0e0e0)
        )
    )
}

@Composable
fun BottomArea(modifier: Modifier, vip: Boolean, tip: String, onClick: () -> Unit) {
    Column(modifier = modifier.background(Color(0xFF101118))) {
        TextButton(
            onClick = onClick,
            modifier = Modifier
                .padding(horizontal = 35.dp, vertical = 8.dp)
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            colorResource(R.color.color_ffffdb00),
                            colorResource(R.color.color_ffff7f00)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                ), colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    stringResource(if (vip) R.string.got_it else R.string.join_now),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.align(Alignment.Center)

                )
                Image(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp),
                    painter = painterResource(R.mipmap.join_arrow),
                    contentDescription = null
                )
            }

        }
        Text(
            tip,
            modifier = Modifier.padding(start = 35.dp, top = 0.dp, end = 35.dp, bottom = 10.dp),
            style = TextStyle(
                textAlign = TextAlign.Center,
                color = colorResource(R.color.color_ff878787),
                fontSize = 12.sp
            )
        )
    }
}

@Preview
@Composable
fun Test() {
}