package com.whitefly.plutocrat.mainmenu.presenters;

import android.content.Context;
import android.os.AsyncTask;

import com.squareup.otto.Subscribe;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.HttpClient;
import com.whitefly.plutocrat.mainmenu.events.LoadTargetsEvent;
import com.whitefly.plutocrat.mainmenu.events.SignOutEvent;
import com.whitefly.plutocrat.mainmenu.views.IMainMenuView;
import com.whitefly.plutocrat.mainmenu.views.ITargetView;
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

    // Constructor
    public MainMenuPresenter(Context context, IMainMenuView mmView, ITargetView targetView) {
        mContext = context;
        mHttp = new HttpClient(context);
        mMainMenuView = mmView;
        mTargetView = targetView;
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
}
