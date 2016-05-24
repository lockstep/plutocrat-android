package com.whitefly.plutocrat.mainmenu.presenters;

import android.content.Context;
import android.os.AsyncTask;

import com.squareup.otto.Subscribe;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.HttpClient;
import com.whitefly.plutocrat.mainmenu.events.BuySharesEvent;
import com.whitefly.plutocrat.mainmenu.events.EngageClickEvent;
import com.whitefly.plutocrat.mainmenu.events.LoadBuyoutsEvent;
import com.whitefly.plutocrat.mainmenu.events.LoadTargetsEvent;
import com.whitefly.plutocrat.mainmenu.events.MoreShareClickEvent;
import com.whitefly.plutocrat.mainmenu.events.SignOutEvent;
import com.whitefly.plutocrat.mainmenu.views.IBuyoutView;
import com.whitefly.plutocrat.mainmenu.views.IMainMenuView;
import com.whitefly.plutocrat.mainmenu.views.ITargetView;
import com.whitefly.plutocrat.models.BuyoutModel;
import com.whitefly.plutocrat.models.TargetModel;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Headers;

/**
 * Created by Satjapot on 5/12/16 AD.
 */
public class MainMenuPresenter {
    // Attributes
    private Context mContext;
    private HttpClient mHttp;

    // Views
    private IMainMenuView mMainMenuView;
    private ITargetView mTargetView;
    private IBuyoutView mBuyoutView;

    // Constructor
    public MainMenuPresenter(Context context, IMainMenuView mmView, ITargetView targetView, IBuyoutView buyoutView) {
        mContext = context;
        mHttp = new HttpClient(context);
        mMainMenuView = mmView;
        mTargetView = targetView;
        mBuyoutView = buyoutView;
    }

    // Methods
    @Subscribe
    public void onSiginOut(SignOutEvent event) {
        new SignOutCallback().execute();
    }

    @Subscribe
    public void onLoadTargets(LoadTargetsEvent event) {
        new LoadDummyTargetCallback().execute(event);
    }

    @Subscribe
    public void onLoadBuyouts(LoadBuyoutsEvent event) {
        new LoadDummyBuyoutCallback().execute(event);
    }

    @Subscribe
    public void onEngageClick(EngageClickEvent event) {
        mMainMenuView.callInitiateDialog(event.getTargetModel());
    }

    @Subscribe
    public void onMoreShareClick(MoreShareClickEvent event) {
        mMainMenuView.goToShareFromInitiate();
    }

    @Subscribe
    public void onBuyShares(BuySharesEvent event) {
        mMainMenuView.toast(String.format("You are buying %d shares of %d each", event.getQty(), event.getPrice()));
    }

    /*
    Request Callback
     */
    private class SignOutCallback extends AsyncTask<Void, Void, Boolean> {
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

    private class LoadDummyTargetCallback extends AsyncTask<LoadTargetsEvent, Void, ArrayList<TargetModel>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<TargetModel> doInBackground(LoadTargetsEvent... params) {
            ArrayList<TargetModel> result = new ArrayList<>();
            // Dummy data
            if(params[0].page == 1) {
                TargetModel model;
                model = new TargetModel();
                model.name = "Aaron Pinchai";
                model.numBuyouts = 23;
                model.numThreats = 7;
                model.daySurvived = 119;
                model.status = TargetModel.TargetStatus.Normal;
                model.picProfile = 0;
                result.add(model);

                model = new TargetModel();
                model.name = "Sara Mayer";
                model.numBuyouts = 21;
                model.numThreats = 2;
                model.daySurvived = 81;
                model.status = TargetModel.TargetStatus.UnderThreat;
                model.picProfile = R.drawable.demo_profile1;
                result.add(model);

                model = new TargetModel();
                model.name = "Peter Cook";
                model.numBuyouts = 13;
                model.numThreats = 0;
                model.daySurvived = 20;
                model.status = TargetModel.TargetStatus.Normal;
                model.picProfile = R.drawable.demo_profile2;
                result.add(model);

                model = new TargetModel();
                model.name = "M. Dorsey";
                model.numBuyouts = 11;
                model.numThreats = 3;
                model.daySurvived = 119;
                model.status = TargetModel.TargetStatus.Normal;
                model.picProfile = R.drawable.demo_profile3;
                result.add(model);
            } else if(params[0].page == 2) {
                TargetModel model;
                model = new TargetModel();
                model.name = "Danielle Steele";
                model.numBuyouts = 21;
                model.numThreats = 5;
                model.daySurvived = 190;
                model.status = TargetModel.TargetStatus.Normal;
                model.picProfile = R.drawable.demo_profile4;
                result.add(model);

                model = new TargetModel();
                model.name = "Satjapot I.";
                model.numBuyouts = 1;
                model.numThreats = 11;
                model.daySurvived = 111;
                model.status = TargetModel.TargetStatus.UnderThreat;
                result.add(model);

                model = new TargetModel();
                model.name = "Amy Sasitorn";
                model.numBuyouts = 12;
                model.numThreats = 5;
                model.daySurvived = 145;
                model.status = TargetModel.TargetStatus.Normal;
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
        protected void onPostExecute(ArrayList<TargetModel> targetModels) {
            if(targetModels.size() > 0) {
                targetModels.add(null);
            }
            mTargetView.setTargetList(targetModels);
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
}
