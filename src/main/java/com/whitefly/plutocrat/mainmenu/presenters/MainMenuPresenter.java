package com.whitefly.plutocrat.mainmenu.presenters;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.squareup.otto.Subscribe;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.exception.APIConnectionException;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.HttpClient;
import com.whitefly.plutocrat.mainmenu.events.BuySharesEvent;
import com.whitefly.plutocrat.mainmenu.events.CheckNotificationEnableEvent;
import com.whitefly.plutocrat.mainmenu.events.EngageClickEvent;
import com.whitefly.plutocrat.mainmenu.events.ExecuteShareEvent;
import com.whitefly.plutocrat.mainmenu.events.GetPlutocratEvent;
import com.whitefly.plutocrat.mainmenu.events.LoadBuyoutsEvent;
import com.whitefly.plutocrat.mainmenu.events.LoadTargetsEvent;
import com.whitefly.plutocrat.mainmenu.events.MoreShareClickEvent;
import com.whitefly.plutocrat.mainmenu.events.SaveAccountSettingsEvent;
import com.whitefly.plutocrat.mainmenu.events.SetHomeStateEvent;
import com.whitefly.plutocrat.mainmenu.events.SignOutEvent;
import com.whitefly.plutocrat.mainmenu.events.UpdateUserNoticeIdEvent;
import com.whitefly.plutocrat.mainmenu.fragments.HomeFragment;
import com.whitefly.plutocrat.mainmenu.views.IBuyoutView;
import com.whitefly.plutocrat.mainmenu.views.IHomeView;
import com.whitefly.plutocrat.mainmenu.views.IMainMenuView;
import com.whitefly.plutocrat.mainmenu.views.ITargetView;
import com.whitefly.plutocrat.models.BuyoutModel;
import com.whitefly.plutocrat.models.MetaModel;
import com.whitefly.plutocrat.models.NewBuyoutModel;
import com.whitefly.plutocrat.models.ShareBundleModel;
import com.whitefly.plutocrat.models.TargetModel;
import com.whitefly.plutocrat.models.UserModel;

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
    private ITargetView mTargetView;
    private IBuyoutView mBuyoutView;

    // Constructor
    public MainMenuPresenter(Context context, IMainMenuView mmView, IHomeView homeView, ITargetView targetView, IBuyoutView buyoutView) {
        mContext = context;
        mHttp = new HttpClient(context);
        mMainMenuView = mmView;
        mHomeView = homeView;
        mTargetView = targetView;
        mBuyoutView = buyoutView;
    }

    // Methods
    public void setHttpClient(HttpClient client) {
        mHttp = client;
    }

    @Subscribe
    public void onGetPlutocratEvent(GetPlutocratEvent event) {
        mTargetView.setPlutocrat(AppPreference.getInstance().getSession().getPlutocrat());
    }

    @Subscribe
    public void onSetHomeState(SetHomeStateEvent event) {
        HomeFragment.State state = HomeFragment.State.Default;
        UserModel userModel = AppPreference.getInstance().getSession().getActiveUser();

        if (userModel.defeated_at != null) {
            state = HomeFragment.State.Suspend;
        } else if (userModel.under_buyout_threat) {
            state = HomeFragment.State.Threat;
        }

        mHomeView.changeState(state, userModel.user_notice_id);
    }

    @Subscribe
    public void onUpdateUserNoticeId(UpdateUserNoticeIdEvent event) {
        HomeFragment.State state = HomeFragment.State.Default;
        UserModel userModel = AppPreference.getInstance().getSession().getActiveUser();
        userModel.user_notice_id = event.getNextNoticeId();

        if (userModel.defeated_at != null) {
            state = HomeFragment.State.Suspend;
        } else if (userModel.attacking_current_user) {
            state = HomeFragment.State.Threat;
        }

        mHomeView.changeState(state, userModel.user_notice_id);
    }

    @Subscribe
    public void onCheckNotificationEnable(CheckNotificationEnableEvent event) {
        UserModel model = AppPreference.getInstance().getSession().getActiveUser();
        mHomeView.handleNotificationEnable(model.is_enable_notification);
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
        new LoadDummyBuyoutCallback().execute(event);
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
                String.format("You are buying %d shares of %d each", model.qty, model.price));
        mMainMenuView.buyIAP(model.sku, payload);
    }

    @Subscribe
    public void onSaveAccountSettings(SaveAccountSettingsEvent event) {
        mMainMenuView.toast("Save complete");
    }

    @Subscribe
    public void onExecuteBuyout(ExecuteShareEvent event) {
        new ExecuteBuyoutCallback().execute(event);
    }

    /*
    Request Callback
     */
    private class SignOutCallback extends AsyncTask<Void, Void, Boolean> {
        private String mErrorMessage;

        @Override
        protected void onPreExecute() {
            mMainMenuView.toast("Signing out...");
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
            ArrayList<TargetModel> result = new ArrayList<>();
            Gson gson = new Gson();
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
                MetaModel model = new MetaModel(e.getMessage());
                mErrorMessage = model.getErrors();
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<TargetModel> targetModels) {
            if(targetModels.size() > 0) {
                targetModels.add(null);
            }
            mTargetView.setTargetList(targetModels, mMetaModel);

            if(mMetaModel.getInt("current_page") == FIRST_PAGE) {
                mTargetView.setPlutocrat(AppPreference.getInstance().getSession().getPlutocrat());
            }
        }
    }

    private class LoadDummyBuyoutCallback extends AsyncTask<LoadBuyoutsEvent, Void, ArrayList<BuyoutModel>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<BuyoutModel> doInBackground(LoadBuyoutsEvent... params) {
            ArrayList<BuyoutModel> result = new ArrayList<>();
            // Dummy data
            if(params[0].page == 1) {
                BuyoutModel model;
                model = new BuyoutModel();
                model.name = "Aaron Pinchai";
                model.shares = 23;
                model.hours = 2;
                model.status = BuyoutModel.BuyoutStatus.Threat;
                model.gameStatus = BuyoutModel.GameStatus.Playing;
                model.picProfile = 0;
                result.add(model);

                model = new BuyoutModel();
                model.name = "Sara Mayer";
                model.shares = 21;
                model.hours = 48;
                model.status = BuyoutModel.BuyoutStatus.Initiate;
                model.gameStatus = BuyoutModel.GameStatus.Lose;
                model.picProfile = R.drawable.demo_profile1;
                result.add(model);

                model = new BuyoutModel();
                model.name = "Peter Cook";
                model.shares = 13;
                model.hours = 96;
                model.status = BuyoutModel.BuyoutStatus.Threat;
                model.gameStatus = BuyoutModel.GameStatus.Lose;
                model.picProfile = R.drawable.demo_profile2;
                result.add(model);

                model = new BuyoutModel();
                model.name = "M. Dorsey";
                model.shares = 11;
                model.hours = 120;
                model.status = BuyoutModel.BuyoutStatus.Initiate;
                model.gameStatus = BuyoutModel.GameStatus.Win;
                model.picProfile = R.drawable.demo_profile3;
                result.add(model);
            } else if(params[0].page == 2) {
                BuyoutModel model;
                model = new BuyoutModel();
                model.name = "Danielle Steele";
                model.shares = 21;
                model.hours = 190;
                model.status = BuyoutModel.BuyoutStatus.Initiate;
                model.gameStatus = BuyoutModel.GameStatus.Lose;
                model.picProfile = R.drawable.demo_profile4;
                result.add(model);

                model = new BuyoutModel();
                model.name = "Satjapot I.";
                model.shares = 1;
                model.hours = 111;
                model.status = BuyoutModel.BuyoutStatus.Initiate;
                model.gameStatus = BuyoutModel.GameStatus.Lose;
                result.add(model);

                model = new BuyoutModel();
                model.name = "Amy Sasitorn";
                model.shares = 12;
                model.hours = 145;
                model.status = BuyoutModel.BuyoutStatus.Initiate;
                model.gameStatus = BuyoutModel.GameStatus.Lose;
                model.picProfile = R.drawable.demo_profile5;
                result.add(model);
            }

            // Dummy take time
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<BuyoutModel> buyoutModels) {
            if(buyoutModels.size() > 0) {
                buyoutModels.add(null);
            }
            mBuyoutView.setTargetList(buyoutModels);
        }
    }

    private class GetNewBuyoutCallback extends AsyncTask<TargetModel, Void, NewBuyoutModel> {
        private TargetModel mTarget;
        private String mErrorMessage = null;

        @Override
        protected void onPreExecute() {
            mMainMenuView.handleLoadingDialog(true);
        }

        @Override
        protected NewBuyoutModel doInBackground(TargetModel... params) {
            Gson gson = new Gson();

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
                MetaModel model = new MetaModel(e.getMessage());
                mErrorMessage = model.getErrors();
            }

            return result;
        }

        @Override
        protected void onPostExecute(NewBuyoutModel model) {
            mMainMenuView.handleLoadingDialog(false);
            if(mErrorMessage != null) {
                mMainMenuView.handleError(mContext.getString(R.string.error_title_cannot_new_buyout),
                        mErrorMessage);
            } else {
                mMainMenuView.callInitiateDialog(mTarget, model);
            }
        }
    }

    private class ExecuteBuyoutCallback extends AsyncTask<ExecuteShareEvent, Void, String> {
        private Gson mGson;
        private String mErrorMessage = null;

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
                MetaModel model = new MetaModel(e.getMessage());
                mErrorMessage = model.getErrors();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String response) {
            mMainMenuView.handleLoadingDialog(false);
            if(mErrorMessage != null) {
                mMainMenuView.handleError(mContext.getString(R.string.error_title_cannot_execute_buyout),
                        mErrorMessage);
            } else {
                try {
                    JSONObject rootJson = new JSONObject(response);
                    JSONObject buyoutJson = rootJson.getJSONObject("buyout");
                    TargetModel initiatingUser = mGson.fromJson(buyoutJson.getString("initiating_user"),
                            TargetModel.class);
                    TargetModel targetUser = mGson.fromJson(buyoutJson.getString("target_user"),
                            TargetModel.class);

                    UserModel currentUser = AppPreference.getInstance().getSession().getActiveUser();
                    currentUser.successful_buyouts_count = initiatingUser.numSuccessfulBuyout;
                    currentUser.matched_buyouts_count = initiatingUser.numMatchedBuyout;
                    currentUser.available_shares_count = initiatingUser.numAvailableShares;

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
}
