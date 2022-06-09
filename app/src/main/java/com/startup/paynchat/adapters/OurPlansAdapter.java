package com.startup.paynchat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.startup.paynchat.R;
import com.startup.paynchat.models.PlansItemsModel;

import java.util.List;

public class OurPlansAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<PlansItemsModel> items;
    private Context ctx;
    private int checkedPosition = 0;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, PlansItemsModel obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public OurPlansAdapter(Context context, List<PlansItemsModel> items) {
        this.items = items;
        this.ctx = context;
    }

    public OurPlansAdapter(Context context, List<PlansItemsModel> items, OnItemClickListener mOnItemClickListener) {
        this.items = items;
        this.ctx = context;
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public View lyt_parent;
        public TextView discount, pkg_price_coins, pkg_price;

        public OriginalViewHolder(View v) {
            super(v);
            discount = (TextView) v.findViewById(R.id.discount);
            pkg_price_coins = (TextView) v.findViewById(R.id.pkg_price_coins);
            pkg_price = (TextView) v.findViewById(R.id.pkg_price);

            lyt_parent = (View) v.findViewById(R.id.cardview);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_our_mainplans, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            view.discount.setText(items.get(position).getDiscount()+"% off");
            view.pkg_price_coins.setText(items.get(position).getCategory());
            view.pkg_price.setText("₹ " + (items.get(position).getPrice()));

            view.lyt_parent.setOnClickListener(view13 -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view13, items.get(position), position);
                }
            });

        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}