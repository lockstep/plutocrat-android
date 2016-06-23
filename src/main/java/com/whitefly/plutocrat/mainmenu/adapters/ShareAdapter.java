package com.whitefly.plutocrat.mainmenu.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.helpers.IAPHelper;
import com.whitefly.plutocrat.mainmenu.events.BuySharesEvent;
import com.whitefly.plutocrat.models.IAPPurchaseModel;
import com.whitefly.plutocrat.models.ShareBundleModel;

import java.util.ArrayList;

/**
 * Created by Satjapot on 5/17/16 AD.
 */
public class ShareAdapter extends RecyclerView.Adapter<ShareAdapter.ViewHolder> {
    private static final int AMOUNT_QTY_SMALL = 1;
    private static final int AMOUNT_QTY_MEDIUM = 50;

    // Attributes
    private Context mContext;
    private ArrayList<ShareBundleModel> mDataSet;
    private String mShareCaption, mSharesCaption;
    private String mCheckCaption, mGetCaption, mBuyCaption;
    private Drawable mBundleSmall, mBundleMedium, mBundleLarge;
    private View.OnClickListener mBuyClick;

    // Getter Methods
    public ArrayList<ShareBundleModel> getDataSet() {
        return mDataSet;
    }

    // Constructor
    public ShareAdapter(Context context, ArrayList<ShareBundleModel> dataset) {
        mContext = context;
        mDataSet = dataset;

        mShareCaption = mContext.getString(R.string.caption_share);
        mSharesCaption = mContext.getString(R.string.caption_shares);
        mGetCaption = mContext.getString(R.string.caption_get);
        mCheckCaption = mContext.getString(R.string.caption_check);
        mBuyCaption = mContext.getString(R.string.caption_acquire);

        mBundleSmall = ContextCompat.getDrawable(context, R.drawable.single_share);
        mBundleMedium = ContextCompat.getDrawable(context, R.drawable.small_batch_share);
        mBundleLarge = ContextCompat.getDrawable(context, R.drawable.large_batch_share);

        mBuyClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareBundleModel model = (ShareBundleModel) v.getTag();
                EventBus.getInstance().post(new BuySharesEvent(model));
            }
        };
    }

    // Methods

    @Override
    public ShareAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_share_bundle, parent, false);
        ViewHolder vh = new ViewHolder(root);

        vh.tvQty = (TextView) root.findViewById(R.id.tv_share_qty);
        vh.tvShareCaption = (TextView) root.findViewById(R.id.tv_share_caption);
        vh.tvPrice = (TextView) root.findViewById(R.id.tv_price);
        vh.tvTotal = (TextView) root.findViewById(R.id.tv_price_total);
        vh.btnBuy = (Button) root.findViewById(R.id.btn_acquire);
        vh.rloBundle = (RelativeLayout) root.findViewById(R.id.rlo_bundle_image);

        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular,
                vh.tvQty, vh.tvPrice, vh.tvTotal, vh.btnBuy);

        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Light,
                vh.tvShareCaption);

        vh.btnBuy.setOnClickListener(mBuyClick);

        return vh;
    }

    @Override
    public void onBindViewHolder(ShareAdapter.ViewHolder holder, int position) {
        // Set value to views
        ShareBundleModel model = mDataSet.get(position);

        if(model != null) {
            holder.tvQty.setText(String.valueOf(model.qty));
            holder.tvPrice.setText(model.getPrice());
            holder.tvTotal.setText(model.getTotalPrice());

            if(model.qty > AMOUNT_QTY_MEDIUM) {
                holder.tvShareCaption.setText(mSharesCaption);
                holder.rloBundle.setBackground(mBundleLarge);
            } else if(model.qty > AMOUNT_QTY_SMALL) {
                holder.tvShareCaption.setText(mSharesCaption);
                holder.rloBundle.setBackground(mBundleMedium);
            } else {
                holder.tvShareCaption.setText(mShareCaption);
                holder.rloBundle.setBackground(mBundleSmall);
            }

            if(model.state == ShareBundleModel.State.Checking) {
                holder.btnBuy.setText(mCheckCaption);
                holder.btnBuy.setClickable(false);
                holder.btnBuy.setEnabled(false);
            } else if(model.state == ShareBundleModel.State.Get) {
                holder.btnBuy.setText(mGetCaption);
                holder.btnBuy.setClickable(true);
                holder.btnBuy.setEnabled(true);
            } else if(model.state == ShareBundleModel.State.Buy) {
                holder.btnBuy.setText(mBuyCaption);
                holder.btnBuy.setClickable(true);
                holder.btnBuy.setEnabled(true);
            }

            holder.btnBuy.setTag(model);
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    /*
        Inner Class
         */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvQty, tvShareCaption, tvPrice, tvTotal;
        public Button btnBuy;
        public RelativeLayout rloBundle;

        public ViewHolder(View v) {
            super(v);
        }
    }
}
