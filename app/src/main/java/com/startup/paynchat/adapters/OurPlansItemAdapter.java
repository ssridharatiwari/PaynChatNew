//package com.startup.paynchat.adapters;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.startup.paynchat.R;
//import com.startup.paynchat.models.PlansItemsModel;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class OurPlansItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//    private List<PlansItemsModel> items = new ArrayList<>();
//    private Context ctx;
//
//    private OnItemClickListener mOnItemClickListener;
//
//    public interface OnItemClickListener {
//        void onBuyPlanClick(View view, PlansItemsModel obj, int position);
//    }
//
//    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
//        this.mOnItemClickListener = mItemClickListener;
//    }
//
//    public OurPlansItemAdapter(Context context, List<PlansItemsModel> items) {
//        this.items = items;
//        this.ctx = context;
//    }
//
//    public class OriginalViewHolder extends RecyclerView.ViewHolder {
//        public TextView name, validity, price, buyPlan;
//        public View lyt_parent;
//
//        public OriginalViewHolder(View v) {
//            super(v);
//            name = (TextView) v.findViewById(R.id.pkg_name);
//            validity = (TextView) v.findViewById(R.id.pkg_time);
//            price = (TextView) v.findViewById(R.id.pkg_price);
//            buyPlan = (TextView) v.findViewById(R.id.tv_buy_pkg);
//
//            lyt_parent = (View) v.findViewById(R.id.cardview);
//        }
//    }
//
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        RecyclerView.ViewHolder vh;
//        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_our_plans, parent, false);
//        vh = new OriginalViewHolder(v);
//        return vh;
//    }
//
//    // Replace the contents of a view (invoked by the layout manager)
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
//        if (holder instanceof OriginalViewHolder) {
//            OriginalViewHolder view = (OriginalViewHolder) holder;
//
//            view.name.setText(items.get(position).getPkgName());
//            view.price.setText("₹"+items.get(position).getPkgPrive());
//            view.validity.setText(items.get(position).getPkgValidity());
//
//            view.buyPlan.setId(position);
//            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (mOnItemClickListener != null) {
//                        mOnItemClickListener.onBuyPlanClick(view, items.get(position), position);
//                    }
//                }
//            });
//        }
//    }
//
//    @Override
//    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//        });
//        super.onAttachedToRecyclerView(recyclerView);
//    }
//
//    @Override
//    public int getItemCount() {
//        return items.size();
//    }
//}