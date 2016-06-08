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
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;
import com.google.gson.Gson;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.exception.IAPException;
import com.whitefly.plutocrat.models.IAPPurchaseModel;

import java.util.UUID;

/**
 * Created by Satjapot on 6/4/16 AD.
 */
public class IAPHelper {
    private static final int IAP_VERSION = 3;
    private static final int IAP_REQUEST_CODE = 50002;
    private static final String IAP_GOOGLE_INTENT = "com.android.vending.billing.InAppBillingService.BIND";
    private static final String IAP_GOOGLE_PACKAGE = "com.android.vending";
    private static final String IAP_INAPP_TYPE = "inapp";

    private static final int BILLING_RESPONSE_RESULT_OK = 0;
    private static final int BILLING_RESPONSE_RESULT_USER_CANCELED = 1;
    private static final int BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE = 2;
    private static final int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3;
    private static final int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4;
    private static final int BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5;
    private static final int BILLING_RESPONSE_RESULT_ERROR = 6;
    private static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;
    private static final int BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8;

    private static final int BILLING_RESPONSE_RESULT_ACTIVIY_ERROR = 101;
    private static final int BILLING_RESPONSE_RESULT_INVALID_PAYLOAD= 102;

    // Attributes
    private Context mActivity;
    private IAPProcessListener mListener;
    private IInAppBillingService mIAPService;
    private Gson mGson;
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

        // Create IAP connection
        Intent serviceIntent =
                new Intent(IAP_GOOGLE_INTENT);
        serviceIntent.setPackage(IAP_GOOGLE_PACKAGE);
        mActivity.bindService(serviceIntent, mIAPServiceConnection, Context.BIND_AUTO_CREATE);
    }

    // Methods
    public void setIAPProcessListener(IAPProcessListener listener) {
        mListener = listener;
    }

    public void onDestroy() {
        if(mIAPService != null) {
            mActivity.unbindService(mIAPServiceConnection);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == IAP_REQUEST_CODE) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == Activity.RESULT_OK) {
                Log.d(AppPreference.DEBUG_APP, "purchaseData: " + purchaseData);
                Log.d(AppPreference.DEBUG_APP, "signatureData: " + dataSignature);
                if(responseCode == BILLING_RESPONSE_RESULT_OK) {
                    IAPPurchaseModel model = mGson.fromJson(purchaseData, IAPPurchaseModel.class);
                    if(model.developerPayload == mLastDeveloperPayload) {
                        if (mListener != null) {
                            mListener.onBuySuccess(responseCode, model);
                        }
                    } else {
                        if (mListener != null) {
                            mListener.onBuyFailed(BILLING_RESPONSE_RESULT_INVALID_PAYLOAD);
                        }
                    }
                } else {
                    if(mListener != null) {
                        mListener.onBuyFailed(responseCode);
                    }
                }
            } else {
                Log.d(AppPreference.DEBUG_APP, "dataSignature: " + dataSignature);
                if(mListener != null) {
                    mListener.onBuyFailed(BILLING_RESPONSE_RESULT_ACTIVIY_ERROR);
                }
            }
        }
    }

    public void buy(String sku, String payload) throws IAPException {
        if(sku == null) {
            sku = "android.test.purchased";
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
        new IAP_ConsumeCallback().execute(purchaseToken);
    }

    /*
    Inner Class
     */
    public interface IAPProcessListener {
        void onBuySuccess(int resultCode, IAPPurchaseModel model);
        void onBuyFailed(int resultCode);
        void onConsumed(int resultCode);
    }

    private class IAP_ConsumeCallback extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            Integer resultCode = -1;
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
            if(mListener != null) {
                mListener.onConsumed(code);
            }
        }
    }
}
