package com.whitefly.plutocrat.mainmenu.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.mainmenu.adapters.listeners.OnLoadmoreListener;
import com.whitefly.plutocrat.models.BuyoutModel;
import com.whitefly.plutocrat.models.TargetModel;

import java.util.ArrayList;

/**
 * Created by Satjapot on 5/17/16 AD.
 */
public class BuyoutAdapter extends RecyclerView.Adapter<BuyoutAdapter.ViewHolder> {
    private static final int VIEW_TYPE_ITEM = 1;
    private static final int VIEW_TYPE_LOADMORE = 2;

    // Attributes
    private Context mContext;
    private ArrayList<BuyoutModel> mDataSet;
    private String mInitiateCaption, mThreatCaption, mPeriodFormat;
    private String mPendingCaption, mSucceedCaption, mFailedCaption;
    private String mAttackingCaption, mEliminatedCaption;
    private int mGreyColor, mRedColor, mGreenColor;
    private OnLoadmoreListener mLoadMoreListener;

    // Getter Methods
    public ArrayList<BuyoutModel> getDataSet() {
        return mDataSet;
    }

    // Setter Methods
    public void setOnLoadmoreListener(OnLoadmoreListener listener) {
        mLoadMoreListener = listener;
    }

    // Constructor
    public BuyoutAdapter(Context context, ArrayList<BuyoutModel> dataset) {
        mContext = context;
        mDataSet = dataset;

        mInitiateCaption = mContext.getString(R.string.caption_action_initiate);
        mThreatCaption = mContext.getString(R.string.caption_action_threat);
        mPeriodFormat = mContext.getString(R.string.value_period);
        mPendingCaption = mContext.getString(R.string.status_pending);
        mSucceedCaption = mContext.getString(R.string.status_succeed);
        mFailedCaption = mContext.getString(R.string.status_failed);
        mAttackingCaption = mContext.getString(R.string.caption_attacking);
        mEliminatedCaption = mContext.getString(R.string.caption_eliminated);

        mGreyColor = ContextCompat.getColor(mContext, R.color.colorGrey);
        mRedColor = ContextCompat.getColor(mContext, R.color.colorRed);
        mGreenColor = ContextCompat.getColor(mContext, R.color.colorGreen);
    }

    // Methods
    public void addItems(ArrayList<BuyoutModel> items) {
        // Delete null row
        if(mDataSet.size() > 0) {
            BuyoutModel modelNull = mDataSet.get(mDataSet.size() - 1);
            if (modelNull == null) {
                mDataSet.remove(modelNull);
            }
        }

        // Add a list to this list
        for (BuyoutModel item: items) {
            mDataSet.add(item);
        }
        notifyDataSetChanged();
    }

    @Override
    public BuyoutAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder vh;
        if(viewType == VIEW_TYPE_ITEM) {
            View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player, parent, false);
            vh = new ViewHolder(root);
            vh.imvProfile = (ImageView) root.findViewById(R.id.imv_profile);
            vh.tvProfile = (TextView) root.findViewById(R.id.tv_profile_nickname);
            vh.tvName = (TextView) root.findViewById(R.id.tv_player_name);
            vh.tvAction = (TextView) root.findViewById(R.id.tv_player_buyouts);
            vh.tvPeroid = (TextView) root.findViewById(R.id.tv_player_threats);
            vh.tvStatus = (TextView) root.findViewById(R.id.tv_player_daysurvived);
            vh.btnEngage = (Button) root.findViewById(R.id.btn_player_engage);
            vh.tvGameStatus = (TextView) root.findViewById(R.id.tv_player_game_status);
        } else {
            View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loadmore, parent, false);
            vh = new ViewHolder(root);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(BuyoutAdapter.ViewHolder holder, int position) {
        // Set value to views
        BuyoutModel model = mDataSet.get(position);

        if(model != null) {
            holder.tvProfile.setText(model.getPicName());
            holder.tvName.setText(model.name);
            holder.tvPeroid.setText(String.format(mPeriodFormat, model.shares, model.getPeriod()));

            holder.tvStatus.setText(mPendingCaption);
            holder.tvStatus.setTextColor(mGreyColor);

            holder.tvGameStatus.setText(mAttackingCaption);
            holder.tvGameStatus.setTextColor(mRedColor);

            if(model.picProfile == BuyoutModel.DEBUG_NO_PROFILE_PICTURE) {
                holder.tvProfile.setVisibility(View.VISIBLE);
                holder.imvProfile.setVisibility(View.GONE);
            } else {
                holder.tvProfile.setVisibility(View.GONE);
                holder.imvProfile.setVisibility(View.VISIBLE);
                holder.imvProfile.setImageDrawable(ContextCompat.getDrawable(mContext, model.picProfile));
            }

            if(model.status == BuyoutModel.BuyoutStatus.Initiate) {
                holder.tvAction.setText(mInitiateCaption);
                if(model.gameStatus == BuyoutModel.GameStatus.Win) {
                    holder.tvStatus.setText(mFailedCaption);
                    holder.tvStatus.setTextColor(mRedColor);

                    holder.btnEngage.setVisibility(View.VISIBLE);
                    holder.tvGameStatus.setVisibility(View.GONE);
                } else if(model.gameStatus == BuyoutModel.GameStatus.Lose) {
                    holder.tvStatus.setText(mSucceedCaption);
                    holder.tvStatus.setTextColor(mGreenColor);

                    holder.btnEngage.setVisibility(View.GONE);
                    holder.tvGameStatus.setVisibility(View.VISIBLE);
                    holder.tvGameStatus.setText(mEliminatedCaption);
                }
            } else {
                holder.tvAction.setText(mThreatCaption);
                if(model.gameStatus == BuyoutModel.GameStatus.Win) {
                    // This case is not impossible
                } else if(model.gameStatus == BuyoutModel.GameStatus.Lose) {
                    holder.tvStatus.setText(mFailedCaption);
                    holder.tvStatus.setTextColor(mGreenColor);

                    holder.btnEngage.setVisibility(View.VISIBLE);
                    holder.tvGameStatus.setVisibility(View.GONE);
                }
            }
        } else {
            // Loading here
            if(mLoadMoreListener != null) {
                mLoadMoreListener.onLoad();
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mDataSet.get(position) == null ? VIEW_TYPE_LOADMORE : VIEW_TYPE_ITEM;
    }

    /*
        Inner Class
         */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imvProfile;
        public TextView tvProfile;
        public TextView tvName, tvAction, tvPeroid, tvStatus;
        public Button btnEngage;
        public TextView tvGameStatus;

        public ViewHolder(View v) {
            super(v);
        }
    }
}
