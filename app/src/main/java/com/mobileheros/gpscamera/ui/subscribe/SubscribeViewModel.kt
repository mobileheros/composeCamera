package com.mobileheros.gpscamera.ui.subscribe

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.google.gson.Gson
import com.mobileheros.gpscamera.utils.Global
import com.mobileheros.gpscamera.utils.PlayBillingHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscribeUiState(
    val isVip: Boolean = Global.isVip.value,
    val list: MutableList<ProductItemBean> = mutableListOf(),
    val index: Int = 0
)

@HiltViewModel
class SubscribeViewModel @Inject constructor(application: Application) :
    ViewModel() { private val playBillingHelper = PlayBillingHelper.getInstance(application)
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
            val list = it.list.apply {
                val i = this.indexOf(this.find { it.checked })
                if (i != -1) {
                    this[i] = this[i].copy(checked = false)
                }
                if (index < size) {
                    this[index] = this[index].copy(checked = true)
                }
            }
            it.copy(list = list, index = index)
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
        if (products.isNotEmpty()) {
            val temp = products.find { it.title == "Yearly" }
            if (temp != null) {
                temp.checked = true
            } else {
                products[0].checked = true
            }
        }
        return products
    }

    fun joinNow(
        activity: Activity
    ) {
        playBillingHelper.processPurchases(
            activity,
            _uiState.value.list[_uiState.value.index].parent,
            _uiState.value.list[_uiState.value.index].product.offerToken
        )
    }
}
