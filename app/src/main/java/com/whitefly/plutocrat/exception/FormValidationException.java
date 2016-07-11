package com.whitefly.plutocrat.exception;

import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by satjapotiamopas on 6/13/16 AD.
 */
public class FormValidationException extends Exception {
    private ArrayList<ValidationItem> mItems;

    public ArrayList<ValidationItem> getItems() {
        return mItems;
    }

    public void addItem(EditText where, String message) {
        mItems.add(new ValidationItem(where, message));
    }

    public FormValidationException() {
        super("Validation is failed.");
        mItems = new ArrayList<>();
    }

    public FormValidationException(String message) {
        super(message);
        mItems = new ArrayList<>();
    }

    /*
    Inner class
     */
    public static class ValidationItem {
        private EditText mWhere;
        private String mMessage;

        public EditText getView() {
            return mWhere;
        }

        public String getMessage() {
            return mMessage;
        }

        public ValidationItem(EditText where, String message) {
            mWhere = where;
            mMessage = message;
        }

        public void raiseError() {
            mWhere.setError(mMessage);
        }
    }
}
