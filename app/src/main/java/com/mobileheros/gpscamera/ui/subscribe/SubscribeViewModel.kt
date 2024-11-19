package com.mobileheros.gpscamera.ui.subscribe

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.google.gson.Gson
import com.mobileheros.gpscamera.R
import com.mobileheros.gpscamera.utils.Global
import com.mobileheros.gpscamera.utils.PlayBillingHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscribeUiState(
    val isVip: Boolean = Global.isVip,
    val list: List<ProductItemBean> = emptyList()
)

@HiltViewModel
class SubscribeViewModel @Inject constructor(playBillingHelper: PlayBillingHelper) : ViewModel() {
    private val _uiState = MutableStateFlow(SubscribeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(list = transform(playBillingHelper.queryProductDetails()))
            }
        }
    }

    fun checkIndex(index: Int) {
        _uiState.update {
            it.copy(list = it.list.also { list ->
                list.find { it.checked }?.checked = false
                if (index < list.size) {
                    list[index].checked = true
                }
            })
        }
    }

    fun transform(list: List<ProductDetails>): MutableList<ProductItemBean> {
        val products: MutableList<ProductItemBean> = mutableListOf()
        products.clear()
        for (i in list.indices) {
            if (list[i].subscriptionOfferDetails.isNullOrEmpty()) continue
            for (j in list[i].subscriptionOfferDetails!!.indices) {
                products.add(
                    ProductItemBean(
                        list[i].subscriptionOfferDetails!![j],
                        list[i]
                    )
                )
            }
        }
        val test = products.groupBy { it.product.basePlanId }.values.map { list1 ->
            if (list1.size > 1) {
                try {
                    list1.firstOrNull { it.product.offerId != null } ?: list1.first()
//                        list1.minByOrNull { it.product.pricingPhases.pricingPhaseList[0].priceAmountMicros }!!
                } catch (e: Exception) {
                    list1.first()
                }
            } else list1[0]
        }
        Log.e("test_product", Gson().toJson(test))
        products.clear()
        products.addAll(test)
//        if (products.isNotEmpty()) {
//            val temp = products.find { it.title == getString(R.string.sub_page_opt_yearly) }
//            if (temp != null) {
//                temp.checked = true
//            } else {
//                products[0].checked = true
//            }
//        }
        return products
    }
}
