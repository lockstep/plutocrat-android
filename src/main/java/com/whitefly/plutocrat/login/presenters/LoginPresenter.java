package com.whitefly.plutocrat.login.presenters;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.squareup.otto.Subscribe;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.exception.APIConnectionException;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.HttpClient;
import com.whitefly.plutocrat.models.MetaModel;
import com.whitefly.plutocrat.models.UserModel;
import com.whitefly.plutocrat.login.events.BackToLoginEvent;
import com.whitefly.plutocrat.login.events.ForgotPasswordEvent;
import com.whitefly.plutocrat.login.events.RegisterEvent;
import com.whitefly.plutocrat.login.events.RequestResetTokenEvent;
import com.whitefly.plutocrat.login.events.SignInEvent;
import com.whitefly.plutocrat.login.models.LoginRequestModel;
import com.whitefly.plutocrat.login.models.RegisterRequestModel;
import com.whitefly.plutocrat.login.views.ILoginMainView;
import com.whitefly.plutocrat.login.views.ILoginView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Satjapot on 5/5/16 AD.
 */
public class LoginPresenter {

    // Attributes
    private Context mContext;
    private HttpClient mHttp;

    private ILoginMainView mLoginMainView;
    private ILoginView mLoginView;

    // Constructor
    public LoginPresenter(Context context, ILoginMainView vLoginMain, ILoginView vLogin) {
        mContext = context;
        mHttp = new HttpClient(context);
        mLoginMainView = vLoginMain;
        mLoginView = vLogin;
    }

    // Event Handler
    @Subscribe
    public void onRegisterClicked(RegisterEvent event) {
        // Create request model
        RegisterRequestModel model = event.getRequestModel();
        new RegisterCallBack().execute(model);
    }

    @Subscribe
    public void onSignInClicked(SignInEvent event) {
        LoginRequestModel model = event.getRequestModel();
        new LoginCallBack().execute(model);
    }

    @Subscribe
    public void onForgetPasswordClicked(ForgotPasswordEvent event) {
        mLoginMainView.goToResetPassword1();
    }

    @Subscribe
    public void onRequestResetClicked(RequestResetTokenEvent event) {
        mLoginMainView.gotoResetPassword2();
    }

    @Subscribe
    public void onBackToLogin(BackToLoginEvent event) {
        mLoginView.changeState(event.getState());
        mLoginMainView.backToLogin();
    }

    /*
    Request Callback
     */
    private class RegisterCallBack extends AsyncTask<RegisterRequestModel, Void, JSONObject> {
        private String mErrorMessage;
        private RegisterRequestModel modelRequest;
        private MetaModel mMetaError;

        @Override
        protected void onPreExecute() {
            mLoginMainView.handleLoadingDialog(true);
        }

        @Override
        protected JSONObject doInBackground(RegisterRequestModel... params) {
            // Request api
            Gson gson = AppPreference.getInstance().getGson();
            JSONObject body = null;

            modelRequest = params[0];
            try {
                String strBody = mHttp.request(gson.toJson(modelRequest))
                        .post(R.string.api_register);

                body = new JSONObject(strBody);
            } catch (IOException e) {
                e.printStackTrace();
                mErrorMessage = e.getMessage();
            } catch (JSONException e) {
                e.printStackTrace();
                mErrorMessage = e.getMessage();
            } catch (APIConnectionException e) {
                e.printStackTrace();
                mMetaError = new MetaModel(e.getMessage());
            }
            return body;
        }

        @Override
        protected void onPostExecute(JSONObject body) {
            mLoginMainView.handleLoadingDialog(false);

            if (mMetaError != null) {
                mLoginView.handleError(mMetaError);
            } else if(mErrorMessage != null) {
                mLoginMainView.handleError(mContext.getString(R.string.error_title_cannot_register),
                        mErrorMessage);
            } else {
                new LoginCallBack().execute(new LoginRequestModel(modelRequest.email, modelRequest.password));
            }
        }
    }

    private class LoginCallBack extends AsyncTask<LoginRequestModel, Void, Boolean> {
        private String mErrorMessage;
        private Gson mGson;
        private MetaModel mMetaError;

        // Constructor
        public LoginCallBack() {
            mGson = AppPreference.getInstance().getGson();
        }

        // Methods
        @Override
        protected void onPreExecute() {
            mLoginMainView.handleLoadingDialog(true);
        }

        @Override
        protected Boolean doInBackground(LoginRequestModel... params) {
            boolean result = false;

            LoginRequestModel model = params[0];
            try {
                String strBody = mHttp.request(mGson.toJson(model))
                        .post(R.string.api_signin);

                JSONObject body = new JSONObject(strBody);

                UserModel modUser = mGson.fromJson(body.getString("user"), UserModel.class);
                AppPreference.getInstance().getSession()
                        .save(mHttp.getResponse().headers(), modUser.id);

                result = AppPreference.getInstance().getSession().isLogin(mHttp);
            } catch (IOException e) {
                e.printStackTrace();
                mErrorMessage = e.getMessage();
            } catch (JSONException e) {
                e.printStackTrace();
                mErrorMessage = e.getMessage();
            } catch (APIConnectionException e) {
                e.printStackTrace();
                mMetaError = new MetaModel(e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean b) {

            if (mMetaError != null) {
                mLoginMainView.handleLoadingDialog(false);
                if(mMetaError.isError() && mMetaError.getKeys().contains("auth")) {
                    mLoginMainView.handleError(mContext.getString(R.string.error_title_authenticate_failed),
                            mMetaError.getValue("auth"));
                } else {
                    mLoginView.handleError(mMetaError);
                }
            } else if(mErrorMessage != null) {
                mLoginMainView.handleLoadingDialog(false);
                mLoginMainView.handleError(mContext.getString(R.string.error_title_authenticate_failed),
                        mErrorMessage);
            } else {
                mLoginMainView.goToMainMenu();
            }
        }
    }
}
