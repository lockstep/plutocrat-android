package com.whitefly.plutocrat.mainmenu.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.mainmenu.adapters.ShareAdapter;
import com.whitefly.plutocrat.mainmenu.events.BuySharesEvent;
import com.whitefly.plutocrat.mainmenu.views.ITabView;
import com.whitefly.plutocrat.models.ShareBundleModel;

import java.util.ArrayList;

/**
 * Created by Satjapot on 5/10/16 AD.
 */
public class ShareFragment extends Fragment implements ITabView {
    public static final String TITLE = "Shares";
    public static final String CURRENCY_FORMAT = "$%d";

    private static final int DEBUG_UNUSED_SHARES = 12;

    // Attributes

    // View
    private TextView mTvTitle;
    private RecyclerView mRvMain;

    // Methods
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static ShareFragment newInstance() {
        ShareFragment fragment = new ShareFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_share, container, false);

        mTvTitle = (TextView) root.findViewById(R.id.tv_title_shares);
        mRvMain = (RecyclerView) root.findViewById(R.id.rv_share_bundles);

        // Initialize
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Light, mTvTitle);

        mTvTitle.setText(String.format(getString(R.string.title_unused_shares), 32));

        mRvMain.setHasFixedSize(true);
        mRvMain.setLayoutManager(new LinearLayoutManager(getContext()));
        ArrayList<ShareBundleModel> list = new ArrayList<>();
        list.add(new ShareBundleModel(null, 1, 25));
        list.add(new ShareBundleModel(null, 5, 25));
        list.add(new ShareBundleModel(null, 10, 20));
        list.add(new ShareBundleModel(null, 50, 15));
        ShareAdapter adapter = new ShareAdapter(getActivity(), list);
        mRvMain.setAdapter(adapter);

        return root;
    }

    @Override
    public int getIcon() {
        return R.drawable.icon_menu_shares;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public void updateView() {

    }
}
