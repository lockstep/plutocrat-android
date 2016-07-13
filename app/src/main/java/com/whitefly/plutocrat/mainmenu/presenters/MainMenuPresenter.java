package com.whitefly.plutocrat.mainmenu.presenters;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.squareup.otto.Subscribe;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.exception.APIConnectionException;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.helpers.HttpClient;
import com.whitefly.plutocrat.mainmenu.events.AttackTimeOutEvent;
import com.whitefly.plutocrat.mainmenu.events.BuySharesEvent;
import com.whitefly.plutocrat.mainmenu.events.EnablePushNotificationEvent;
import com.whitefly.plutocrat.mainmenu.events.EngageClickEvent;
import com.whitefly.plutocrat.mainmenu.events.ExecuteShareEvent;
import com.whitefly.plutocrat.mainmenu.events.FailMatchBuyoutEvent;
import com.whitefly.plutocrat.mainmenu.events.GetPlutocratEvent;
import com.whitefly.plutocrat.mainmenu.events.LoadBuyoutsEvent;
import com.whitefly.plutocrat.mainmenu.events.LoadTargetsEvent;
import com.whitefly.plutocrat.mainmenu.events.MatchBuyoutEvent;
import com.whitefly.plutocrat.mainmenu.events.MoreShareClickEvent;
import com.whitefly.plutocrat.mainmenu.events.SaveAccountSettingsEvent;
import com.whitefly.plutocrat.mainmenu.events.SaveImageProfileEvent;
import com.whitefly.plutocrat.mainmenu.events.SendReceiptCompleteEvent;
import com.whitefly.plutocrat.mainmenu.events.SendReceiptEvent;
import com.whitefly.plutocrat.mainmenu.events.SetHomeStateEvent;
import com.whitefly.plutocrat.mainmenu.events.SignOutEvent;
import com.whitefly.plutocrat.mainmenu.events.UpdateSettingsEvent;
import com.whitefly.plutocrat.mainmenu.events.UpdateUserNoticeIdEvent;
import com.whitefly.plutocrat.mainmenu.fragments.HomeFragment;
import com.whitefly.plutocrat.mainmenu.views.IAccountSettingView;
import com.whitefly.plutocrat.mainmenu.views.IHomeView;
import com.whitefly.plutocrat.mainmenu.views.IMainMenuView;
import com.whitefly.plutocrat.mainmenu.views.events.LoadBuyoutCompletedEvent;
import com.whitefly.plutocrat.mainmenu.views.events.LoadPlutocratCompletedEvent;
import com.whitefly.plutocrat.mainmenu.views.events.LoadTargetCompletedEvent;
import com.whitefly.plutocrat.mainmenu.views.events.MatchBuyoutCompletedEvent;
import com.whitefly.plutocrat.mainmenu.views.events.UpdateHomeViewEvent;
import com.whitefly.plutocrat.models.BuyoutModel;
import com.whitefly.plutocrat.models.IAPPurchaseModel;
import com.whitefly.plutocrat.models.MetaModel;
import com.whitefly.plutocrat.models.NewBuyoutModel;
import com.whitefly.plutocrat.models.ShareBundleModel;
import com.whitefly.plutocrat.models.TargetModel;
import com.whitefly.plutocrat.models.UserModel;
import com.whitefly.plutocrat.services.CloudMessageInstantIdService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Headers;

/**
 * Created by Satjapot on 5/12/16 AD.
 */
public class MainMenuPresenter {
    private static final int FIRST_PAGE = 1;

    // Attributes
    private Context mContext;
    private HttpClient mHttp;

    // Views
    private IMainMenuView mMainMenuView;
    private IHomeView mHomeView;

    // Constructor
    public MainMenuPresenter(Context context, IMainMenuView mmView, IHomeView homeView) {
        mContext = context;
        mHttp = new HttpClient(context);
        mMainMenuView = mmView;
        mHomeView = homeView;
    }

    // Methods
    public void setHttpClient(HttpClient client) {
        mHttp = client;
    }

    @Subscribe
    public void onGetPlutocratEvent(GetPlutocratEvent event) {
        TargetModel plutocrat = AppPreference.getInstance().getSession().getPlutocrat();
        EventBus.getInstance().post(new LoadPlutocratCompletedEvent(plutocrat));
    }

    @Subscribe
    public void onSetHomeState(SetHomeStateEvent event) {
        HomeFragment.State state = HomeFragment.State.Default;
        UserModel userModel = AppPreference.getInstance().getSession().getActiveUser();

        if (userModel.defeatedAt != null) {
            state = HomeFragment.State.Suspend;
        } else if (userModel.isUnderBuyoutThreat && userModel.activeInboundBuyout != null) {
            state = HomeFragment.State.Threat;
        }

        EventBus.getInstance().post(new UpdateHomeViewEvent(state, userModel.userNoticeId));
    }

    @Subscribe
    public void onUpdateUserNoticeId(UpdateUserNoticeIdEvent event) {
        UserModel userModel = AppPreference.getInstance().getSession().getActiveUser();
        userModel.userNoticeId = event.getNextNoticeId();

        AppPreference.getInstance().getCurrentUserPersistence().noticeId = userModel.userNoticeId;
        AppPreference.getInstance().saveUserPersistence();

        onSetHomeState(new SetHomeStateEvent());
    }

    @Subscribe
    public void onEnablePushNotification(EnablePushNotificationEvent event) {
        String fcmToken = AppPreference.getInstance().getSharedPreference().getString(
                AppPreference.PREFKEY_FCM_TOKEN, FirebaseInstanceId.getInstance().getToken());
        if(fcmToken != null) {
            new SaveRegisterTokenCallback().execute(fcmToken);
            AsyncTaskCompat.executeParallel(new SaveRegisterTokenCallback(), fcmToken);
        }
    }

    @Subscribe
    public void onSiginOut(SignOutEvent event) {
        AsyncTaskCompat.executeParallel(new SignOutCallback(), (Void) null);
    }

    @Subscribe
    public void onLoadTargets(LoadTargetsEvent event) {
        AsyncTaskCompat.executeParallel(new LoadTargetBuyoutCallBack(), event);
    }

    @Subscribe
    public void onLoadBuyouts(LoadBuyoutsEvent event) {
        AsyncTaskCompat.executeParallel(new LoadBuyoutsCallBack(), event);
    }

    @Subscribe
    public void onEngageClick(EngageClickEvent event) {
        AppPreference.getInstance().setCurrentTarget(event.getTargetModel());
        AsyncTaskCompat.executeParallel(new GetNewBuyoutCallback(), event.getTargetModel());
    }

    @Subscribe
    public void onMoreShareClick(MoreShareClickEvent event) {
        mMainMenuView.goToShareFromInitiate();
    }

    @Subscribe
    public void onBuyShares(BuySharesEvent event) {
        ShareBundleModel model = event.getBundleDetail();

        if(model.state == ShareBundleModel.State.Get) {
            if(event.getBundleDetail().purchaseData != null) {
                EventBus.getInstance().post(new SendReceiptEvent(event.getBundleDetail().purchaseData));
            }
        } else if(model.state == ShareBundleModel.State.Buy) {
            mMainMenuView.buyIAP(model.sku, null);
        }
    }

    @Subscribe
    public void onSendReceipt(SendReceiptEvent event) {
        AsyncTaskCompat.executeParallel(new SendReceiptCallback(), event.getPurchasedItem());
    }

    @Subscribe
    public void onSaveAccountSettings(SaveAccountSettingsEvent event) {
        AsyncTaskCompat.executeParallel(new SaveAccountSettingCallback(), event);
    }

    @Subscribe
    public void onSaveImageProfile(SaveImageProfileEvent event) {
        AsyncTaskCompat.executeParallel(new SaveImageProfileCallback(), event);
    }

    @Subscribe
    public void onExecuteBuyout(ExecuteShareEvent event) {
        AsyncTaskCompat.executeParallel(new ExecuteBuyoutCallback(), event);
    }

    @Subscribe
    public void onMatchBuyout(MatchBuyoutEvent event) {
        AsyncTaskCompat.executeParallel(new MatchBuyoutCallback(), (Void) null);
    }

    @Subscribe
    public void onFailMatchBuyout(FailMatchBuyoutEvent event) {
        AsyncTaskCompat.executeParallel(new FailMatchBuyoutCallback(), (Void) null);
    }

    @Subscribe
    public void onAttackTimeOut(AttackTimeOutEvent event) {
        AsyncTaskCompat.executeParallel(new AttackTimeOutCallback(), (Void) null);
    }

    /*
    Request Callback
     */
    private class SignOutCallback extends AsyncTask<Void, Void, Boolean> {
        private String mErrorMessage;

        @Override
        protected void onPreExecute() {
            mMainMenuView.handleLoadingDialog(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;
            Headers headers = AppPreference.getInstance().getSession().getHeaders();

            // Call api
            try {
                mHttp.header(headers).delete(R.string.api_signout);
                result = mHttp.getResponse().isSuccessful();
            } catch (IOException e) {
                e.printStackTrace();
                mErrorMessage = mContext.getString(R.string.error_connection);
            } catch (APIConnectionException e) {
                e.printStackTrace();
                MetaModel model = new MetaModel(e.getMessage());
                mErrorMessage = model.getErrors();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            mMainMenuView.handleLoadingDialog(false);
            if(b) {
                AppPreference.getInstance().getSession().destroy();
                mMainMenuView.goToLogin();
            } else {
                mMainMenuView.handleError(mContext.getString(R.string.error_title_cannot_sign_out),
                        mErrorMessage, null);
            }
        }
    }

    private class LoadTargetBuyoutCallBack extends AsyncTask<LoadTargetsEvent, Void, ArrayList<TargetModel>> {
        private MetaModel mMetaModel;
        private String mErrorMessage;

        @Override
        protected ArrayList<TargetModel> doInBackground(LoadTargetsEvent... params) {
            Gson gson = AppPreference.getInstance().getGson();
            ArrayList<TargetModel> result = new ArrayList<>();
            Headers headers = AppPreference.getInstance().getSession().getHeaders();
            int page = params[0].page;
            String requestParam = String.format("{page:%d}", page);

            try {
                String jsonBody = mHttp.header(headers).request(requestParam).get(R.string.api_targets);
                JSONObject root = new JSONObject(jsonBody);
                mMetaModel = new MetaModel(root.getJSONObject("meta"));
                JSONArray users = root.getJSONArray("users");

                if(users.length() > 0) {
                    for (int i = 0, n = users.length(); i < n; i++) {
                        TargetModel model = gson.fromJson(users.get(i).toString(), TargetModel.class);
                        result.add(model);

                        if(page == FIRST_PAGE && i == 0) {
                            AppPreference.getInstance().getSession()
                                    .savePlutocrat(model.isPlutocrat ? model : null);
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                mErrorMessage = mContext.getString(R.string.error_connection);
            } catch (APIConnectionException e) {
                e.printStackTrace();
                mMetaModel = new MetaModel(e.getMessage());
                mErrorMessage = mMetaModel.getErrors();
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<TargetModel> targetModels) {
            if(targetModels.size() > 0) {
                targetModels.add(null);
            }

            if(mMetaModel != null && mMetaModel.getInt("current_page") == FIRST_PAGE) {
                TargetModel plutocrat = AppPreference.getInstance().getSession().getPlutocrat();
                if(plutocrat != null) {
                    targetModels.remove(0);
                }
                EventBus.getInstance().post(new LoadPlutocratCompletedEvent(plutocrat));
            } else if(mErrorMessage != null) {
                mMainMenuView.handleError(mContext.getString(R.string.error_title_cannot_load_targets),
                        mErrorMessage, null);
            }

            EventBus.getInstance().post(new LoadTargetCompletedEvent(targetModels, mMetaModel));
        }
    }

    private class LoadBuyoutsCallBack extends AsyncTask<LoadBuyoutsEvent, Void, ArrayList<BuyoutModel>> {
        private MetaModel mMetaModel;
        private String mErrorMessage;

        @Override
        protected ArrayList<BuyoutModel> doInBackground(LoadBuyoutsEvent... params) {
            Gson gson = AppPreference.getInstance().getGson();
            ArrayList<BuyoutModel> result = new ArrayList<>();
            Headers headers = AppPreference.getInstance().getSession().getHeaders();
            int page = params[0].page;
            String requestParam = String.format("{page:%d}", page);
            String url = String.format(mContext.getString(R.string.api_get_buyouts),
                    AppPreference.getInstance().getSession().getActiveUser().id);

            try {
                String jsonBody = mHttp.header(headers).request(requestParam).get(url);
                JSONObject root = new JSONObject(jsonBody);
                mMetaModel = new MetaModel(root.getJSONObject("meta"));
                JSONArray buyouts = root.getJSONArray("buyouts");

                if(buyouts.length() > 0) {
                    for (int i = 0, n = buyouts.length(); i < n; i++) {
                        JSONObject row = buyouts.getJSONObject(i);

                        BuyoutModel model = gson.fromJson(row.toString(), BuyoutModel.class);
                        model.initiatingUser = gson.fromJson(row.getString("initiating_user"), TargetModel.class);
                        model.targetUser = gson.fromJson(row.getString("target_user"), TargetModel.class);

                        result.add(model);
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                mErrorMessage = mContext.getString(R.string.error_connection);
            } catch (APIConnectionException e) {
                e.printStackTrace();
                mMetaModel = new MetaModel(e.getMessage());
                mErrorMessage = mMetaModel.getErrors();
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<BuyoutModel> buyoutModels) {
            if(buyoutModels.size() > 0) {
                buyoutModels.add(null);
            }
            EventBus.getInstance().post(new LoadBuyoutCompletedEvent(buyoutModels, mMetaModel));

            if (mErrorMessage != null) {
                mMainMenuView.handleError(mContext.getString(R.string.error_title_cannot_load_buyouts),
                        mErrorMessage, mMetaModel);
            }
        }
    }

    private class GetNewBuyoutCallback extends AsyncTask<TargetModel, Void, NewBuyoutModel> {
        private TargetModel mTarget;
        private MetaModel mMetaError;
        private String mErrorMessage = null;

        @Override
        protected void onPreExecute() {
            mMainMenuView.handleLoadingDialog(true);
        }

        @Override
        protected NewBuyoutModel doInBackground(TargetModel... params) {
            Gson gson = AppPreference.getInstance().getGson();

            NewBuyoutModel result = null;
            mTarget = params[0];
            UserModel currentUser = AppPreference.getInstance().getSession().getActiveUser();
            Headers headers = AppPreference.getInstance().getSession().getHeaders();

            String url = String.format(mContext.getString(R.string.api_newbuyout), mTarget.id);
            String requestParam = String.format("{\"initiating_user_id\":%d}", mTarget.id);

            try {
                String bodyString = mHttp.header(headers).request(requestParam).get(url);
                JSONObject jsonBody = new JSONObject(bodyString);
                result = gson.fromJson(jsonBody.getString("new_buyout"), NewBuyoutModel.class);
            } catch (IOException e) {
                e.printStackTrace();
                mErrorMessage = mContext.getString(R.string.error_connection);
            } catch (JSONException e) {
                e.printStackTrace();
                mErrorMessage = mContext.getString(R.string.error_connection);
            } catch (APIConnectionException e) {
                e.printStackTrace();
                mMetaError = new MetaModel(e.getMessage());
                mErrorMessage = mMetaError.getErrors();
            }

            return result;
        }

        @Override
        protected void onPostExecute(NewBuyoutModel model) {
            mMainMenuView.handleLoadingDialog(false);
            if(mErrorMessage != null) {
                mMainMenuView.handleError(mContext.getString(R.string.error_title_cannot_new_buyout),
                        mErrorMessage, mMetaError);
            } else {
                mMainMenuView.callInitiateDialog(mTarget, model);
            }
        }
    }

    private class ExecuteBuyoutCallback extends AsyncTask<ExecuteShareEvent, Void, String> {
        private Gson mGson;
        private String mErrorMessage = null;
        private MetaModel mMetaError = null;

        public ExecuteBuyoutCallback() {
            mGson = AppPreference.getInstance().getGson();
        }

        @Override
        protected void onPreExecute() {
            mMainMenuView.handleLoadingDialog(true);
        }

        @Override
        protected String doInBackground(ExecuteShareEvent... params) {
            ExecuteShareEvent param = params[0];
            String result = null;
            Headers headers = AppPreference.getInstance().getSession().getHeaders();
            UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();

            String url = String.format(mContext.getString(R.string.api_execute_buyout), param.getInitiateUserId());
            String getProfileURL = String.format(mContext.getString(R.string.api_profile), activeUser.id);

            try {
                JSONObject requestJson = new JSONObject();
                requestJson.put("number_of_shares", param.getNumberOfShare());
                String requestString = requestJson.toString();

                result = mHttp.header(headers).request(requestString).post(url);

                String userUpdateResponse = mHttp.header(headers).get(getProfileURL);
                AppPreference.getInstance().getSession().updateUserJson(userUpdateResponse);
            } catch (IOException e) {
                e.printStackTrace();
                mErrorMessage = mContext.getString(R.string.error_connection);
            } catch (JSONException e) {
                e.printStackTrace();
                mErrorMessage = mContext.getString(R.string.error_connection);
            } catch (APIConnectionException e) {
                e.printStackTrace();
                mMetaError = new MetaModel(e.getMessage());
                mErrorMessage = mMetaError.getErrors();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String response) {
            mMainMenuView.handleLoadingDialog(false);
            if(mErrorMessage != null) {
                mMainMenuView.handleError(mContext.getString(R.string.error_title_cannot_execute_buyout),
                        mErrorMessage, mMetaError);
            } else {
                try {
                    JSONObject rootJson = new JSONObject(response);
                    JSONObject buyoutJson = rootJson.getJSONObject("buyout");
                    TargetModel targetUser = mGson.fromJson(buyoutJson.getString("target_user"),
                            TargetModel.class);

                    TargetModel currentTarget = AppPreference.getInstance().getCurrentTarget();
                    currentTarget.isUnderBuyoutThreat = targetUser.isUnderBuyoutThreat;
                    currentTarget.numSuccessfulBuyout = targetUser.numSuccessfulBuyout;
                    currentTarget.numMatchedBuyout = targetUser.numMatchedBuyout;
                    currentTarget.numAvailableShares = targetUser.numAvailableShares;
                    AppPreference.getInstance().setCurrentTarget(null);

                    mMainMenuView.toast(mContext.getString(R.string.caption_buyout_initiated));
                    mMainMenuView.closeInitiatePage(response != null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MatchBuyoutCallback extends AsyncTask<Void, Void, TargetModel> {
        private String mErrorMessage = null;
        private MetaModel mMetaError = null;

        @Override
        protected void onPreExecute() {
            mMainMenuView.handleLoadingDialog(true);
        }

        @Override
        protected TargetModel doInBackground(Void... params) {
            TargetModel result = null;
            Gson gson = AppPreference.getInstance().getGson();
            UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();
            Headers headers = AppPreference.getInstance().getSession().getHeaders();

            String url = String.format(mContext.getString(R.string.api_match_buyout), activeUser.activeInboundBuyout.id);

            try {
                String response = mHttp.header(headers).patch(url);

                JSONObject userJson = new JSONObject(response);

                result = gson.fromJson(userJson.getString("user"), TargetModel.class);
            } catch (IOException e) {
                e.printStackTrace();
                mErrorMessage = mContext.getString(R.string.error_connection);
            } catch (APIConnectionException e) {
                e.printStackTrace();
                mMetaError = new MetaModel(e.getMessage());
                mErrorMessage = mMetaError.getErrors();
            } catch (JSONException e) {
                mErrorMessage = mContext.getString(R.string.error_connection);
            }

            return result;
        }

        @Override
        protected void onPostExecute(TargetModel user) {
            mMainMenuView.handleLoadingDialog(false);

            if (mErrorMessage != null) {
                mMainMenuView.handleError(mContext.getString(R.string.error_title_cannot_match_shares),
                        mErrorMessage, mMetaError);
            } else {
                AppPreference.getInstance().getSession().updateInitiatingUser(null);
                UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();
                activeUser.updateTargetData(user);
                activeUser.activeInboundBuyout = null;

                EventBus.getInstance().post(new MatchBuyoutCompletedEvent(MatchBuyoutCompletedEvent.Result.Matched));
            }
        }
    }

    private class FailMatchBuyoutCallback extends AsyncTask<Void, Void, Void> {
        private String mErrorMessage = null;
        private MetaModel mMetaError = null;

        @Override
        protected void onPreExecute() {
            mMainMenuView.handleLoadingDialog(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();
            Headers headers = AppPreference.getInstance().getSession().getHeaders();

            String url = String.format(mContext.getString(R.string.api_fail_buyout), activeUser.activeInboundBuyout.id);

            try {
                String response = mHttp.header(headers).patch(url);

                AppPreference.getInstance().getSession().updateUserJson(response);

                activeUser = AppPreference.getInstance().getSession().getActiveUser();
                if(activeUser.terminalBuyout == null) {
                    mErrorMessage = "Cannot get terminal buyout";
                } else {
                    AppPreference.getInstance().getSession().updateTerminalUser(activeUser.terminalBuyout.initiatingUser);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                mErrorMessage = mContext.getString(R.string.error_connection);
            } catch (APIConnectionException e) {
                e.printStackTrace();
                mMetaError = new MetaModel(e.getMessage());
                mErrorMessage = mMetaError.getErrors();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mMainMenuView.handleLoadingDialog(false);

            if (mErrorMessage != null) {
                mMainMenuView.handleError(mContext.getString(R.string.error_title_cannot_execute_buyout),
                        mErrorMessage, mMetaError);
            } else {
                AppPreference.getInstance().getSession().updateInitiatingUser(null);

                EventBus.getInstance().post(new MatchBuyoutCompletedEvent(MatchBuyoutCompletedEvent.Result.Failed));
            }
        }
    }

    private class AttackTimeOutCallback extends AsyncTask<Void, Void, Void> {
        private String mErrorMessage = null;
        private MetaModel mMetaError = null;

        @Override
        protected void onPreExecute() {
            mMainMenuView.handleLoadingDialog(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();
            Headers headers = AppPreference.getInstance().getSession().getHeaders();

            String url = String.format(mContext.getString(R.string.api_profile), activeUser.id);

            try {
                String response = mHttp.header(headers).get(url);

                AppPreference.getInstance().getSession().updateUserJson(response);
            } catch (IOException e) {
                e.printStackTrace();
                mErrorMessage = mContext.getString(R.string.error_connection);
            } catch (APIConnectionException e) {
                e.printStackTrace();
                mMetaError = new MetaModel(e.getMessage());
                mErrorMessage = mMetaError.getErrors();
            } catch (JSONException e) {
                mErrorMessage = mContext.getString(R.string.error_connection);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mMainMenuView.handleLoadingDialog(false);

            if (mErrorMessage != null) {
                mMainMenuView.handleError(mContext.getString(R.string.error_connection),
                        mErrorMessage, mMetaError);
            } else {
                EventBus.getInstance().post(new MatchBuyoutCompletedEvent(MatchBuyoutCompletedEvent.Result.Failed));
            }
        }
    }

    private class SaveAccountSettingCallback extends AsyncTask<SaveAccountSettingsEvent, Void, Boolean> {
        private String mErrorMessage = null;
        private MetaModel mMetaError = null;
        private IAccountSettingView mResponseView;

        @Override
        protected void onPreExecute() {
            mMainMenuView.handleLoadingDialog(true);
        }

        @Override
        protected Boolean doInBackground(SaveAccountSettingsEvent... params) {
            Boolean result = false;
            SaveAccountSettingsEvent param = params[0];
            Gson gson = AppPreference.getInstance().getGson();
            UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();
            Headers headers = AppPreference.getInstance().getSession().getHeaders();
            mResponseView = param.getResponseView();

            String url = String.format(mContext.getString(R.string.api_save_settings), activeUser.id);

            try {
                String requestString = gson.toJson(param);
                JSONObject requestJson = new JSONObject(requestString);
                if(param.getNewPassword() == null) {
                    requestJson.remove("password");
                    requestJson.remove("current_password");
                }

                mHttp.header(headers).request(requestJson.toString());
                if(param.getProfilePicture() != null) {
                    mHttp.addMultipartImage("profile_image", param.getProfilePicture());
                }

                String response = mHttp.patch(url);

                JSONObject bodyJson = new JSONObject(response);
                if(! bodyJson.isNull("meta")) {
                    throw new APIConnectionException(response);
                }

                AppPreference.getInstance().getSession().updateUserJson(response);
                activeUser.email = param.getEmail();
                activeUser.isTransactionalEmailsEnabled = param.isTransactionEmailEnabled();
                activeUser.isProductEmailsEnabled = param.isProductEmailEnabled();

                result = true;
            } catch (IOException e) {
                e.printStackTrace();
                mErrorMessage = mContext.getString(R.string.error_connection);
            } catch (APIConnectionException e) {
                e.printStackTrace();
                mMetaError = new MetaModel(e.getMessage());
                mErrorMessage = mMetaError.getErrors();
            } catch (JSONException e) {
                mErrorMessage = mContext.getString(R.string.error_connection);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            mMainMenuView.handleLoadingDialog(false);

            if (mMetaError != null) {
                mResponseView.handleError(mMetaError);
            } else if(mErrorMessage != null) {
                mMainMenuView.handleError(mContext.getString(R.string.error_title_cannot_save_settings),
                        mErrorMessage, mMetaError);
            } else {
                mMainMenuView.toast(mContext.getString(R.string.save_successfully));
                EventBus.getInstance().post(new SetHomeStateEvent());
                EventBus.getInstance().post(new UpdateSettingsEvent());
            }
        }
    }

    private class SaveImageProfileCallback extends AsyncTask<SaveImageProfileEvent, Void, Boolean> {
        private String mErrorMessage = null;
        private MetaModel mMetaError = null;
        private IAccountSettingView mResponseView;

        @Override
        protected void onPreExecute() {
            mMainMenuView.handleLoadingDialog(true);
        }

        @Override
        protected Boolean doInBackground(SaveImageProfileEvent... params) {
            Boolean result = false;
            SaveImageProfileEvent param = params[0];
            Gson gson = AppPreference.getInstance().getGson();
            UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();
            Headers headers = AppPreference.getInstance().getSession().getHeaders();
            mResponseView = param.getResponseView();

            String url = String.format(mContext.getString(R.string.api_save_settings), activeUser.id);

            try {
                mHttp.header(headers);
                if(param.getProfilePicture() != null) {
                    mHttp.addMultipartImage("profile_image", param.getProfilePicture());
                }

                String response = mHttp.patch(url);

                JSONObject bodyJson = new JSONObject(response);
                if(! bodyJson.isNull("meta")) {
                    throw new APIConnectionException(response);
                }

                AppPreference.getInstance().getSession().updateUserJson(response);

                result = true;
            } catch (IOException e) {
                e.printStackTrace();
                mErrorMessage = mContext.getString(R.string.error_connection);
            } catch (APIConnectionException e) {
                e.printStackTrace();
                mMetaError = new MetaModel(e.getMessage());
                mErrorMessage = mMetaError.getErrors();
            } catch (JSONException e) {
                mErrorMessage = mContext.getString(R.string.error_connection);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            mMainMenuView.handleLoadingDialog(false);

            if (mMetaError != null) {
                mResponseView.handleError(mMetaError);
            } else if(mErrorMessage != null) {
                mMainMenuView.handleError(mContext.getString(R.string.error_title_cannot_save_settings),
                        mErrorMessage, mMetaError);
            } else {
                EventBus.getInstance().post(new SetHomeStateEvent());
                EventBus.getInstance().post(new UpdateSettingsEvent());
            }
        }
    }

    private class SaveRegisterTokenCallback extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String token = params[0];
            Headers headers = AppPreference.getInstance().getSession().getHeaders();
            int LastUserId = AppPreference.getInstance().getSession().user_id;
            String apiUrl = String.format(mContext.getString(R.string.api_profile), LastUserId);

            try {
                JSONObject requestJson = new JSONObject();
                JSONArray dataArrayJson = new JSONArray();
                JSONObject dataTokenJson = new JSONObject();
                dataTokenJson.put(CloudMessageInstantIdService.FIELD_NAME_TOKEN, token);
                dataTokenJson.put(CloudMessageInstantIdService.FIELD_NAME_PLATFORM, AppPreference.PLATFORM);
                dataArrayJson.put(dataTokenJson);
                requestJson.put(CloudMessageInstantIdService.FIELD_NAME_DEVICE_ATTRIBUTES, dataArrayJson);

                mHttp.header(headers).request(requestJson.toString()).patch(apiUrl);
            } catch (IOException | APIConnectionException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private class SendReceiptCallback extends AsyncTask<IAPPurchaseModel, Void, IAPPurchaseModel> {
        private String mErrorMessage = null;
        private MetaModel mMetaError = null;

        @Override
        protected void onPreExecute() {
            mMainMenuView.handleLoadingDialog(true);
        }

        @Override
        protected IAPPurchaseModel doInBackground(IAPPurchaseModel... params) {
            Headers headers = AppPreference.getInstance().getSession().getHeaders();
            int userId = AppPreference.getInstance().getSession().getActiveUser().id;
            String apiUrl = String.format(mContext.getString(R.string.api_send_receipt), userId);

            IAPPurchaseModel param = params[0];

            try {
                JSONObject requestJson = new JSONObject();
                requestJson.put("type", AppPreference.PLATFORM);
                requestJson.put("purchase_token", param.purchaseToken);
                requestJson.put("product_identifier", param.productId);
                requestJson.put("transaction_identifier", param.orderId);

                Log.d(AppPreference.DEBUG_APP, "Request query: " + requestJson.toString());

                String response = mHttp.header(headers).request(requestJson.toString()).post(apiUrl);

                JSONObject bodyJson = new JSONObject(response);
                if(! bodyJson.isNull("meta")) {
                    throw new APIConnectionException(response);
                }

                AppPreference.getInstance().getSession().updateUserJson(response);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                mErrorMessage = mContext.getString(R.string.error_buy_share);
            } catch (APIConnectionException e) {
                e.printStackTrace();
                mMetaError = new MetaModel(e.getMessage());
                mErrorMessage = mMetaError.getErrors();
            }

            return param;
        }

        @Override
        protected void onPostExecute(IAPPurchaseModel purchasedItem) {
            mMainMenuView.handleLoadingDialog(false);

            if (mMetaError != null) {
                if(mMetaError.getKeys().contains("")) { // TODO: Set correct key to check duplication error
                    EventBus.getInstance().post(
                            new SendReceiptCompleteEvent(SendReceiptCompleteEvent.State.Duplicated, purchasedItem, mMetaError));
                } else {
                    EventBus.getInstance().post(
                            new SendReceiptCompleteEvent(SendReceiptCompleteEvent.State.Failed, purchasedItem, mMetaError));
                }
            } else if(mErrorMessage != null) {
                JSONObject errorJSON = new JSONObject();
                try {
                    JSONArray errorMessageJSON =  new JSONArray();
                    errorMessageJSON.put(mErrorMessage);

                    JSONObject errorTypeJSON = new JSONObject();
                    errorTypeJSON.put("", errorMessageJSON);

                    errorJSON.put("errors", errorTypeJSON);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mMetaError = new MetaModel(errorJSON);
                EventBus.getInstance().post(
                        new SendReceiptCompleteEvent(SendReceiptCompleteEvent.State.Failed, purchasedItem, mMetaError));
            } else {
                EventBus.getInstance().post(
                        new SendReceiptCompleteEvent(SendReceiptCompleteEvent.State.Succeed, purchasedItem, null));
            }
        }
    }
}
