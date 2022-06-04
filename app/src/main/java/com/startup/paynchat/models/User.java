package com.startup.paynchat.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

/**
 * Created by a_man on 5/4/2017.
 */

public class User implements Parcelable {
    private boolean online;
    private boolean typing;
    private String age, call_rate;
    @Exclude
    private boolean selected;
    @Exclude
    private boolean inviteAble;
    private boolean isCounsellor;
    private String id;
    private String name, status, image, userId, userPlayerId;
    private String role;

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getCall_rate() {
        return call_rate;
    }

    public void setCall_rate(String call_rate) {
        this.call_rate = call_rate;
    }

    public User() {
    }

    protected User(Parcel in) {
        online = in.readByte() != 0;
        typing = in.readByte() != 0;
        selected = in.readByte() != 0;
        inviteAble = in.readByte() != 0;
        isCounsellor = in.readByte() != 0;
        id = in.readString();
        name = in.readString();
        status = in.readString();
        image = in.readString();
        userPlayerId = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public User(String id, String name, String status, String image, String userId) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.online = false;
        this.image = image;
        this.userId = userId;
        this.typing = false;
        this.inviteAble = false;
        this.isCounsellor = false;
    }

    public User(String id, String name, String status, String image, String userId, String age, String call_rate) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.online = false;
        this.image = image;
        this.userId = userId;
        this.age = age;
        this.call_rate = call_rate;
        this.typing = false;
        this.inviteAble = false;
        this.isCounsellor = false;
    }

    public User(String id, String name, String status, String image, String userId, String role) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.online = false;
        this.image = image;
        this.userId = userId;
        this.typing = false;
        this.inviteAble = false;
        this.isCounsellor = false;
        this.role = role;
    }


    public User(String id, String name) {
        this.id = id;
        this.name = name;
        this.status = "";
        this.online = false;
        this.image = "";
        this.typing = false;
        this.inviteAble = true;
        this.isCounsellor = false;
    }

    public User(String id, String name, String status, String image, boolean isCounsellor) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.online = false;
        this.image = image;
        this.typing = false;
        this.inviteAble = false;
        this.isCounsellor = isCounsellor;
    }

    public User(String id, String name, boolean isCounsellor) {
        this.id = id;
        this.name = name;
        this.status = "";
        this.online = false;
        this.image = "";
        this.typing = false;
        this.inviteAble = true;
        this.isCounsellor = isCounsellor;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isCounsellor() {
        return isCounsellor;
    }

    public void setCounsellor(boolean counsellor) {
        isCounsellor = counsellor;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOnline() {
        return online;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public boolean isInviteAble() {
        return inviteAble;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static boolean validate(User user) {
        return user != null && user.getId() != null && user.getName() != null && user.getStatus() != null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (online ? 1 : 0));
        dest.writeByte((byte) (typing ? 1 : 0));
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeByte((byte) (inviteAble ? 1 : 0));
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(status);
        dest.writeString(image);
        dest.writeString(userPlayerId);
    }
}
