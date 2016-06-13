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
        // Create requset model
        mLoginView.toast("Signing in...");

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
        private String error;
        private RegisterRequestModel modelRequest;

        @Override
        protected void onPreExecute() {
            // Call loading dialog
            mLoginView.toast("Registering...");
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
                error = e.getMessage();
            } catch (JSONException e) {
                e.printStackTrace();
                error = e.getMessage();
            } catch (APIConnectionException e) {
                e.printStackTrace();
                MetaModel metaModel = new MetaModel(e.getMessage());
                error = metaModel.getErrors();
            }
            return body;
        }

        @Override
        protected void onPostExecute(JSONObject body) {
            Gson gson = AppPreference.getInstance().getGson();

            // Validation
            if(body == null) {
                mLoginView.toast(String.format("Error: %s", error));
                return;
            }

            // Deserialize JSON
            try {
                // Check for error
                if(! body.isNull("meta")) {
                    JSONObject meta = body.getJSONObject("meta");

                    // Get meta
                    MetaModel modMeta = new MetaModel(meta);
                    mLoginView.toast(String.format("Error: %s", modMeta.getErrors()));
                    return;
                }

                UserModel user = gson.fromJson(body.getJSONObject("user").toString(), UserModel.class);

                // Call login api to go home page
                new LoginCallBack().execute(new LoginRequestModel(modelRequest.email, modelRequest.password));
            } catch (JSONException e) {
                e.printStackTrace();
                mLoginView.toast(e.getMessage());
            }
        }
    }

    private class LoginCallBack extends AsyncTask<LoginRequestModel, Void, Boolean> {
        private String error;
        private Gson gson;

        // Constructor
        public LoginCallBack() {
            gson = AppPreference.getInstance().getGson();;
        }

        // Methods
        @Override
        protected void onPreExecute() {
            // Call loading dialog
        }

        @Override
        protected Boolean doInBackground(LoginRequestModel... params) {
            // Request api
            boolean result = false;

            LoginRequestModel model = params[0];
            try {
                String strBody = mHttp.request(gson.toJson(model))
                        .post(R.string.api_signin);

                JSONObject body = new JSONObject(strBody);

                UserModel modUser = gson.fromJson(body.getString("user"), UserModel.class);
                AppPreference.getInstance().getSession()
                        .save(mHttp.getResponse().headers(), modUser.id);

                result = AppPreference.getInstance().getSession().isLogin(mHttp);
            } catch (IOException e) {
                e.printStackTrace();
                error = e.getMessage();
            } catch (JSONException e) {
                e.printStackTrace();
                error = e.getMessage();
            } catch (APIConnectionException e) {
                e.printStackTrace();
                MetaModel metaModel = new MetaModel(e.getMessage());
                error = metaModel.getErrors();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            if(error != null) {
                mLoginView.toast(String.format("Error: %s", error));
            } else {
                // Save updated user data to session
                mLoginView.toast("Sign in complete");
                mLoginMainView.goToMainMenu();
            }
        }
    }
}
