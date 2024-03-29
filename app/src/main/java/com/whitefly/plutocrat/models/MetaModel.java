package com.whitefly.plutocrat.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Satjapot on 5/10/16 AD.
 * This class will manage about meta object that retrieving from API server.
 * The key is get by only use a word.
 */
public class MetaModel {
    public static final int ERROR_CAST_INTEGER_VALUE = -999;

    // Attributes
    private HashMap<String, String> mMaps;
    private boolean mIsError;

    // Constructor
    public MetaModel(JSONObject meta) {
        mMaps = new HashMap<>();
        mIsError = false;

        create(meta);
    }

    public MetaModel(String root) {
        try {
            JSONObject rootJson = new JSONObject(root);

            mMaps = new HashMap<>();
            mIsError = false;

            create(rootJson.getJSONObject("meta"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // Methods
    private void create(JSONObject meta) {
        // Clear hash map
        mMaps.clear();

        // Try to get a key for identify what is meta type
        Iterator<String> outerKeys = meta.keys();
        String key = outerKeys.next();
        mIsError = key.equals("errors");
        if(mIsError) {
            // Get error message
            try {
                JSONObject errors = meta.getJSONObject(key);
                Iterator<String> innerKeys = errors.keys();
                while(innerKeys.hasNext()) {
                    key = innerKeys.next();
                    mMaps.put(key, errors.getJSONArray(key).get(0).toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            // Get a key
            outerKeys = meta.keys();
            while (outerKeys.hasNext()) {
                key = outerKeys.next();
                try {
                    mMaps.put(key, meta.getString(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Do nothing. just go to next key
                }
            }
        }
    }

    public boolean isError() {
        return mIsError;
    }

    public String getErrors() {
        // Concat error message
        StringBuilder sb = new StringBuilder();
        for(String key : mMaps.keySet()) {
            sb.append(String.format("%s %s", key, mMaps.get(key)));
            sb.append(",");
        }
        return sb.toString();
    }

    public Set<String> getKeys() {
        return mMaps.keySet();
    }

    public String getValue(String key) {
        return mMaps.get(key);
    }

    public int getInt(String key) {
        return getInt(key, ERROR_CAST_INTEGER_VALUE);
    }

    public int getInt(String key, int defValue) {
        int result = defValue;
        try{
            result = Integer.parseInt(this.getValue(key));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean hasKey(String key) {
        return mMaps.containsKey(key);
    }
}
