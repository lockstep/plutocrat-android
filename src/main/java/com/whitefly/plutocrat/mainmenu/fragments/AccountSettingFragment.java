package com.whitefly.plutocrat.mainmenu.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.helpers.ImageInputHelper;
import com.whitefly.plutocrat.mainmenu.events.SaveAccountSettingsEvent;

import java.io.File;
import java.io.IOException;

/**
 * Created by satjapotiamopas on 5/25/16 AD.
 */
public class AccountSettingFragment extends DialogFragment
        implements View.OnClickListener, ImageInputHelper.ImageActionListener{
    private static final String DEBUG_DISPLAY_NAME = "Danielle Steele";
    private static final String DEBUG_EMAIL = "danielle@watershedcapital.com";

    private static final int IMAGE_OUTPUT_X = 250;
    private static final int IMAGE_OUTPUT_Y = 250;
    private static final int IMAGE_ASPECT_X = 1;
    private static final int IMAGE_ASPECT_Y = 1;

    // Attributes
    private AlertDialog mImageDialog;
    private ImageInputHelper imageInputHelper;
    private Bitmap mSavingPicture;

    // Views
    private TextView mTvProfilePicture;
    private ImageView mImvProfilePicture;
    private EditText mEdtDisplayName, mEdtEmail, mEdtNewPassword, mEdtConfirmPassword, mEdtCurrentPassword;
    private CheckBox mChkEnableNotification, mChkEnableUpdates;
    private Button mBtnSave;
    private LinearLayout mLloBack;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FAQFragment.
     */
    public static AccountSettingFragment newInstance() {
        AccountSettingFragment fragment = new AccountSettingFragment();
        fragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme);

        return fragment;
    }

    private void captureImageInitialization() {

        View root = getActivity().getLayoutInflater().inflate(R.layout.dialog_choose_images, null, false);
        TextView tvCamera = (TextView) root.findViewById(R.id.tv_take_camera);
        TextView tvGallery = (TextView) root.findViewById(R.id.tv_take_gallery);

        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular,
                tvCamera, tvGallery,
                (TextView) root.findViewById(R.id.tv_dialog_title));

        tvCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageInputHelper.takePhotoWithCamera();
                mImageDialog.dismiss();
            }
        });
        tvGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageInputHelper.selectImageFromGallery();
                mImageDialog.dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(root);

        mImageDialog = builder.create();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(imageInputHelper == null) {
            imageInputHelper = new ImageInputHelper(this);
            imageInputHelper.setImageActionListener(this);
        }
        captureImageInitialization();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setWindowAnimations(android.R.style.Animation_Dialog);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account_settings, container, false);

        mTvProfilePicture       = (TextView) root.findViewById(R.id.tv_profile_picture);
        mImvProfilePicture      = (ImageView) root.findViewById(R.id.imv_profile_picture);
        mEdtDisplayName         = (EditText) root.findViewById(R.id.edt_display_name);
        mEdtEmail               = (EditText) root.findViewById(R.id.edt_email);
        mEdtNewPassword         = (EditText) root.findViewById(R.id.edt_new_password);
        mEdtConfirmPassword     = (EditText) root.findViewById(R.id.edt_confirm_password);
        mEdtCurrentPassword     = (EditText) root.findViewById(R.id.edt_current_password);
        mChkEnableNotification  = (CheckBox) root.findViewById(R.id.chk_enable_notification);
        mChkEnableUpdates       = (CheckBox) root.findViewById(R.id.chk_enable_updates);
        mBtnSave                = (Button) root.findViewById(R.id.btn_save_settings);
        mLloBack                = (LinearLayout) root.findViewById(R.id.btn_back);

        // Initialize
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular,
                mTvProfilePicture, mEdtDisplayName, mEdtEmail, mEdtNewPassword, mEdtConfirmPassword,
                mEdtCurrentPassword, mChkEnableNotification, mChkEnableUpdates, mBtnSave,
                (TextView) root.findViewById(R.id.tv_account_setting_header),
                (TextView) root.findViewById(R.id.tv_btn_back),
                (TextView) root.findViewById(R.id.tv_display_name),
                (TextView) root.findViewById(R.id.tv_email),
                (TextView) root.findViewById(R.id.tv_new_pw),
                (TextView) root.findViewById(R.id.tv_confirm_pw),
                (TextView) root.findViewById(R.id.tv_current_pw));

        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Italic,
                (TextView) root.findViewById(R.id.tv_tap_to_change),
                (TextView) root.findViewById(R.id.tv_account_setting_note));

        mEdtDisplayName.setText(DEBUG_DISPLAY_NAME);
        mEdtEmail.setText(DEBUG_EMAIL);

        mEdtDisplayName.requestFocus();

        // Event handler
        mBtnSave.setOnClickListener(this);
        mLloBack.setOnClickListener(this);
        mTvProfilePicture.setOnClickListener(this);
        mImvProfilePicture.setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View v) {
        if(v == mBtnSave) {
            SaveAccountSettingsEvent event = new SaveAccountSettingsEvent(
                    mSavingPicture,
                    mEdtDisplayName.getText().toString(),
                    mEdtEmail.getText().toString(),
                    mEdtNewPassword.getText().toString(),
                    mEdtConfirmPassword.getText().toString(),
                    mEdtCurrentPassword.getText().toString(),
                    mChkEnableNotification.isChecked(),
                    mChkEnableUpdates.isChecked()
            );

            EventBus.getInstance().post(event);
        } else if(v == mLloBack) {
            this.dismiss();
        } else if(v == mTvProfilePicture || v == mImvProfilePicture) {
            mImageDialog.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        imageInputHelper.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Implement ImageInputHelper.ImageActionListener
     */
    public void showPictureProfile(Uri uri) {
        try {
            do {
                mSavingPicture = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
            } while ( mSavingPicture == null);

            RoundedBitmapDrawable circularBitmapDrawable =
                    RoundedBitmapDrawableFactory.create(getActivity().getResources(), mSavingPicture);
            circularBitmapDrawable.setCircular(true);

            mImvProfilePicture.setImageDrawable(circularBitmapDrawable);

            mImvProfilePicture.setVisibility(View.VISIBLE);
            mTvProfilePicture.setVisibility(View.GONE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onImageSelectedFromGallery(Uri uri, File imageFile) {
        imageInputHelper.requestCropImage(uri, IMAGE_OUTPUT_X, IMAGE_OUTPUT_Y, IMAGE_ASPECT_X, IMAGE_ASPECT_Y);
    }

    @Override
    public void onImageTakenFromCamera(Uri uri, File imageFile) {
        imageInputHelper.requestCropImage(uri, IMAGE_OUTPUT_X, IMAGE_OUTPUT_Y, IMAGE_ASPECT_X, IMAGE_ASPECT_Y);
    }

    @Override
    public void onImageCropped(Uri uri, File imageFile) {
        showPictureProfile(uri);
    }
}
