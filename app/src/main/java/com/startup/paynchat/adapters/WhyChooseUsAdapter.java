package com.startup.paynchat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.startup.paynchat.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ssridharatiwari on 2021.
 */

public class WhyChooseUsAdapter extends RecyclerView.Adapter<WhyChooseUsAdapter.MyViewHolder> implements Filterable {
    private Context context;
    private List<String> spinnerList;
    private List<String> spinnerListFiltered;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, desc;
        public RelativeLayout itemHead;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.item_count);
            desc = view.findViewById(R.id.item_name);
        }
    }

    public WhyChooseUsAdapter(Context context, List<String> contactList) {
        this.context = context;
        this.spinnerList = contactList;
        this.spinnerListFiltered = contactList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_whychooseus, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final String itemName = spinnerList.get(position);
        position = holder.getAdapterPosition();

        int finalPosition = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send selected contact in callback
            }
        });

        if (position+1 < 10) {
            holder.title.setText("0"+(position+1) + "");
        }else {
            holder.title.setText((position+1) + "");
        }
        holder.desc.setText(itemName);
    }

    @Override
    public int getItemCount() {
        return spinnerListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    spinnerListFiltered = spinnerList;
                } else {
                    List<String> filteredList = new ArrayList<>();
                    for (String row : spinnerList) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.toLowerCase().contains(charString.toLowerCase()) ||
                                row.contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }
                    spinnerListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = spinnerListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                spinnerListFiltered = (ArrayList<String>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void updateListData(List<String> list) {
        spinnerList = list;
        spinnerListFiltered = list;
        notifyDataSetChanged();
    }

}