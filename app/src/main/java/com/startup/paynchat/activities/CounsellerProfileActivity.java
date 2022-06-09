package com.startup.paynchat.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.widget.NestedScrollView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.startup.paynchat.GlobalVariables;
import com.startup.paynchat.utils.PreferenceConnector;
import com.sinch.android.rtc.calling.Call;
import com.startup.paynchat.R;
import com.startup.paynchat.fragments.UserSelectDialogFragment;
import com.startup.paynchat.models.AttachmentTypes;
import com.startup.paynchat.models.Chat;
import com.startup.paynchat.models.Message;
import com.startup.paynchat.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CounsellerProfileActivity extends BaseActivity implements View.OnClickListener {
    private TextView txtOurService;
    private Context context;
    private static String EXTRA_DATA_CHAT = "extradatachat";
    private static final int REQUEST_CODE_CHAT_FORWARD = 97;

    private boolean callIsVideo;
    private User user;
    private Chat chat;
    private ImageView usersImage;
    private TextView userName, userRole, userStatus, age, callRate;
    private View callAudio, callVideo, chatToUser;
    private UserSelectDialogFragment userSelectDialogFragment;
    private ArrayList<Message> messageForwardList = new ArrayList<>();
    private NestedScrollView nestedScroll;
    private int CALLRATE;
    private String counsID = "";

    public static Intent newIntent(Context context, Chat chat) {
        Intent intent = new Intent(context, CounsellerProfileActivity.class);
        intent.putExtra(EXTRA_DATA_CHAT, chat);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counsellerprofile);
        context = this;

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_DATA_CHAT)) {
            chat = intent.getParcelableExtra(EXTRA_DATA_CHAT);
        } else {
            finish();
        }

        initViews();

        callAudio.setClickable(false);
        callVideo.setClickable(false);

        IsPlanSubscribed(PreferenceConnector.readString(mContext, PreferenceConnector.LOGINEDUSERID, ""));

    }

    private void GetCounsellorDetail(String userid) {
//        {"result":true,"message":"data found","data":[{"id":live_status":"Offline"}]}
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.GETCOUNSELLORDETAILBYPHONE, response -> {
            Log.d("res==GETCOUDETAILBYID=>", response);
            try {
                JSONObject json = new JSONObject(response);
                String str_result = json.getString("result");

                if (str_result.equalsIgnoreCase("true")) {
                    JSONArray data = json.getJSONArray("data");
                    JSONObject data_obj = data.getJSONObject(0);

                    CounsellerProfileActivity.this.user = new User();
//                    {"result":true,"message":"data found","data":[{"contact":"+919758844422","mail":"","role":"Psychologist","image":"\/images\/lowyer_images\/Nishaa roy_.jpg","status":"Active","live_status":"Offline"}]}

                    String isFav = "0";
                    boolean isFavaraite = false;
                    if (data_obj.has("is_fav")) {
                        isFav = data_obj.getString("is_fav");
                    }
                    if (isFav.equalsIgnoreCase("0")) {
                        isFavaraite = false;
                    }else {
                        isFavaraite = true;
                    }

                    CounsellerProfileActivity.this.user = new User(
                            data_obj.getString("contact"), data_obj.getString("name"),
                            "Online",
                            GlobalVariables.IMGPREURL + data_obj.getString("image"),
                            data_obj.getString("id"),
                            data_obj.getString("age"),
                            data_obj.getString("call_rate"),
                            isFavaraite);

                    try{
                        CALLRATE = Integer.parseInt(data_obj.getString("call_rate"));
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                    user.setName(user.getName());
                    chat.setChatImage(user.getImage());
                    setUserInfoApi(data_obj);

                    if ((data_obj.getString("live_status")).equalsIgnoreCase("Offline")) {
                        isOnline = false;
                    } else {
                        isOnline = true;
                    }
//                    chat.setChatStatus(data_obj.getString("name"));
                } else {

                }
//                setUserInfo();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", userid);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(request);
    }

    private boolean isOnline = false;

    private void initViews() {
        ImageView ivBackButton = findViewById(R.id.back_button);
        ivBackButton.setOnClickListener(this);

        txtOurService = findViewById(R.id.txt_ourservice);
        userName = findViewById(R.id.tv_username);
        userRole = findViewById(R.id.tv_role);
        userStatus = findViewById(R.id.tv_status);
        usersImage = findViewById(R.id.img_avatar);

        age = findViewById(R.id.age);
        callRate = findViewById(R.id.call_rate);

        nestedScroll = findViewById(R.id.nested_scroll);
        nestedScroll.setVisibility(View.INVISIBLE);

        callAudio = findViewById(R.id.img_audio);
        callVideo = findViewById(R.id.img_call);
        chatToUser = findViewById(R.id.img_chat);
        callAudio.setOnClickListener(this);
        callVideo.setOnClickListener(this);
        chatToUser.setOnClickListener(this);

        usersRef.child(chat.getUserId()).addValueEventListener(userValueChangeListener);
        GetCounsellorDetail(chat.getUserId());

        counsID = chat.getUserId();
    }

    @Override
    protected void onDestroy() {
        if (usersRef != null && userValueChangeListener != null)
            usersRef.removeEventListener(userValueChangeListener);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        usersRef.child(chat.getUserId()).addValueEventListener(userValueChangeListener);
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_button:
                finish();
                break;
            case R.id.img_call:
                IsPlanSubscribed("");
                if (isVideoActive) {
                    if (isOnline) {
                        if (user != null) {
                            callIsVideo = true;
                            placeCall();
                        } else {
                            Toast.makeText(mContext, R.string.just_moment, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(mContext, "Counsellor is offline", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    OpenPurchaseActivity(mContext);
                }
                break;
            case R.id.img_audio:
                IsPlanSubscribed("");
                if (isAudioActive) {
                    if (isOnline) {
                        if (user != null) {
                            callIsVideo = false;
                            placeCall();
                        } else {
                            Toast.makeText(mContext, R.string.just_moment, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(mContext, "Counsellor is offline", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    OpenPurchaseActivity(mContext);
                }
                break;
            case R.id.img_chat:
                IsPlanSubscribed("");
                if (isChatActive) {
                    if (isOnline) {
                        if (chat.isGroup() && chat.isLatest()) {
                            ArrayList<Message> newGroupForwardList = new ArrayList<>();
                            Message newMessage = new Message();
                            newMessage.setBody(getString(R.string.invitation_group));
                            newMessage.setAttachmentType(AttachmentTypes.NONE_NOTIFICATION);
                            newMessage.setAttachment(null);
                            newGroupForwardList.add(newMessage);
                            openChat(ChatActivity.newIntent(mContext, newGroupForwardList, chat), null);
                        } else {
                            openChat(ChatActivity.newIntent(mContext, messageForwardList, chat), null);
                        }
                    } else {
                        Toast.makeText(mContext, "Counsellor is offline", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    OpenPurchaseActivity(mContext);
                }
                break;
            default:
                break;
        }
    }

    private void openChat(Intent intent, View userImage) {
        if (userImage == null) {
            userImage = usersImage;
        }

        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, userImage, "userImage");
        startActivityForResult(intent, REQUEST_CODE_CHAT_FORWARD, activityOptionsCompat.toBundle());

        if (userSelectDialogFragment != null)
            userSelectDialogFragment.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (REQUEST_CODE_CHAT_FORWARD):
                if (resultCode == Activity.RESULT_OK) {
                    //show forward dialog to choose users
                    messageForwardList.clear();
                    ArrayList<Message> temp = data.getParcelableArrayListExtra("FORWARD_LIST");
                    messageForwardList.addAll(temp);
//                    userSelectDialogFragment = UserSelectDialogFragment.newUserSelectInstance(myUsers);
//                    FragmentManager manager = getSupportFragmentManager();
//                    Fragment frag = manager.findFragmentByTag(USER_SELECT_TAG);
//                    if (frag != null) {
//                        manager.beginTransaction().remove(frag).commit();
//                    }
//                    userSelectDialogFragment.show(manager, USER_SELECT_TAG);
                }
                break;
        }
    }

    @Override
    void onSinchConnected() {
        callAudio.setClickable(true);
        callVideo.setClickable(true);
    }

    @Override
    void onSinchDisconnected() {
        callAudio.setClickable(false);
        callVideo.setClickable(false);
    }

    private ValueEventListener userValueChangeListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (mContext != null) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    CounsellerProfileActivity.this.user = user;
//                    user.setName(chat.getChatName());
//                    chat.setChatImage(user.getImage());
//                    chat.setChatStatus(user.getStatus());
                    setUserInfo();
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private void setUserInfo() {
        userName.setText(chat.getChatName());
        //status.setText(chat.getChatStatus());
        userName.setSelected(true);
//        Glide.with(this).load(GlobalVariables.IMGPREURL+chat.getChatImage()).apply(new RequestOptions().placeholder(R.drawable.cousellers)).into(usersImage);
    }

    private void setUserInfoApi(JSONObject data_obj) {
        try {
            userName.setText(data_obj.getString("name"));

            userRole.setText(data_obj.getString("role"));
            userStatus.setText(data_obj.getString("live_status"));
            userStatus.setText("Online");

            age.setText(data_obj.getString("age")+" YEAR");
            callRate.setText(data_obj.getString("call_rate")+"/MIN");

            Glide.with(this).load(GlobalVariables.IMGPREURL + data_obj.getString("image")).apply(new RequestOptions().placeholder(R.drawable.cousellers)).into(usersImage);

            nestedScroll.setVisibility(View.VISIBLE);

            LoadUserLoginData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void LoadUserLoginData() {
        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.USERAUTOLOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("res=LoginUser==>", response);
                try {
                    JSONObject json = new JSONObject(response);

                    String str_result = json.getString("result");

                    if (str_result.equalsIgnoreCase("true")) {
                        JSONArray data = json.getJSONArray("data");
                        JSONObject data_obj = data.getJSONObject(0);
                        PreferenceConnector.writeString(mContext, PreferenceConnector.LOGINEDUSERID, data_obj.getString("id"));
                        PreferenceConnector.writeString(mContext, PreferenceConnector.LOGINEDNAME, data_obj.getString("name"));
                        PreferenceConnector.writeString(mContext, PreferenceConnector.LOGINEDPHONE, data_obj.getString("contact"));
                        PreferenceConnector.writeString(mContext, PreferenceConnector.LOGINEDEMAIL, "");

                        if (data_obj.has("wallet_bal")) {
                            PreferenceConnector.writeInteger(mContext, PreferenceConnector.WALLETBAL, data_obj.getInt("wallet_bal"));
                        }

                        CALLTIMEINSEC = GetMAxCallingInSec(
                                PreferenceConnector.readInteger(mContext, PreferenceConnector.WALLETBAL, 0),
                                CALLRATE);
                    } else {
                        Toast.makeText(mContext, json.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", PreferenceConnector.readString(mContext, PreferenceConnector.LOGINEDPHONE, ""));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(request);
    }

    public int GetMAxCallingInSec(int wallBal, int CALLRATE){
        if (CALLRATE != 0) {
            return (wallBal / CALLRATE) * 60;
        }else {
            return 0;
        }
    }


    private void placeCall() {
        if (callIsVideo) {
            if (!IsVideoPurchased()) {
                OpenPurchaseActivity(this);
            } else {
                Calling(videoData);
            }
        } else {
            if (!IsAudioPurchased()) {
                OpenPurchaseActivity(this);
            } else {
                Calling(audioData);
            }
        }
    }

    int CALLTIMEINSEC = 3600;
    private void Calling(String data) {
        if (permissionsAvailable(permissionsSinch)) {
            try {
                Call call = callIsVideo ? getSinchServiceInterface().callUserVideo(user.getId()) : getSinchServiceInterface().callUser(user.getId());
                if (call == null) {
                    // Service failed for some reason, show a Toast and abort
                    Toast.makeText(this, getString(R.string.sinch_start_error), Toast.LENGTH_LONG).show();
                    return;
                }
                String callId = call.getCallId();
                CallScreenActivity.userCounsellor = new User();
                CallScreenActivity.userCounsellor = user;
                CallScreenActivity.userInOut = "OUT";
                CallScreenActivity.userCallId = callId;

                PreferenceConnector.writeString(mContext, PreferenceConnector.callingdata, data);

//                if (data.contains("#:#")) {
//                    CALLTIMEINSEC = Integer.parseInt(data.split("#:#")[1]) * 60;
//                }
                CALLTIMEINSEC = GetMAxCallingInSec(
                        PreferenceConnector.readInteger(mContext, PreferenceConnector.WALLETBAL, 0),
                        CALLRATE);
                startActivity(CallScreenActivity.newIntent(this, user, callId, "OUT", CALLTIMEINSEC));
            } catch (Exception e) {
                Log.e("CHECK", e.getMessage());
                //ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsSinch, 69);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 69:
                if (permissionsAvailable(permissions))
                    placeCall();
                break;
        }
    }

}