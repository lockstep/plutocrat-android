package com.whitefly.plutocrat.helpers;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;
import com.google.gson.Gson;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.exception.IAPException;
import com.whitefly.plutocrat.helpers.utils.Security;
import com.whitefly.plutocrat.models.IAPItemDetailModel;
import com.whitefly.plutocrat.models.IAPPurchaseModel;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Satjapot on 6/4/16 AD.
 */
public class IAPHelper {
    public static final String IAP_TEST_PRODUCT = "android.test.purchased";

    public static final int BILLING_RESPONSE_RESULT_OK = 0;
    public static final int BILLING_RESPONSE_RESULT_USER_CANCELED = 1;
    public static final int BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE = 2;
    public static final int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3;
    public static final int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4;
    public static final int BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5;
    public static final int BILLING_RESPONSE_RESULT_ERROR = 6;
    public static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;
    public static final int BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8;

    public static final int BILLING_RESPONSE_RESULT_SERVER_NOT_RESPONSE = -100;
    public static final int BILLING_RESPONSE_RESULT_ACTIVITY_ERROR = 101;
    public static final int BILLING_RESPONSE_RESULT_INVALID_PAYLOAD= 102;
    public static final int BILLING_RESPONSE_RESULT_KEY_VERIFY_FAILED = 103;

    public static final int METHOD_CONSUME = 1;
    public static final int METHOD_GET_PURCHASED = 2;
    public static final int METHOD_GET_DETAILS = 3;

    private static final int IAP_VERSION = 3;
    private static final int IAP_REQUEST_CODE = 50002;
    private static final String IAP_GOOGLE_INTENT = "com.android.vending.billing.InAppBillingService.BIND";
    private static final String IAP_GOOGLE_PACKAGE = "com.android.vending";
    private static final String IAP_INAPP_TYPE = "inapp";

    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final String IAP_BUNDLE_GET_ITEM_DETAIL = "ITEM_ID_LIST";
    private static final String IAP_BUNDLE_ITEM_DETAIL = "DETAILS_LIST";

    private static final String PUBLIC_IAP_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlPnYdpcAodnLSLUZiWcPJZQYeCFVew4wk08SIv21wzxdKL1Mmo79TvSVmccU1lUSMlvVQ5JEwuAe39ebu7gZlLZPHg7dkTlvaffDeRvlMZEugnVboXR/KEj1a+f5FNDrdC+D6RAMF2o/6p+bhBVt06FeWiSGx868g4CyBg8SfWnuUUDq2gIEuOLlQhoPzCTEPsN37nqcnX8mKKX2Juv3cJSNeUoRieg2JKjudDih8d6G0hvURYjHQv0AidzrPiSpYh6+oc+jgJxtSn4iRAHJu76P6NeSnMBUqeMKkK1JU2PSQGLHZVhWGEEh5gm+V6xO5t0y87KIxTjPF6rqEPxpkwIDAQAB";

    // Attributes
    private Context mActivity;
    private IInAppBillingService mIAPService;
    private Gson mGson;
    private ArrayList<IAPProcessListener> mListeners;
    private ServiceConnection mIAPServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIAPService = IInAppBillingService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIAPService = null;
        }
    };

    private String mLastDeveloperPayload;

    // Constructor
    public IAPHelper(Context activity) {
        mActivity = activity;
        mGson = AppPreference.getInstance().getGson();
        mListeners = new ArrayList<>();

        // Create IAP connection
        Intent serviceIntent =
                new Intent(IAP_GOOGLE_INTENT);
        serviceIntent.setPackage(IAP_GOOGLE_PACKAGE);
        mActivity.bindService(serviceIntent, mIAPServiceConnection, Context.BIND_AUTO_CREATE);
    }

    // Methods
    public boolean isReady() {
        return mIAPService != null;
    }

    public void addIAPProcessListener(IAPProcessListener listener) {
        if(! mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeIAPProcessListener(IAPProcessListener listener) {
        if(mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    public void onDestroy() {
        if(mIAPService != null) {
            mActivity.unbindService(mIAPServiceConnection);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == IAP_REQUEST_CODE) {
            int responseCode = data.getIntExtra(RESPONSE_CODE, 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == Activity.RESULT_OK) {
                Log.d(AppPreference.DEBUG_APP, "purchaseData: " + purchaseData);
                Log.d(AppPreference.DEBUG_APP, "signatureData: " + dataSignature);
                if(responseCode == BILLING_RESPONSE_RESULT_OK) {
                    IAPPurchaseModel model = mGson.fromJson(purchaseData, IAPPurchaseModel.class);
                    if(! model.developerPayload.equals(mLastDeveloperPayload)) {
                        for (IAPProcessListener listener : mListeners) {
                            listener.onBuyFailed(BILLING_RESPONSE_RESULT_INVALID_PAYLOAD);
                        }
                        return;
                    }
                    if(! model.productId.equals(IAPHelper.IAP_TEST_PRODUCT) &&
                            ! Security.verifyPurchase(PUBLIC_IAP_KEY, purchaseData, dataSignature)) {
                        for (IAPProcessListener listener : mListeners) {
                            listener.onBuyFailed(BILLING_RESPONSE_RESULT_KEY_VERIFY_FAILED);
                        }
                        return;
                    }

                    for (IAPProcessListener listener : mListeners) {
                        listener.onBuySuccess(responseCode, model);
                    }
                } else {
                    for (IAPProcessListener listener : mListeners) {
                        listener.onBuyFailed(responseCode);
                    }
                }
            } else {
                Log.d(AppPreference.DEBUG_APP, "dataSignature: " + dataSignature);
                for (IAPProcessListener listener : mListeners) {
                    listener.onBuyFailed(BILLING_RESPONSE_RESULT_ACTIVITY_ERROR);
                }
            }
        }
    }

    public void buy(String sku, String payload) throws IAPException {
        if(sku == null) {
            sku = IAP_TEST_PRODUCT;
        }
        if(payload == null) {
            payload = UUID.randomUUID().toString();
        }
        mLastDeveloperPayload = payload;

        try {
            Bundle buyIntentBundle = mIAPService.getBuyIntent(IAP_VERSION, mActivity.getPackageName(),
                    sku, IAP_INAPP_TYPE, payload);
            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
            if (pendingIntent != null) {
                if(mActivity instanceof Activity) {
                    ((Activity) mActivity).startIntentSenderForResult(
                            pendingIntent.getIntentSender(), IAP_REQUEST_CODE, new Intent(),
                            Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new IAPException(mActivity.getString(R.string.error_iap_default));
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
            throw new IAPException(mActivity.getString(R.string.error_iap_default));
        }
    }

    public void consume(String purchaseToken) {
        AsyncTaskCompat.executeParallel(new IAP_ConsumeCallback(), purchaseToken);
    }

    public void getPurchased() {
        AsyncTaskCompat.executeParallel(new IAP_GetPurchasedCallback(), null);
    }

    public void getItemDetails(String[] itemIds) {
        if(itemIds.length == 0) return;
        ArrayList<String> idList = new ArrayList<>();
        for(String itemId : itemIds) {
            idList.add(itemId);
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(IAP_BUNDLE_GET_ITEM_DETAIL, idList);

        AsyncTaskCompat.executeParallel(new IAP_GetItemDetails(), bundle);
    }

    /*
    Inner Class
     */
    public interface IAPProcessListener {
        enum ProcessState {
            PreExecute, PostExecute
        }

        void onBuySuccess(int resultCode, IAPPurchaseModel model);
        void onBuyFailed(int resultCode);
        void onConsumed(int resultCode);
        void onProcessing(int methodId, ProcessState state);
        void onPurchasedItemLoaded(int resultCode, ArrayList<IAPPurchaseModel> items);
        void onItemDetailsLoaded(int resultCode, ArrayList<IAPItemDetailModel> itemDetails);
    }

    /*
    AsyncTask
     */
    private class IAP_ConsumeCallback extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            for (IAPProcessListener listener : mListeners) {
                listener.onProcessing(METHOD_CONSUME, IAPProcessListener.ProcessState.PreExecute);
            }
        }

        @Override
        protected Integer doInBackground(String... params) {
            do {
                // Wait until Helper ready
            } while(mIAPService == null);

            Integer resultCode = BILLING_RESPONSE_RESULT_SERVER_NOT_RESPONSE;
            String purchaseToken = null;
            if(params.length == 0) {
                purchaseToken = "inapp:" + mActivity.getPackageName() + ":android.test.purchased";
            } else {
                purchaseToken = params[0];
            }
            try {
                resultCode = mIAPService.consumePurchase(IAP_VERSION, mActivity.getPackageName(), purchaseToken);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.d(AppPreference.DEBUG_APP, "Consume complete");
            return resultCode;
        }

        @Override
        protected void onPostExecute(Integer code) {
            for (IAPProcessListener listener : mListeners) {
                listener.onProcessing(METHOD_CONSUME, IAPProcessListener.ProcessState.PostExecute);
                listener.onConsumed(code);
            }
        }
    }

    private class IAP_GetPurchasedCallback extends AsyncTask<String, Void, Integer> {
        private ArrayList<IAPPurchaseModel> mPurchaseItems;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            for (IAPProcessListener listener : mListeners) {
                listener.onProcessing(METHOD_GET_PURCHASED, IAPProcessListener.ProcessState.PreExecute);
            }
        }

        @Override
        protected Integer doInBackground(String... params) {
            Integer resultCode = BILLING_RESPONSE_RESULT_SERVER_NOT_RESPONSE;
            String continuationToken = null;
            mPurchaseItems = new ArrayList<>();

            do {
                // Wait until Helper ready
            } while(mIAPService == null);

            try {
                do {
                    Bundle result = mIAPService.getPurchases(IAP_VERSION, mActivity.getPackageName(), IAP_INAPP_TYPE, null);
                    resultCode = result.getInt(RESPONSE_CODE);
                    continuationToken = null;
                    if(result.containsKey("INAPP_CONTINUATION_TOKEN")) {
                        continuationToken = result.getString("INAPP_CONTINUATION_TOKEN");
                    }
                    if (resultCode == BILLING_RESPONSE_RESULT_OK) {
                        ArrayList<String> productIds = result.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                        ArrayList<String> purchaseData = result.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                        ArrayList<String> signatures = result.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");

                        for(int i=0, n=productIds.size(); i<n; i++) {
                            String data = purchaseData.get(i);
                            String signature = signatures.get(i);
                            IAPPurchaseModel item = mGson.fromJson(data, IAPPurchaseModel.class);
                            item.signedData = data;
                            item.productSignature = signature;
                            mPurchaseItems.add(item);
                        }
                    }
                } while(continuationToken != null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return resultCode;
        }

        @Override
        protected void onPostExecute(Integer code) {
            for (IAPProcessListener listener : mListeners) {
                listener.onProcessing(METHOD_GET_PURCHASED, IAPProcessListener.ProcessState.PostExecute);
                listener.onPurchasedItemLoaded(code, mPurchaseItems);
            }
        }
    }

    private class IAP_GetItemDetails extends AsyncTask<Bundle, Void, Integer> {
        ArrayList<IAPItemDetailModel> mItemDetails;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            for (IAPProcessListener listener : mListeners) {
                listener.onProcessing(METHOD_GET_DETAILS, IAPProcessListener.ProcessState.PreExecute);
            }
        }

        @Override
        protected Integer doInBackground(Bundle... bundles) {
            int resultCode = BILLING_RESPONSE_RESULT_SERVER_NOT_RESPONSE;
            Bundle itemIdList = bundles[0];
            mItemDetails = new ArrayList<>();

            do {
                // Wait until Helper ready
            } while(mIAPService == null);

            try {
                Bundle result = mIAPService.getSkuDetails(IAP_VERSION, mActivity.getPackageName(), IAP_INAPP_TYPE, itemIdList);
                resultCode = result.getInt(RESPONSE_CODE);
                if(result.containsKey(IAP_BUNDLE_ITEM_DETAIL)) {
                    ArrayList<String> itemDetailsJson = result.getStringArrayList(IAP_BUNDLE_ITEM_DETAIL);
                    Log.d(AppPreference.DEBUG_APP, "IAP Details: " + itemDetailsJson.toString());
                    for(String itemJson : itemDetailsJson) {
                        IAPItemDetailModel model = mGson.fromJson(itemJson, IAPItemDetailModel.class);
                        mItemDetails.add(model);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            return resultCode;
        }

        @Override
        protected void onPostExecute(Integer code) {
            for (IAPProcessListener listener : mListeners) {
                listener.onProcessing(METHOD_GET_DETAILS, IAPProcessListener.ProcessState.PostExecute);
                listener.onItemDetailsLoaded(code, mItemDetails);
            }
        }
    }
}
