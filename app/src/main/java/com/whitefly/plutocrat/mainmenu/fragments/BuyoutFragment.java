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

import com.squareup.otto.Subscribe;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.mainmenu.adapters.BuyoutAdapter;
import com.whitefly.plutocrat.mainmenu.adapters.listeners.OnLoadmoreListener;
import com.whitefly.plutocrat.mainmenu.events.LoadBuyoutsEvent;
import com.whitefly.plutocrat.mainmenu.views.ITabView;
import com.whitefly.plutocrat.mainmenu.views.events.LoadBuyoutCompletedEvent;
import com.whitefly.plutocrat.models.BuyoutModel;
import com.whitefly.plutocrat.models.MetaModel;
import com.whitefly.plutocrat.models.UserModel;

import java.util.ArrayList;

/**
 * Created by Satjapot on 5/10/16 AD.
 */
public class BuyoutFragment extends Fragment implements ITabView {
    public static final String TITLE = "Buyouts";
    private static final int FIRST_TIME_PAGE = 0;
    private static final int FIRST_PAGE = 1;
    private static final int BUYOUT_USERS_PER_PAGE = 4;

    // Attributes
    private BuyoutAdapter mAdapter;
    private int cpage;

    // Views
    private RecyclerView mRvMain;
    private SwipeRefreshLayout mSRLMain, mSRLEmpty;
    private TextView mTvTitle, mTvEmpty;

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

    public void reload() {
        cpage = FIRST_PAGE;
        EventBus.getInstance().post(new LoadBuyoutsEvent(cpage, BUYOUT_USERS_PER_PAGE));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getInstance().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_buyout, container, false);

        // Get Views
        mRvMain = (RecyclerView) root.findViewById(R.id.rv_players);
        mSRLMain = (SwipeRefreshLayout) root.findViewById(R.id.srl_players);
        mSRLEmpty = (SwipeRefreshLayout) root.findViewById(R.id.srl_players_empty);
        mTvTitle = (TextView) root.findViewById(R.id.tv_buyout_sub_title);
        mTvEmpty = (TextView) root.findViewById(R.id.tv_empty);

        // Initiate
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Light, mTvTitle,
                (TextView) root.findViewById(R.id.tv_buyout_title));
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Bold, mTvEmpty);

        ArrayList<BuyoutModel> dataset = new ArrayList<>();
        dataset.add(null);
        mAdapter = new BuyoutAdapter(getActivity(), dataset);

        mRvMain.setHasFixedSize(true);
        mRvMain.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvMain.setNestedScrollingEnabled(true);
        mRvMain.setAdapter(mAdapter);

        mSRLMain.setVisibility(View.VISIBLE);

        updateView();

        // Get Adapter
        cpage = FIRST_TIME_PAGE;

        // Event Handler
        mSRLMain.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload();
            }
        });
        mSRLEmpty.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload();
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
    public void updateView() {
        UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();
        String subTitle = getActivity().getResources().getQuantityString(R.plurals.title_success_buyout,
                activeUser.numSuccessfulBuyout, activeUser.numSuccessfulBuyout);
        mTvTitle.setText(subTitle);
    }

    /*
    Bus event
     */
    @Subscribe
    public void onLoadBuyoutCompleted(LoadBuyoutCompletedEvent event) {
        ArrayList<BuyoutModel> items = event.getItems();
        MetaModel meta = event.getMeta();

        BuyoutAdapter adapter = (BuyoutAdapter) mRvMain.getAdapter();
        if(meta != null && meta.hasKey("current_page") && meta.getInt("current_page") == FIRST_PAGE) {
            if(items.size() == 0) {
                mSRLMain.setVisibility(View.GONE);
                mSRLEmpty.setVisibility(View.VISIBLE);
            } else {
                adapter.getDataSet().clear();
                mSRLMain.setVisibility(View.VISIBLE);
                mSRLEmpty.setVisibility(View.GONE);
            }
        }
        adapter.addItems(items);

        // Switch off all loading widget
        mSRLMain.setRefreshing(false);
        mSRLEmpty.setRefreshing(false);

        updateView();
    }
}
