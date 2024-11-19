package com.mobileheros.gpscamera.ui.subscribe

import android.content.Context
import com.android.billingclient.api.ProductDetails
import com.mobileheros.gpscamera.R

data class ProductItemBean(
    var product: ProductDetails.SubscriptionOfferDetails,
    val parent: ProductDetails,
    var checked: Boolean = false
) {
    var title: String = ""
    var subTitle: String = ""
    var discountTitle: String = ""
}

fun ProductItemBean.transform(context: Context, hasTradeHistory: Boolean = false) {
    try {
        val list = product.pricingPhases.pricingPhaseList
        if (list.isNotEmpty()) {
            val showDiscount = list.size > 1 && !hasTradeHistory
            var period = list.last().billingPeriod.substring(1)
            if (list.size > 1) {
                subTitle = list.last().formattedPrice
                discountTitle = list.last().formattedPrice
            } else {
                subTitle = list.last().formattedPrice
            }
            when (period.toCharArray().first { it.isLetter() }.toString()) {
                "D" -> {
                    title = context.getString(R.string.sub_page_opt_daily)
                }

                "W" -> {
                    title = context.getString(R.string.sub_page_ts_weekly)
                }

                "M" -> {
                    title = context.getString(R.string.sub_page_opt_monthly)
                }

                "Y" -> {
                    title = context.getString(R.string.sub_page_opt_yearly)
                }

                else -> {}
            }
//            if (showDiscount) {
//                subTitle = context.getString(R.string.sub_page_then) + " " + subTitle
//                val bean = list.minBy { it.priceAmountMicros }
//                val sb = StringBuilder(title)
//                period = bean.billingPeriod.substring(1)
//                val char = period.first { it.isLetter() }
//                val value = period.substring(0, period.indexOf(char)).toInt()
//                sb.append(", $value")
//                sb.append(
//                    when (char.toString()) {
//                        "D" -> " ${context.getString(R.string.sub_page_day)}"
//                        "W" -> " ${context.getString(R.string.sub_page_week)}"
//                        "M" -> " ${context.getString(R.string.sub_page_month)}"
//                        "Y" -> " ${context.getString(R.string.sub_page_year)}"
//                        else -> ""
//                    }
//                )
////                if (value > 1) {
////                    sb.append("s")
////                }
//                if (bean.priceAmountMicros == 0L) {
//                    sb.append(" ${context.getString(R.string.sub_page_free_trial)}")
//                } else {
//                    sb.append(" ${context.getString(R.string.sub_page_discount)}")
//                }
//                title = sb.toString()
//            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}