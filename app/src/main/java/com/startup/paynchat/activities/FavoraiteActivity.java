package com.startup.paynchat.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.startup.paynchat.GlobalVariables;
import com.startup.paynchat.R;
import com.startup.paynchat.adapters.MenuUsersRecyclerAdapter;
import com.startup.paynchat.fragments.UserSelectDialogFragment;
import com.startup.paynchat.interfaces.ChatItemClickListener;
import com.startup.paynchat.models.Chat;
import com.startup.paynchat.models.Message;
import com.startup.paynchat.models.User;
import com.startup.paynchat.utils.Helper;
import com.startup.paynchat.utils.PreferenceConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FavoraiteActivity extends AppCompatActivity implements ChatItemClickListener, View.OnClickListener {
    private static final int REQUEST_CODE_CHAT_FORWARD = 99;
    private final int CONTACTS_REQUEST_CODE = 321;
    private static String USER_SELECT_TAG = "userselectdialog";
    private static String OPTIONS_MORE = "optionsmore";
    private static String PROFILE_EDIT_TAG = "profileedittag";
    private static String GROUP_CREATE_TAG = "groupcreatedialog";
    private static String CONFIRM_TAG = "confirmtag";
    private ImageView vipMembership, backImage;
    private MenuUsersRecyclerAdapter menuUsersRecyclerAdapter;
    private ArrayList<User> myUsers = new ArrayList<>();
    private ArrayList<Message> messageForwardList = new ArrayList<>();
    private UserSelectDialogFragment userSelectDialogFragment;
    private Context mContext;
    private TextView txtNoData;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initUi();
        setupMenu();
        setProfileImage();
        vipMembership.setOnClickListener(this);
        backImage.setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(view18 -> {
            Helper.closeKeyboard(this, view18);
            onBackPressed();
        });

        findViewById(R.id.txt_back).setOnClickListener(view18 -> {
            Helper.closeKeyboard(this, view18);
            onBackPressed();
        });
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
        setContentView(R.layout.activity_fav);
        recyclerView = (RecyclerView) findViewById(R.id.rv_counsellers);
        vipMembership = findViewById(R.id.users_image);
        backImage = findViewById(R.id.back_button);
        txtNoData = findViewById(R.id.txt_nodata);
    }

    private void setupMenu() {
        ShowProgressDialog("Loading", "Loading Data");
        myUsers = new ArrayList<>();
        ArrayList<User> mcounsellerList = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.GETWISHLISTCONSELLORLIST, new Response.Listener<String>() {
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

                        myUsers = mcounsellerList;
                        menuUsersRecyclerAdapter = new MenuUsersRecyclerAdapter(mContext, myUsers);


                        recyclerView.setLayoutManager(new GridLayoutManager(FavoraiteActivity.this, 2));
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setAdapter(menuUsersRecyclerAdapter);

                        recyclerView.setVisibility(View.VISIBLE);
                        txtNoData.setVisibility(View.GONE);
                    } else {
                        txtNoData.setText(str_message);
                        recyclerView.setVisibility(View.GONE);
                        txtNoData.setVisibility(View.VISIBLE);
//                        Toast.makeText(mContext, str_message, Toast.LENGTH_SHORT).show();
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
            case R.id.users_image:
//                if (userMe != null)
//                    OptionsFragment.newInstance(getSinchServiceInterface()).show(getSupportFragmentManager(), OPTIONS_MORE);
                Intent intent = new Intent(FavoraiteActivity.this, ViewPlansActivity.class);
                intent.putExtra("gotoform", "0");
                startActivity(intent);
                break;


        }
    }

    @Override
    public void placeCall(boolean callIsVideo, User user) {
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
