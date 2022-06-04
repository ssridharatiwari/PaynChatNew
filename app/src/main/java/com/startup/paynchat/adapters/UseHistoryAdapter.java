package com.startup.paynchat.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.startup.paynchat.R;
import com.startup.paynchat.models.UseHistoryModel;
import com.startup.paynchat.models.User;
import java.util.ArrayList;

public class UseHistoryAdapter extends RecyclerView.Adapter<UseHistoryAdapter.BaseViewHolder> implements Filterable {
    private Context context;
    private ItemClickListener itemClickListener;
    private ArrayList<UseHistoryModel> dataList, dataListFiltered;
    private Filter filter;

    public interface ItemClickListener {
        void onItemClick(UseHistoryModel chat, int position);
    }

    public UseHistoryAdapter(@NonNull Context context, @Nullable ArrayList<UseHistoryModel> users) {
        if (context instanceof ItemClickListener) {
            this.itemClickListener = (ItemClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ItemClickListener");
        }

        this.context = context;
        this.dataList = users;
        this.dataListFiltered = users;
        this.filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    dataListFiltered = dataList;
                } else {
                    ArrayList<UseHistoryModel> filteredList = new ArrayList<>();
                    for (UseHistoryModel row : dataList) {
                        String toCheckWith = TextUtils.isEmpty(row.getPlan_title()) ? row.getPlan_type() : row.getPrice();
                        if (toCheckWith.toLowerCase().startsWith(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    dataListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = dataListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                dataListFiltered = (ArrayList<UseHistoryModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemViewType(int position) {
        return dataListFiltered.get(position).getSub_id().equals("-1") && dataListFiltered.get(position).getPlan_title().equals("-1") ? 0 : 1;
    }

    @NonNull
    @Override
    public UseHistoryAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UseHistoryAdapter.UsersViewHolder(LayoutInflater.from(context).inflate(R.layout.item_history, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UseHistoryAdapter.BaseViewHolder holder, int position) {
        if (holder instanceof UseHistoryAdapter.UsersViewHolder) {
            ((UseHistoryAdapter.UsersViewHolder) holder).setData(dataListFiltered.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return dataListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return this.filter;
    }

    class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(View itemView) {
            super(itemView);
        }
    }

    class UserInviteHeaderViewHolder extends UseHistoryAdapter.BaseViewHolder {
        private TextView inviteTitle;

        UserInviteHeaderViewHolder(final View itemView) {
            super(itemView);
            inviteTitle = itemView.findViewById(R.id.inviteTitle);
        }

        public void setData(User user) {
            inviteTitle.setText(context.getString(R.string.invite_to) + " " + context.getString(R.string.app_name));
        }
    }

    class UsersViewHolder extends UseHistoryAdapter.BaseViewHolder {
        private TextView planName, planCat, planType, planStatus;

        UsersViewHolder(final View itemView) {
            super(itemView);
            planName = itemView.findViewById(R.id.plan_title);
            planCat = itemView.findViewById(R.id.price);
            planType = itemView.findViewById(R.id.plan_type);
            planStatus = itemView.findViewById(R.id.status);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != -1) {
                    itemClickListener.onItemClick(dataListFiltered.get(pos), pos);
                }
            });
        }

        public void setData(UseHistoryModel user) {
            planName.setText("Plan Name: "+user.getPlan_title());
            planCat.setText("Plan Price: "+user.getPrice());
            planStatus.setText("Plan Status: "+user.getSub_status());
            planName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }
}
