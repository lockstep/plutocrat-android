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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.mainmenu.adapters.TargetAdapter;
import com.whitefly.plutocrat.mainmenu.adapters.listeners.OnLoadmoreListener;
import com.whitefly.plutocrat.mainmenu.events.EngageClickEvent;
import com.whitefly.plutocrat.mainmenu.events.GetPlutocratEvent;
import com.whitefly.plutocrat.mainmenu.events.LoadTargetsEvent;
import com.whitefly.plutocrat.mainmenu.views.ITabView;
import com.whitefly.plutocrat.mainmenu.views.ITargetView;
import com.whitefly.plutocrat.models.MetaModel;
import com.whitefly.plutocrat.models.TargetModel;

import java.util.ArrayList;

/**
 * Created by Satjapot on 5/10/16 AD.
 */
public class TargetFragment extends Fragment implements ITabView, ITargetView {
    public static final String TITLE = "Targets";
    private static final int FIRST_PAGE = 1;
    private static final int TARGET_USERS_PER_PAGE = 4;

    public enum HeaderState {
        Plutocrat, NoPlutocrat
    }

    // Attributes
    private HeaderState mState;
    private TargetAdapter mAdapter;
    private int cpage;

    // Views
    private RecyclerView mRvMain;
    private SwipeRefreshLayout mSRLMain, mSRLEmpty;
    private LinearLayout mLloPlutocrat, mLloNoPlutocrat;
    private Button mBtnEngage;
    private TextView mTvPlutocratNickname, mTvPlutocratName, mTvPlutocratBuyouts, mTvEmpty;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static TargetFragment newInstance() {
        TargetFragment fragment = new TargetFragment();
        return fragment;
    }

    // Methods
    private void changeState(HeaderState state, TargetModel user) {
        mState = state;
        switch (mState) {
            case Plutocrat:
                mLloPlutocrat.setVisibility(View.VISIBLE);
                mLloNoPlutocrat.setVisibility(View.GONE);

                mTvPlutocratNickname.setText(user.getNickName());
                mTvPlutocratName.setText(user.name);
                mTvPlutocratBuyouts.setText(
                        String.format(getString(R.string.value_plutocrat_buyouts), user.numSuccessfulBuyout));
                break;
            case NoPlutocrat:
                mLloPlutocrat.setVisibility(View.GONE);
                mLloNoPlutocrat.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void updateList() {
        mAdapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_target, container, false);

        // Get Views
        mRvMain = (RecyclerView) root.findViewById(R.id.rv_players);
        mSRLMain = (SwipeRefreshLayout) root.findViewById(R.id.srl_players);
        mSRLEmpty = (SwipeRefreshLayout) root.findViewById(R.id.srl_players_empty);
        mLloPlutocrat = (LinearLayout) root.findViewById(R.id.llo_targets_plutocrat);
        mLloNoPlutocrat = (LinearLayout) root.findViewById(R.id.llo_targets_noplutocrat);
        mTvPlutocratNickname = (TextView) root.findViewById(R.id.tv_plutocrat_profile_nickname);
        mTvPlutocratName = (TextView) root.findViewById(R.id.tv_plutocrat_name);
        mTvPlutocratBuyouts = (TextView) root.findViewById(R.id.tv_plutocrat_buyouts);
        mTvEmpty = (TextView) root.findViewById(R.id.tv_empty);
        mBtnEngage = (Button) root.findViewById(R.id.btn_player_engage);

        // Initiate
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular,
                mTvPlutocratNickname, mTvPlutocratName, mTvPlutocratBuyouts, mBtnEngage,
                (TextView) root.findViewById(R.id.tv_plutocrat_caption));
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Bold, mTvEmpty);
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Light,
                (TextView) root.findViewById(R.id.tv_no_plutocrat));

        ArrayList<TargetModel> dataset = new ArrayList<>();
        dataset.add(null);

        mRvMain.setHasFixedSize(true);
        mRvMain.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvMain.setNestedScrollingEnabled(true);
        mAdapter = new TargetAdapter(getActivity(), dataset);
        mRvMain.setAdapter(mAdapter);

        EventBus.getInstance().post(new GetPlutocratEvent());

        updateView();

        // Get Adapter
        cpage = FIRST_PAGE;
        EventBus.getInstance().post(new LoadTargetsEvent(cpage, TARGET_USERS_PER_PAGE));

        // Event Handler
        mSRLMain.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Loading new list
                cpage = FIRST_PAGE;
                EventBus.getInstance().post(new LoadTargetsEvent(cpage, TARGET_USERS_PER_PAGE));
            }
        });
        mSRLEmpty.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Loading new list
                cpage = FIRST_PAGE;
                EventBus.getInstance().post(new LoadTargetsEvent(cpage, TARGET_USERS_PER_PAGE));
            }
        });
        mAdapter.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoad() {
                EventBus.getInstance().post(new LoadTargetsEvent(++cpage, TARGET_USERS_PER_PAGE));
            }
        });
        mBtnEngage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TargetModel model = AppPreference.getInstance().getSession().getPlutocrat();

                EventBus.getInstance().post(new EngageClickEvent(model));
            }
        });

        return root;
    }

    @Override
    public int getIcon() {
        return R.drawable.icon_menu_targets;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public void updateView() {

    }

    @Override
    public void setTargetList(ArrayList<TargetModel> items, MetaModel meta) {
        if(meta != null && meta.hasKey("current_page") && meta.getInt("current_page") == FIRST_PAGE) {
            if(items.size() == 0) {
                mSRLMain.setVisibility(View.GONE);
                mSRLEmpty.setVisibility(View.VISIBLE);
            } else {
                mAdapter.getDataSet().clear();
                mSRLMain.setVisibility(View.VISIBLE);
                mSRLEmpty.setVisibility(View.GONE);
            }
        }
        mAdapter.addItems(items);

        // Switch off all loading widget
        mSRLMain.setRefreshing(false);
        mSRLEmpty.setRefreshing(false);
    }

    @Override
    public void setPlutocrat(TargetModel user) {
        HeaderState state = user == null ? HeaderState.NoPlutocrat : HeaderState.Plutocrat;
        changeState(state, user);
    }
}
