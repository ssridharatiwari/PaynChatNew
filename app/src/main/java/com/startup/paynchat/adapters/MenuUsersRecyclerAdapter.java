package com.startup.paynchat.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.startup.paynchat.R;
import com.startup.paynchat.interfaces.ChatItemClickListener;
import com.startup.paynchat.models.Chat;
import com.startup.paynchat.models.User;
import com.startup.paynchat.utils.Helper;

import java.util.ArrayList;

public class MenuUsersRecyclerAdapter extends RecyclerView.Adapter<MenuUsersRecyclerAdapter.BaseViewHolder> implements Filterable {
    private Context context;
    private ChatItemClickListener itemClickListener;
    private ArrayList<User> dataList, dataListFiltered;
    private Filter filter;
    private User userMe;
    private boolean isShowSmall;

    public MenuUsersRecyclerAdapter(@NonNull Context context, @Nullable ArrayList<User> users) {
        if (context instanceof ChatItemClickListener) {
            this.itemClickListener = (ChatItemClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ChatItemClickListener");
        }

        this.context = context;
        this.isShowSmall = false;
        this.userMe = new Helper(context).getLoggedInUser();
        this.dataList = users;
        this.dataListFiltered = users;
        this.filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    dataListFiltered = dataList;
                } else {
                    ArrayList<User> filteredList = new ArrayList<>();
                    for (User row : dataList) {
                        String toCheckWith = TextUtils.isEmpty(row.getName()) ? row.getId() : row.getName();
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
                dataListFiltered = (ArrayList<User>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public MenuUsersRecyclerAdapter(Context context, ArrayList<User> users, boolean isShowSmall) {
        if (context instanceof ChatItemClickListener) {
            this.itemClickListener = (ChatItemClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ChatItemClickListener");
        }

        this.context = context;
        this.isShowSmall = isShowSmall;
        this.userMe = new Helper(context).getLoggedInUser();
        this.dataList = users;
        this.dataListFiltered = users;
        this.filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    dataListFiltered = dataList;
                } else {
                    ArrayList<User> filteredList = new ArrayList<>();
                    for (User row : dataList) {
                        String toCheckWith = TextUtils.isEmpty(row.getName()) ? row.getId() : row.getName();
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
                dataListFiltered = (ArrayList<User>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemViewType(int position) {
        return dataListFiltered.get(position).getId().equals("-1") && dataListFiltered.get(position).getName().equals("-1") ? 0 : 1;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (isShowSmall) {
            return new UsersViewHolder(LayoutInflater.from(context).inflate(R.layout.item_menu_user_small, parent, false));
        }else{
            return new UsersViewHolder(LayoutInflater.from(context).inflate(R.layout.item_menu_user, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        if (holder instanceof UsersViewHolder) {
            ((UsersViewHolder) holder).setData(dataListFiltered.get(position));
        } else if (holder instanceof UserInviteHeaderViewHolder) {
//            ((UserInviteHeaderViewHolder) holder).setData(dataListFiltered.get(position));
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

    class UserInviteHeaderViewHolder extends BaseViewHolder {
        private TextView inviteTitle;

        UserInviteHeaderViewHolder(final View itemView) {
            super(itemView);
            inviteTitle = itemView.findViewById(R.id.inviteTitle);
        }

        public void setData(User user) {
            inviteTitle.setText(context.getString(R.string.invite_to) + " " + context.getString(R.string.app_name));
        }
    }

    class UsersViewHolder extends BaseViewHolder {
        private ImageView userImage;
        private TextView userName, userRole;
        private ImageView callAudio, callVideo, chatToUser;

        UsersViewHolder(final View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.user_image);
            userName = itemView.findViewById(R.id.user_name);
            userRole = itemView.findViewById(R.id.user_role);

            callAudio = itemView.findViewById(R.id.img_audio);
            callVideo = itemView.findViewById(R.id.img_call);
            chatToUser = itemView.findViewById(R.id.img_chat);

            callAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    itemClickListener.onCallChat(false, userMe);
                }
            });

            callVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    itemClickListener.onCallChat(true, userMe);
                }
            });

            chatToUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    itemClickListener.onMessageChat(new Chat(dataListFiltered.get(pos), userMe.getId()), pos, userImage);
                }
            });

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != -1) {
                    if (dataList.get(pos).isInviteAble()) {
//                        Intent it = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + dataListFiltered.get(pos).getId()));
//                        it.putExtra("sms_body", String.format(context.getString(R.string.invitation_message), context.getString(R.string.app_name), context.getPackageName()));
//                        context.startActivity(it);
                        Helper.openShareIntent(context, null, (context.getString(R.string.hey_there) + " " + context.getString(R.string.app_name) + "\n" + context.getString(R.string.download_now) + ": " + ("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
                    } else {
                        itemClickListener.onChatItemClick(new Chat(dataListFiltered.get(pos), userMe.getId()), pos, userImage);
                    }
                }
            });
        }

        public void setData(User user) {
            userName.setText(user.getName());
            userRole.setText(user.getRole());
            Glide.with(context).load(user.getImage()).apply(new RequestOptions().placeholder(R.drawable.girl)).into(userImage);
            userName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }
}
