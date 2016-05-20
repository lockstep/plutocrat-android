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
import android.widget.Toast;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.mainmenu.adapters.TargetAdapter;
import com.whitefly.plutocrat.mainmenu.adapters.listeners.OnLoadmoreListener;
import com.whitefly.plutocrat.mainmenu.events.EngageClickEvent;
import com.whitefly.plutocrat.mainmenu.events.LoadTargetsEvent;
import com.whitefly.plutocrat.mainmenu.views.ITabView;
import com.whitefly.plutocrat.mainmenu.views.ITargetView;
import com.whitefly.plutocrat.models.TargetModel;

import java.util.ArrayList;

/**
 * Created by Satjapot on 5/10/16 AD.
 */
public class TargetFragment extends Fragment implements ITabView, ITargetView {
    public static final String TITLE = "Targets";
    public static final int PER_PAGE = 4;

    public enum HeaderState {
        Plutocrat, NoPlutocrat
    }

    // Attributes
    private HeaderState mState;
    private TargetAdapter mAdapter;
    private int cpage;

    // Views
    private RecyclerView mRvMain;
    private SwipeRefreshLayout mSRLMain;
    private LinearLayout mLloPlutocrat, mLloNoPlutocrat;
    private Button mBtnEngage;

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
    private void changeState(HeaderState state) {
        mState = state;
        switch (mState) {
            case Plutocrat:
                mLloPlutocrat.setVisibility(View.VISIBLE);
                mLloNoPlutocrat.setVisibility(View.GONE);
                break;
            case NoPlutocrat:
                mLloPlutocrat.setVisibility(View.GONE);
                mLloNoPlutocrat.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_target, container, false);

        // Get Views
        mRvMain = (RecyclerView) root.findViewById(R.id.rv_players);
        mSRLMain = (SwipeRefreshLayout) root.findViewById(R.id.srl_players);
        mLloPlutocrat = (LinearLayout) root.findViewById(R.id.llo_targets_plutocrat);
        mLloNoPlutocrat = (LinearLayout) root.findViewById(R.id.llo_targets_noplutocrat);
        mBtnEngage = (Button) root.findViewById(R.id.btn_player_engage);

        // Initiate
        ArrayList<TargetModel> dataset = new ArrayList<>();
        dataset.add(null);

        mRvMain.setHasFixedSize(true);
        mRvMain.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvMain.setNestedScrollingEnabled(true);
        mAdapter = new TargetAdapter(getActivity(), dataset);
        mRvMain.setAdapter(mAdapter);

        changeState(HeaderState.Plutocrat);

        // Get Adapter
        cpage = 1;
        EventBus.getInstance().post(new LoadTargetsEvent(cpage, PER_PAGE));

        // Event Handler
        mSRLMain.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Loading new list
                cpage = 1;
                EventBus.getInstance().post(new LoadTargetsEvent(cpage, PER_PAGE));
            }
        });
        mAdapter.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoad() {
                EventBus.getInstance().post(new LoadTargetsEvent(++cpage, PER_PAGE));
            }
        });
        // Debug
        mLloPlutocrat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeState(HeaderState.NoPlutocrat);
            }
        });
        mLloNoPlutocrat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeState(HeaderState.Plutocrat);
            }
        });
        mBtnEngage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TargetModel model = new TargetModel();
                model.name = "Aaron Pinchai";
                model.numBuyouts = 35;
                model.numThreats = 7;
                model.daySurvived = 119;
                model.status = TargetModel.TargetStatus.Normal;
                model.isPlutocrat = true;
                model.picProfile = 0;

                EventBus.getInstance().post(new EngageClickEvent(model));
            }
        });

        return root;
    }

    @Override
    public int getIcon() {
        return R.drawable.icon_menu_default;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public void setTargetList(ArrayList<TargetModel> items) {
        if(cpage == 1) {
            mAdapter.getDataSet().clear();
        }
        mAdapter.addItems(items);

        // Switch off all loading widget
        mSRLMain.setRefreshing(false);
    }
}
