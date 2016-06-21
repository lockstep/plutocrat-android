package com.whitefly.plutocrat.mainmenu;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.exception.IAPException;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.helpers.FormValidationHelper;
import com.whitefly.plutocrat.helpers.IAPHelper;
import com.whitefly.plutocrat.helpers.text.CustomTypefaceSpan;
import com.whitefly.plutocrat.helpers.view.CustomViewPager;
import com.whitefly.plutocrat.login.LoginActivity;
import com.whitefly.plutocrat.login.views.ILoginView;
import com.whitefly.plutocrat.mainmenu.events.SignOutEvent;
import com.whitefly.plutocrat.mainmenu.fragments.AboutFragment;
import com.whitefly.plutocrat.mainmenu.fragments.AccountSettingFragment;
import com.whitefly.plutocrat.mainmenu.fragments.BuyoutFragment;
import com.whitefly.plutocrat.mainmenu.fragments.FAQFragment;
import com.whitefly.plutocrat.mainmenu.fragments.HomeFragment;
import com.whitefly.plutocrat.mainmenu.fragments.InitiateFragment;
import com.whitefly.plutocrat.mainmenu.fragments.ShareFragment;
import com.whitefly.plutocrat.mainmenu.fragments.TargetFragment;
import com.whitefly.plutocrat.mainmenu.presenters.MainMenuPresenter;
import com.whitefly.plutocrat.mainmenu.views.IAccountSettingView;
import com.whitefly.plutocrat.mainmenu.views.IHomeView;
import com.whitefly.plutocrat.mainmenu.views.IMainMenuView;
import com.whitefly.plutocrat.mainmenu.views.ITabView;
import com.whitefly.plutocrat.models.IAPPurchaseModel;
import com.whitefly.plutocrat.models.MetaModel;
import com.whitefly.plutocrat.models.NewBuyoutModel;
import com.whitefly.plutocrat.models.TargetModel;

import java.util.ArrayList;
import java.util.Set;

public class MainMenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IMainMenuView {
    public static final int FRAGMENT_HOME_INDEX = 0;
    public static final int FRAGMENT_TARGETS_INDEX = 1;
    public static final int FRAGMENT_BUYOUTS_INDEX = 2;
    public static final int FRAGMENT_SHARES_INDEX = 3;
    public static final int FRAGMENT_ABOUT_INDEX = 4;

    private static final int SLOP_PERIOD = 180;

    private static final String FRAGMENT_INITIATE = "frg_initiate";
    private static final String FRAGMENT_FAQ = "frg_faq";
    private static final String FRAGMENT_ACCOUNT_SETTINGS = "frg_account_settings";

    // Attributes
    private MenuPagerAdapter mAdapter;
    private MainMenuPresenter presenter;

    private DialogFragment mFrgAccountSetting;
    private IAPHelper mIAPHelper;
    private AlertDialog mLoadingDialog, mErrorDialog;
    private TextView mTvLoadingMessage;
    private CustomTypefaceSpan mFontSpan;
    private FormValidationHelper mValidator;

    private float mTouchXDown, mTouchXUp;
    private int mTouchSlop;

    // Views
    private CustomViewPager mMainPager;
    private TabLayout mTabLayout;

    // Methods
    private void createLoadingDialog() {
        View root = getLayoutInflater().inflate(R.layout.dialog_loading, null, false);
        mTvLoadingMessage = (TextView) root.findViewById(R.id.tv_loading_message);

        mLoadingDialog = new AlertDialog.Builder(this)
                .setView(root)
                .setCancelable(false)
                .create();
        mLoadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void suspendMenu() {
        mMainPager.setPagingEnabled(false);
        mTabLayout.setClickable(false);
        mTabLayout.getTabAt(FRAGMENT_TARGETS_INDEX).getCustomView().setVisibility(View.GONE);
        mTabLayout.getTabAt(FRAGMENT_BUYOUTS_INDEX).getCustomView().setVisibility(View.GONE);
        mTabLayout.getTabAt(FRAGMENT_SHARES_INDEX).getCustomView().setVisibility(View.GONE);
        mTabLayout.getTabAt(FRAGMENT_ABOUT_INDEX).getCustomView().setVisibility(View.GONE);
    }

    public void activateMenu() {
        mMainPager.setPagingEnabled(true);
        mTabLayout.setClickable(true);
        mTabLayout.getTabAt(FRAGMENT_TARGETS_INDEX).getCustomView().setVisibility(View.VISIBLE);
        mTabLayout.getTabAt(FRAGMENT_BUYOUTS_INDEX).getCustomView().setVisibility(View.VISIBLE);
        mTabLayout.getTabAt(FRAGMENT_SHARES_INDEX).getCustomView().setVisibility(View.VISIBLE);
        mTabLayout.getTabAt(FRAGMENT_ABOUT_INDEX).getCustomView().setVisibility(View.VISIBLE);
    }

    public void showAccountSettingFragment() {
        mFrgAccountSetting.show(getFragmentManager(), FRAGMENT_ACCOUNT_SETTINGS);
    }

    public IAPHelper getIAPHelper() {
        return mIAPHelper;
    }

    public void goToTab(int index) {
        mTabLayout.getTabAt(index).select();
    }

    public void updateCurrentTab() {
        ((ITabView) mAdapter.getItem(mTabLayout.getSelectedTabPosition())).updateView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        AppPreference.getInstance().loadFonts(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Get Views
        mMainPager = (CustomViewPager) findViewById(R.id.vpg_main);
        mTabLayout = (TabLayout) findViewById(R.id.tabbar);

        mFrgAccountSetting = AccountSettingFragment.newInstance();

        // Initialize
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular,
                (TextView) navigationView.findViewById(R.id.tv_nav_license));

        AppPreference.getInstance().getSession().loadPlutocrat();
        AppPreference.getInstance().onLoadInstanceState(savedInstanceState);

        mFontSpan =
                new CustomTypefaceSpan("", AppPreference.getInstance().getFont(AppPreference.FontType.Regular));

        Menu menu = navigationView.getMenu();
        for (int i=0, n=menu.size(); i<n; i++) {
            MenuItem menuItem = menu.getItem(i);

            SpannableString text = new SpannableString(menuItem.getTitle());
            text.setSpan(mFontSpan, 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        mTouchSlop = SLOP_PERIOD;
        mIAPHelper = new IAPHelper(this);
        mValidator = new FormValidationHelper();
        mValidator.addView("number_of_shares", "Number of shares", null);

        if(mAdapter == null) {
            mAdapter = new MenuPagerAdapter(getSupportFragmentManager());
        }
        if(presenter == null) {
            presenter = new MainMenuPresenter(this, this,
                    (IHomeView) mAdapter.getItem(FRAGMENT_HOME_INDEX),
                    (IAccountSettingView) mFrgAccountSetting);
        }
        mMainPager.setAdapter(mAdapter);
        mMainPager.setOffscreenPageLimit(mAdapter.getCount());
        mTabLayout.setupWithViewPager(mMainPager);

        // Add title & icon
        ColorStateList tabColorList = ContextCompat.getColorStateList(this, R.color.tab_item);
        for(int i=0, n=mAdapter.getCount(); i<n; i++) {
            ITabView view = (ITabView) mAdapter.getItem(i);
            TabLayout.Tab tab = mTabLayout.getTabAt(i);

            View root = getLayoutInflater().inflate(R.layout.menu_custom, null);
            // Get views
            TextView tvTitle = (TextView) root.findViewById(R.id.tv_title);
            ImageView imvIcon = (ImageView) root.findViewById(R.id.imv_icon);

            // Initiate
            tvTitle.setTextColor(tabColorList);
            Drawable drawable = ContextCompat.getDrawable(this, view.getIcon());
            imvIcon.setImageDrawable(drawable);
            tvTitle.setText(view.getTitle());

            AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular, tvTitle);

            tab.setCustomView(root);
        }

        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return ! mTabLayout.isClickable();
            }
        };
        LinearLayout tabStrip = ((LinearLayout)mTabLayout.getChildAt(0));
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setOnTouchListener(touchListener);
        }
        // Force to select first item
        mTabLayout.getTabAt(FRAGMENT_TARGETS_INDEX).select();
        mTabLayout.getTabAt(FRAGMENT_HOME_INDEX).select();

        createLoadingDialog();

        // Event Handler
        mIAPHelper.setIAPProcessListener(new IAPHelper.IAPProcessListener() {
            @Override
            public void onBuySuccess(int resultCode, IAPPurchaseModel model) {
                // TODO: Delete Debug
//                mIAPHelper.consume(model.purchaseToken);
            }

            @Override
            public void onBuyFailed(int resultCode) {
                Log.d(AppPreference.DEBUG_APP, "IAP Error Code:" + resultCode);
            }

            @Override
            public void onConsumed(int resultCode) {
                Log.d(AppPreference.DEBUG_APP, "IAP Consumed Code:" + resultCode);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchXDown = ev.getX();
                break;
            case MotionEvent.ACTION_UP:
                mTouchXUp = ev.getX();
                float deltaX = mTouchXUp - mTouchXDown;
                if(Math.abs(deltaX) > mTouchSlop) {
                    if(mTouchXUp > mTouchXDown) {
                        if(mMainPager.getCurrentItem() == FRAGMENT_HOME_INDEX) {
                            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                            drawer.openDrawer(GravityCompat.START);
                        }
                    }
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            boolean isPopupShowed = getFragmentManager().findFragmentByTag(FRAGMENT_ACCOUNT_SETTINGS) != null
                    || getFragmentManager().findFragmentByTag(FRAGMENT_FAQ) != null;
            if(! isPopupShowed && mTabLayout.getSelectedTabPosition() != FRAGMENT_HOME_INDEX) {
                goToTab(FRAGMENT_HOME_INDEX);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            android.app.FragmentTransaction t = getFragmentManager().beginTransaction();

            mFrgAccountSetting.show(t, FRAGMENT_ACCOUNT_SETTINGS);
        } else if (id == R.id.nav_faq) {
            android.app.FragmentTransaction t = getFragmentManager().beginTransaction();

            FAQFragment.newInstance().show(t, FRAGMENT_FAQ);
        } else if (id == R.id.nav_signout) {
            EventBus.getInstance().post(new SignOutEvent());

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getInstance().register(presenter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        EventBus.getInstance().unregister(presenter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mIAPHelper.onActivityResult(requestCode, resultCode, data);

        android.app.Fragment accountSettingFragment = getFragmentManager().findFragmentByTag(FRAGMENT_ACCOUNT_SETTINGS);
        if (accountSettingFragment != null) {
            accountSettingFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mIAPHelper.onDestroy();
        if(presenter != null) {
            presenter = null;
        }
        if(mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        AppPreference.getInstance().onSaveInstanceState(outState);

        super.onSaveInstanceState(outState);
    }

    /*
        Pager Adapter
         */
    private class MenuPagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<Fragment> mFragments;

        public MenuPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragments = new ArrayList<>();

            // Create fragments
            mFragments.add(HomeFragment.newInstance());
            mFragments.add(TargetFragment.newInstance());
            mFragments.add(BuyoutFragment.newInstance());
            mFragments.add(ShareFragment.newInstance());
            mFragments.add(AboutFragment.newInstance());
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }

    /*
    Implement View
     */
    @Override
    public void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.BUNDLE_INITIATE_LOGIN_STATE, ILoginView.ViewState.Login);
        startActivity(intent);

        finish();
    }

    @Override
    public void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void callInitiateDialog(TargetModel model, NewBuyoutModel newBuyout) {
        android.app.FragmentTransaction t = getFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        InitiateFragment.newInstance(model, newBuyout).show(t, FRAGMENT_INITIATE);
    }

    @Override
    public void goToShareFromInitiate() {
        ((DialogFragment) getFragmentManager().findFragmentByTag(FRAGMENT_INITIATE)).dismiss();

        mTabLayout.getTabAt(FRAGMENT_SHARES_INDEX).select();
    }

    @Override
    public void buyIAP(String sku, String payload) {
        try {
            mIAPHelper.buy(sku, payload);
        } catch (IAPException e) {
            toast(e.getMessage());
        }
    }

    @Override
    public void handleLoadingDialog(boolean isShow) {
        AlertDialog alertLoading = mLoadingDialog;
        DialogFragment initiatePage = (DialogFragment) getFragmentManager().findFragmentByTag(FRAGMENT_INITIATE);
        if(initiatePage != null && initiatePage instanceof InitiateFragment) {
            alertLoading = ((InitiateFragment) initiatePage).getLoadingDialog(null);
        }
        DialogFragment settingsPage = (DialogFragment) getFragmentManager().findFragmentByTag(FRAGMENT_ACCOUNT_SETTINGS);
        if(settingsPage != null && settingsPage instanceof AccountSettingFragment) {
            alertLoading = ((AccountSettingFragment) settingsPage).getLoadingDialog(null);
        }

        if(isShow) {
            alertLoading.show();
        } else {
            alertLoading.hide();
        }
    }

    @Override
    public void closeInitiatePage(boolean isSuccess) {
        if(isSuccess) {
            ((DialogFragment) getFragmentManager().findFragmentByTag(FRAGMENT_INITIATE)).dismiss();

            ((TargetFragment) mAdapter.getItem(FRAGMENT_TARGETS_INDEX)).updateList();
        }
    }

    @Override
    public void handleError(String title, String message, MetaModel meta) {
        Set<String> keys = meta.getKeys();
        for(String key : keys) {
            String name = mValidator.getName(key);
            if(name != null) {
                message = String.format("%s %s", name, meta.getValue(key));
            }
        }

        SpannableString spanTitle = new SpannableString(title);
        spanTitle.setSpan(mFontSpan, 0, spanTitle.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        SpannableString spanMessage = new SpannableString(message);
        spanMessage.setSpan(mFontSpan, 0, spanMessage.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        SpannableString negativeText = new SpannableString(getString(R.string.caption_close));
        negativeText.setSpan(mFontSpan, 0, negativeText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        new AlertDialog.Builder(this)
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
}
