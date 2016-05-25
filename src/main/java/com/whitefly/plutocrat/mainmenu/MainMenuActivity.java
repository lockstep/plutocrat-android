package com.whitefly.plutocrat.mainmenu;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
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

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.login.LoginActivity;
import com.whitefly.plutocrat.mainmenu.events.SignOutEvent;
import com.whitefly.plutocrat.mainmenu.fragments.AboutFragment;
import com.whitefly.plutocrat.mainmenu.fragments.BuyoutFragment;
import com.whitefly.plutocrat.mainmenu.fragments.FAQFragment;
import com.whitefly.plutocrat.mainmenu.fragments.HomeFragment;
import com.whitefly.plutocrat.mainmenu.fragments.InitiateFragment;
import com.whitefly.plutocrat.mainmenu.fragments.ShareFragment;
import com.whitefly.plutocrat.mainmenu.fragments.TargetFragment;
import com.whitefly.plutocrat.mainmenu.presenters.MainMenuPresenter;
import com.whitefly.plutocrat.mainmenu.views.IBuyoutView;
import com.whitefly.plutocrat.mainmenu.views.IMainMenuView;
import com.whitefly.plutocrat.mainmenu.views.ITabView;
import com.whitefly.plutocrat.mainmenu.views.ITargetView;
import com.whitefly.plutocrat.models.TargetModel;

import java.util.ArrayList;

public class MainMenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IMainMenuView {
    private static final int FRAGMENT_HOME_INDEX = 0;
    private static final int FRAGMENT_TARGETS_INDEX = 1;
    private static final int FRAGMENT_BUYOUTS_INDEX = 2;
    private static final int FRAGMENT_SHARES_INDEX = 3;
    private static final int FRAGMENT_ABOUT_INDEX = 4;

    private static final String FRAGMENT_INITIATE = "frg_initiate";
    private static final String FRAGMENT_FAQ = "frg_faq";

    // Attributes
    private MenuPagerAdapter mAdapter;
    private MainMenuPresenter presenter;

    // Views
    private ViewPager mMainPager;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Get Views
        mMainPager = (ViewPager) findViewById(R.id.vpg_main);
        mTabLayout = (TabLayout) findViewById(R.id.tabbar);

        // Initialize
        if(mAdapter == null) {
            mAdapter = new MenuPagerAdapter(getSupportFragmentManager());
        }
        if(presenter == null) {
            presenter = new MainMenuPresenter(this, this,
                    (ITargetView) mAdapter.getItem(FRAGMENT_TARGETS_INDEX),
                    (IBuyoutView) mAdapter.getItem(FRAGMENT_BUYOUTS_INDEX));
        }
        mMainPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mMainPager);
        // Add title & icon
        for(int i=0, n=mAdapter.getCount(); i<n; i++) {
            ITabView view = (ITabView) mAdapter.getItem(i);
            TabLayout.Tab tab = mTabLayout.getTabAt(i);

            View root = getLayoutInflater().inflate(R.layout.menu_custom, null);
            // Get views
            TextView tvTitle = (TextView) root.findViewById(R.id.tv_title);
            ImageView imvIcon = (ImageView) root.findViewById(R.id.imv_icon);

            // Initiate
            Drawable drawable = ContextCompat.getDrawable(this, view.getIcon());
            imvIcon.setImageDrawable(drawable);
            tvTitle.setText(view.getTitle());

            tab.setCustomView(root);
        }
        // Force to select first item
        mTabLayout.getTabAt(FRAGMENT_TARGETS_INDEX).select();
        mTabLayout.getTabAt(FRAGMENT_HOME_INDEX).select();
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
            // Handle the camera action
            Toast.makeText(this, "Manage Account", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_faq) {
            android.app.FragmentTransaction t = getFragmentManager().beginTransaction();

            FAQFragment.newInstance().show(t, FRAGMENT_FAQ);
        } else if (id == R.id.nav_signout) {
            EventBus.getInstance().post(new SignOutEvent());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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
}
