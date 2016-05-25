package com.whitefly.plutocrat.mainmenu.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.mainmenu.events.BuySharesEvent;
import com.whitefly.plutocrat.mainmenu.views.ITabView;

/**
 * Created by Satjapot on 5/10/16 AD.
 */
public class ShareFragment extends Fragment implements ITabView, View.OnClickListener {
    public static final String TITLE = "Shares";
    public static final String CURRENCY_FORMAT = "$%d";
    private static final int PRICE_PER_SHARE_FOR_BUNDLE_SMALL = 25;
    private static final int PRICE_PER_SHARE_FOR_BUNDLE_MEDIUM = 20;
    private static final int PRICE_PER_SHARE_FOR_BUNDLE_LARGE = 15;

    private static final int DEBUG_UNUSED_SHARES = 12;

    // Attributes

    // View
    private TextView mTvTitle;
    private TextView mTvPriceBundleSmall, mTvPriceBundleMedium, mTvPriceBundleLarge;
    private EditText mEdtQuantityBundleSmall, mEdtQuantityBundleMedium, mEdtQuantityLarge;
    private Button mBtnAcquireBundleSmall, mBtnAcquireBundleMedium, mBtnAcquireBundleLarge;

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
        mEdtQuantityBundleSmall = (EditText) root.findViewById(R.id.edt_share_small);
        mEdtQuantityBundleMedium = (EditText) root.findViewById(R.id.edt_share_medium);
        mEdtQuantityLarge = (EditText) root.findViewById(R.id.edt_share_large);
        mBtnAcquireBundleSmall = (Button) root.findViewById(R.id.btn_acquire_small);
        mBtnAcquireBundleMedium = (Button) root.findViewById(R.id.btn_acquire_medium);
        mBtnAcquireBundleLarge = (Button) root.findViewById(R.id.btn_acquire_large);
        mTvPriceBundleSmall = (TextView) root.findViewById(R.id.tv_price_small);
        mTvPriceBundleMedium = (TextView) root.findViewById(R.id.tv_price_medium);
        mTvPriceBundleLarge = (TextView) root.findViewById(R.id.tv_price_large);

        // Initialize
        mTvTitle.setText(String.format(getActivity().getString(R.string.title_unused_shares), DEBUG_UNUSED_SHARES));
        mTvPriceBundleSmall.setText(String.format(CURRENCY_FORMAT, PRICE_PER_SHARE_FOR_BUNDLE_SMALL));
        mTvPriceBundleMedium.setText(String.format(CURRENCY_FORMAT, PRICE_PER_SHARE_FOR_BUNDLE_MEDIUM));
        mTvPriceBundleLarge.setText(String.format(CURRENCY_FORMAT, PRICE_PER_SHARE_FOR_BUNDLE_LARGE));

        // Event handler
        mBtnAcquireBundleSmall.setOnClickListener(this);
        mBtnAcquireBundleMedium.setOnClickListener(this);
        mBtnAcquireBundleLarge.setOnClickListener(this);

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
    public void onClick(View v) {
        int price;
        int qty;
        String strQty;

        if(v == mBtnAcquireBundleLarge) {
            price = PRICE_PER_SHARE_FOR_BUNDLE_LARGE;
            strQty = mEdtQuantityLarge.getText().toString();
        } else if(v == mBtnAcquireBundleMedium) {
            price = PRICE_PER_SHARE_FOR_BUNDLE_MEDIUM;
            strQty = mEdtQuantityBundleMedium.getText().toString();
        } else {
            price = PRICE_PER_SHARE_FOR_BUNDLE_SMALL;
            strQty = mEdtQuantityBundleSmall.getText().toString();
        }

        try{
            qty = Integer.parseInt(strQty);
            EventBus.getInstance().post(new BuySharesEvent(qty, price));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), R.string.err_input_shares_number, Toast.LENGTH_SHORT).show();
        }
    }
}
