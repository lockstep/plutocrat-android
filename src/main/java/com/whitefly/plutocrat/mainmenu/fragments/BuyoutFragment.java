package com.whitefly.plutocrat.mainmenu.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.mainmenu.adapters.BuyoutAdapter;
import com.whitefly.plutocrat.mainmenu.adapters.listeners.OnLoadmoreListener;
import com.whitefly.plutocrat.mainmenu.events.LoadBuyoutsEvent;
import com.whitefly.plutocrat.mainmenu.views.IBuyoutView;
import com.whitefly.plutocrat.mainmenu.views.ITabView;
import com.whitefly.plutocrat.models.BuyoutModel;

import java.util.ArrayList;

/**
 * Created by Satjapot on 5/10/16 AD.
 */
public class BuyoutFragment extends Fragment implements ITabView, IBuyoutView {
    public static final String TITLE = "Buyouts";
    private static final int FIRST_PAGE = 1;
    private static final int BUYOUT_USERS_PER_PAGE = 4;
    private static final int DEBUG_SUCCESSFUL_BUYOUTS = 32;

    // Attributes
    private BuyoutAdapter mAdapter;
    private int cpage;

    // Views
    private RecyclerView mRvMain;
    private SwipeRefreshLayout mSRLMain;
    private TextView mTitle;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static BuyoutFragment newInstance() {
        BuyoutFragment fragment = new BuyoutFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_buyout, container, false);

        // Get Views
        mRvMain = (RecyclerView) root.findViewById(R.id.rv_players);
        mSRLMain = (SwipeRefreshLayout) root.findViewById(R.id.srl_players);
        mTitle = (TextView) root.findViewById(R.id.tv_title_buyout);

        // Initiate
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Light, mTitle);
        mTitle.setText(String.format(getActivity().getString(R.string.title_success_buyout), DEBUG_SUCCESSFUL_BUYOUTS));

        ArrayList<BuyoutModel> dataset = new ArrayList<>();
        dataset.add(null);

        mRvMain.setHasFixedSize(true);
        mRvMain.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvMain.setNestedScrollingEnabled(true);
        mAdapter = new BuyoutAdapter(getActivity(), dataset);
        mRvMain.setAdapter(mAdapter);

        // Get Adapter
        cpage = FIRST_PAGE;
        EventBus.getInstance().post(new LoadBuyoutsEvent(cpage, BUYOUT_USERS_PER_PAGE));

        // Event Handler
        mSRLMain.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Loading new list
                cpage = FIRST_PAGE;
                EventBus.getInstance().post(new LoadBuyoutsEvent(cpage, BUYOUT_USERS_PER_PAGE));
            }
        });
        mAdapter.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoad() {
                EventBus.getInstance().post(new LoadBuyoutsEvent(++cpage, BUYOUT_USERS_PER_PAGE));
            }
        });

        return root;
    }

    @Override
    public int getIcon() {
        return R.drawable.icon_menu_buyouts;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public void setTargetList(ArrayList<BuyoutModel> items) {
        if(cpage == FIRST_PAGE) {
            mAdapter.getDataSet().clear();
        }
        mAdapter.addItems(items);

        // Switch off all loading widget
        mSRLMain.setRefreshing(false);
    }
}
