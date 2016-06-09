package com.whitefly.plutocrat.helpers;

import android.content.Context;
import android.util.Log;

import com.whitefly.plutocrat.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.framed.Header;

/**
 * Created by Satjapot on 5/9/16 AD.
 * Control http task as singleton.
 */
public class HttpClient {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public enum HttpMethod {
        GET, POST, PATCH, DELETE
    }

    // Attributes
    private Context mContext;
    private OkHttpClient mClient;
    private String mHost;
    private Headers mHeaders;
    private String mRequestString;
    private Response mResponse;
    private String mResponseBody;

    // Getter methods
    public Context getContext() {
        return mContext;
    }
    public String getResponseBody() {
        return mResponseBody;
    }
    public Response getResponse() {
        return mResponse;
    }

    // Constructor
    public HttpClient(Context context) {
        mClient = new OkHttpClient();
        mContext = context;
        mHost = context.getString(R.string.api_host);
    }

    // Methods
    private Request getRequest(String url, HttpMethod method) {
        // Create request
        Request.Builder reqBuilder = new Request.Builder()
                .header("Content-Type", "application/json");

        switch (method) {
            case GET:
                reqBuilder.get();

                if(mRequestString != null) {
                    // Create request parameter
                    try {
                        URI uri = new URI(url);

                        JSONObject query = new JSONObject(mRequestString);
                        HttpUrl.Builder httpBuilder = new HttpUrl.Builder();
                        Iterator<String> keys = query.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            httpBuilder.addQueryParameter(key, query.getString(key));
                        }
                        httpBuilder.scheme(uri.getScheme())
                                .host(uri.getHost())
                                .addEncodedPathSegments(uri.getPath());

                        url = httpBuilder.build().toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Create request failed so do nothing
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case POST:
                if(mRequestString != null) {
                    reqBuilder.post(RequestBody.create(JSON, mRequestString));
                }
                break;
            case PATCH:
                if(mRequestString != null) {
                    reqBuilder.patch(RequestBody.create(JSON, mRequestString));
                }
                break;
            case DELETE:
                if(mRequestString == null) {
                    reqBuilder.delete();
                } else {
                    reqBuilder.delete(RequestBody.create(JSON, mRequestString));
                }
                break;
        }
        if(mHeaders != null) {
            reqBuilder.headers(mHeaders);
        }
        reqBuilder.url(url);
        return reqBuilder.build();
    }

    public HttpClient request(String jsonRequest) {
        mRequestString = jsonRequest;

        return this;
    }

    public HttpClient header(Headers headers) {
        mHeaders = headers;

        return this;
    }

    public String execute(String apiUrl, HttpMethod method) throws IOException {
        // Input
        String url = String.format("%s%s", mHost, apiUrl);
        Request request = getRequest(url, method);
        int requestTime = 5;

        // Clear data
        mResponse = null;
        mResponseBody = null;
        do {
            // Send the request
            Log.d(AppPreference.DEBUG_APP, String.format("Try to request...%d", requestTime));
            try {
                mResponse = mClient.newCall(request).execute();
                if(mResponse.isSuccessful()) {
                    // For resolving conflict about thread, get them now.
                    mResponseBody = mResponse.body().string();
                    Log.d(AppPreference.DEBUG_APP, mResponseBody);
                } else {
                    Log.d(AppPreference.DEBUG_APP, mResponse.message());
                    requestTime--;

                    // If try to to Request more than this times, error should occurs
                    if(requestTime <= 0) {
                        throw new SocketTimeoutException(mResponse.message());
                    }
                }
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                requestTime--;

                // If try to to Request more than this times, error should occurs
                if(requestTime <= 0) {
                    throw e;
                }
            }
        } while (mResponse == null || ! mResponse.isSuccessful());

        // Clear request body
        mRequestString = null;
        mHeaders = null;

        return mResponseBody;
    }

    /*
    Helper methods
     */
    public String execute(int apiId, HttpMethod method) throws IOException {
        return execute(mContext.getString(apiId), method);
    }

    public String get(int apiId) throws IOException {
        return execute(apiId, HttpMethod.GET);
    }
    public String post(int apiId) throws IOException {
        return execute(apiId, HttpMethod.POST);
    }
    public String patch(int apiId) throws IOException {
        return execute(apiId, HttpMethod.PATCH);
    }
    public String delete(int apiId) throws IOException {
        return execute(apiId, HttpMethod.DELETE);
    }

    public String get(String apiUrl) throws IOException {
        return execute(apiUrl, HttpMethod.GET);
    }
    public String post(String apiUrl) throws IOException {
        return execute(apiUrl, HttpMethod.POST);
    }
    public String patch(String apiUrl) throws IOException {
        return execute(apiUrl, HttpMethod.PATCH);
    }
    public String delete(String apiUrl) throws IOException {
        return execute(apiUrl, HttpMethod.DELETE);
    }
}
