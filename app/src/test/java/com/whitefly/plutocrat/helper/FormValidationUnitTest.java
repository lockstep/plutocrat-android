package com.whitefly.plutocrat.helper;

import android.content.Context;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.widget.EditText;

import com.whitefly.plutocrat.exception.FormValidationException;
import com.whitefly.plutocrat.helpers.FormValidationHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by satjapotiamopas on 6/15/16 AD.
 */
@RunWith(MockitoJUnitRunner.class)
public class FormValidationUnitTest {

    private FormValidationHelper mValidationHelper;

    @Mock
    private Context mContext;

    @Mock
    private EditText mEdtText1, mEdtText2;

    @Before
    public void createValidationHelper() {
        mValidationHelper = new FormValidationHelper();
        mEdtText1 = mock(EditText.class);
        mEdtText2 = mock(EditText.class);
    }

    @Test
    public void testRequiredValidation() {
        String testId = "test_id";
        when(mEdtText1.getText()).thenReturn(mock(Editable.class));
        when(mEdtText1.getText().toString()).thenReturn("");
        mEdtText1.setText("");

        mValidationHelper.addView(testId, "Test Text", mEdtText1);

        try {
            mValidationHelper.begin()
                    .ruleRequired(testId)
                    .validate();

            fail();
        } catch (FormValidationException e) {
            assertTrue(e.getItems().size() == 1);
        }
    }

    @Test
    public void testMatchValidation() {
        String testId1 = "test_id_1";
        when(mEdtText1.getText()).thenReturn(mock(Editable.class));
        when(mEdtText1.getText().toString()).thenReturn("ab");
        String testId2 = "test_id_2";
        when(mEdtText2.getText()).thenReturn(mock(Editable.class));
        when(mEdtText2.getText().toString()).thenReturn("cd");

        mValidationHelper.addView(testId1, "Test Text", mEdtText1);
        mValidationHelper.addView(testId2, "Test Text", mEdtText2);

        try {
            mValidationHelper.begin()
                    .ruleMatched(testId2, testId1)
                    .validate();

            fail();
        } catch (FormValidationException e) {
            assertTrue(e.getItems().size() > 0);
        }
    }

}
