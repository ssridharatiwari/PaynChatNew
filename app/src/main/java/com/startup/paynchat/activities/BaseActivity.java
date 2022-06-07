package com.startup.paynchat.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.startup.paynchat.GlobalVariables;
import com.startup.paynchat.R;
import com.startup.paynchat.models.Contact;
import com.startup.paynchat.models.Message;
import com.startup.paynchat.models.User;
import com.startup.paynchat.services.SinchService;
import com.startup.paynchat.utils.Helper;
import com.startup.paynchat.utils.PreferenceConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by a_man on 01-01-2018.
 */

public abstract class BaseActivity extends AppCompatActivity implements ServiceConnection {
    protected String[] permissionsRecord = {Manifest.permission.VIBRATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    //    protected String[] permissionsContact = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    protected String[] permissionsStorage = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    protected String[] permissionsCamera = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    protected String[] permissionsSinch = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.READ_PHONE_STATE};

    protected User userMe;
    protected Helper helper;
    protected Context mContext;
    private HashMap<String, Contact> savedContacts;

    protected DatabaseReference usersRef, groupRef, chatRef, inboxRef;
    private SinchService.SinchServiceInterface mSinchServiceInterface;

    abstract void onSinchConnected();

    abstract void onSinchDisconnected();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        helper = new Helper(mContext);
        userMe = helper.getLoggedInUser();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();//get firebase instance
        usersRef = firebaseDatabase.getReference(Helper.REF_USERS);//instantiate user's firebase reference
        groupRef = firebaseDatabase.getReference(Helper.REF_GROUP);//instantiate group's firebase reference
        chatRef = firebaseDatabase.getReference(Helper.REF_CHAT);//instantiate chat's firebase reference
        inboxRef = firebaseDatabase.getReference(Helper.REF_INBOX);//instantiate inbox's firebase reference
        GetAllCousellorsListWeb();
        //startService(new Intent(this, FirebaseChatService.class));
        getApplicationContext().bindService(new Intent(mContext, SinchService.class), this, BIND_AUTO_CREATE);
    }

    public boolean IsChatPurchased() {
        IsPlanSubscribed("");
        return isChatActive;
    }

    public boolean IsAudioPurchased() {
        IsPlanSubscribed("");
        return isAudioActive;
    }

    public boolean IsVideoPurchased() {
        IsPlanSubscribed("");
        return isVideoActive;
    }

    public void OpenPurchaseActivity(Context context) {
        Intent svIntent = new Intent(context, ViewPlansActivity.class);
        svIntent.putExtra("gotoform", "0");
        context.startActivity(svIntent);
    }

//    public ArrayList<User> GetAllUSers() {
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef;
//        myRef = database.getReference();
////        final DatabaseReference userReference = myRef.child("users").child("online").equals("true");
////public User(String id, String name, String status, String image, boolean isCounsellor) {
//        ArrayList<User> userList = new ArrayList<>();
//        userReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                    User userModel = postSnapshot.getValue(User.class);
//                    userList.add(userModel);
////                    Toast.makeText(BaseActivity.this, userModel.getName(), Toast.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });
//        return userList;
//    }

    public static ArrayList<User> counsellerList = new ArrayList<>();

    private void GetAllCousellorsListWeb() {
        counsellerList = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.GETCONSELLORLIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response====h=====>>>>>", response);
                try {
                    JSONObject json = new JSONObject(response);
                    String str_message = json.getString("message");
                    String str_result = json.getString("result");

                    if (str_result.equalsIgnoreCase("true")) {
                        JSONArray data = json.getJSONArray("data");
                        for (int data_i = 0; data_i < ((JSONArray) data).length(); data_i++) {
                            JSONObject data_obj = data.getJSONObject(data_i);

                            //{"result":true,"message":"data found","data":[{"id":"19","name":"Shridhar","contact":"+917691001989","address":null,"languages":null,"mail":"ssonu0001@gmail.com","experience":null,"about":null,"password":null,"image":null,"adhar_no":null,"adhar_img":null,"ac_holder":null,"ac_number":null,"bank":null,"other":null,"role":"lowyer","status":null,"created_at":null,"updated_at":null}]}
                            counsellerList.add(new User(data_obj.getString("contact"), data_obj.getString("name"),
                                    data_obj.getString("status"),
                                    GlobalVariables.IMGPREURL + data_obj.getString("image"),
                                    data_obj.getString("id")));
//                            {"id":"21",,"mail":"shiv@gmail.com","role":"lowyer","status":"Block"}
                        }
                    } else {
                        Toast.makeText(mContext, str_message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.d("error", error.toString()));
        queue.add(request);
    }


    @Override
    protected void onDestroy() {
        mContext = null;
        super.onDestroy();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (SinchService.class.getName().equals(componentName.getClassName())) {
            mSinchServiceInterface = (SinchService.SinchServiceInterface) iBinder;
            onSinchConnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        if (SinchService.class.getName().equals(componentName.getClassName())) {
            mSinchServiceInterface = null;
            onSinchDisconnected();
        }
    }

    protected SinchService.SinchServiceInterface getSinchServiceInterface() {
        return mSinchServiceInterface;
    }

    protected boolean permissionsAvailable(String[] permissions) {
        boolean granted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }
        return granted;
    }

    protected void refreshMyContactsCache(HashMap<String, Contact> contactsToSet) {
        savedContacts = (contactsToSet != null) ? contactsToSet : helper.getMyContacts();
    }

    protected HashMap<String, Contact> getSavedContacts() {
        if (savedContacts == null) refreshMyContactsCache(null);
        return savedContacts;
    }

    protected String getNameById(String senderId) {
        if (savedContacts == null) refreshMyContactsCache(null);
        String senderIDEndTrim = Helper.getEndTrim(senderId);
        if (savedContacts.containsKey(senderIDEndTrim))
            return savedContacts.get(senderIDEndTrim).getName();
        else
            return senderId;
    }

    protected void setNotificationMessageNames(Message newMessage) {
        String[] bodySplit = newMessage.getBody().split(" ");
        if (bodySplit.length > 1) {
            String userOneIDEndTrim = Helper.getEndTrim(bodySplit[0]);
            if (TextUtils.isDigitsOnly(userOneIDEndTrim)) {
                String userNameInPhone;
                if (bodySplit[0].equals(userMe.getId()))
                    userNameInPhone = getString(R.string.you);
                else
                    userNameInPhone = newMessage.getSenderName();
//                    userNameInPhone = getNameById(userOneIDEndTrim);
                if (userNameInPhone.equals(userOneIDEndTrim))
                    userNameInPhone = bodySplit[0];
                newMessage.setBody(userNameInPhone + newMessage.getBody().substring(bodySplit[0].length()));
            }
            String userTwoIDEndTrim = Helper.getEndTrim(bodySplit[bodySplit.length - 1]);
            if (TextUtils.isDigitsOnly(userTwoIDEndTrim)) {
                String userNameInPhone;
                if (bodySplit[bodySplit.length - 1].equals(userMe.getId()))
                    userNameInPhone = getString(R.string.you);
                else
                    userNameInPhone = newMessage.getSenderName();
//                    userNameInPhone = getNameById(userOneIDEndTrim);
                if (userNameInPhone.equals(userTwoIDEndTrim))
                    userNameInPhone = bodySplit[bodySplit.length - 1];
                newMessage.setBody(newMessage.getBody().substring(0, newMessage.getBody().indexOf(bodySplit[bodySplit.length - 1])) + userNameInPhone);
            }
        }
    }

    //App TODO by  ssridhara
    //Make by default false
    public static boolean isChatActive = true, isAudioActive = true, isVideoActive = true;
    public static String audioData = "", videoData = "", chatData = "";

    public static boolean IsPlanSubscribed(Context mContext) {
        int walletBal = PreferenceConnector.readInteger(mContext, PreferenceConnector.WALLETBAL, 0);
        if (walletBal <= 20) {
            isChatActive = false;
            isVideoActive = false;
            isChatActive = false;

            return false;
        } else {
            isChatActive = true;
            isVideoActive = true;
            isChatActive = true;

            return true;
        }
    }

    public void IsPlanSubscribed(String id) {
        int walletBal = PreferenceConnector.readInteger(mContext, PreferenceConnector.WALLETBAL, 0);
        if (walletBal <= 20) {
            isChatActive = false;
            isVideoActive = false;
            isChatActive = false;
        }else{
            isChatActive = true;
            isVideoActive = true;
            isChatActive = true;
        }

//        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
//        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.ISPLANACTIVE, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                Log.d("response=====i====>>>>>", response);
//                try {
//                    JSONObject json = new JSONObject(response);
//                    String str_result = json.getString("result");
//                    if (str_result.equalsIgnoreCase("true")) {
//                        JSONArray data = json.getJSONArray("plan_true_false");
//                        for (int j = 0; j < data.length(); j++) {
//                            JSONObject data_obj = data.getJSONObject(j);
//                            Iterator<String> iterator = data_obj.keys();
//                            while (iterator.hasNext()) {
//                                String key = iterator.next();
//                                Log.i("TAG", "key:" + key + "--Value::" + data_obj.optString(key));
//                                if (key.equalsIgnoreCase("Chat")) {
//                                    if (data_obj.optString(key).equalsIgnoreCase("True")) {
//                                        isChatActive = true;
//                                        JSONArray datamin = data_obj.getJSONArray("data");
//                                        for (int i = 0; i < datamin.length(); i++) {
//                                            JSONObject datamin_obj = data.getJSONObject(i);
//                                            if (datamin_obj.has("id") && datamin_obj.has("time")) {
//                                                chatData = datamin_obj.getString("id") + "#:#" + (datamin_obj.getString("time").split(" ")[0]);
//                                            }
//                                        }
//                                    } else {
//                                        isChatActive = false;
//                                    }
//                                } else if (key.equalsIgnoreCase("Audio")) {
//                                    if (data_obj.optString(key).equalsIgnoreCase("True")) {
//                                        isAudioActive = true;
//                                        JSONArray datamin = data_obj.getJSONArray("data");
//                                        for (int i = 0; i < datamin.length(); i++) {
//                                            JSONObject datamin_obj = datamin.getJSONObject(i);
//                                            if (datamin_obj.has("id") && datamin_obj.has("time")) {
//                                                audioData = datamin_obj.getString("id") + "#:#" + (datamin_obj.getString("time").split(" ")[0]);
//                                            }
//                                        }
//                                    } else {
//                                        isAudioActive = false;
//                                    }
//                                } else if (key.equalsIgnoreCase("Video")) {
//                                    if (data_obj.optString(key).equalsIgnoreCase("True")) {
//                                        isVideoActive = true;
//                                        JSONArray datamin = data_obj.getJSONArray("data");
//                                        for (int i = 0; i < datamin.length(); i++) {
//                                            JSONObject datamin_obj = data.getJSONObject(i);
//                                            if (datamin_obj.has("id") && datamin_obj.has("time")) {
//                                                videoData = datamin_obj.getString("id") + "#:#" + (datamin_obj.getString("time").split(" ")[0]);
//                                            }
//                                        }
//                                    } else {
//                                        isVideoActive = false;
//                                    }
//                                }
//                            }
//                        }
//                    } else {
////                        Toast.makeText(mContext, json.getString("message"), Toast.LENGTH_SHORT).show();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    Log.i("TAG", "--Value::" + "error");
//                }
//            }
//        }, error -> Log.d("error", error.toString())) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("user_id", id);
//                return params;
//            }
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("Content-Type", "application/x-www-form-urlencoded");
//                return params;
//            }
//        };
//        queue.add(request);
    }

}
