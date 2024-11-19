package com.mobileheros.gpscamera.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchaseHistoryResponseListener
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchaseHistory
import com.google.gson.Gson
import com.mobileheros.gpscamera.utils.Constants.PRODUCT_ID
import com.orhanobut.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayBillingHelper @Inject constructor(
    @ApplicationContext
    context: Context,
) : PurchasesUpdatedListener, BillingClientStateListener {

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().enablePrepaidPlans().build()
        )
        .build()

    init {
        Logger.e("init-----")
        startConnection()
    }

    fun startConnection() {
        Logger.e("startConnection")
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        Logger.e(billingResult.toString())
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "Billing response OK")
            // The BillingClient is ready. You can now query Products Purchases.
        } else {
            Log.e(TAG, billingResult.debugMessage)
            retryBillingServiceConnection()
        }
    }

    override fun onBillingServiceDisconnected() {
        // Try to restart the connection on the next request to
        // Google Play by calling the startConnection() method.
        Log.e(TAG, "GBPL Service disconnected")
        retryBillingServiceConnection()
    }

    // Billing connection retry logic. This is a simple max retry pattern
    private fun retryBillingServiceConnection() {
        val maxTries = 3
        var tries = 1
        var isConnectionEstablished = false
        do {
            try {
                billingClient.startConnection(object : BillingClientStateListener {
                    override fun onBillingServiceDisconnected() {

                    }

                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        tries++
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            isConnectionEstablished = true
                            Log.d(TAG, "Billing connection retry succeeded.")
                        } else {
                            Log.e(
                                TAG,
                                "Billing connection retry failed: ${billingResult.debugMessage}"
                            )
                        }
                    }
                })
                runBlocking {
                    delay(1000)
                }
            } catch (e: Exception) {
                e.message?.let { Log.e(TAG, it) }
                tries++
            }
        } while (tries <= maxTries && !isConnectionEstablished)
    }


    //查询商品列表
    suspend fun queryProductDetails(): List<ProductDetails> {
        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    mutableListOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PRODUCT_ID)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build(),
                    )
                )
                .build()

        // leverage queryProductDetails Kotlin extension function
        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(queryProductDetailsParams)
        }

        Log.e("test", productDetailsResult.toString())


        // Process the result.
        productDetailsResult.billingResult.responseCode
        Log.e("test", productDetailsResult.productDetailsList.toString())

        if (productDetailsResult.billingResult.responseCode == BillingResponseCode.OK) {
            return productDetailsResult.productDetailsList ?: mutableListOf()
        }
        return mutableListOf()
    }

    //购买
    fun processPurchases(activity: Activity, productDetails: ProductDetails, offerToken: String) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                .setProductDetails(productDetails)
                // to get an offer token, call ProductDetails.subscriptionOfferDetails()
                // for a list of offers that are available to the user
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

// Launch the billing flow
        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)


    }


    //交易结果回调
    override fun onPurchasesUpdated(billingResult: BillingResult, list: MutableList<Purchase>?) {
        Log.e("test_purchase_result", "${billingResult.responseCode}")
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> if (null != list) {
                for (purchase in list) {
                    runBlocking {
                        handlePurchase(purchase)
                    }
                }
            } else Log.d(TAG, "Null Purchase List Returned from OK response!")

            BillingClient.BillingResponseCode.USER_CANCELED -> Log.i(
                TAG,
                "onPurchasesUpdated: User canceled the purchase"
            )

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> Log.i(
                TAG,
                "onPurchasesUpdated: The user already owns this item"
            )

            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> Log.e(
                TAG,
                "onPurchasesUpdated: Developer error means that Google Play " +
                        "does not recognize the configuration. If you are just getting started, " +
                        "make sure you have configured the application correctly in the " +
                        "Google Play Console. The SKU product ID must match and the APK you " +
                        "are using must be signed with release keys."
            )

            else -> Log.d(
                TAG,
                "BillingResult [" + billingResult.responseCode + "]: " + billingResult.debugMessage
            )
        }
        if (billingResult.responseCode != BillingResponseCode.OK) {
//            EventBus.getDefault().post(SubscribeSuccessEvent(false))
        }
    }

    //确认交易
    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState === Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                val ackPurchaseResult = withContext(Dispatchers.IO) {
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams.build()) {
                        Log.d(TAG, "acknowledgePurchase responseCode: ${it.responseCode}")
                        if (it.responseCode == BillingResponseCode.OK) {
                            runBlocking(Dispatchers.Main) {
//                                EventBus.getDefault().post(SubscribeSuccessEvent(true))
                                queryPurchases(null)
                            }
                        }
                    }
                }
            }
        }
    }

    //查询订阅
    fun queryPurchases(
        context: Context?,
        listener: PurchasesResponseListener = PurchasesResponseListener { billingResult, list ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                Log.e("test", list.toString())
                val result = list.filter { it.purchaseState == PurchaseState.PURCHASED }
//                Global.isVip = false
                if (Global.isVip != (result.isNotEmpty())) {
                    Global.isVip = (result.isNotEmpty())
                    //已订阅
                    runBlocking(Dispatchers.Main) {
//                        EventBus.getDefault().post(SubscribeStatusEvent())
                    }
                    context?.localConfig?.putData("isVip", Global.isVip)
                }
            }
        }
    ) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)

// uses queryPurchasesAsync Kotlin extension function
        val purchasesResult = billingClient.queryPurchasesAsync(
            params.build(), listener
        )
// check purchasesResult.billingResult
// process returned purchasesResult.purchasesList, e.g. display the plans user owns

    }

    fun queryHistory(): Boolean {
        val params = QueryPurchaseHistoryParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
        var result = false

// uses queryPurchaseHistory Kotlin extension function
        runBlocking(Dispatchers.IO) {
            val purchaseHistoryResult = billingClient.queryPurchaseHistory(params.build())
            purchaseHistoryResult.billingResult
            Log.e("test_history", Gson().toJson(purchaseHistoryResult.purchaseHistoryRecordList))

            if (purchaseHistoryResult.billingResult.responseCode == BillingResponseCode.OK) {
                result = purchaseHistoryResult.purchaseHistoryRecordList?.any {
                    it.products.contains(
                        PRODUCT_ID
                    )
                } == true
            }
        }
        return result

// check purchaseHistoryResult.billingResult
// process returned purchaseHistoryResult.purchaseHistoryRecordList, e.g. display purchase

    }

    fun queryHistoryAsync(
        listener: PurchaseHistoryResponseListener = PurchaseHistoryResponseListener { result, list ->
            if (result.responseCode == BillingResponseCode.OK) {
                val hasRecord = list?.any {
                    it.products.contains(
                        PRODUCT_ID
                    )
                } == true
            }
        }
    ) {
        val params = QueryPurchaseHistoryParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
        billingClient.queryPurchaseHistoryAsync(params.build(), listener)
    }

    companion object {
        private val TAG = PlayBillingHelper::class.java.simpleName

//        @Volatile
//        private var sInstance: PlayBillingHelper? = null
//
//        // Standard boilerplate double check locking pattern for thread-safe singletons.
//        @JvmStatic
//        fun getInstance(
//            application: Application,
//        ) = sInstance ?: synchronized(this) {
//            sInstance ?: PlayBillingHelper(
//                application,
//            )
//                .also { sInstance = it }
//        }
    }


}