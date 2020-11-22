package com.function.karaoke.hardware.utils;

import android.app.Activity;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;

import java.util.ArrayList;
import java.util.List;

public class Billing {

    private final BillingClient billingClient;
    private final Activity activity;
    private int counter = 0;

    public Billing(Activity activity, PurchasesUpdatedListener purchasesUpdatedListener) {
        this.activity = activity;
        billingClient = BillingClient.newBuilder(activity)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        connect();
    }

    private void connect() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    displayProducts();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                counter++;
                if (counter < 1000) {
                    connect();
                } else {
                    showToastUnableToConnect();
                }
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    private void showToastUnableToConnect() {
        Toast.makeText(activity.getBaseContext(), "Unable to connect", Toast.LENGTH_SHORT).show();
    }

    private void displayProducts() {
        Purchase.PurchasesResult allPurchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        for (Purchase purchase : allPurchases.getPurchasesList()) {
            handlePurchase(purchase);
        }
        List<String> skuList = new ArrayList<>();
        skuList.add("karaoke_test");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                (billingResult, skuDetailsList) -> {
                    if (skuDetailsList.size() != 0) {
                        startFlow(skuDetailsList);
                    }
                });
    }

    public void startFlow(List<SkuDetails> skuDetails) {
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails.get(0))
                .build();
        int responseCode = billingClient.launchBillingFlow(activity, billingFlowParams).getResponseCode();
    }

    public void handlePurchase(Purchase purchase) {
        // Purchase retrieved from BillingClient#queryPurchases or your PurchasesUpdatedListener.
        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.

        if (!purchase.isAcknowledged()) {
            ConsumeParams consumeParams =
                    ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();

            ConsumeResponseListener listener = (billingResult, purchaseToken) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Handle the success of the consume operation.
                }
            };
            billingClient.consumeAsync(consumeParams, listener);
        }
    }
}
