package com.whitefly.plutocrat.mainmenu.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    private static final int SHARE_1_PRICE = 25;
    private static final int SHARE_50_PRICE = 20;
    private static final int SHARE_100_PRICE = 15;

    private static final int DEBUG_UNUSED_SHARES = 12;

    // Attributes

    // View
    private TextView mTvTitle;
    private EditText mEdtQty1, mEdtQty50, mEdtQty100;
    private Button mBtnAcquire1, mBtnAcquire50, mBtnAcquire100;

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
        mEdtQty1 = (EditText) root.findViewById(R.id.edt_share_1);
        mEdtQty50 = (EditText) root.findViewById(R.id.edt_share_50);
        mEdtQty100 = (EditText) root.findViewById(R.id.edt_share_100);
        mBtnAcquire1 = (Button) root.findViewById(R.id.btn_acquire_1);
        mBtnAcquire50 = (Button) root.findViewById(R.id.btn_acquire_50);
        mBtnAcquire100 = (Button) root.findViewById(R.id.btn_acquire_100);

        // Initialize
        mTvTitle.setText(String.format(getActivity().getString(R.string.title_unused_shares), DEBUG_UNUSED_SHARES));

        // Event handler
        mBtnAcquire1.setOnClickListener(this);
        mBtnAcquire50.setOnClickListener(this);
        mBtnAcquire100.setOnClickListener(this);

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

        if(v == mBtnAcquire100) {
            price = SHARE_100_PRICE;
            strQty = mEdtQty100.getText().toString();
        } else if(v == mBtnAcquire50) {
            price = SHARE_50_PRICE;
            strQty = mEdtQty50.getText().toString();
        } else {
            price = SHARE_1_PRICE;
            strQty = mEdtQty1.getText().toString();
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
