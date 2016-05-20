package com.whitefly.plutocrat.mainmenu.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.mainmenu.adapters.listeners.OnLoadmoreListener;
import com.whitefly.plutocrat.mainmenu.events.EngageClickEvent;
import com.whitefly.plutocrat.models.TargetModel;

import java.util.ArrayList;

/**
 * Created by Satjapot on 5/17/16 AD.
 */
public class TargetAdapter extends RecyclerView.Adapter<TargetAdapter.ViewHolder> {
    private static final int VIEW_TYPE_ITEM = 1;
    private static final int VIEW_TYPE_LOADMORE = 2;

    // Attributes
    private Context mContext;
    private ArrayList<TargetModel> mDataSet;
    private String mBuyoutFormat, mThreatFormat, mDaySurvivedFormat;
    private OnLoadmoreListener mLoadMoreListener;
    private View.OnClickListener mEngageClick;

    // Getter Methods
    public ArrayList<TargetModel> getDataSet() {
        return mDataSet;
    }

    // Setter Methods
    public void setOnLoadmoreListener(OnLoadmoreListener listener) {
        mLoadMoreListener = listener;
    }

    // Constructor
    public TargetAdapter(Context context, ArrayList<TargetModel> dataset) {
        mContext = context;
        mDataSet = dataset;

        mBuyoutFormat = mContext.getString(R.string.value_buyouts);
        mThreatFormat = mContext.getString(R.string.value_threats);
        mDaySurvivedFormat = mContext.getString(R.string.value_daysurvived);

        mEngageClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getInstance().post(new EngageClickEvent((TargetModel) v.getTag()));
            }
        };
    }

    // Methods
    public void addItems(ArrayList<TargetModel> items) {
        // Delete null row
        if(mDataSet.size() > 0) {
            TargetModel modelNull = mDataSet.get(mDataSet.size() - 1);
            if (modelNull == null) {
                mDataSet.remove(modelNull);
            }
        }

        // Add a list to this list
        for (TargetModel item: items) {
            mDataSet.add(item);
        }
        notifyDataSetChanged();
    }

    @Override
    public TargetAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder vh;
        if(viewType == VIEW_TYPE_ITEM) {
            View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player, parent, false);
            vh = new ViewHolder(root);
            vh.imvProfile = (ImageView) root.findViewById(R.id.imv_profile);
            vh.tvProfile = (TextView) root.findViewById(R.id.tv_profile_nickname);
            vh.tvName = (TextView) root.findViewById(R.id.tv_player_name);
            vh.tvBuyout = (TextView) root.findViewById(R.id.tv_player_buyouts);
            vh.tvThreat = (TextView) root.findViewById(R.id.tv_player_threats);
            vh.tvSurvived = (TextView) root.findViewById(R.id.tv_player_daysurvived);
            vh.btnEngage = (Button) root.findViewById(R.id.btn_player_engage);
            vh.tvGameStatus = (TextView) root.findViewById(R.id.tv_player_game_status);

            vh.btnEngage.setOnClickListener(mEngageClick);
        } else {
            View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loadmore, parent, false);
            vh = new ViewHolder(root);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(TargetAdapter.ViewHolder holder, int position) {
        // Set value to views
        TargetModel model = mDataSet.get(position);

        if(model != null) {
            holder.tvProfile.setText(model.getNickName());
            holder.tvName.setText(model.name);
            holder.tvBuyout.setText(String.format(mBuyoutFormat, model.numBuyouts));
            holder.tvThreat.setText(String.format(mThreatFormat, model.numThreats));
            holder.tvSurvived.setText(String.format(mDaySurvivedFormat, model.daySurvived));

            if(model.picProfile == 0) {
                holder.tvProfile.setVisibility(View.VISIBLE);
                holder.imvProfile.setVisibility(View.GONE);
            } else {
                holder.tvProfile.setVisibility(View.GONE);
                holder.imvProfile.setVisibility(View.VISIBLE);
                holder.imvProfile.setImageDrawable(ContextCompat.getDrawable(mContext, model.picProfile));
            }

            if (model.status == TargetModel.TargetStatus.Normal) {
                holder.btnEngage.setVisibility(View.VISIBLE);
                holder.tvGameStatus.setVisibility(View.GONE);
            } else {
                holder.btnEngage.setVisibility(View.GONE);
                holder.tvGameStatus.setVisibility(View.VISIBLE);
            }

            holder.btnEngage.setTag(model);
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
        public TextView tvName, tvBuyout, tvThreat, tvSurvived;
        public Button btnEngage;
        public TextView tvGameStatus;

        public ViewHolder(View v) {
            super(v);
        }
    }
}
