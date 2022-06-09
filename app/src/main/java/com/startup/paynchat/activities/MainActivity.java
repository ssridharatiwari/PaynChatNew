package com.startup.paynchat.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;
import com.startup.paynchat.adapters.WhyChooseUsAdapter;
import com.sinch.android.rtc.calling.Call;
import com.startup.paynchat.GlobalVariables;
import com.startup.paynchat.R;
import com.startup.paynchat.adapters.HomeAdapter;
import com.startup.paynchat.adapters.MenuUsersRecyclerAdapter;
import com.startup.paynchat.adapters.ViewPagerAdapter;
import com.startup.paynchat.fragments.MyCallsFragment;
import com.startup.paynchat.fragments.MyChatsFragment;
import com.startup.paynchat.fragments.OptionsFragment;
import com.startup.paynchat.fragments.ProfileEditDialogFragment;
import com.startup.paynchat.fragments.UserSelectDialogFragment;
import com.startup.paynchat.interfaces.ChatItemClickListener;
import com.startup.paynchat.models.AttachmentTypes;
import com.startup.paynchat.models.Chat;
import com.startup.paynchat.models.CategoryModel;
import com.startup.paynchat.models.Message;
import com.startup.paynchat.models.User;
import com.startup.paynchat.services.FetchMyUsersService;
import com.startup.paynchat.utils.Helper;
import com.startup.paynchat.utils.PreferenceConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity implements ChatItemClickListener, View.OnClickListener {
    private static final int REQUEST_CODE_CHAT_FORWARD = 99;
    private final int CONTACTS_REQUEST_CODE = 321;
    private static String USER_SELECT_TAG = "userselectdialog";
    private static String OPTIONS_MORE = "optionsmore";
    private static String PROFILE_EDIT_TAG = "profileedittag";
    private static String GROUP_CREATE_TAG = "groupcreatedialog";
    private static String CONFIRM_TAG = "confirmtag";
    private ImageView vipMembership, backImage, dialogUserImage;
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
        initUi();
        setupMenu();
        LoadBanner();
        initBottomMenu();
//        setProfileImage();
        vipMembership.setOnClickListener(this);
        backImage.setOnClickListener(this);
        updateFcmToken();

        boolean newUser = getIntent().getBooleanExtra("newUser", false);
        if (newUser && userMe != null) {
            Toast.makeText(mContext, R.string.setup_profile_msg, Toast.LENGTH_LONG).show();
            ProfileEditDialogFragment.newInstance(true).show(getSupportFragmentManager(), PROFILE_EDIT_TAG);
        }

        LoadUserLoginData();

//        LoadWhyChooseUs();
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

    private List<CategoryModel> items = new ArrayList<>();

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

                            int drawable;
                            drawable = R.drawable.girl;
                            items.add(new CategoryModel(drawable, id, category, ""));
                        }
                    } else {
                        Toast.makeText(mContext, str_message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HomeAdapter mAdapter = new HomeAdapter(MainActivity.this, items);
                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_services);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(mAdapter);
                mAdapter.setOnItemClickListener(new HomeAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, CategoryModel obj, int position) {

//                        Intent intent = new Intent(MainActivity.this, ServiceActivity.class);
//                        intent.putExtra("serviceid", obj.getId());
//                        intent.putExtra("servicename", obj.getCategory());
//                        startActivity(intent);
                    }
                });

                HideProgressDialog();
            }
        }, error -> Log.d("error", error.toString()));
        queue.add(request);
    }

    private void LoadBanner() {
        ArrayList<SlideModel> imageList = new ArrayList<SlideModel>();

        imageList.add(new SlideModel(R.drawable.banner, ScaleTypes.FIT));
        imageList.add(new SlideModel(R.drawable.banner_two, ScaleTypes.FIT));

        ImageSlider imageSlider = findViewById(R.id.image_slider);
        imageSlider.setImageList(imageList);
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
        HideProgressDialog();
        super.onDestroy();
    }

    private void initUi() {
        setContentView(R.layout.activity_main);
        vipMembership = findViewById(R.id.users_image);
        toolbarContainer = findViewById(R.id.toolbarContainer);
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
        ShowProgressDialog("Loading", "Loading Data");
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
                                String imgUrl = "";
                                if (data_obj.has("image")) {
                                    imgUrl = GlobalVariables.IMGPREURL + data_obj.getString("image");
                                } else {
                                    imgUrl = "";
                                }
                                //{"result":true,"message":"data found","data":[{"id":"19","name":"Shridhar","contact":"+917691001989","address":null,"languages":null,"mail":"ssonu0001@gmail.com","experience":null,"about":null,"password":null,"image":null,"adhar_no":null,"adhar_img":null,"ac_holder":null,"ac_number":null,"bank":null,"other":null,"role":"lowyer","status":null,"created_at":null,"updated_at":null}]}
                                mcounsellerList.add(new User(data_obj.getString("contact"), data_obj.getString("name"),
                                        data_obj.getString("status"),
                                        imgUrl,
                                        data_obj.getString("id"), data_obj.getString("role")));
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
                    recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setAdapter(menuUsersRecyclerAdapter);
                }
            }, error -> Log.d("error", error.toString()));
            queue.add(request);
        } else {
            myUsers = counsellerList;
            menuUsersRecyclerAdapter = new MenuUsersRecyclerAdapter(mContext, myUsers);

            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_counsellers);
            recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(menuUsersRecyclerAdapter);
        }
        LoadServices();
    }

    private void LoadWhyChooseUs() {
        List<String> itemWhyChooseUs = new ArrayList<>();
        itemWhyChooseUs.add("Save Your Time and Money");
        itemWhyChooseUs.add("Save your Relationship");
        itemWhyChooseUs.add("Secure and Confidential");
        itemWhyChooseUs.add("5+ Years Experienced Experts");
        itemWhyChooseUs.add("For going to Right Direction");
        itemWhyChooseUs.add("For Mental Peace and Peaceful Resolution");
        itemWhyChooseUs.add("Unlimited Conversations with Experts");
        itemWhyChooseUs.add("Connect with a Verfied Psychologists and Attorney in Minutes");
        itemWhyChooseUs.add("A team of experts Available on call 24x7");
        WhyChooseUsAdapter mAdapter = new WhyChooseUsAdapter(this, itemWhyChooseUs);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_whychooseus);
        recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 1));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
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
                        FetchMyUsersService.startMyUsersService(MainActivity.this, userMe.getId(), idToken);
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
        startActivity(CounsellerProfileActivity.newIntent(mContext, chat));
    }

    @Override
    public void onCallChat(boolean isVideoCall, User user) {
        placeCall(isVideoCall, user);
    }

    @Override
    public void onMessageChat(Chat chat, int position, View userImage) {
        if (chat.isGroup() && chat.isLatest()) {
            ArrayList<Message> newGroupForwardList = new ArrayList<>();
            Message newMessage = new Message();
            newMessage.setBody(getString(R.string.invitation_group));
            newMessage.setAttachmentType(AttachmentTypes.NONE_NOTIFICATION);
            newMessage.setAttachment(null);
            newGroupForwardList.add(newMessage);
            openChat(ChatActivity.newIntent(mContext, newGroupForwardList, chat), userImage);
        } else {
            openChat(ChatActivity.newIntent(mContext, messageForwardList, chat), userImage);
        }
    }


    private void openChat(Intent intent, View userImage) {
        if (userImage == null) {
            userImage = vipMembership;
        }

        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, userImage, "userImage");
        startActivityForResult(intent, REQUEST_CODE_CHAT_FORWARD, activityOptionsCompat.toBundle());

        if (userSelectDialogFragment != null)
            userSelectDialogFragment.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.users_image:
//                if (userMe != null)
//                    OptionsFragment.newInstance(getSinchServiceInterface()).show(getSupportFragmentManager(), OPTIONS_MORE);
                Intent intent = new Intent(MainActivity.this, ViewPlansActivity.class);
                intent.putExtra("gotoform", "0");
                startActivity(intent);
                finishAffinity();
                break;
        }
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
                String callId = call.getCallId();
                startActivity(CallScreenActivity.newIntent(mContext, user, callId, "OUT",
                        CallScreenActivity.MAXCALLTIMEINSEC));
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
        ShowProgressDialog("Loading", "Loading Data");
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.MAKEWISHLIST, new Response.Listener<String>() {
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

                HideProgressDialog();
            }
        }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", PreferenceConnector.readString(mContext, PreferenceConnector.LOGINEDUSERID, ""));
                params.put("lowyer_id", user.getUserId());
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


    private ProgressDialog progressDialog;

    private void ShowProgressDialog(String strShowTextTitle, String strShowMessage) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(strShowTextTitle);
            progressDialog.setMessage(strShowMessage);
            progressDialog.setCancelable(false);
        }
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void HideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();

    }


    private void initBottomMenu() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectFragment = null;
                Intent svIntent;
                switch (item.getItemId()) {
                    case R.id.nav_home:

                        break;
                    case R.id.nav_fav:
                        svIntent = new Intent(MainActivity.this, FavoraiteActivity.class);
                        startActivity(svIntent);
                        break;
                    case R.id.nav_chatbox:
                        svIntent = new Intent(MainActivity.this, MainActivityOld.class);
                        startActivity(svIntent);
                        break;
                    case R.id.nav_setting:
                        OptionsFragment.newInstance(getSinchServiceInterface()).show(getSupportFragmentManager(), OPTIONS_MORE);
                        break;
                }

                return false;
            }
        });
    }

    public void switchContent(Fragment fragment) {
        hideKeyboard();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        // check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
