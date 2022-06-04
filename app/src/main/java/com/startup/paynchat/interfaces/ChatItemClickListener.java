package com.startup.paynchat.interfaces;

import android.view.View;

import com.startup.paynchat.models.Chat;
import com.startup.paynchat.models.User;

/**
 * Created by a_man on 5/10/2017.
 */

public interface ChatItemClickListener {
    void onChatItemClick(Chat chat, int position, View userImage);
    void onMessageChat(Chat chat, int position, View userImage);
    void onCallChat(boolean isVideoCall, User user);
    void placeCall(boolean callIsVideo, User user);
}
