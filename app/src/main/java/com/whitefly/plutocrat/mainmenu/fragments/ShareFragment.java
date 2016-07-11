package com.whitefly.plutocrat.mainmenu.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.whitefly.plutocrat.helpers.IAPHelper;
import com.whitefly.plutocrat.mainmenu.MainMenuActivity;
import com.whitefly.plutocrat.mainmenu.adapters.ShareAdapter;
import com.whitefly.plutocrat.mainmenu.events.SendReceiptCompleteEvent;
import com.whitefly.plutocrat.mainmenu.views.ITabView;
import com.whitefly.plutocrat.models.IAPItemDetailModel;
import com.whitefly.plutocrat.models.IAPPurchaseModel;
import com.whitefly.plutocrat.models.ShareBundleModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Satjapot on 5/10/16 AD.
 */
public class ShareFragment extends Fragment implements ITabView {
    public static final String TITLE = "Shares";
    public static final String ITEM_TEST1 = "com.whitefly.plutocrat.iap.test_item";
    public static final String ITEM_TEST2 = "com.whitefly.plutocrat.iap.test_item2";

    // Attributes
    private IAPHelper mIAPHelper;
    private ShareAdapter mAdapter;
    private ArrayList<ShareBundleModel> mDataSet;

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
        View root = inflater.inflate(R.layout.fragment_share, container, false);

        mTvTitle = (TextView) root.findViewById(R.id.tv_share_sub_title);
        mRvMain = (RecyclerView) root.findViewById(R.id.rv_share_bundles);

        // Initialize
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Light, mTvTitle,
                (TextView) root.findViewById(R.id.tv_share_title));

        String[] itemArray = getActivity().getResources().getStringArray(R.array.iap_item_list);


        mRvMain.setHasFixedSize(true);
        mRvMain.setLayoutManager(new LinearLayoutManager(getContext()));
        mDataSet = new ArrayList<>();
        mAdapter =
                new ShareAdapter(getActivity(), mDataSet);
        mRvMain.setAdapter(mAdapter);

        mIAPHelper = ((MainMenuActivity) getActivity()).getIAPHelper();
        mIAPHelper.addIAPProcessListener(new IAPHelper.IAPProcessListener() {
            @Override
            public void onBuySuccess(int resultCode, IAPPurchaseModel model) {

            }

            @Override
            public void onBuyFailed(int resultCode) {

            }

            @Override
            public void onConsumed(int resultCode) {
                mIAPHelper.getPurchased();
            }

            @Override
            public void onProcessing(int methodId, ProcessState state) {

            }

            @Override
            public void onPurchasedItemLoaded(int resultCode, ArrayList<IAPPurchaseModel> items) {
                if(resultCode == IAPHelper.BILLING_RESPONSE_RESULT_OK) {
                    ArrayList<ShareBundleModel> list = mAdapter.getDataSet();
                    for(ShareBundleModel shareItem : list) {
                        shareItem.state = ShareBundleModel.State.Buy;
                        for(IAPPurchaseModel purchasedItem : items) {
                            if(shareItem.sku.equals(purchasedItem.productId)) {
                                shareItem.state = ShareBundleModel.State.Get;
                                shareItem.purchaseData = purchasedItem;
                                break;
                            }
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onItemDetailsLoaded(int resultCode, ArrayList<IAPItemDetailModel> itemDetails) {
                if(resultCode == IAPHelper.BILLING_RESPONSE_RESULT_OK) {
                    mDataSet.clear();
                    for(IAPItemDetailModel detail : itemDetails) {
                        mDataSet.add(new ShareBundleModel(detail));
                    }
                    Collections.sort(mDataSet, new Comparator<ShareBundleModel>() {
                        @Override
                        public int compare(ShareBundleModel t1, ShareBundleModel t2) {
                            return t1.qty - t2.qty;
                        }
                    });
                    mAdapter.notifyDataSetChanged();

                    mIAPHelper.getPurchased();
                }
            }
        });
        mIAPHelper.getItemDetails(itemArray);

        updateView();

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
        mTvTitle.setText(String.format(getString(R.string.title_unused_shares),
                AppPreference.getInstance().getSession().getActiveUser().numAvailableShares));
    }

    @Subscribe
    public void onSendReceiptComplete(SendReceiptCompleteEvent event) {
        if(event.getState() == SendReceiptCompleteEvent.State.Succeed) {
            updateView();
            mIAPHelper.consume(event.getPurchasedItem().purchaseToken);
        }
     }
}
