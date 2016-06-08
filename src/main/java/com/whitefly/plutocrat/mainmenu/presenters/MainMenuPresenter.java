package com.whitefly.plutocrat.mainmenu.presenters;

import android.content.Context;
import android.os.AsyncTask;

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
import com.whitefly.plutocrat.mainmenu.events.CheckNotificationEnableEvent;
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
import com.whitefly.plutocrat.mainmenu.events.SetHomeStateEvent;
import com.whitefly.plutocrat.mainmenu.events.SignOutEvent;
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
    private IAccountSettingView mSettingView;

    // Constructor
    public MainMenuPresenter(Context context, IMainMenuView mmView, IHomeView homeView, IAccountSettingView settingView) {
        mContext = context;
        mHttp = new HttpClient(context);
        mMainMenuView = mmView;
        mHomeView = homeView;
        mSettingView = settingView;
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
//        } else if (userModel.isUnderBuyoutThreat) {
        } else if (userModel.activeInboundBuyout != null) {
            state = HomeFragment.State.Threat;
        }

        EventBus.getInstance().post(new UpdateHomeViewEvent(state, userModel.userNoticeId));
    }

    @Subscribe
    public void onUpdateUserNoticeId(UpdateUserNoticeIdEvent event) {
        HomeFragment.State state = HomeFragment.State.Default;
        UserModel userModel = AppPreference.getInstance().getSession().getActiveUser();
        userModel.userNoticeId = event.getNextNoticeId();

        AppPreference.getInstance().getCurrentUserPersistence().noticeId = userModel.userNoticeId;
        AppPreference.getInstance().saveUserPersistence();

        if (userModel.defeatedAt != null) {
            state = HomeFragment.State.Suspend;
        } else if (userModel.isAttackingCurrentUser) {
            state = HomeFragment.State.Threat;
        }

        mHomeView.changeState(state, userModel.userNoticeId);
    }

    @Subscribe
    public void onEnablePushNotification(EnablePushNotificationEvent event) {
        String fcmToken = AppPreference.getInstance().getSharedPreference().getString(
                AppPreference.PREFKEY_FCM_TOKEN, FirebaseInstanceId.getInstance().getToken());
        if(fcmToken != null) {
            new SaveRegisterTokenCallback().execute(fcmToken);
        }
    }

    @Subscribe
    public void onCheckNotificationEnable(CheckNotificationEnableEvent event) {
        UserModel model = AppPreference.getInstance().getSession().getActiveUser();
        mHomeView.handleNotificationEnable(model.isEnableNotification);
    }

    @Subscribe
    public void onSiginOut(SignOutEvent event) {
        new SignOutCallback().execute();
    }

    @Subscribe
    public void onLoadTargets(LoadTargetsEvent event) {
        new LoadTargetBuyoutCallBack().execute(event);
    }

    @Subscribe
    public void onLoadBuyouts(LoadBuyoutsEvent event) {
        new LoadBuyoutsCallBack().execute(event);
    }

    @Subscribe
    public void onEngageClick(EngageClickEvent event) {
        AppPreference.getInstance().setCurrentTarget(event.getTargetModel());
        new GetNewBuyoutCallback().execute(event.getTargetModel());
    }

    @Subscribe
    public void onMoreShareClick(MoreShareClickEvent event) {
        mMainMenuView.goToShareFromInitiate();
    }

    @Subscribe
    public void onBuyShares(BuySharesEvent event) {
        ShareBundleModel model = event.getBundleDetail();
        String payload = null; // TODO: connect to server for get developerPayload

        mMainMenuView.toast(
                String.format("You are buying %d shares of %f each", model.qty, model.price));
        mMainMenuView.buyIAP(model.sku, payload);
    }

    @Subscribe
    public void onSaveAccountSettings(SaveAccountSettingsEvent event) {
        new SaveAccountSettingCallback().execute(event);
    }

    @Subscribe
    public void onExecuteBuyout(ExecuteShareEvent event) {
        new ExecuteBuyoutCallback().execute(event);
    }

    @Subscribe
    public void onMatchBuyout(MatchBuyoutEvent event) {
        new MatchBuyoutCallback().execute();
    }

    @Subscribe
    public void onFailMatchBuyout(FailMatchBuyoutEvent event) {
        new FailMatchBuyoutCallback().execute();
    }

    @Subscribe
    public void onAttackTimeOut(AttackTimeOutEvent event) {
        new AttackTimeOutCallback().execute();
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
                mMainMenuView.toast("Error: Cannot sign out");
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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
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
            EventBus.getInstance().post(new LoadTargetCompletedEvent(targetModels, mMetaModel));

            if(mMetaModel.getInt("current_page") == FIRST_PAGE) {
                TargetModel plutocrat = AppPreference.getInstance().getSession().getPlutocrat();
                EventBus.getInstance().post(new LoadPlutocratCompletedEvent(plutocrat));
            }
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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (APIConnectionException e) {
                e.printStackTrace();
                MetaModel model = new MetaModel(e.getMessage());
                mErrorMessage = model.getErrors();
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<BuyoutModel> buyoutModels) {
            if (mErrorMessage != null) {
                mMainMenuView.handleError(mContext.getString(R.string.error_title_cannot_execute_buyout),
                        mErrorMessage, mMetaModel);
            } else {
                if(buyoutModels.size() > 0) {
                    buyoutModels.add(null);
                }
                EventBus.getInstance().post(new LoadBuyoutCompletedEvent(buyoutModels, mMetaModel));
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

            String url = String.format(mContext.getString(R.string.api_newbuyout), currentUser.id);
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
            mGson = new Gson();
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

            String url = String.format(mContext.getString(R.string.api_execute_buyout), param.getInitiateUserId());

            try {
                JSONObject requestJson = new JSONObject();
                requestJson.put("number_of_shares", param.getNumberOfShare());
                String requestString = requestJson.toString();

                result = mHttp.header(headers).request(requestString).post(url);
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
                    TargetModel initiatingUser = mGson.fromJson(buyoutJson.getString("initiating_user"),
                            TargetModel.class);
                    TargetModel targetUser = mGson.fromJson(buyoutJson.getString("target_user"),
                            TargetModel.class);

                    UserModel currentUser = AppPreference.getInstance().getSession().getActiveUser();
                    currentUser.numSuccessfulBuyout = initiatingUser.numSuccessfulBuyout;
                    currentUser.numMatchedBuyout = initiatingUser.numMatchedBuyout;
                    currentUser.numAvailableShares = initiatingUser.numAvailableShares;

                    TargetModel currentTarget = AppPreference.getInstance().getCurrentTarget();
                    currentTarget.isUnderBuyoutThreat = targetUser.isUnderBuyoutThreat;
                    currentTarget.numSuccessfulBuyout = targetUser.numSuccessfulBuyout;
                    currentTarget.numMatchedBuyout = targetUser.numMatchedBuyout;
                    currentTarget.numAvailableShares = targetUser.numAvailableShares;
                    AppPreference.getInstance().setCurrentTarget(null);

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

    private class FailMatchBuyoutCallback extends AsyncTask<Void, Void, TargetModel> {
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

            String url = String.format(mContext.getString(R.string.api_fail_buyout), activeUser.activeInboundBuyout.id);

            try {
                String response = mHttp.header(headers).patch(url);

                JSONObject rootJson = new JSONObject(response);

                result = gson.fromJson(rootJson.getString("user"), TargetModel.class);

                JSONObject userJson = new JSONObject(rootJson.getString("user"));
                if(userJson.isNull("terminal_buyout")) {
                    mErrorMessage = "Cannot get terminal buyout";
                } else {
                    activeUser.terminalBuyout =
                            gson.fromJson(userJson.getString("terminal_buyout"), BuyoutModel.class);

                    String terminalUser = mHttp.header(headers)
                            .get(String.format(mHttp.getContext().getString(R.string.api_profile),
                                    activeUser.terminalBuyout.initiatingUserId));

                    JSONObject terminalUserJSON = new JSONObject(terminalUser);
                    TargetModel terminalTarget = gson.fromJson(terminalUserJSON.getString("user"), TargetModel.class);
                    AppPreference.getInstance().getSession().updateTerminalUser(terminalTarget);
                }
            } catch (IOException e) {
                e.printStackTrace();
                mErrorMessage = mContext.getString(R.string.error_connection);
            } catch (APIConnectionException e) {
                e.printStackTrace();
                mMetaError = new MetaModel(e.getMessage());
                mErrorMessage = mMetaError.getErrors();
            } catch (JSONException e) {
                e.printStackTrace();
                mErrorMessage = mContext.getString(R.string.error_connection);
            }

            return result;
        }

        @Override
        protected void onPostExecute(TargetModel user) {
            mMainMenuView.handleLoadingDialog(false);

            if (mErrorMessage != null) {
                mMainMenuView.handleError(mContext.getString(R.string.error_title_cannot_execute_buyout),
                        mErrorMessage, mMetaError);
            } else {
                AppPreference.getInstance().getSession().updateInitiatingUser(null);
                UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();
                activeUser.updateTargetData(user);
                activeUser.activeInboundBuyout = null;

                EventBus.getInstance().post(new MatchBuyoutCompletedEvent(MatchBuyoutCompletedEvent.Result.Failed));
            }
        }
    }

    private class AttackTimeOutCallback extends AsyncTask<Void, Void, TargetModel> {
        private String mErrorMessage = null;
        private MetaModel mMetaError = null;

        @Override
        protected void onPreExecute() {
            mMainMenuView.handleLoadingDialog(true);
        }

        @Override
        protected TargetModel doInBackground(Void... params) {
            TargetModel result = null;
            UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();
            Headers headers = AppPreference.getInstance().getSession().getHeaders();

            String url = String.format(mContext.getString(R.string.api_profile), activeUser.id);

            try {
                String response = mHttp.header(headers).get(url);

                AppPreference.getInstance().getSession().updateUserJson(response, mHttp);
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

                AppPreference.getInstance().getSession().updateUserJson(response, mHttp);
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
                mSettingView.handleError(mMetaError);
            } else if(mErrorMessage != null) {
                mMainMenuView.handleError(mContext.getString(R.string.error_title_cannot_save_settings),
                        mErrorMessage, mMetaError);
            } else {
                mMainMenuView.toast("Save successful.");
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
                dataTokenJson.put(CloudMessageInstantIdService.FIELD_NAME_PLATFORM,
                        CloudMessageInstantIdService.FIELD_VALUE_PLATFORM);
                dataArrayJson.put(dataTokenJson);
                requestJson.put(CloudMessageInstantIdService.FIELD_NAME_DEVICE_ATTRIBUTES, dataArrayJson);

                mHttp.header(headers).request(requestJson.toString()).patch(apiUrl);
            } catch (IOException | APIConnectionException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
