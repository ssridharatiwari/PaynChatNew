package com.startup.paynchat.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;
import com.startup.paynchat.GlobalVariables;
import com.startup.paynchat.R;
import com.startup.paynchat.adapters.HomeAdapter;
import com.startup.paynchat.adapters.MemberListAdapter;
import com.startup.paynchat.adapters.MenuUsersRecyclerAdapter;
import com.startup.paynchat.adapters.ViewPagerAdapter;
import com.startup.paynchat.fragments.MyCallsFragment;
import com.startup.paynchat.fragments.MyChatsFragment;
import com.startup.paynchat.fragments.OptionsFragment;
import com.startup.paynchat.fragments.OptionsFragmentCounsellor;
import com.startup.paynchat.fragments.UserSelectDialogFragment;
import com.startup.paynchat.interfaces.ChatItemClickListener;
import com.startup.paynchat.models.AppointmentUser;
import com.startup.paynchat.models.CategoryModel;
import com.startup.paynchat.models.Chat;
import com.startup.paynchat.models.Message;
import com.startup.paynchat.models.User;
import com.startup.paynchat.services.FetchMyUsersService;
import com.startup.paynchat.utils.Helper;
import com.startup.paynchat.utils.PreferenceConnector;
import com.sinch.android.rtc.calling.Call;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivityCounsellor extends BaseActivity implements ChatItemClickListener, View.OnClickListener {
    private static final int REQUEST_CODE_CHAT_FORWARD = 99;
    private final int CONTACTS_REQUEST_CODE = 321;
    private static String USER_SELECT_TAG = "userselectdialog";
    private static String OPTIONS_MORE = "optionsmore";
    private static String PROFILE_EDIT_TAG = "profileedittag";
    private static String GROUP_CREATE_TAG = "groupcreatedialog";
    private static String CONFIRM_TAG = "confirmtag";
    private ImageView usersImage, backImage, dialogUserImage, profileImage;
    private RelativeLayout toolbarContainer;

    private MenuUsersRecyclerAdapter menuUsersRecyclerAdapter;
    private ArrayList<User> myUsers = new ArrayList<>();
    private ArrayList<Message> messageForwardList = new ArrayList<>();
    private UserSelectDialogFragment userSelectDialogFragment;
    private DatabaseReference myInboxRef;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        svContext = this;
        initUi();
        resumeApp();
//        setupMenu();
//        LoadBanner();

        setProfileImage();
        usersImage.setOnClickListener(this);
        backImage.setOnClickListener(this);
        setupViewPager();
        updateFcmToken();

//        boolean newUser = getIntent().getBooleanExtra("newUser", false);
//        if (newUser && userMe != null) {
//            Toast.makeText(mContext, R.string.setup_profile_msg, Toast.LENGTH_LONG).show();
//            ProfileEditDialogFragment.newInstance(true).show(gelayouttSupportFragmentManager(), PROFILE_EDIT_TAG);
//        }
//        LoadServices();
        Helper helper = new Helper(this);
        statusSelected = PreferenceConnector.readString(svContext, PreferenceConnector.ENQSTATUS, PENDING);

        GetCounsellorDetail(helper.getLoggedInUser().getId());

    }

    private User user;
    private boolean isOnline = false;

    private void GetCounsellorDetail(String userid) {
        Log.d("res==GETCOUDETAILBYID=>", userid);
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

                    MainActivityCounsellor.this.user = new User();
//                    {"result":true,"message":"data found","data":[{"contact":"+919758844422","mail":"","role":"Psychologist","image":"\/images\/lowyer_images\/Nishaa roy_.jpg","status":"Active","live_status":"Offline"}]}
                    MainActivityCounsellor.this.user = new User(
                            data_obj.getString("contact"), data_obj.getString("name"),
                            "",
                            GlobalVariables.IMGPREURL + data_obj.getString("image"),
                            data_obj.getString("id"));
//                    user.setName(user.getName());

                    setUserInfoApi(data_obj);

                    if ((data_obj.getString("live_status")).equalsIgnoreCase("Offline")) {
                        isOnline = false;
                    } else {
                        isOnline = true;
                    }

                    LoadEnquiryData();
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
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(request);
    }

    private TextView userName, userRole, userStatus;

    private void setUserInfoApi(JSONObject data_obj) {
        userName = findViewById(R.id.tv_username);
        userRole = findViewById(R.id.tv_role);
        userStatus = findViewById(R.id.tv_status);

        try {
            userName.setText(data_obj.getString("name"));
            userRole.setText(data_obj.getString("role"));

            if (data_obj.getString("live_status").equalsIgnoreCase("Offline")) {
                sb.setChecked(false);
                userStatus.setText("Please change your status as your need");
            } else {
                sb.setChecked(true);
                userStatus.setText("Please change your status as your need");
            }

//            if (data_obj.getString("image").equalsIgnoreCase("") || data_obj.getString("image").equalsIgnoreCase("null")) {
//                Glide.with(this).load(user.getImage()).apply(new RequestOptions().placeholder(R.drawable.cousellers)).into(profileImage);
//            } else {
//                Glide.with(this).load(GlobalVariables.IMGPREURL + data_obj.getString("image")).apply(new RequestOptions().placeholder(R.drawable.cousellers)).into(profileImage);
//            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    List<CategoryModel> items = new ArrayList<>();

    private void LoadServices() {
        items = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.GETCATEGORY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response==========>>>>>", response);
                try {
                    JSONObject json = new JSONObject(response);
                    String str_message = json.getString("message");
                    String str_result = json.getString("result");

                    if (str_result.equalsIgnoreCase("true")) {
                        JSONArray data = json.getJSONArray("data");
                        for (int data_i = 0; data_i < ((JSONArray) data).length(); data_i++) {
                            JSONObject data_obj = data.getJSONObject(data_i);

                            //{"result":true,"message":"data found","data":[{"id":"5","category":"Legal Advice"}]}
                            String id = data_obj.getString("id");
                            String category = data_obj.getString("category");
                            items.add(new CategoryModel(R.drawable.girl, id, category, ""));
                        }
                    } else {
                        Toast.makeText(mContext, str_message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HomeAdapter mAdapter = new HomeAdapter(MainActivityCounsellor.this, items);
                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_services);
                recyclerView.setLayoutManager(new GridLayoutManager(MainActivityCounsellor.this, 2));
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(mAdapter);
                mAdapter.setOnItemClickListener(new HomeAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, CategoryModel obj, int position) {
                        Intent intent = new Intent(MainActivityCounsellor.this, ServiceActivity.class);
                        intent.putExtra("serviceid", obj.getId());
                        intent.putExtra("servicename", obj.getCategory());
                        startActivity(intent);
                    }
                });
            }
        }, error -> Log.d("error", error.toString()));
        queue.add(request);
    }

    private void LoadBanner() {
        ArrayList<SlideModel> imageList = new ArrayList<SlideModel>();

        imageList.add(new SlideModel(R.drawable.banner, ScaleTypes.FIT));

        ImageSlider imageSlider = findViewById(R.id.image_slider);
        imageSlider.setImageList(imageList);
    }

    @Override
    protected void onDestroy() {
        //markOnline(false);
//        if (myInboxRef != null && chatChildEventListener != null)
//            myInboxRef.removeEventListener(chatChildEventListener);
        super.onDestroy();
    }

//    private void registerChatUpdates() {
//        if (myInboxRef == null) {
//            myInboxRef = inboxRef.child(userMe.getId());
//            myInboxRef.addChildEventListener(chatChildEventListener);
//        }
//    }

    private SwitchCompat sb;

    private void initUi() {
        setContentView(R.layout.activity_main_counsellor);
        usersImage = findViewById(R.id.users_image);
        profileImage = findViewById(R.id.logo);
        toolbarContainer = findViewById(R.id.toolbarContainer);
        backImage = findViewById(R.id.back_button);

        sb = (SwitchCompat) findViewById(R.id.switch_status);
        sb.setTextOff("OFF");
        sb.setTextOn("ON");

        sb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    UpdateStatus("Online");
                } else {
                    UpdateStatus("Offline");
                }
            }
        });
    }

    private void UpdateStatus(String liveStatus) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.UPDATCOUNSELLORSTATUS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response==========>>>>>", response);
                try {
                    JSONObject json = new JSONObject(response);
                    String str_message = json.getString("message");
                    String str_result = json.getString("result");

                    if (str_result.equalsIgnoreCase("true")) {
                        Toast.makeText(mContext, str_message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, str_message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", user.getUserId());
                params.put("live_status", liveStatus);
                params.put("submit", "submit");
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

//e-dishaa.getminiweb.com/api/update-counsellor.php?id=1&name=Sambhu+Sharma&contact=+919079149908&mail=shiv@gmail.com&live_status=Online&submit=submit
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
        usersRef.child(userMe.getId()).child("userPlayerId").setValue(OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getUserId());
    }

    private void setupViewPager() {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(MyChatsFragment.newInstance(false), getString(R.string.tab_title_chat));
        adapter.addFrag(MyChatsFragment.newInstance(true), getString(R.string.tab_title_group));
        adapter.addFrag(new MyCallsFragment(), getString(R.string.tab_title_call));
    }

    private void reduceMarginsInTabs(TabLayout tabLayout, int marginOffset) {
        View tabStrip = tabLayout.getChildAt(0);
        if (tabStrip instanceof ViewGroup) {
            ViewGroup tabStripGroup = (ViewGroup) tabStrip;
            for (int i = 0; i < ((ViewGroup) tabStrip).getChildCount(); i++) {
                View tabView = tabStripGroup.getChildAt(i);
                if (tabView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) tabView.getLayoutParams()).leftMargin = marginOffset;
                    ((ViewGroup.MarginLayoutParams) tabView.getLayoutParams()).rightMargin = marginOffset;
                }
            }

            tabLayout.requestLayout();
        }
    }

    private void setupMenu() {
        myUsers = new ArrayList<>();
        if (counsellerList.size() == 0) {
            ArrayList<User> mcounsellerList = new ArrayList<>();
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.GETCONSELLORLIST, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("response==========>>>>>", response);
                    try {
                        JSONObject json = new JSONObject(response);
                        String str_message = json.getString("message");
                        String str_result = json.getString("result");

                        if (str_result.equalsIgnoreCase("true")) {
                            JSONArray data = json.getJSONArray("data");
                            for (int data_i = 0; data_i < ((JSONArray) data).length(); data_i++) {
                                JSONObject data_obj = data.getJSONObject(data_i);

                                //{"result":true,"message":"data found","data":[{"id":"19","name":"Shridhar","contact":"+917691001989","address":null,"languages":null,"mail":"ssonu0001@gmail.com","experience":null,"about":null,"password":null,"image":null,"adhar_no":null,"adhar_img":null,"ac_holder":null,"ac_number":null,"bank":null,"other":null,"role":"lowyer","status":null,"created_at":null,"updated_at":null}]}
                                mcounsellerList.add(new User(data_obj.getString("contact"), data_obj.getString("name"),
                                        data_obj.getString("status"),
                                        GlobalVariables.IMGPREURL + data_obj.getString("image"),
                                        data_obj.getString("id")));
                            }
                        } else {
                            Toast.makeText(mContext, str_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    myUsers = mcounsellerList;
                    menuUsersRecyclerAdapter = new MenuUsersRecyclerAdapter(mContext, myUsers);

                    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_counsellers);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivityCounsellor.this, LinearLayoutManager.HORIZONTAL, false));
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setAdapter(menuUsersRecyclerAdapter);
                }
            }, error -> Log.d("error", error.toString()));
            queue.add(request);
        } else {
            myUsers = counsellerList;
            menuUsersRecyclerAdapter = new MenuUsersRecyclerAdapter(mContext, myUsers);

            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_counsellers);
            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivityCounsellor.this, LinearLayoutManager.HORIZONTAL, false));
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(menuUsersRecyclerAdapter);
        }
    }


    private void setProfileImage() {
//        if (userMe != null)
//            Glide.with(mContext).load(userMe.getImage()).apply(new RequestOptions().placeholder(R.drawable.profile)).into(usersImage);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CONTACTS_REQUEST_CODE:
//                refreshMyContacts();
                break;
        }
    }

    private void refreshMyContacts() {
//        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
        if (!FetchMyUsersService.STARTED) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                firebaseUser.getIdToken(false).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String idToken = task.getResult().getToken();
                        FetchMyUsersService.startMyUsersService(MainActivityCounsellor.this, userMe.getId(), idToken);
                    }
                });
            }
        }
//        } else {
//            FragmentManager manager = getSupportFragmentManager();
//            ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newConfirmInstance(getString(R.string.permission_contact_title),
//                    getString(R.string.permission_contact_message), getString(R.string.okay), getString(R.string.no),
//                    view -> {
//                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST_CODE);
//                    },
//                    view -> {
//                        finish();
//                    });
//            confirmationDialogFragment.show(manager, CONFIRM_TAG);
//        }
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

    private void userUpdated() {
        userMe = helper.getLoggedInUser();
        setProfileImage();
        FragmentManager manager = getSupportFragmentManager();
        Fragment frag = manager.findFragmentByTag(OPTIONS_MORE);
        if (frag != null) {
            ((OptionsFragment) frag).setUserDetails(userMe);
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
//        if (chat.isGroup() && chat.isLatest()) {
//            ArrayList<Message> newGroupForwardList = new ArrayList<>();
//            Message newMessage = new Message();
//            newMessage.setBody(getString(R.string.invitation_group));
//            newMessage.setAttachmentType(AttachmentTypes.NONE_NOTIFICATION);
//            newMessage.setAttachment(null);
//            newGroupForwardList.add(newMessage);
//            openChat(ChatActivity.newIntent(mContext, newGroupForwardList, chat), userImage);
//        } else {
//            openChat(ChatActivity.newIntent(mContext, messageForwardList, chat), userImage);
//        }

        startActivity(CounsellerProfileActivity.newIntent(mContext, chat));
    }

    @Override
    public void onMessageChat(Chat chat, int position, View userImage) {

    }

    @Override
    public void onCallChat(boolean isVideoCall, User user) {

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

    private void deleteChatsFromFirebase(ArrayList<String> chatIdsToDelete) {
        for (String chatChild : chatIdsToDelete) {
            myInboxRef.child(chatChild.startsWith(Helper.GROUP_PREFIX) ? chatChild : Helper.getUserIdFromChatChild(userMe.getId(), chatChild)).setValue(null);
            ArrayList<Message> msgs = helper.getMessages(chatChild);
            ArrayList<String> deletedMessages = helper.getMessagesDeleted(chatChild);
            for (Message msg : msgs)
                deletedMessages.add(msg.getId());
            helper.setMessages(chatChild, new ArrayList<>());
            helper.setMessagesDeleted(chatChild, deletedMessages);
        }
    }

    public static String GetCurrentDate() {
        Calendar today = Calendar.getInstance();
        int date = today.get(Calendar.DATE);
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);

        return year + "-" + (month >= 10 ? month : "0" + month) + "-" + (date >= 10 ? date : "0" + date);
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
                CallScreenActivity.userCounsellor = new User();
                CallScreenActivity.userCounsellor = user;
                CallScreenActivity.userInOut = "OUT";
                String callId = call.getCallId();
                CallScreenActivity.userCallId = callId;
                startActivity(CallScreenActivity.newIntent(mContext, user, callId, "OUT", CallScreenActivity.MAXCALLTIMEINSEC));
            } catch (Exception e) {
                Log.e("CHECK", e.getMessage());
                //ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsSinch, 69);
        }
    }

//    private void myUsersResult(ArrayList<User> myUsers) {
//        this.myUsers.clear();
//        this.myUsers.addAll(myUsers);
////        for (int i = 0; i<=myUsers.size(); i++){
////            if (myUsers.get(i).isInviteAble()){
////                myUsers.remove(i);
////            }
////        }
//        //refreshUsers(-1);
//        menuUsersRecyclerAdapter.notifyDataSetChanged();
//        registerChatUpdates();
//    }


    private TextView txtFrom, txtTo, txtAllPending, txtAllFollowuP, txtAllConverted, txtAllInProcess;
    private boolean isDateFrom = true;

    public void resumeApp() {
        rvMembers = findViewById(R.id.rv_member);
        txtNoData = findViewById(R.id.txt_nodata);

        txtFrom = findViewById(R.id.datePicker_from);
        txtTo = findViewById(R.id.datePicker_to);
        txtAllPending = findViewById(R.id.all_pending);
        txtAllFollowuP = findViewById(R.id.all_followup);
        txtAllConverted = findViewById(R.id.all_converted);
        txtAllInProcess = findViewById(R.id.all_inprocess);

        bottom_sheet = findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(bottom_sheet);

        txtAllPending.setOnClickListener(this);
        txtAllFollowuP.setOnClickListener(this);
        txtAllConverted.setOnClickListener(this);
        txtAllInProcess.setOnClickListener(this);

        StatusSelected();

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                int month = monthOfYear + 1;
                String selectedDate = year + "-" + (month >= 10 ? month : "0" + month)
                        + "-" + (dayOfMonth >= 10 ? dayOfMonth : "0" + dayOfMonth);

                if (isDateFrom) {
                    txtFrom.setText(selectedDate);
                } else {
                    txtTo.setText(selectedDate);
                }
            }
        };

        txtTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtDate = txtTo.getText().toString().trim();
                if (txtDate.equalsIgnoreCase("Select to date")) {
                    txtDate = getcurrentcalDate();
                }
                isDateFrom = false;
                String[] strDate = txtDate.split("-");
                new DatePickerDialog(svContext, date,
                        Integer.parseInt(strDate[0]),
                        Integer.parseInt(strDate[1]) - 1,
                        Integer.parseInt(strDate[2])).show();
            }
        });

        txtFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtDate = txtFrom.getText().toString().trim();
                if (txtDate.equalsIgnoreCase("Select from date")) {
                    txtDate = getcurrentcalDate();
                }
                isDateFrom = true;
                String[] strDate = txtDate.split("-");
                new DatePickerDialog(svContext, date,
                        Integer.parseInt(strDate[0]),
                        Integer.parseInt(strDate[1]) - 1,
                        Integer.parseInt(strDate[2])).show();
            }
        });

        ImageView imgSearch = (ImageView) findViewById(R.id.filter_search);
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((txtFrom.getText().toString().trim()).equalsIgnoreCase("Select from date")) {
                    Toast.makeText(svContext, "Please select from date first", Toast.LENGTH_LONG).show();
                } else if ((txtTo.getText().toString().trim()).equalsIgnoreCase("Select to date")) {
                    Toast.makeText(svContext, "Please select to date first", Toast.LENGTH_LONG).show();
                } else {
                    LoadEnquiryData();
                }
            }
        });
    }


    public static String getcurrentcalDate() {
        Calendar today = Calendar.getInstance();
        int date = today.get(Calendar.DATE);
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);
        month++;

        return year + "-" + (month >= 10 ? month : "0" + month)
                + "-" + (date >= 10 ? date : "0" + date);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //AppExecutors.getInstance().diskIO().execute(() -> {
        //    lstMembers = AppDatabase.getInstance(svContext).membersDao().getMemberList();
        //    initRvMember();
        //});
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lyt_pending:
            case R.id.lyt_folllowup:
            case R.id.lyt_inprocess:
            case R.id.lyt_converted:
                ChangeStatus(enqId, statusSelected);
                break;
            case R.id.all_pending:
                statusSelected = PENDING;
                StatusSelected();
                break;
            case R.id.all_followup:
                statusSelected = NOANSWER;
                StatusSelected();
                break;
            case R.id.all_inprocess:
                statusSelected = MISBEHAVIOR;
                StatusSelected();
                break;
            case R.id.all_converted:
                statusSelected = CONVERTED;
                StatusSelected();
                break;
            case R.id.back_button:

                break;
            case R.id.addConversation:

                break;
            case R.id.users_image:
                if (userMe != null)
                    OptionsFragmentCounsellor.newInstance(getSinchServiceInterface()).show(getSupportFragmentManager(), OPTIONS_MORE);
                break;
        }
    }


    private static final int REQUEST_CODE = 1010;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;

    @Override
    protected void onPause() {
        super.onPause();
    }


    LinkedList<String> lstUploadData = new LinkedList<>();

    private void initRvMember() {
//        lstMembers = AppDatabase.getInstance(svContext).membersDao().getMemberList();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvMembers.setLayoutManager(layoutManager);
        rvMembers.setHasFixedSize(true);
        MemberListAdapter mAdapter = new MemberListAdapter(this, lstMembers);
        rvMembers.setAdapter(mAdapter);
        if (lstMembers.size() == 0){
            rvMembers.setVisibility(View.GONE);
            txtNoData.setVisibility(View.VISIBLE);
        }else {
            rvMembers.setVisibility(View.VISIBLE);
            txtNoData.setVisibility(View.GONE);
        }

        mAdapter.setOnItemClickListener(new MemberListAdapter.OnItemClickListener() {
            @Override
            public void onCallClick(View view, AppointmentUser memberModel, int position) {
                callIsVideo = false;
                Calling("1#:#10", memberModel);
            }

            @Override
            public void onVideoCallClick(View view, AppointmentUser memberModel, int position) {
                callIsVideo = true;
                Calling("1#:#10", memberModel);
            }

            @Override
            public void onChatClick(View view, AppointmentUser memberModel, int position) {
                openChat(ChatActivity.newIntent(mContext, messageForwardList, new Chat(new User(memberModel.getContact(),
                        memberModel.getUser_name()), "Hi")), null);
            }

            @Override
            public void onStatusClick(View view, AppointmentUser memberModel, int position) {
                showBottomSheetDialog(memberModel);
            }
        });
    }

    int CALLTIMEINSEC = 3600;
    boolean callIsVideo = false;
    private void Calling(String data, AppointmentUser memberModel) {
        if (permissionsAvailable(permissionsSinch)) {
            try {
                Call call = callIsVideo ? getSinchServiceInterface().callUserVideo(memberModel.getContact()) : getSinchServiceInterface().callUser(memberModel.getContact());
                if (call == null) {
                    // Service failed for some reason, show a Toast and abort
                    Toast.makeText(this, getString(R.string.sinch_start_error), Toast.LENGTH_LONG).show();
                    return;
                }
                String callId = call.getCallId();
                CallScreenActivity.userCounsellor = new User();
                CallScreenActivity.userCounsellor = new User(memberModel.getContact(), memberModel.getUser_name(),
                        memberModel.getStatus(),GlobalVariables.IMGPREURL + memberModel.getUser_name(),
                        memberModel.getContact());
                CallScreenActivity.userInOut = "OUT";
                CallScreenActivity.userCallId = callId;

                PreferenceConnector.writeString(mContext, PreferenceConnector.callingdata, data);

                if (data.contains("#:#")) {
                    CALLTIMEINSEC = Integer.parseInt(data.split("#:#")[1])*60;
                }
                startActivity(CallScreenActivity.newIntent(this, user, callId, "OUT", CALLTIMEINSEC));
            } catch (Exception e) {
                Log.e("CHECK", e.getMessage());
                //ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsSinch, 69);
        }
    }


    public String callingNumber = "";

    private void ChangeStatus(String enqId, String status) {
        myUsers = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.STATUSCHANGE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response==========>>>>>", response);
                try {
                    JSONObject json = new JSONObject(response);
                    String str_result = json.getString("error");
                    if (str_result.equals("false")) {
                        LoadData();
                        Toast.makeText(svContext, json.getString("msg"), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(svContext, json.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("appo_id", enqId);
                params.put("status", status);
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


    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private View bottom_sheet;
    private String enqId;

    private void showBottomSheetDialog(AppointmentUser memberModel) {
        enqId = memberModel.getAppo_id() + "";
        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        final View view = getLayoutInflater().inflate(R.layout.sheet_list, null);

        ((View) view.findViewById(R.id.lyt_pending)).setOnClickListener(this);
        ((View) view.findViewById(R.id.lyt_folllowup)).setOnClickListener(this);
        ((View) view.findViewById(R.id.lyt_inprocess)).setOnClickListener(this);
        ((View) view.findViewById(R.id.lyt_converted)).setOnClickListener(this);

        ((View) view.findViewById(R.id.lyt_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.cancel();
            }
        });

        mBottomSheetDialog = new BottomSheetDialog(this);
        mBottomSheetDialog.setContentView(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBottomSheetDialog = null;
            }
        });
    }


    private String PENDING = "Pending";
    private String NOANSWER = "NoAnswer";
    private String MISBEHAVIOR = "Misbehavior";
    private String CONVERTED = "Completed";
    private String statusSelected = PENDING;

    private void StatusSelected() {
        if (statusSelected.equalsIgnoreCase(PENDING)) {
            statusSelected = PENDING;
            txtAllPending.setBackground(getResources().getDrawable(R.drawable.back_left_dark));
            txtAllFollowuP.setBackground(getResources().getDrawable(R.drawable.back_center));
            txtAllInProcess.setBackground(getResources().getDrawable(R.drawable.back_center));
            txtAllConverted.setBackground(getResources().getDrawable(R.drawable.back_right));

            PreferenceConnector.writeString(svContext, PreferenceConnector.ENQSTATUS, PENDING);

            txtAllPending.setTextColor(getResources().getColor(R.color.white));
            txtAllFollowuP.setTextColor(getResources().getColor(R.color.black));
            txtAllInProcess.setTextColor(getResources().getColor(R.color.black));
            txtAllConverted.setTextColor(getResources().getColor(R.color.black));
        } else if (statusSelected.equalsIgnoreCase(NOANSWER)) {
            statusSelected = NOANSWER;
            txtAllPending.setBackground(getResources().getDrawable(R.drawable.back_left));
            txtAllFollowuP.setBackground(getResources().getDrawable(R.drawable.back_center_dark));
            txtAllInProcess.setBackground(getResources().getDrawable(R.drawable.back_center));
            txtAllConverted.setBackground(getResources().getDrawable(R.drawable.back_right));

            PreferenceConnector.writeString(svContext, PreferenceConnector.ENQSTATUS, NOANSWER);

            txtAllPending.setTextColor(getResources().getColor(R.color.black));
            txtAllFollowuP.setTextColor(getResources().getColor(R.color.white));
            txtAllInProcess.setTextColor(getResources().getColor(R.color.black));
            txtAllConverted.setTextColor(getResources().getColor(R.color.black));
        } else if (statusSelected.equalsIgnoreCase(MISBEHAVIOR)) {
            statusSelected = MISBEHAVIOR;
            txtAllPending.setBackground(getResources().getDrawable(R.drawable.back_left));
            txtAllFollowuP.setBackground(getResources().getDrawable(R.drawable.back_center));
            txtAllInProcess.setBackground(getResources().getDrawable(R.drawable.back_center_dark));
            txtAllConverted.setBackground(getResources().getDrawable(R.drawable.back_right));

            PreferenceConnector.writeString(svContext, PreferenceConnector.ENQSTATUS, MISBEHAVIOR);

            txtAllPending.setTextColor(getResources().getColor(R.color.black));
            txtAllFollowuP.setTextColor(getResources().getColor(R.color.black));
            txtAllInProcess.setTextColor(getResources().getColor(R.color.white));
            txtAllConverted.setTextColor(getResources().getColor(R.color.black));

        } else if (statusSelected.equalsIgnoreCase(CONVERTED)) {
            statusSelected = CONVERTED;
            txtAllPending.setBackground(getResources().getDrawable(R.drawable.back_left));
            txtAllFollowuP.setBackground(getResources().getDrawable(R.drawable.back_center));
            txtAllInProcess.setBackground(getResources().getDrawable(R.drawable.back_center));
            txtAllConverted.setBackground(getResources().getDrawable(R.drawable.back_right_dark));

            PreferenceConnector.writeString(svContext, PreferenceConnector.ENQSTATUS, CONVERTED);

            txtAllPending.setTextColor(getResources().getColor(R.color.black));
            txtAllFollowuP.setTextColor(getResources().getColor(R.color.black));
            txtAllInProcess.setTextColor(getResources().getColor(R.color.black));
            txtAllConverted.setTextColor(getResources().getColor(R.color.white));
        } else {
            statusSelected = "";
            txtAllPending.setBackground(getResources().getDrawable(R.drawable.back_left));
            txtAllFollowuP.setBackground(getResources().getDrawable(R.drawable.back_center));
            txtAllInProcess.setBackground(getResources().getDrawable(R.drawable.back_center));
            txtAllConverted.setBackground(getResources().getDrawable(R.drawable.back_right));

            PreferenceConnector.writeString(svContext, PreferenceConnector.ENQSTATUS, "");

            txtAllPending.setTextColor(getResources().getColor(R.color.black));
            txtAllFollowuP.setTextColor(getResources().getColor(R.color.black));
            txtAllInProcess.setTextColor(getResources().getColor(R.color.black));
            txtAllConverted.setTextColor(getResources().getColor(R.color.black));
        }

        LoadData();
    }

    private void LoadData() {
        String userID = PreferenceConnector.readString(svContext, PreferenceConnector.LOGINEDUSERID, "");
        items = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.GETAPPOINTMENTS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response=====enq===>>>", response);
                try {
                    JSONObject json = new JSONObject(response);
                    String str_message = json.getString("message");
                    String str_result = json.getString("result");
//                    {"result":true,"message":"data found","data":[{"task_id":"9","appo_id":"3",
                    if (str_result.equalsIgnoreCase("true")) {
                        JSONArray data = json.getJSONArray("data");
                        for (int data_i = 0; data_i < ((JSONArray) data).length(); data_i++) {
                            JSONObject data_obj = data.getJSONObject(data_i);

                            String appointStatus = PENDING;
                            if (data_obj.has("status")) {
                                appointStatus = data_obj.getString("status");
                            }
                            lstMembers.add(new AppointmentUser(data_obj.getString("contact"),
                                    data_obj.getString("user_name"), data_obj.getString("query"),
                                    data_obj.getString("subcategory"), data_obj.getString("appo_id"),
                                    appointStatus));

                            LoadEnquiryData();
                        }
                    } else {
                        Toast.makeText(mContext, str_message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Log.e("..getcurrentcalDate..", getcurrentcalDate());
                Map<String, String> params = new HashMap<String, String>();
                params.put("coun_id", PreferenceConnector.readString(svContext, PreferenceConnector.LOGINEDUSERID, ""));
                params.put("start_date", getcurrentcalDate());
                params.put("end_date", getcurrentcalDate());
                params.put("status", PENDING);
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


    private void LoadEnquiryData() {
        strFromDate = txtFrom.getText().toString().trim();
        strToDate = txtTo.getText().toString().trim();
        if (strFromDate.equalsIgnoreCase("Select from date")) {
            strFromDate = GetCurrentDate();
        }
        if (strToDate.equalsIgnoreCase("Select to date")) {
            strToDate = "";
        }

        initRvMember();
    }

    private Context svContext;
    private ViewGroup root;
    private RecyclerView rvMembers;
    private TextView txtNoData;
    private List<AppointmentUser> lstMembers = new ArrayList<>();

    private final View[] allViewWithClick = {};
    private final int[] allViewWithClickId = {0};

    private final EditText[] edTexts = {};
    private final String[] edTextsError = {};
    private final int[] editTextsClickId = {0};
    private String strFromDate = "", strToDate = "";
}
