package com.function.karaoke.interaction.utils;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;

import java.util.ArrayList;
import java.util.List;

public class Billing {

    private final BillingClient billingClient;
    private final Activity activity;
    private final boolean displayProducts;
    //    private final ConsumeResponseListener consumeResponseListener;
    private AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {

        }
    };
    private int counter = 0;
    private List<SkuDetails> skuDetailsList;

    public Billing(Activity activity, PurchasesUpdatedListener purchasesUpdatedListener, boolean displayProducts, ReadyListener readyListener) {
        this.activity = activity;
//        this.consumeResponseListener = consumeResponseListener;
        this.displayProducts = displayProducts;
        billingClient = BillingClient.newBuilder(activity)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        connect(readyListener);
    }

    private void connect(ReadyListener readyListener) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    if (displayProducts)
                        getProducts();
                    else
                        acknowledgePreviousOrders();
                    readyListener.ready();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                counter++;
                if (counter < 1000) {
                    connect(readyListener);
                } else {
                    showToastUnableToConnect();
                }
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    public void subscribeListener(AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener) {
        this.acknowledgePurchaseResponseListener = acknowledgePurchaseResponseListener;
    }

    private void acknowledgePreviousOrders() {
        Purchase.PurchasesResult allPurchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        if (allPurchases.getPurchasesList() != null)
            for (Purchase purchase : allPurchases.getPurchasesList()) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED)
                    handlePurchase(purchase);

            }
    }

    public boolean isSubscribed() {
        Purchase.PurchasesResult allPurchases = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
        if (allPurchases.getPurchasesList() != null)
            for (Purchase purchase : allPurchases.getPurchasesList()) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED)
                    return true;

            }
        return false;
    }

    private void showToastUnableToConnect() {
        Toast.makeText(activity.getBaseContext(), "Unable to connect", Toast.LENGTH_SHORT).show();
    }

    private void getProducts() {
        List<String> skuList = new ArrayList<>();
        skuList.add("yearly_1");
        skuList.add("monthly_1");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        billingClient.querySkuDetailsAsync(params.build(),
                this::onSkuDetailsResponse);
    }

    public void startFlow(int subscriptionNumber) {
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetailsList.get(subscriptionNumber))
                .build();
        int responseCode = billingClient.launchBillingFlow(activity, billingFlowParams).getResponseCode();
    }

    public void handlePurchase(Purchase purchase) {
        // Purchase retrieved from BillingClient#queryPurchases or your PurchasesUpdatedListener.
        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.

        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
            billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
        }
    }

    private void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
        if (skuDetailsList.size() != 0) {
            this.skuDetailsList = skuDetailsList;
//            startFlow(skuDetailsList);
        } else {
            Toast.makeText(activity.getBaseContext(), "No items for sale at the moment", Toast.LENGTH_LONG).show();
        }
    }

    public interface ReadyListener {
        void ready();
    }
}
