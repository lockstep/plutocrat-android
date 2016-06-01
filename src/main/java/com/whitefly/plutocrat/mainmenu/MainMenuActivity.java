package com.whitefly.plutocrat.mainmenu;

import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.gson.Gson;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.exception.IAPException;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
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
import com.whitefly.plutocrat.mainmenu.views.IBuyoutView;
import com.whitefly.plutocrat.mainmenu.views.IHomeView;
import com.whitefly.plutocrat.mainmenu.views.IMainMenuView;
import com.whitefly.plutocrat.mainmenu.views.ITabView;
import com.whitefly.plutocrat.mainmenu.views.ITargetView;
import com.whitefly.plutocrat.models.IAPPurchaseModel;
import com.whitefly.plutocrat.models.TargetModel;

import java.util.ArrayList;
import java.util.UUID;

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

    private float mTouchXDown, mTouchXUp;
    private int mTouchSlop;

    // Views
    private CustomViewPager mMainPager;
    private TabLayout mTabLayout;

    public void suspendMenu() {
        mMainPager.setPagingEnabled(false);
        mTabLayout.getTabAt(FRAGMENT_TARGETS_INDEX).getCustomView().setVisibility(View.GONE);
        mTabLayout.getTabAt(FRAGMENT_BUYOUTS_INDEX).getCustomView().setVisibility(View.GONE);
        mTabLayout.getTabAt(FRAGMENT_SHARES_INDEX).getCustomView().setVisibility(View.GONE);
        mTabLayout.getTabAt(FRAGMENT_ABOUT_INDEX).getCustomView().setVisibility(View.GONE);
    }

    public void activateMenu() {
        mMainPager.setPagingEnabled(true);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Get Views
        mMainPager = (CustomViewPager) findViewById(R.id.vpg_main);
        mTabLayout = (TabLayout) findViewById(R.id.tabbar);

        mFrgAccountSetting = AccountSettingFragment.newInstance();

        // Initialize
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular,
                (TextView) navigationView.findViewById(R.id.tv_nav_license));

        CustomTypefaceSpan fontSpan =
                new CustomTypefaceSpan("", AppPreference.getInstance().getFont(AppPreference.FontType.Regular));
        Menu menu = navigationView.getMenu();
        for (int i=0, n=menu.size(); i<n; i++) {
            MenuItem menuItem = menu.getItem(i);

            SpannableString text = new SpannableString(menuItem.getTitle());
            text.setSpan(fontSpan, 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        mTouchSlop = SLOP_PERIOD;
        mIAPHelper = new IAPHelper(this);

        if(mAdapter == null) {
            mAdapter = new MenuPagerAdapter(getSupportFragmentManager());
        }
        if(presenter == null) {
            presenter = new MainMenuPresenter(this, this,
                    (IHomeView) mAdapter.getItem(FRAGMENT_HOME_INDEX),
                    (ITargetView) mAdapter.getItem(FRAGMENT_TARGETS_INDEX),
                    (IBuyoutView) mAdapter.getItem(FRAGMENT_BUYOUTS_INDEX));
        }
        mMainPager.setAdapter(mAdapter);
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
        // Force to select first item
        mTabLayout.getTabAt(FRAGMENT_TARGETS_INDEX).select();
        mTabLayout.getTabAt(FRAGMENT_HOME_INDEX).select();


        // Event Handler
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getCustomView().getVisibility() == View.GONE) {
                    mTabLayout.getTabAt(FRAGMENT_HOME_INDEX).select();
                } else {
                    mMainPager.setCurrentItem(tab.getPosition());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mIAPHelper.setIAPProcessListener(new IAPHelper.IAPProcessListener() {
            @Override
            public void onBuySuccess(int resultCode, IAPPurchaseModel model) {
                // TODO: Delete Debug
                mIAPHelper.consume(model.purchaseToken);
            }

            @Override
            public void onBuyFailed(int resultCode) {

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
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
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
    public void callInitiateDialog(TargetModel model) {
        android.app.FragmentTransaction t = getFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        InitiateFragment.newInstance(model).show(t, FRAGMENT_INITIATE);
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
}
