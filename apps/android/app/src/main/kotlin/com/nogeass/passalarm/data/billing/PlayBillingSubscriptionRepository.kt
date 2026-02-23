package com.nogeass.passalarm.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.nogeass.passalarm.domain.model.ProPeriod
import com.nogeass.passalarm.domain.model.ProProduct
import com.nogeass.passalarm.domain.model.ProStatus
import com.nogeass.passalarm.domain.model.ProTier
import com.nogeass.passalarm.domain.repository.SubscriptionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class PlayBillingSubscriptionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : SubscriptionRepository {

    companion object {
        private const val PRODUCT_MONTHLY = "com.nogeass.passalarm.pro.monthly"
        private const val PRODUCT_YEARLY = "com.nogeass.passalarm.pro.yearly"
        private val PRODUCT_IDS = listOf(PRODUCT_MONTHLY, PRODUCT_YEARLY)
    }

    private val _status = MutableStateFlow(ProStatus.Free)

    private var pendingPurchaseResult: ((Result<ProStatus>) -> Unit)? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            val status = mapPurchasesToStatus(purchases)
            _status.value = status
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    !purchase.isAcknowledged
                ) {
                    acknowledgePurchase(purchase)
                }
            }
            pendingPurchaseResult?.invoke(Result.success(status))
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            pendingPurchaseResult?.invoke(Result.success(_status.value))
        } else {
            pendingPurchaseResult?.invoke(
                Result.failure(
                    BillingException(
                        billingResult.responseCode,
                        billingResult.debugMessage,
                    )
                )
            )
        }
        pendingPurchaseResult = null
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    // ── SubscriptionRepository ──────────────────────────────────────────

    override fun observeStatus(): Flow<ProStatus> = _status.asStateFlow()

    override suspend fun currentStatus(): ProStatus {
        ensureConnected()
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val result = suspendCancellableCoroutine { cont ->
            billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    cont.resume(purchases)
                } else {
                    cont.resumeWithException(
                        BillingException(billingResult.responseCode, billingResult.debugMessage)
                    )
                }
            }
        }
        val status = mapPurchasesToStatus(result)
        _status.value = status
        return status
    }

    override suspend fun fetchProducts(): List<ProProduct> {
        ensureConnected()
        val productList = PRODUCT_IDS.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        val result = suspendCancellableCoroutine { cont ->
            billingClient.queryProductDetailsAsync(params) { billingResult, detailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    cont.resume(detailsList)
                } else {
                    cont.resumeWithException(
                        BillingException(billingResult.responseCode, billingResult.debugMessage)
                    )
                }
            }
        }
        return result.mapNotNull { details -> mapProductDetails(details) }
    }

    override suspend fun purchase(activity: Activity, product: ProProduct): ProStatus {
        ensureConnected()

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(product.id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val queryParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val detailsList = suspendCancellableCoroutine { cont ->
            billingClient.queryProductDetailsAsync(queryParams) { billingResult, details ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    cont.resume(details)
                } else {
                    cont.resumeWithException(
                        BillingException(billingResult.responseCode, billingResult.debugMessage)
                    )
                }
            }
        }

        val productDetails = detailsList.firstOrNull()
            ?: throw BillingException(-1, "Product not found: ${product.id}")

        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            ?: throw BillingException(-1, "No offer available for: ${product.id}")

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
            .build()

        return suspendCancellableCoroutine { cont ->
            pendingPurchaseResult = { result ->
                result.fold(
                    onSuccess = { cont.resume(it) },
                    onFailure = { cont.resumeWithException(it) },
                )
            }
            cont.invokeOnCancellation { pendingPurchaseResult = null }

            val launchResult = billingClient.launchBillingFlow(activity, billingFlowParams)
            if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
                pendingPurchaseResult = null
                cont.resumeWithException(
                    BillingException(launchResult.responseCode, launchResult.debugMessage)
                )
            }
        }
    }

    override suspend fun restorePurchases(): ProStatus = currentStatus()

    // ── Private helpers ─────────────────────────────────────────────────

    private suspend fun ensureConnected() {
        if (billingClient.isReady) return
        suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        cont.resume(Unit)
                    } else {
                        cont.resumeWithException(
                            BillingException(
                                billingResult.responseCode,
                                billingResult.debugMessage,
                            )
                        )
                    }
                }

                override fun onBillingServiceDisconnected() {
                    // Will reconnect on next call to ensureConnected()
                }
            })
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { /* fire and forget */ }
    }

    private fun mapPurchasesToStatus(purchases: List<Purchase>): ProStatus {
        val activePurchase = purchases.firstOrNull { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                purchase.products.any { it in PRODUCT_IDS }
        } ?: return ProStatus.Free

        val productId = activePurchase.products.firstOrNull { it in PRODUCT_IDS }
        val period = when (productId) {
            PRODUCT_MONTHLY -> ProPeriod.MONTHLY
            PRODUCT_YEARLY -> ProPeriod.YEARLY
            else -> null
        }

        return ProStatus(
            tier = ProTier.PRO,
            expiresAt = null, // Google Play manages renewal automatically
            period = period,
        )
    }

    private fun mapProductDetails(details: ProductDetails): ProProduct? {
        val offer = details.subscriptionOfferDetails?.firstOrNull() ?: return null
        val phases = offer.pricingPhases.pricingPhaseList
        // The paid phase is the last one (trial phases come first)
        val paidPhase = phases.lastOrNull() ?: return null

        val period = when (details.productId) {
            PRODUCT_MONTHLY -> ProPeriod.MONTHLY
            PRODUCT_YEARLY -> ProPeriod.YEARLY
            else -> return null
        }

        val pricePerMonth = if (period == ProPeriod.YEARLY) {
            val monthlyMicros = paidPhase.priceAmountMicros / 12
            val monthlyPrice = monthlyMicros / 1_000_000.0
            String.format("%s%.0f", paidPhase.priceCurrencyCode + " ", monthlyPrice)
        } else {
            null
        }

        // Detect free trial phase (priceAmountMicros == 0)
        val trialPhase = phases.firstOrNull { it.priceAmountMicros == 0L }
        val trialText = trialPhase?.let { parseTrialText(it.billingPeriod) }

        return ProProduct(
            id = details.productId,
            period = period,
            displayPrice = paidPhase.formattedPrice,
            pricePerMonth = pricePerMonth,
            trialText = trialText,
        )
    }

    private fun parseTrialText(isoPeriod: String): String? {
        // ISO 8601 duration: P1W, P1M, P3D, P1Y, etc.
        val regex = Regex("^P(\\d+)([DWMY])$")
        val match = regex.find(isoPeriod) ?: return null
        val value = match.groupValues[1].toIntOrNull() ?: return null
        return when (match.groupValues[2]) {
            "D" -> "${value}日間無料"
            "W" -> "${value}週間無料"
            "M" -> "${value}ヶ月無料"
            "Y" -> "${value}年間無料"
            else -> null
        }
    }
}

class BillingException(
    val responseCode: Int,
    message: String,
) : Exception("Billing error ($responseCode): $message")
