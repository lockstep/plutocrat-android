package com.whitefly.plutocrat.helpers;

import android.support.annotation.Nullable;
import android.widget.EditText;

import com.whitefly.plutocrat.exception.FormValidationException;

import java.util.HashMap;

/**
 * Created by satjapotiamopas on 6/13/16 AD.
 */
public class FormValidationHelper {
    private static final String MESSAGE_DEFAULT_REQUIRED = "%s is required.";
    private static final String MESSAGE_DEFAULT_MATCHED = "%s is not matched.";
    private static final String MESSAGE_DEFAULT_CUSTOM = "%s %s.";

    private HashMap<String, EditText> mWhereViewList;
    private HashMap<String, String> mNameList;

    public FormValidationHelper() {
        mWhereViewList = new HashMap<>();
        mNameList = new HashMap<>();
    }

    public FormValidationHelper addView(String id, String name, @Nullable EditText view) {
        mWhereViewList.put(id, view);
        mNameList.put(id, name);

        return this;
    }

    public EditText getView(String id) {
        return mWhereViewList.get(id);
    }

    public String getName(String id) {
        return mNameList.get(id);
    }

    public ValidationRule begin() throws FormValidationException {
        if(mWhereViewList.size() == 0) {
            throw new FormValidationException("No view added.");
        }

        return new ValidationRule();
    }

    public class ValidationRule {
        private FormValidationException mException;

        public ValidationRule() {
            mException = new FormValidationException();
        }

        public void validate() throws FormValidationException {
            if(mException.getItems().size() > 0) {
                throw mException;
            }
        }

        public ValidationRule ruleRequired(String id) {
            return ruleRequired(id, true);
        }

        public ValidationRule ruleRequired(String id, boolean withTrim) {
            EditText view = getView(id);
            if(view != null) {

                String name = getName(id);
                String value = withTrim ? view.getText().toString().trim() : view.getText().toString();

                if (value.equals("")) {
                    mException.addItem(getView(id), String.format(MESSAGE_DEFAULT_REQUIRED, name));
                }
            }

            return this;
        }

        public ValidationRule ruleMatched(String id1, String id2) {
            EditText view1 = getView(id1);
            EditText view2 = getView(id2);

            if(view1 != null && view2 != null) {
                String name1 = getName(id1);
                String name2 = getName(id2);
                String value1 = view1.getText().toString();
                String value2 = view2.getText().toString();

                if (!value1.equals(value2)) {
                    mException.addItem(view1, String.format(MESSAGE_DEFAULT_MATCHED, name1));
                }
            }

            return this;
        }

        public ValidationRule ruleRaiseError(String id, String message) {
            EditText view = getView(id);
            String name = getName(id);

            mException.addItem(view, String.format(MESSAGE_DEFAULT_CUSTOM, name, message));
            return this;
        }

    }
}
