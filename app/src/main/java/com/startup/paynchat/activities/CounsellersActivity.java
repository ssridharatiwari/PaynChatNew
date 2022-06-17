package com.startup.paynchat.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;
import com.sinch.android.rtc.calling.Call;
import com.startup.paynchat.GlobalVariables;
import com.startup.paynchat.R;
import com.startup.paynchat.adapters.MenuCounsellerRecyclerAdapter;
import com.startup.paynchat.adapters.ViewPagerAdapter;
import com.startup.paynchat.fragments.MyCallsFragment;
import com.startup.paynchat.fragments.MyChatsFragment;
import com.startup.paynchat.fragments.ProfileEditDialogFragment;
import com.startup.paynchat.fragments.UserSelectDialogFragment;
import com.startup.paynchat.interfaces.ChatItemClickListener;
import com.startup.paynchat.models.Chat;
import com.startup.paynchat.models.Message;
import com.startup.paynchat.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CounsellersActivity extends BaseActivity implements ChatItemClickListener, View.OnClickListener {
    private static final int REQUEST_CODE_CHAT_FORWARD = 99;
    private static String USER_SELECT_TAG = "userselectdialog";
    private static String PROFILE_EDIT_TAG = "profileedittag";
    private ImageView usersImage, backImage;
    private MenuCounsellerRecyclerAdapter menuUsersRecyclerAdapter;
    private ArrayList<User> myUsers = new ArrayList<>();
    private ArrayList<Message> messageForwardList = new ArrayList<>();
    private UserSelectDialogFragment userSelectDialogFragment;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUi();
        setupMenu();

        usersImage.setOnClickListener(this);
        usersImage.setVisibility(View.INVISIBLE);
        backImage.setOnClickListener(this);
        setupViewPager();
        updateFcmToken();

        boolean newUser = getIntent().getBooleanExtra("newUser", false);
        if (newUser && userMe != null) {
            Toast.makeText(mContext, R.string.setup_profile_msg, Toast.LENGTH_LONG).show();
            ProfileEditDialogFragment.newInstance(true).show(getSupportFragmentManager(), PROFILE_EDIT_TAG);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initUi() {
        setContentView(R.layout.activity_counsellers);
        usersImage = findViewById(R.id.users_image);
        backImage = findViewById(R.id.back_button);
    }

    private void updateFcmToken() {
        OneSignal.addSubscriptionObserver(stateChanges -> {
            if (!stateChanges.getFrom().getSubscribed() && stateChanges.getTo().getSubscribed()) {
                usersRef.child(userMe.getId()).child("userPlayerId").setValue(stateChanges.getTo().getUserId());
                helper.setMyPlayerId(stateChanges.getTo().getUserId());
            }
        });
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        if (status != null && status.getSubscriptionStatus() != null && status.getSubscriptionStatus().getUserId() != null) {
            usersRef.child(userMe.getId()).child("userPlayerId").setValue(status.getSubscriptionStatus().getUserId());
            helper.setMyPlayerId(status.getSubscriptionStatus().getUserId());
        }
    }

    private void setupViewPager() {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(MyChatsFragment.newInstance(false), getString(R.string.tab_title_chat));
        adapter.addFrag(MyChatsFragment.newInstance(true), getString(R.string.tab_title_group));
        adapter.addFrag(new MyCallsFragment(), getString(R.string.tab_title_call));
    }

    private void setupMenu() {
        myUsers = new ArrayList<>();
        if (counsellerList.size() == 0) {
            ArrayList<User> mcounsellerList = new ArrayList<>();
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.GETCONSELLORLIST, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("response====m=====>>>>>", response);
                    try {
                        JSONObject json = new JSONObject(response);
                        String str_message = json.getString("message");
                        String str_result = json.getString("result");

                        if (str_result.equalsIgnoreCase("true")) {
                            JSONArray data = json.getJSONArray("data");
                            for (int data_i = 0; data_i < ((JSONArray) data).length(); data_i++) {
                                JSONObject data_obj = data.getJSONObject(data_i);

                                String imgUrl = "";
                                if (data_obj.has("image")){
                                    imgUrl = GlobalVariables.IMGPREURL+data_obj.getString("image");
                                }else{
                                    imgUrl = "";
                                }
                                //{"result":true,"message":"data found","data":[{"id":"19","name":"Shridhar","contact":"+917691001989","address":null,"languages":null,"mail":"ssonu0001@gmail.com","experience":null,"about":null,"password":null,"image":null,"adhar_no":null,"adhar_img":null,"ac_holder":null,"ac_number":null,"bank":null,"other":null,"role":"lowyer","status":null,"created_at":null,"updated_at":null}]}
                                mcounsellerList.add(new User(data_obj.getString("contact"), data_obj.getString("name"),
                                        data_obj.getString("status"),
                                        imgUrl,
                                        data_obj.getString("id")
                                        ));
                            }
                        } else {
                            Toast.makeText(mContext, str_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    myUsers = mcounsellerList;
                    menuUsersRecyclerAdapter = new MenuCounsellerRecyclerAdapter(mContext, myUsers);

                    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_counsellers);
                    recyclerView.setLayoutManager(new LinearLayoutManager(CounsellersActivity.this, LinearLayoutManager.VERTICAL, false));
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setAdapter(menuUsersRecyclerAdapter);
                }
            }, error -> Log.d("error", error.toString()));
            queue.add(request);

        } else {
            myUsers = counsellerList;
            menuUsersRecyclerAdapter = new MenuCounsellerRecyclerAdapter(mContext, myUsers);

            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_counsellers);
            recyclerView.setLayoutManager(new LinearLayoutManager(CounsellersActivity.this, LinearLayoutManager.HORIZONTAL, false));
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(menuUsersRecyclerAdapter);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
                    userSelectDialogFragment = UserSelectDialogFragment.newUserSelectInstance(myUsers);
                    FragmentManager manager = getSupportFragmentManager();
                    Fragment frag = manager.findFragmentByTag(USER_SELECT_TAG);
                    if (frag != null) {
                        manager.beginTransaction().remove(frag).commit();
                    }
                    userSelectDialogFragment.show(manager, USER_SELECT_TAG);
                }
                break;
        }
    }

    @Override
    void onSinchConnected() {

    }

    @Override
    void onSinchDisconnected() {

    }

    @Override
    public void onChatItemClick(Chat chat, int position, View userImage) {
        startActivity(CounsellerProfileActivity.newIntent(mContext, chat));
    }

    @Override
    public void onMessageChat(Chat chat, int position, View userImage) {

    }

    @Override
    public void onCallChat(boolean isVideoCall, User user) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_button:
                onBackPressed();
                break;
            case R.id.addConversation:

                break;
        }
    }

    @Override
    public void placeCall(boolean callIsVideo, User user) {
        if (permissionsAvailable(permissionsSinch)) {
            try {
                Call call = callIsVideo ? getSinchServiceInterface().callUserVideo(user.getId()) : getSinchServiceInterface().callUser(user.getId());
                if (call == null) {
                    // Service failed for some reason, show a Toast and abort
                    Toast.makeText(mContext, R.string.sinch_start_error, Toast.LENGTH_LONG).show();
                    return;
                }

//                String callId = call.getCallId();
//                startActivity(CallScreenActivity.
//                        newIntent(mContext, user, callId, "OUT", CALLTIMEINSEC));
            } catch (Exception e) {
                Log.e("CHECK", e.getMessage());
                //ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsSinch, 69);
        }
    }

    @Override
    public void makeFav(boolean callIsVideo, User user) {

    }
}
