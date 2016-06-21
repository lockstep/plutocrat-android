package com.whitefly.plutocrat.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.exception.APIConnectionException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
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
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final int HTML_CODE_UNPROCESSABLE_ENTITY = 422;
    private static final int HTML_CODE_UNAUTHORIZED = 401;

    private static final int IMAGE_QUALITY_FULL = 100;

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
    private HashMap<String, String> mMultipartRequest;

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
        mClient = new OkHttpClient.Builder()
            .addNetworkInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request().newBuilder().addHeader("Connection", "close").build();
                    return chain.proceed(request);
                }
            })
            .build();
        mContext = context;
        mHost = context.getString(R.string.api_host);
        mMultipartRequest = new HashMap<>();
    }

    // Methods
    private Request getRequest(String url, HttpMethod method) {
        Request result = null;
        // Create request
        Request.Builder reqBuilder = new Request.Builder();

        MultipartBody.Builder requestMultipartBuilder = null;
        if(mMultipartRequest.size() > 0) {
            requestMultipartBuilder = new MultipartBody.Builder();
            for(String name :mMultipartRequest.keySet()) {
                requestMultipartBuilder.addFormDataPart(name, name + ".png", RequestBody.create(MEDIA_TYPE_PNG,
                        Base64.decode(mMultipartRequest.get(name), Base64.DEFAULT)));
            }
        }

        RequestBody requestBody = null;
        if(mRequestString != null) {
            if(requestMultipartBuilder == null) {
                reqBuilder.header("Content-Type", "application/json");
                requestBody = RequestBody.create(JSON, mRequestString);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(mRequestString);
                    Iterator<String> keys = jsonObject.keys();
                    while(keys.hasNext()) {
                        String key = keys.next();
                        requestMultipartBuilder.addFormDataPart(key, jsonObject.getString(key));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                requestBody = requestMultipartBuilder.build();
            }
        } else {
            reqBuilder.header("Content-Type", "application/json");
            if(requestMultipartBuilder == null) {
                requestBody = RequestBody.create(null, new byte[0]);
            } else {
                requestBody = requestMultipartBuilder.build();
            }
        }

        switch (method) {
            case GET:
                reqBuilder.get();

                if (mRequestString != null) {
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
                reqBuilder.post(requestBody);
                break;
            case PATCH:
                reqBuilder.patch(requestBody);
                break;
            case DELETE:
                reqBuilder.delete(requestBody);
                break;
        }
        if(mHeaders != null) {
            reqBuilder.headers(mHeaders);
        }
        reqBuilder.url(url);
        result = reqBuilder.build();
        return result;
    }

    public HttpClient request(String jsonRequest) {
        mRequestString = jsonRequest;

        return this;
    }

    public HttpClient header(Headers headers) {
        mHeaders = headers;

        return this;
    }

    public HttpClient addMultipartImage(String name, Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY_FULL, baos);
        byte[] b = baos.toByteArray();
        String value = Base64.encodeToString(b, Base64.DEFAULT);

        mMultipartRequest.put(name, value);

        return this;
    }

    public String execute(String apiUrl, HttpMethod method) throws IOException, APIConnectionException {
        // Input
        String url = String.format("%s%s", mHost, apiUrl);
        Request request = getRequest(url, method);
        int requestTime = 5;

        Log.d(AppPreference.DEBUG_APP, String.format("Request URL: %s", url));

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

                    int responseCode = mResponse.code();
                    Log.d(AppPreference.DEBUG_APP, "HTTP Response code: " + responseCode);
                    if(responseCode == HTML_CODE_UNPROCESSABLE_ENTITY || responseCode == HTML_CODE_UNAUTHORIZED) {
                        throw new APIConnectionException(mResponse.body().string());
                    }

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
        mMultipartRequest.clear();

        return mResponseBody;
    }

    /*
    Helper methods
     */
    public String execute(int apiId, HttpMethod method) throws IOException, APIConnectionException {
        return execute(mContext.getString(apiId), method);
    }

    public String get(int apiId) throws IOException, APIConnectionException {
        return execute(apiId, HttpMethod.GET);
    }
    public String post(int apiId) throws IOException, APIConnectionException {
        return execute(apiId, HttpMethod.POST);
    }
    public String patch(int apiId) throws IOException, APIConnectionException {
        return execute(apiId, HttpMethod.PATCH);
    }
    public String delete(int apiId) throws IOException, APIConnectionException {
        return execute(apiId, HttpMethod.DELETE);
    }

    public String get(String apiUrl) throws IOException, APIConnectionException {
        return execute(apiUrl, HttpMethod.GET);
    }
    public String post(String apiUrl) throws IOException, APIConnectionException {
        return execute(apiUrl, HttpMethod.POST);
    }
    public String patch(String apiUrl) throws IOException, APIConnectionException {
        return execute(apiUrl, HttpMethod.PATCH);
    }
    public String delete(String apiUrl) throws IOException, APIConnectionException {
        return execute(apiUrl, HttpMethod.DELETE);
    }
}
