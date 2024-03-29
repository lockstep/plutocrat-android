package com.whitefly.plutocrat.mainmenu.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.squareup.otto.Subscribe;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.exception.FormValidationException;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.helpers.ExifUtil;
import com.whitefly.plutocrat.helpers.FormValidationHelper;
import com.whitefly.plutocrat.helpers.ImageInputHelper;
import com.whitefly.plutocrat.mainmenu.MainMenuActivity;
import com.whitefly.plutocrat.mainmenu.events.LoadBuyoutsEvent;
import com.whitefly.plutocrat.mainmenu.events.LoadTargetsEvent;
import com.whitefly.plutocrat.mainmenu.events.SaveAccountSettingsEvent;
import com.whitefly.plutocrat.mainmenu.events.SaveImageProfileEvent;
import com.whitefly.plutocrat.mainmenu.events.SetHomeStateEvent;
import com.whitefly.plutocrat.mainmenu.events.UpdateSettingsEvent;
import com.whitefly.plutocrat.mainmenu.views.IAccountSettingView;
import com.whitefly.plutocrat.mainmenu.views.ITabView;
import com.whitefly.plutocrat.models.MetaModel;
import com.whitefly.plutocrat.models.UserModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by Satjapot on 7/5/16 AD.
 */
public class SettingsFragment extends Fragment
        implements View.OnClickListener, ImageInputHelper.ImageActionListener, IAccountSettingView,
        RequestListener<Uri, GlideDrawable>, ITabView {

    public static final String TITLE = "Settings";

    private static final int IMAGE_OUTPUT_X = 250;
    private static final int IMAGE_OUTPUT_Y = 250;
    private static final int IMAGE_ASPECT_X = 1;
    private static final int IMAGE_ASPECT_Y = 1;

    private static final String FORM_DISPLAY_NAME_ID = "display_name";
    private static final String FORM_EMAIL_ID = "email";
    private static final String FORM_NEW_PASSWORD_ID = "password";
    private static final String FORM_CONFIRM_PASSWORD_ID = "confirm_password";
    private static final String FORM_CURRENT_PASSWORD_ID = "current_password";

    // Attributes
    private AlertDialog mImageDialog;
    private ImageInputHelper imageInputHelper;
    private Bitmap mSavingPicture;
    private boolean mIsCreateView;
    private FormValidationHelper mFormValidator;

    // Views
    private TextView mTvProfilePicture;
    private ImageView mImvProfilePicture;
    private EditText mEdtDisplayName, mEdtEmail, mEdtNewPassword, mEdtConfirmPassword, mEdtCurrentPassword;
    private CheckBox mChkEnableTransactionalEmail, mChkEnableProductUpdates;
    private Button mBtnSave;
    private LinearLayout mLloBack, mLloHeader;
    private RelativeLayout mRloHeaderBar;

    private Dialog mLoadingDialog;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FAQFragment.
     */
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
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

    public void clearView() {
        UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();

        mTvProfilePicture.setText(activeUser.getNickName());
        Glide.with(getActivity()).load(Uri.parse(activeUser.profileImage))
                .listener(this)
                .bitmapTransform(new CropCircleTransformation(getActivity()))
                .into(mImvProfilePicture);

        mEdtDisplayName.setText(activeUser.name);
        mEdtEmail.setText(activeUser.email);
        mEdtNewPassword.setText("");
        mEdtConfirmPassword.setText("");
        mEdtCurrentPassword.setText("");
        mChkEnableTransactionalEmail.setChecked(activeUser.isTransactionalEmailsEnabled);
        mChkEnableProductUpdates.setChecked(activeUser.isProductEmailsEnabled);

        mIsCreateView = false;
    }

    private void createLoadingDialog(LayoutInflater inflater, ViewGroup parent) {
        View root = inflater.inflate(R.layout.dialog_loading, parent, false);

        mLoadingDialog = new Dialog(getActivity());
        mLoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLoadingDialog.setContentView(root);
        mLoadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mLoadingDialog.setCancelable(false);
    }

    private void showErrorDialog(String title, String message) {
        Typeface typeface = AppPreference.getInstance().getFont(AppPreference.FontType.Regular);

        SpannableString spanTitle = new SpannableString(title);
        spanTitle.setSpan(typeface, 0, spanTitle.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        SpannableString spanMessage = new SpannableString(message);
        spanMessage.setSpan(typeface, 0, spanMessage.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        SpannableString negativeText = new SpannableString(getString(R.string.caption_close));
        negativeText.setSpan(typeface, 0, negativeText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        new AlertDialog.Builder(getActivity())
                .setTitle(spanTitle)
                .setMessage(spanMessage)
                .setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.register(this);

        if(imageInputHelper == null) {
            imageInputHelper = new ImageInputHelper(this);
            imageInputHelper.setImageActionListener(this);
        }
        captureImageInitialization();
        mFormValidator = new FormValidationHelper();
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
        mChkEnableTransactionalEmail = (CheckBox) root.findViewById(R.id.chk_enable_transactional);
        mChkEnableProductUpdates = (CheckBox) root.findViewById(R.id.chk_enable_product_update_email);
        mBtnSave                = (Button) root.findViewById(R.id.btn_save_settings);
        mLloBack                = (LinearLayout) root.findViewById(R.id.btn_back);
        mLloHeader              = (LinearLayout) root.findViewById(R.id.llo_account_header);
        mRloHeaderBar           = (RelativeLayout) root.findViewById(R.id.rlo_account_bar);

        // Initialize
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular,
                mTvProfilePicture, mEdtDisplayName, mEdtEmail, mEdtNewPassword, mEdtConfirmPassword,
                mEdtCurrentPassword, mChkEnableTransactionalEmail, mChkEnableProductUpdates, mBtnSave,
                (TextView) root.findViewById(R.id.tv_account_setting_header),
                (TextView) root.findViewById(R.id.tv_btn_back));

        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Bold,
                (TextView) root.findViewById(R.id.tv_display_name),
                (TextView) root.findViewById(R.id.tv_email),
                (TextView) root.findViewById(R.id.tv_new_pw),
                (TextView) root.findViewById(R.id.tv_confirm_pw),
                (TextView) root.findViewById(R.id.tv_current_pw));

        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Italic,
                (TextView) root.findViewById(R.id.tv_account_setting_note));

        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.BoldItalic,
                (TextView) root.findViewById(R.id.tv_tap_to_change));

        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Light,
                (TextView) root.findViewById(R.id.tv_title_account),
                (TextView) root.findViewById(R.id.tv_settings_sub_title));

        mFormValidator.addView(FORM_DISPLAY_NAME_ID, "Display name", mEdtDisplayName);
        mFormValidator.addView(FORM_EMAIL_ID, "E-mail", mEdtEmail);
        mFormValidator.addView(FORM_NEW_PASSWORD_ID, "New Password", mEdtNewPassword);
        mFormValidator.addView(FORM_CONFIRM_PASSWORD_ID, "Confirm Password", mEdtConfirmPassword);
        mFormValidator.addView(FORM_CURRENT_PASSWORD_ID, "Current Password", mEdtCurrentPassword);

        mLloHeader.setVisibility(View.VISIBLE);
        mRloHeaderBar.setVisibility(View.GONE);

        mIsCreateView = true;

        createLoadingDialog(inflater, container);

        // Event handler
        mBtnSave.setOnClickListener(this);
        mLloBack.setOnClickListener(this);
        mTvProfilePicture.setOnClickListener(this);
        mImvProfilePicture.setOnClickListener(this);
        mEdtCurrentPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    mBtnSave.performClick();
                    return true;
                }
                return false;
            }
        });
        mEdtNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().equals("")) {
                    mEdtConfirmPassword.setError(null);
                    mEdtCurrentPassword.setError(null);
                }
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(mIsCreateView) {
            clearView();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        mEdtDisplayName.setError(null);
        mEdtEmail.setError(null);
        mEdtNewPassword.setError(null);
        mEdtConfirmPassword.setError(null);
        mEdtCurrentPassword.setError(null);
    }

    @Override
    public void onClick(View v) {
        if(v == mBtnSave) {
            SaveAccountSettingsEvent event = null;

            InputMethodManager imm =
                    (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            try {
                String newPassword = mEdtNewPassword.getText().toString();
                String currentPassword = mEdtCurrentPassword.getText().toString();

                if(! newPassword.equals("")) {
                    mFormValidator.begin()
                            .ruleMatched(FORM_CONFIRM_PASSWORD_ID, FORM_NEW_PASSWORD_ID)
                            .validate();
                }

                event = new SaveAccountSettingsEvent(
                        null,
                        mEdtDisplayName.getText().toString().trim(),
                        mEdtEmail.getText().toString().trim(),
                        newPassword.equals("") ? null : newPassword,
                        currentPassword.equals("") ? null : currentPassword,
                        mChkEnableTransactionalEmail.isChecked(),
                        mChkEnableProductUpdates.isChecked(),
                        this
                );

                EventBus.getInstance().post(event);
            } catch (FormValidationException e) {
                e.printStackTrace();

                for(FormValidationException.ValidationItem item : e.getItems()) {
                    item.raiseError();
                }
                if(e.getItems().size() > 0) {
                    e.getItems().get(0).getView().requestFocus();
                }
            }
        } else if(v == mTvProfilePicture || v == mImvProfilePicture) {
            mImageDialog.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        EventBus.register(this);

        imageInputHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.unregister(this);
    }

    /**
     * Implement RequestListener
     */
    @Override
    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
        mImvProfilePicture.setVisibility(View.GONE);
        mTvProfilePicture.setVisibility(View.VISIBLE);
        return false;
    }

    @Override
    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
        mImvProfilePicture.setVisibility(View.VISIBLE);
        mTvProfilePicture.setVisibility(View.GONE);
        return false;
    }

    /**
     * Implement ImageInputHelper.ImageActionListener
     */
    public void showPictureProfile(Uri uri) {
        try {
            do {
                mSavingPicture = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
            } while ( mSavingPicture == null);

            Glide.clear(mImvProfilePicture);
            Glide.with(getActivity())
                    .load(uri)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .listener(this)
                    .bitmapTransform(new CropCircleTransformation(getActivity()))
                    .into(mImvProfilePicture);

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    EventBus.getInstance().post(new SaveImageProfileEvent(mSavingPicture, SettingsFragment.this));
                }
            });
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            showErrorDialog(getString(R.string.error_title_cannot_save_picture),
                    getString(R.string.error_profile_picture));
        }
    }

    @Override
    public void onImageSelectedFromGallery(Uri uri, File imageFile) {
        imageInputHelper.requestCropImage(uri, IMAGE_OUTPUT_X, IMAGE_OUTPUT_Y, IMAGE_ASPECT_X, IMAGE_ASPECT_Y);
    }

    @Override
    public void onImageTakenFromCamera(Uri uri, File imageFile) {
        try {
            Bitmap bitmap = null;
            do {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
            } while (bitmap == null);

            bitmap = ExifUtil.decodeSampledBitmapFromFile(imageFile, IMAGE_OUTPUT_X, IMAGE_OUTPUT_Y);
            bitmap = ExifUtil.rotateBitmap(imageFile.getAbsolutePath().toString(), bitmap);

            OutputStream fileOutput = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutput);
            fileOutput.flush();
            fileOutput.close();
            bitmap.recycle();

            imageInputHelper.requestCropImage(uri, IMAGE_OUTPUT_X, IMAGE_OUTPUT_Y, IMAGE_ASPECT_X, IMAGE_ASPECT_Y);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog(getString(R.string.error_title_cannot_save_picture),
                    getString(R.string.error_profile_picture));
        }
    }

    @Override
    public void onImageCropped(Uri uri, File imageFile) {
        showPictureProfile(uri);
    }

    @Override
    public void handleError(MetaModel meta) {
        try {
            FormValidationHelper.ValidationRule rules = mFormValidator.begin();
            for(String key : meta.getKeys()) {
                rules.ruleRaiseError(key, meta.getValue(key));
            }
            rules.validate();
        } catch (FormValidationException e) {
            e.printStackTrace();

            for(FormValidationException.ValidationItem item : e.getItems()) {
                item.raiseError();
            }
            if(e.getItems().size() > 0) {
                e.getItems().get(0).getView().requestFocus();
            }
        }
    }

    @Override
    public int getIcon() {
        return R.drawable.icon_menu_account;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public void updateView() {

    }

    @Subscribe
    public void onUpdateSettings(UpdateSettingsEvent event) {
        clearView();
    }
}

