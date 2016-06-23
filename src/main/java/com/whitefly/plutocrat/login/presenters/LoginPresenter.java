package com.whitefly.plutocrat.login.presenters;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;

import com.google.gson.Gson;
import com.squareup.otto.Subscribe;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.exception.APIConnectionException;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.helpers.HttpClient;
import com.whitefly.plutocrat.login.events.ResetPassword1ErrorEvent;
import com.whitefly.plutocrat.login.events.ResetPassword2ErrorEvent;
import com.whitefly.plutocrat.login.events.ResetPasswordEvent;
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
        AsyncTaskCompat.executeParallel(new RegisterCallBack(), model);
    }

    @Subscribe
    public void onSignInClicked(SignInEvent event) {
        LoginRequestModel model = event.getRequestModel();
        AsyncTaskCompat.executeParallel(new LoginCallBack(), model);
    }

    @Subscribe
    public void onForgetPasswordClicked(ForgotPasswordEvent event) {
        mLoginMainView.goToResetPassword1();
    }

    @Subscribe
    public void onRequestResetClicked(RequestResetTokenEvent event) {
        if(event.isSubmitClick()) {
            AsyncTaskCompat.executeParallel(new RequestResetPasswordCallback(), event.getEmail());
        } else {
            mLoginMainView.gotoResetPassword2();
        }
    }

    @Subscribe
    public void onResetPassword(ResetPasswordEvent event) {
        AsyncTaskCompat.executeParallel(new ResetPasswordCallback(), event);
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
        protected void onPostExecute(JSONObject response) {
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
        protected void onPostExecute(Boolean isValid) {

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

    private class RequestResetPasswordCallback extends AsyncTask<String, Void, Boolean> {
        private String mErrorMessage;
        private MetaModel mMetaError;

        @Override
        protected void onPreExecute() {
            mLoginMainView.handleLoadingDialog(true);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = false;
            String email = params[0];

            try {
                JSONObject requestJson = new JSONObject();
                if(!email.trim().equals("")) {
                    requestJson.put("email", email);
                }

                mHttp.request(requestJson.toString()).post(R.string.api_reset_password);

                result = mHttp.getResponseCode() == HttpClient.HTTP_CODE_CREATED;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                mErrorMessage = e.getMessage();
            } catch (APIConnectionException e) {
                e.printStackTrace();
                mMetaError = new MetaModel(e.getMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean isValid) {
            mLoginMainView.handleLoadingDialog(false);

            if(! isValid) {
                mErrorMessage = mContext.getString(R.string.error_cannot_send_email);
            }

            if (mMetaError != null) {
                EventBus.getInstance().post(new ResetPassword1ErrorEvent(mMetaError));
            } else if(mErrorMessage != null) {
                mLoginMainView.handleError(mContext.getString(R.string.error_title_cannot_send_email),
                        mErrorMessage);
            } else {
                mLoginMainView.gotoResetPassword2();
            }
        }
    }

    private class ResetPasswordCallback extends AsyncTask<ResetPasswordEvent, Void, Boolean> {
        private String mErrorMessage;
        private MetaModel mMetaError;

        @Override
        protected void onPreExecute() {
            mLoginMainView.handleLoadingDialog(true);
        }

        @Override
        protected Boolean doInBackground(ResetPasswordEvent... params) {
            boolean result = false;
            ResetPasswordEvent event = params[0];
            Gson gson = AppPreference.getInstance().getGson();

            try {
                JSONObject requestJson = new JSONObject();
                String value;
                value = event.getEmail();
                if(!value.equals("")) {
                    requestJson.put("email", value);
                }

                value = event.getToken();
                if(!value.equals("")) {
                    requestJson.put("reset_password_token", value);
                }

                value = event.getNewPassword();
                if(!value.equals("")) {
                    requestJson.put("password", value);
                }

                value = event.getConfirmPassword();
                if(!value.equals("")) {
                    requestJson.put("password_confirmation", value);
                }

                String response = mHttp.request(requestJson.toString()).patch(R.string.api_reset_password);
                UserModel user = gson.fromJson(response, UserModel.class);


                result = user != null;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                mErrorMessage = e.getMessage();
            } catch (APIConnectionException e) {
                e.printStackTrace();
                mMetaError = new MetaModel(e.getMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean isValid) {
            mLoginMainView.handleLoadingDialog(false);

            if(! isValid) {
                mErrorMessage = mContext.getString(R.string.error_cannot_send_email);
            }

            if (mMetaError != null) {
                EventBus.getInstance().post(new ResetPassword2ErrorEvent(mMetaError));
            } else if(mErrorMessage != null) {
                mLoginMainView.handleError(mContext.getString(R.string.error_title_cannot_send_email),
                        mErrorMessage);
            } else {
                mLoginView.changeState(ILoginView.ViewState.Login);
                mLoginMainView.backToLogin();
            }
        }
    }
}
