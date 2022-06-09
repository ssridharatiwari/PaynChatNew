package com.startup.paynchat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.startup.paynchat.R;
import com.startup.paynchat.models.PlansItemsModel;

import java.util.ArrayList;
import java.util.List;

public class OurPlansSelectionAdapter extends RecyclerView.Adapter<OurPlansSelectionAdapter.SingleViewHolder> {
    private Context context;
    private List<PlansItemsModel> items;
    // if checkedPosition = -1, there is no default selection
    // if checkedPosition = 0, 1st item is selected by default
    private int checkedPosition = 0;
    private OurPlansAdapter.OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, PlansItemsModel obj, int position);
    }

    public void setOnItemClickListener(final OurPlansAdapter.OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public OurPlansSelectionAdapter(Context context, List<PlansItemsModel> employees) {
        this.context = context;
        this.items = employees;
    }

    public void setItems(ArrayList<PlansItemsModel> employees) {
        this.items = new ArrayList<>();
        this.items = employees;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SingleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_our_mainplans, viewGroup, false);
        return new SingleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SingleViewHolder singleViewHolder, int position) {
        singleViewHolder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class SingleViewHolder extends RecyclerView.ViewHolder {
        public View lyt_parent;
        public ImageView imageView;
        public TextView discount, pkg_price_coins, pkg_price;

        SingleViewHolder(@NonNull View itemView) {
            super(itemView);
            discount = (TextView) itemView.findViewById(R.id.discount);
            pkg_price_coins = (TextView) itemView.findViewById(R.id.pkg_price_coins);
            pkg_price = (TextView) itemView.findViewById(R.id.pkg_price);
            imageView = (ImageView) itemView.findViewById(R.id.img_selection);

            lyt_parent = (View) itemView.findViewById(R.id.cardview);

        }

        void bind(final PlansItemsModel model) {
            if (checkedPosition == -1) {
                imageView.setVisibility(View.GONE);
            } else {
                if (checkedPosition == getAdapterPosition()) {
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    imageView.setVisibility(View.GONE);
                }
            }

            discount.setText(model.getDiscount()+"% off");
            pkg_price_coins.setText(model.getCategory());
            pkg_price.setText("₹ " + (model.getPrice()));

            lyt_parent.setOnClickListener(view13 -> {
                if (checkedPosition != getAdapterPosition()) {
                    notifyItemChanged(checkedPosition);
                    checkedPosition = getAdapterPosition();
                }
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view13, model, getAdapterPosition());
                }
            });

        }
    }

    public PlansItemsModel getSelected() {
        if (checkedPosition != -1) {
            return items.get(checkedPosition);
        }
        return null;
    }
}