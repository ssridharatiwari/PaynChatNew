package com.startup.paynchat.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.startup.paynchat.R;
import com.startup.paynchat.models.AppointmentUser;

import java.util.ArrayList;
import java.util.List;


public class MemberListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    Context context;
    List<AppointmentUser> memberList;
    List<AppointmentUser> memberFilterList;
    RecyclerView mRecyclerView;
    long AnimDuration = 300;
    private boolean on_attached = true;
    private OnItemClickListener mItemClickListener;

    public static final int VIEW_TYPE_NULL = 0;
    public static final int VIEW_TYPE_ITEM = 1;

    public MemberListAdapter(Context context, List<AppointmentUser> memberModelList) {
        this.context = context;
        this.memberList = memberModelList;
        this.memberFilterList = memberModelList;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_NULL) {
            return new NullViewHolder(inflater.inflate(R.layout.item_not_found_layout, parent, false));
        } else {
            View view = inflater.inflate(R.layout.item_memberlist, parent, false);
            return new ViewHolder(view, mItemClickListener, memberFilterList);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                Log.d("MemberModelAdapter", "onScrollStateChanged: Called " + newState);
                on_attached = false;
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) viewHolder;
            AppointmentUser memberData = memberFilterList.get(position);
            holder.memberName.setText(memberData.getUser_name());
            holder.memberNumber.setText(memberData.getSubcategory()+"jjj\n"+memberData.getQuery());
            holder.memberStatus.setText(memberData.getSubcategory()+"\n"+memberData.getQuery());

            setFadeInAnimation(viewHolder.itemView, position);
        }

        if (viewHolder instanceof NullViewHolder) {
            NullViewHolder holder = (NullViewHolder) viewHolder;
            holder.textView.setText("There is nothing!");
            holder.animationView.setAnimation(R.raw.empty_file_state);
            holder.animationView.setOnClickListener(v -> holder.animationView.playAnimation());
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (memberList.size() <= 0) {
            return VIEW_TYPE_NULL;
        } else {
            return VIEW_TYPE_ITEM;
        }
        //int viewType = VIEW_TYPE_ITEM;
        //if (position == 0) viewType = VIEW_TYPE_NULL;
        //return viewType;
    }

    @Override
    public int getItemCount() {
        return memberFilterList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString().toLowerCase();
                if (charString.isEmpty()) {
                    memberFilterList = memberList;
                } else {
                    List<AppointmentUser> filteredList = new ArrayList<>();
                    for (AppointmentUser modelRow : memberList) {
                        if (modelRow.getUser_name().toLowerCase().contains(charString)
                                ) {
                            filteredList.add(modelRow);
                        }
                    }
                    memberFilterList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = memberFilterList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                memberFilterList = (List<AppointmentUser>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    // allow clicks events to be caught
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mItemClickListener = onItemClickListener;
    }

    // convenience method for getting data at click position
    public AppointmentUser  getItem(int position) {
        return memberFilterList.get(position);
    }

    // parent activity will implement this method to respond to click event
    public interface OnItemClickListener {
        void onCallClick(View view, AppointmentUser memberModel, int position);
        void onVideoCallClick(View view, AppointmentUser memberModel, int position);
        void onChatClick(View view, AppointmentUser memberModel, int position);
        void onStatusClick(View view, AppointmentUser memberModel, int position);
    }

    public void removeItem(int pos) {
        memberList.remove(pos);
        memberFilterList = memberList;
        notifyItemRemoved(pos);
    }

    private void setFadeInAnimation(View itemView, int position) {
        if (!on_attached) {
            position = -1;
        }
        boolean isNotFirstItem = position == -1;
        position++;
        itemView.setAlpha(0.f);
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator animator = ObjectAnimator.ofFloat(itemView, "alpha", 0.f, 0.5f, 1.0f);
        ObjectAnimator.ofFloat(itemView, "alpha", 0.f).start();
        animator.setStartDelay(isNotFirstItem ? AnimDuration / 2 : (position * AnimDuration / 3));
        animator.setDuration(AnimDuration);
        animatorSet.play(animator);
        animator.start();
    }

    private class NullViewHolder extends RecyclerView.ViewHolder {
        public LottieAnimationView animationView;
        public TextView textView;

        public NullViewHolder(@NonNull View itemView) {
            super(itemView);
            animationView = itemView.findViewById(R.id.not_found_animation);
            textView = itemView.findViewById(R.id.not_found_textView);
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout rootLayout;
        public TextView memberName, memberNumber, memberStatus, changeStatus;
        public TextView btnCall, btnVideoCall, btnChat;

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public ViewHolder(View itemView, final OnItemClickListener mItemClickListener, List<AppointmentUser> list) {
            super(itemView);
            memberName = itemView.findViewById(R.id.name);
            memberNumber = itemView.findViewById(R.id.number);
            memberStatus = itemView.findViewById(R.id.status);
            changeStatus = itemView.findViewById(R.id.change_status);
            btnCall = itemView.findViewById(R.id.txt_call);
            btnVideoCall = itemView.findViewById(R.id.txt_Video_call);
            btnChat = itemView.findViewById(R.id.txt_message);
            rootLayout = itemView.findViewById(R.id.lyt_parent);

            btnCall.setOnClickListener(view -> {
                if (mItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mItemClickListener.onCallClick(view, list.get(position), position);
                    }
                }
            });

            btnVideoCall.setOnClickListener(view -> {
                if (mItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mItemClickListener.onVideoCallClick(view, list.get(position), position);
                    }
                }
            });

            btnChat.setOnClickListener(view -> {
                if (mItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mItemClickListener.onChatClick(view, list.get(position), position);
                    }
                }
            });

            changeStatus.setOnClickListener(view -> {
                if (mItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mItemClickListener.onStatusClick(view, list.get(position), position);
                    }
                }
            });
        }
    }

}