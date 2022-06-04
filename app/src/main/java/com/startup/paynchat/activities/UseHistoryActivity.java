package com.startup.paynchat.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;
import com.startup.paynchat.GlobalVariables;
import com.startup.paynchat.R;
import com.startup.paynchat.adapters.UseHistoryAdapter;
import com.startup.paynchat.fragments.UserSelectDialogFragment;
import com.startup.paynchat.models.UseHistoryModel;
import com.startup.paynchat.utils.PreferenceConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UseHistoryActivity extends BaseActivity implements UseHistoryAdapter.ItemClickListener, View.OnClickListener {
    private static final int REQUEST_CODE_CHAT_FORWARD = 99;
    private static String USER_SELECT_TAG = "userselectdialog";
    private static String PROFILE_EDIT_TAG = "profileedittag";
    private ImageView usersImage, backImage;
    private UseHistoryAdapter useAdapter;
    private ArrayList<UseHistoryModel> muList = new ArrayList<>();
    private UserSelectDialogFragment userSelectDialogFragment;

    @Override
    void onSinchConnected() {

    }

    @Override
    void onSinchDisconnected() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUi();
        setupData();

        usersImage.setOnClickListener(this);
        usersImage.setVisibility(View.INVISIBLE);
        backImage.setOnClickListener(this);
        updateFcmToken();
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

    private void setupData() {
        muList = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.VIEWCURRENTACTIVEPLANS, new Response.Listener<String>() {
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
                            muList.add(new UseHistoryModel(data_obj.getString("sub_id"),
                                    data_obj.getString("plan_id"), data_obj.getString("plan_title"),
                                    data_obj.getString("plan_cat"), data_obj.getString("plan_type"),
                                    data_obj.getString("sub_status"), data_obj.getString("price")));
                        }
                    } else {
                        Toast.makeText(mContext, str_message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                useAdapter = new UseHistoryAdapter(mContext, muList);

                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_counsellers);
                recyclerView.setLayoutManager(new LinearLayoutManager(UseHistoryActivity.this, LinearLayoutManager.VERTICAL, false));
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(useAdapter);
            }
        }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", PreferenceConnector.readString(getApplicationContext(), PreferenceConnector.LOGINEDUSERID, ""));
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_button:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onItemClick(UseHistoryModel chat, int position) {

    }
}
