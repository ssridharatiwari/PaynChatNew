package com.startup.paynchat.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.startup.paynchat.GlobalVariables;
import com.startup.paynchat.R;
import com.startup.paynchat.models.Country;
import com.startup.paynchat.models.User;
import com.startup.paynchat.utils.Helper;
import com.startup.paynchat.utils.KeyboardUtil;
import com.startup.paynchat.utils.PreferenceConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConsultantSignInActivity extends AppCompatActivity {
    private AppCompatSpinner spinnerCountryCodes;
    private EditText edname, etPhone;
    private KeyboardUtil keyboardUtil;
    private ProgressDialog progressDialog;
    private Context context;
    private Helper helper;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        helper = new Helper(this);

        User user = helper.getLoggedInUser();
        //init vars
        keyboardUtil = KeyboardUtil.getInstance(this);
        progressDialog = new ProgressDialog(this);

        //if there is number to authenticate in preferences then initiate
        setContentView(R.layout.activity_sign_in_coun);
        //setup number selection
        spinnerCountryCodes = findViewById(R.id.countryCode);
        etPhone = findViewById(R.id.phoneNumber);
        edname = findViewById(R.id.edname);

        setupCountryCodes();
        findViewById(R.id.submit).setOnClickListener(view -> {
            CheckCounsellor(((Country) spinnerCountryCodes.getSelectedItem()).getDialCode() + etPhone.getText().toString().replaceAll("\\s+", ""));
        });

        findViewById(R.id.login_consultant).setOnClickListener(view -> {
            Intent svIntent = new Intent(ConsultantSignInActivity.this, SignInActivity.class);
            startActivity(svIntent);
            finish();
        });

        findViewById(R.id.reg_consultant).setOnClickListener(view -> {
//            Intent svIntent = new Intent(ConsultantSignInActivity.this, ConsultantSignUpActivity.class);
//            startActivity(svIntent);
//            finish();
        });

//        if (user != null) {
//            if (user.isCounsellor()) {
//                CheckCounsellor(user.getId());
//            }
//        } else {
////            permissionHandler.postDelayed(permissionRunnable, 200);
//        }
    }

    private DatabaseReference userRef;
    String phoneNumberInPrefs;
    String nameInPrefs;

    private void loginSignupCounsellor(String phoneNumberInPrefs, String nameInPrefs) {
        this.phoneNumberInPrefs = phoneNumberInPrefs;
        this.nameInPrefs = nameInPrefs;
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        userRef = firebaseDatabase.getReference(Helper.REF_USERS).child(phoneNumberInPrefs);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    try {
                        User user = dataSnapshot.getValue(User.class);
                        if (User.validate(user)) {
                            helper.setLoggedInUser(user);
                            done(false);
                            Toast.makeText(context, "done", Toast.LENGTH_SHORT).show();
                        } else {
                            createUser(new User(phoneNumberInPrefs, nameInPrefs, getString(R.string.hey_there) + " " + getString(R.string.app_name), "", true));
                        }
                    } catch (Exception ex) {
                        createUser(new User(phoneNumberInPrefs, nameInPrefs, getString(R.string.hey_there) + " " + getString(R.string.app_name), "", true));
                    }
                } else {
                    createUser(new User(phoneNumberInPrefs, nameInPrefs, getString(R.string.hey_there) + " " + getString(R.string.app_name), "", true));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                createUser(new User(phoneNumberInPrefs, nameInPrefs, getString(R.string.hey_there) + " " + getString(R.string.app_name), "", true));

            }
        });
    }

    private void done(boolean newUser) {
        Intent intent = new Intent(this, MainActivityCounsellor.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("newUser", newUser);
        startActivity(intent);
        ConsultantSignInActivity.this.finish();
    }

    private void createUser(final User newUser) {
        userRef.setValue(newUser).addOnSuccessListener(aVoid -> {
            helper.setLoggedInUser(newUser);
            done(false);
        }).addOnFailureListener(e -> Toast.makeText(ConsultantSignInActivity.this, e.toString()+"", Toast.LENGTH_LONG).show());
    }


    private void CheckCounsellor(String counsid) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.GETCOUNSELLORDETAILBYPHONE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response===cc=====>>>>>", response);
//                {"result":true,"message":"data found","data":[{"id":"55","name":"Sapna","contact":"+917691001989","mail":"angelsapna@gmail.com",
//                "role":"Lawyer, Counseller","image":null,"status":"Active","live_status":"Online"}]}
                try {
                    JSONObject json = new JSONObject(response);
                    String str_result = json.getString("result");
//                        submit();
                    if (str_result.equalsIgnoreCase("true")) {
                        JSONArray data = json.getJSONArray("data");
                        JSONObject data_obj = data.getJSONObject(0);

                        PreferenceConnector.writeString(context, PreferenceConnector.LOGINEDUSERID, data_obj.getString("id"));
                        PreferenceConnector.writeString(context, PreferenceConnector.LOGINEDNAME, data_obj.getString("name"));
                        PreferenceConnector.writeString(context, PreferenceConnector.LOGINEDPHONE, data_obj.getString("contact"));
                        PreferenceConnector.writeString(context, PreferenceConnector.LOGINEDEMAIL, data_obj.getString("mail"));


                        loginSignupCounsellor(data_obj.getString("contact"), data_obj.getString("name"));
                    } else {
                        helper.logout(context);
                        Toast.makeText(context, "Please register first. Contact Admin", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", counsid);
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


    private void setupCountryCodes() {
        int indiaPos = 0;
        ArrayList<Country> countries = getCountries();
        if (countries != null) {
            ArrayAdapter<Country> myAdapter = new ArrayAdapter<Country>(this, R.layout.item_country_spinner, countries);
            spinnerCountryCodes.setAdapter(myAdapter);

            String locale = getResources().getConfiguration().locale.getCountry();
            if (locale != null) {
                int mcPos = -1;
                for (int i = 0; i < countries.size(); i++) {
                    if (countries.get(i).getCode().toLowerCase().equals(locale.toLowerCase())) {
                        mcPos = i;
                        break;
                    }
                    if (countries.get(i).getDialCode().equals("+91")) {
                        indiaPos = i;
                    }
                }
                if (mcPos != -1) {
                    spinnerCountryCodes.setSelection(indiaPos);
                }
            }
        }
    }

    private ArrayList<Country> getCountries() {
        ArrayList<Country> toReturn = new ArrayList<>();
        try {
            JSONArray countrArray = new JSONArray(readEncodedJsonString());
            toReturn = new ArrayList<>();
            for (int i = 0; i < countrArray.length(); i++) {
                JSONObject jsonObject = countrArray.getJSONObject(i);
                String countryName = jsonObject.getString("name");
                String countryDialCode = jsonObject.getString("dial_code");
                String countryCode = jsonObject.getString("code");
                Country country = new Country(countryCode, countryName, countryDialCode);
                toReturn.add(country);
            }
            Collections.sort(toReturn, (lhs, rhs) -> lhs.getName().compareTo(rhs.getName()));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    private String readEncodedJsonString() throws IOException {
        String base64 = "W3sibmFtZSI6IkFmZ2hhbmlzdGFuIiwiZGlhbF9jb2RlIjoiKzkzIiwiY29kZSI6IkFGIn0seyJuYW1lIjoiQWxiYW5pYSIsImRpYWxfY29kZSI6IiszNTUiLCJjb2RlIjoiQUwifSx7Im5hbWUiOiJBbGdlcmlhIiwiZGlhbF9jb2RlIjoiKzIxMyIsImNvZGUiOiJEWiJ9LHsibmFtZSI6IkFtZXJpY2FuU2Ftb2EiLCJkaWFsX2NvZGUiOiIrMSA2ODQiLCJjb2RlIjoiQVMifSx7Im5hbWUiOiJBbmRvcnJhIiwiZGlhbF9jb2RlIjoiKzM3NiIsImNvZGUiOiJBRCJ9LHsibmFtZSI6IkFuZ29sYSIsImRpYWxfY29kZSI6IisyNDQiLCJjb2RlIjoiQU8ifSx7Im5hbWUiOiJBbmd1aWxsYSIsImRpYWxfY29kZSI6IisxIDI2NCIsImNvZGUiOiJBSSJ9LHsibmFtZSI6IkFudGFyY3RpY2EiLCJkaWFsX2NvZGUiOiIrNjcyIiwiY29kZSI6IkFRIn0seyJuYW1lIjoiQW50aWd1YSBhbmQgQmFyYnVkYSIsImRpYWxfY29kZSI6IisxMjY4IiwiY29kZSI6IkFHIn0seyJuYW1lIjoiQXJnZW50aW5hIiwiZGlhbF9jb2RlIjoiKzU0IiwiY29kZSI6IkFSIn0seyJuYW1lIjoiQXJtZW5pYSIsImRpYWxfY29kZSI6IiszNzQiLCJjb2RlIjoiQU0ifSx7Im5hbWUiOiJBcnViYSIsImRpYWxfY29kZSI6IisyOTciLCJjb2RlIjoiQVcifSx7Im5hbWUiOiJBdXN0cmFsaWEiLCJkaWFsX2NvZGUiOiIrNjEiLCJjb2RlIjoiQVUifSx7Im5hbWUiOiJBdXN0cmlhIiwiZGlhbF9jb2RlIjoiKzQzIiwiY29kZSI6IkFUIn0seyJuYW1lIjoiQXplcmJhaWphbiIsImRpYWxfY29kZSI6Iis5OTQiLCJjb2RlIjoiQVoifSx7Im5hbWUiOiJCYWhhbWFzIiwiZGlhbF9jb2RlIjoiKzEgMjQyIiwiY29kZSI6IkJTIn0seyJuYW1lIjoiQmFocmFpbiIsImRpYWxfY29kZSI6Iis5NzMiLCJjb2RlIjoiQkgifSx7Im5hbWUiOiJCYW5nbGFkZXNoIiwiZGlhbF9jb2RlIjoiKzg4MCIsImNvZGUiOiJCRCJ9LHsibmFtZSI6IkJhcmJhZG9zIiwiZGlhbF9jb2RlIjoiKzEgMjQ2IiwiY29kZSI6IkJCIn0seyJuYW1lIjoiQmVsYXJ1cyIsImRpYWxfY29kZSI6IiszNzUiLCJjb2RlIjoiQlkifSx7Im5hbWUiOiJCZWxnaXVtIiwiZGlhbF9jb2RlIjoiKzMyIiwiY29kZSI6IkJFIn0seyJuYW1lIjoiQmVsaXplIiwiZGlhbF9jb2RlIjoiKzUwMSIsImNvZGUiOiJCWiJ9LHsibmFtZSI6IkJlbmluIiwiZGlhbF9jb2RlIjoiKzIyOSIsImNvZGUiOiJCSiJ9LHsibmFtZSI6IkJlcm11ZGEiLCJkaWFsX2NvZGUiOiIrMSA0NDEiLCJjb2RlIjoiQk0ifSx7Im5hbWUiOiJCaHV0YW4iLCJkaWFsX2NvZGUiOiIrOTc1IiwiY29kZSI6IkJUIn0seyJuYW1lIjoiQm9saXZpYSwgUGx1cmluYXRpb25hbCBTdGF0ZSBvZiIsImRpYWxfY29kZSI6Iis1OTEiLCJjb2RlIjoiQk8ifSx7Im5hbWUiOiJCb3NuaWEgYW5kIEhlcnplZ292aW5hIiwiZGlhbF9jb2RlIjoiKzM4NyIsImNvZGUiOiJCQSJ9LHsibmFtZSI6IkJvdHN3YW5hIiwiZGlhbF9jb2RlIjoiKzI2NyIsImNvZGUiOiJCVyJ9LHsibmFtZSI6IkJyYXppbCIsImRpYWxfY29kZSI6Iis1NSIsImNvZGUiOiJCUiJ9LHsibmFtZSI6IkJyaXRpc2ggSW5kaWFuIE9jZWFuIFRlcnJpdG9yeSIsImRpYWxfY29kZSI6IisyNDYiLCJjb2RlIjoiSU8ifSx7Im5hbWUiOiJCcnVuZWkgRGFydXNzYWxhbSIsImRpYWxfY29kZSI6Iis2NzMiLCJjb2RlIjoiQk4ifSx7Im5hbWUiOiJCdWxnYXJpYSIsImRpYWxfY29kZSI6IiszNTkiLCJjb2RlIjoiQkcifSx7Im5hbWUiOiJCdXJraW5hIEZhc28iLCJkaWFsX2NvZGUiOiIrMjI2IiwiY29kZSI6IkJGIn0seyJuYW1lIjoiQnVydW5kaSIsImRpYWxfY29kZSI6IisyNTciLCJjb2RlIjoiQkkifSx7Im5hbWUiOiJDYW1ib2RpYSIsImRpYWxfY29kZSI6Iis4NTUiLCJjb2RlIjoiS0gifSx7Im5hbWUiOiJDYW1lcm9vbiIsImRpYWxfY29kZSI6IisyMzciLCJjb2RlIjoiQ00ifSx7Im5hbWUiOiJDYW5hZGEiLCJkaWFsX2NvZGUiOiIrMSIsImNvZGUiOiJDQSJ9LHsibmFtZSI6IkNhcGUgVmVyZGUiLCJkaWFsX2NvZGUiOiIrMjM4IiwiY29kZSI6IkNWIn0seyJuYW1lIjoiQ2F5bWFuIElzbGFuZHMiLCJkaWFsX2NvZGUiOiIrIDM0NSIsImNvZGUiOiJLWSJ9LHsibmFtZSI6IkNlbnRyYWwgQWZyaWNhbiBSZXB1YmxpYyIsImRpYWxfY29kZSI6IisyMzYiLCJjb2RlIjoiQ0YifSx7Im5hbWUiOiJDaGFkIiwiZGlhbF9jb2RlIjoiKzIzNSIsImNvZGUiOiJURCJ9LHsibmFtZSI6IkNoaWxlIiwiZGlhbF9jb2RlIjoiKzU2IiwiY29kZSI6IkNMIn0seyJuYW1lIjoiQ2hpbmEiLCJkaWFsX2NvZGUiOiIrODYiLCJjb2RlIjoiQ04ifSx7Im5hbWUiOiJDaHJpc3RtYXMgSXNsYW5kIiwiZGlhbF9jb2RlIjoiKzYxIiwiY29kZSI6IkNYIn0seyJuYW1lIjoiQ29jb3MgKEtlZWxpbmcpIElzbGFuZHMiLCJkaWFsX2NvZGUiOiIrNjEiLCJjb2RlIjoiQ0MifSx7Im5hbWUiOiJDb2xvbWJpYSIsImRpYWxfY29kZSI6Iis1NyIsImNvZGUiOiJDTyJ9LHsibmFtZSI6IkNvbW9yb3MiLCJkaWFsX2NvZGUiOiIrMjY5IiwiY29kZSI6IktNIn0seyJuYW1lIjoiQ29uZ28iLCJkaWFsX2NvZGUiOiIrMjQyIiwiY29kZSI6IkNHIn0seyJuYW1lIjoiQ29uZ28sIFRoZSBEZW1vY3JhdGljIFJlcHVibGljIG9mIHRoZSIsImRpYWxfY29kZSI6IisyNDMiLCJjb2RlIjoiQ0QifSx7Im5hbWUiOiJDb29rIElzbGFuZHMiLCJkaWFsX2NvZGUiOiIrNjgyIiwiY29kZSI6IkNLIn0seyJuYW1lIjoiQ29zdGEgUmljYSIsImRpYWxfY29kZSI6Iis1MDYiLCJjb2RlIjoiQ1IifSx7Im5hbWUiOiJDb3RlIGQnSXZvaXJlIiwiZGlhbF9jb2RlIjoiKzIyNSIsImNvZGUiOiJDSSJ9LHsibmFtZSI6IkNyb2F0aWEiLCJkaWFsX2NvZGUiOiIrMzg1IiwiY29kZSI6IkhSIn0seyJuYW1lIjoiQ3ViYSIsImRpYWxfY29kZSI6Iis1MyIsImNvZGUiOiJDVSJ9LHsibmFtZSI6IkN5cHJ1cyIsImRpYWxfY29kZSI6IiszNTciLCJjb2RlIjoiQ1kifSx7Im5hbWUiOiJDemVjaCBSZXB1YmxpYyIsImRpYWxfY29kZSI6Iis0MjAiLCJjb2RlIjoiQ1oifSx7Im5hbWUiOiJEZW5tYXJrIiwiZGlhbF9jb2RlIjoiKzQ1IiwiY29kZSI6IkRLIn0seyJuYW1lIjoiRGppYm91dGkiLCJkaWFsX2NvZGUiOiIrMjUzIiwiY29kZSI6IkRKIn0seyJuYW1lIjoiRG9taW5pY2EiLCJkaWFsX2NvZGUiOiIrMSA3NjciLCJjb2RlIjoiRE0ifSx7Im5hbWUiOiJEb21pbmljYW4gUmVwdWJsaWMiLCJkaWFsX2NvZGUiOiIrMSA4NDkiLCJjb2RlIjoiRE8ifSx7Im5hbWUiOiJFY3VhZG9yIiwiZGlhbF9jb2RlIjoiKzU5MyIsImNvZGUiOiJFQyJ9LHsibmFtZSI6IkVneXB0IiwiZGlhbF9jb2RlIjoiKzIwIiwiY29kZSI6IkVHIn0seyJuYW1lIjoiRWwgU2FsdmFkb3IiLCJkaWFsX2NvZGUiOiIrNTAzIiwiY29kZSI6IlNWIn0seyJuYW1lIjoiRXF1YXRvcmlhbCBHdWluZWEiLCJkaWFsX2NvZGUiOiIrMjQwIiwiY29kZSI6IkdRIn0seyJuYW1lIjoiRXJpdHJlYSIsImRpYWxfY29kZSI6IisyOTEiLCJjb2RlIjoiRVIifSx7Im5hbWUiOiJFc3RvbmlhIiwiZGlhbF9jb2RlIjoiKzM3MiIsImNvZGUiOiJFRSJ9LHsibmFtZSI6IkV0aGlvcGlhIiwiZGlhbF9jb2RlIjoiKzI1MSIsImNvZGUiOiJFVCJ9LHsibmFtZSI6IkZhbGtsYW5kIElzbGFuZHMgKE1hbHZpbmFzKSIsImRpYWxfY29kZSI6Iis1MDAiLCJjb2RlIjoiRksifSx7Im5hbWUiOiJGYXJvZSBJc2xhbmRzIiwiZGlhbF9jb2RlIjoiKzI5OCIsImNvZGUiOiJGTyJ9LHsibmFtZSI6IkZpamkiLCJkaWFsX2NvZGUiOiIrNjc5IiwiY29kZSI6IkZKIn0seyJuYW1lIjoiRmlubGFuZCIsImRpYWxfY29kZSI6IiszNTgiLCJjb2RlIjoiRkkifSx7Im5hbWUiOiJGcmFuY2UiLCJkaWFsX2NvZGUiOiIrMzMiLCJjb2RlIjoiRlIifSx7Im5hbWUiOiJGcmVuY2ggR3VpYW5hIiwiZGlhbF9jb2RlIjoiKzU5NCIsImNvZGUiOiJHRiJ9LHsibmFtZSI6IkZyZW5jaCBQb2x5bmVzaWEiLCJkaWFsX2NvZGUiOiIrNjg5IiwiY29kZSI6IlBGIn0seyJuYW1lIjoiR2Fib24iLCJkaWFsX2NvZGUiOiIrMjQxIiwiY29kZSI6IkdBIn0seyJuYW1lIjoiR2FtYmlhIiwiZGlhbF9jb2RlIjoiKzIyMCIsImNvZGUiOiJHTSJ9LHsibmFtZSI6Ikdlb3JnaWEiLCJkaWFsX2NvZGUiOiIrOTk1IiwiY29kZSI6IkdFIn0seyJuYW1lIjoiR2VybWFueSIsImRpYWxfY29kZSI6Iis0OSIsImNvZGUiOiJERSJ9LHsibmFtZSI6IkdoYW5hIiwiZGlhbF9jb2RlIjoiKzIzMyIsImNvZGUiOiJHSCJ9LHsibmFtZSI6IkdpYnJhbHRhciIsImRpYWxfY29kZSI6IiszNTAiLCJjb2RlIjoiR0kifSx7Im5hbWUiOiJHcmVlY2UiLCJkaWFsX2NvZGUiOiIrMzAiLCJjb2RlIjoiR1IifSx7Im5hbWUiOiJHcmVlbmxhbmQiLCJkaWFsX2NvZGUiOiIrMjk5IiwiY29kZSI6IkdMIn0seyJuYW1lIjoiR3JlbmFkYSIsImRpYWxfY29kZSI6IisxIDQ3MyIsImNvZGUiOiJHRCJ9LHsibmFtZSI6Ikd1YWRlbG91cGUiLCJkaWFsX2NvZGUiOiIrNTkwIiwiY29kZSI6IkdQIn0seyJuYW1lIjoiR3VhbSIsImRpYWxfY29kZSI6IisxIDY3MSIsImNvZGUiOiJHVSJ9LHsibmFtZSI6Ikd1YXRlbWFsYSIsImRpYWxfY29kZSI6Iis1MDIiLCJjb2RlIjoiR1QifSx7Im5hbWUiOiJHdWVybnNleSIsImRpYWxfY29kZSI6Iis0NCIsImNvZGUiOiJHRyJ9LHsibmFtZSI6Ikd1aW5lYSIsImRpYWxfY29kZSI6IisyMjQiLCJjb2RlIjoiR04ifSx7Im5hbWUiOiJHdWluZWEtQmlzc2F1IiwiZGlhbF9jb2RlIjoiKzI0NSIsImNvZGUiOiJHVyJ9LHsibmFtZSI6Ikd1eWFuYSIsImRpYWxfY29kZSI6Iis1OTUiLCJjb2RlIjoiR1kifSx7Im5hbWUiOiJIYWl0aSIsImRpYWxfY29kZSI6Iis1MDkiLCJjb2RlIjoiSFQifSx7Im5hbWUiOiJIb2x5IFNlZSAoVmF0aWNhbiBDaXR5IFN0YXRlKSIsImRpYWxfY29kZSI6IiszNzkiLCJjb2RlIjoiVkEifSx7Im5hbWUiOiJIb25kdXJhcyIsImRpYWxfY29kZSI6Iis1MDQiLCJjb2RlIjoiSE4ifSx7Im5hbWUiOiJIb25nIEtvbmciLCJkaWFsX2NvZGUiOiIrODUyIiwiY29kZSI6IkhLIn0seyJuYW1lIjoiSHVuZ2FyeSIsImRpYWxfY29kZSI6IiszNiIsImNvZGUiOiJIVSJ9LHsibmFtZSI6IkljZWxhbmQiLCJkaWFsX2NvZGUiOiIrMzU0IiwiY29kZSI6IklTIn0seyJuYW1lIjoiSW5kaWEiLCJkaWFsX2NvZGUiOiIrOTEiLCJjb2RlIjoiSU4ifSx7Im5hbWUiOiJJbmRvbmVzaWEiLCJkaWFsX2NvZGUiOiIrNjIiLCJjb2RlIjoiSUQifSx7Im5hbWUiOiJJcmFuLCBJc2xhbWljIFJlcHVibGljIG9mIiwiZGlhbF9jb2RlIjoiKzk4IiwiY29kZSI6IklSIn0seyJuYW1lIjoiSXJhcSIsImRpYWxfY29kZSI6Iis5NjQiLCJjb2RlIjoiSVEifSx7Im5hbWUiOiJJcmVsYW5kIiwiZGlhbF9jb2RlIjoiKzM1MyIsImNvZGUiOiJJRSJ9LHsibmFtZSI6IklzbGUgb2YgTWFuIiwiZGlhbF9jb2RlIjoiKzQ0IiwiY29kZSI6IklNIn0seyJuYW1lIjoiSXNyYWVsIiwiZGlhbF9jb2RlIjoiKzk3MiIsImNvZGUiOiJJTCJ9LHsibmFtZSI6Ikl0YWx5IiwiZGlhbF9jb2RlIjoiKzM5IiwiY29kZSI6IklUIn0seyJuYW1lIjoiSmFtYWljYSIsImRpYWxfY29kZSI6IisxIDg3NiIsImNvZGUiOiJKTSJ9LHsibmFtZSI6IkphcGFuIiwiZGlhbF9jb2RlIjoiKzgxIiwiY29kZSI6IkpQIn0seyJuYW1lIjoiSmVyc2V5IiwiZGlhbF9jb2RlIjoiKzQ0IiwiY29kZSI6IkpFIn0seyJuYW1lIjoiSm9yZGFuIiwiZGlhbF9jb2RlIjoiKzk2MiIsImNvZGUiOiJKTyJ9LHsibmFtZSI6IkthemFraHN0YW4iLCJkaWFsX2NvZGUiOiIrNyA3IiwiY29kZSI6IktaIn0seyJuYW1lIjoiS2VueWEiLCJkaWFsX2NvZGUiOiIrMjU0IiwiY29kZSI6IktFIn0seyJuYW1lIjoiS2lyaWJhdGkiLCJkaWFsX2NvZGUiOiIrNjg2IiwiY29kZSI6IktJIn0seyJuYW1lIjoiS29yZWEsIERlbW9jcmF0aWMgUGVvcGxlJ3MgUmVwdWJsaWMgb2YiLCJkaWFsX2NvZGUiOiIrODUwIiwiY29kZSI6IktQIn0seyJuYW1lIjoiS29yZWEsIFJlcHVibGljIG9mIiwiZGlhbF9jb2RlIjoiKzgyIiwiY29kZSI6IktSIn0seyJuYW1lIjoiS3V3YWl0IiwiZGlhbF9jb2RlIjoiKzk2NSIsImNvZGUiOiJLVyJ9LHsibmFtZSI6Ikt5cmd5enN0YW4iLCJkaWFsX2NvZGUiOiIrOTk2IiwiY29kZSI6IktHIn0seyJuYW1lIjoiTGFvIFBlb3BsZSdzIERlbW9jcmF0aWMgUmVwdWJsaWMiLCJkaWFsX2NvZGUiOiIrODU2IiwiY29kZSI6IkxBIn0seyJuYW1lIjoiTGF0dmlhIiwiZGlhbF9jb2RlIjoiKzM3MSIsImNvZGUiOiJMViJ9LHsibmFtZSI6IkxlYmFub24iLCJkaWFsX2NvZGUiOiIrOTYxIiwiY29kZSI6IkxCIn0seyJuYW1lIjoiTGVzb3RobyIsImRpYWxfY29kZSI6IisyNjYiLCJjb2RlIjoiTFMifSx7Im5hbWUiOiJMaWJlcmlhIiwiZGlhbF9jb2RlIjoiKzIzMSIsImNvZGUiOiJMUiJ9LHsibmFtZSI6IkxpYnlhbiBBcmFiIEphbWFoaXJpeWEiLCJkaWFsX2NvZGUiOiIrMjE4IiwiY29kZSI6IkxZIn0seyJuYW1lIjoiTGllY2h0ZW5zdGVpbiIsImRpYWxfY29kZSI6Iis0MjMiLCJjb2RlIjoiTEkifSx7Im5hbWUiOiJMaXRodWFuaWEiLCJkaWFsX2NvZGUiOiIrMzcwIiwiY29kZSI6IkxUIn0seyJuYW1lIjoiTHV4ZW1ib3VyZyIsImRpYWxfY29kZSI6IiszNTIiLCJjb2RlIjoiTFUifSx7Im5hbWUiOiJNYWNhbyIsImRpYWxfY29kZSI6Iis4NTMiLCJjb2RlIjoiTU8ifSx7Im5hbWUiOiJNYWNlZG9uaWEsIFRoZSBGb3JtZXIgWXVnb3NsYXYgUmVwdWJsaWMgb2YiLCJkaWFsX2NvZGUiOiIrMzg5IiwiY29kZSI6Ik1LIn0seyJuYW1lIjoiTWFkYWdhc2NhciIsImRpYWxfY29kZSI6IisyNjEiLCJjb2RlIjoiTUcifSx7Im5hbWUiOiJNYWxhd2kiLCJkaWFsX2NvZGUiOiIrMjY1IiwiY29kZSI6Ik1XIn0seyJuYW1lIjoiTWFsYXlzaWEiLCJkaWFsX2NvZGUiOiIrNjAiLCJjb2RlIjoiTVkifSx7Im5hbWUiOiJNYWxkaXZlcyIsImRpYWxfY29kZSI6Iis5NjAiLCJjb2RlIjoiTVYifSx7Im5hbWUiOiJNYWxpIiwiZGlhbF9jb2RlIjoiKzIyMyIsImNvZGUiOiJNTCJ9LHsibmFtZSI6Ik1hbHRhIiwiZGlhbF9jb2RlIjoiKzM1NiIsImNvZGUiOiJNVCJ9LHsibmFtZSI6Ik1hcnNoYWxsIElzbGFuZHMiLCJkaWFsX2NvZGUiOiIrNjkyIiwiY29kZSI6Ik1IIn0seyJuYW1lIjoiTWFydGluaXF1ZSIsImRpYWxfY29kZSI6Iis1OTYiLCJjb2RlIjoiTVEifSx7Im5hbWUiOiJNYXVyaXRhbmlhIiwiZGlhbF9jb2RlIjoiKzIyMiIsImNvZGUiOiJNUiJ9LHsibmFtZSI6Ik1hdXJpdGl1cyIsImRpYWxfY29kZSI6IisyMzAiLCJjb2RlIjoiTVUifSx7Im5hbWUiOiJNYXlvdHRlIiwiZGlhbF9jb2RlIjoiKzI2MiIsImNvZGUiOiJZVCJ9LHsibmFtZSI6Ik1leGljbyIsImRpYWxfY29kZSI6Iis1MiIsImNvZGUiOiJNWCJ9LHsibmFtZSI6Ik1pY3JvbmVzaWEsIEZlZGVyYXRlZCBTdGF0ZXMgb2YiLCJkaWFsX2NvZGUiOiIrNjkxIiwiY29kZSI6IkZNIn0seyJuYW1lIjoiTW9sZG92YSwgUmVwdWJsaWMgb2YiLCJkaWFsX2NvZGUiOiIrMzczIiwiY29kZSI6Ik1EIn0seyJuYW1lIjoiTW9uYWNvIiwiZGlhbF9jb2RlIjoiKzM3NyIsImNvZGUiOiJNQyJ9LHsibmFtZSI6Ik1vbmdvbGlhIiwiZGlhbF9jb2RlIjoiKzk3NiIsImNvZGUiOiJNTiJ9LHsibmFtZSI6Ik1vbnRlbmVncm8iLCJkaWFsX2NvZGUiOiIrMzgyIiwiY29kZSI6Ik1FIn0seyJuYW1lIjoiTW9udHNlcnJhdCIsImRpYWxfY29kZSI6IisxNjY0IiwiY29kZSI6Ik1TIn0seyJuYW1lIjoiTW9yb2NjbyIsImRpYWxfY29kZSI6IisyMTIiLCJjb2RlIjoiTUEifSx7Im5hbWUiOiJNb3phbWJpcXVlIiwiZGlhbF9jb2RlIjoiKzI1OCIsImNvZGUiOiJNWiJ9LHsibmFtZSI6Ik15YW5tYXIiLCJkaWFsX2NvZGUiOiIrOTUiLCJjb2RlIjoiTU0ifSx7Im5hbWUiOiJOYW1pYmlhIiwiZGlhbF9jb2RlIjoiKzI2NCIsImNvZGUiOiJOQSJ9LHsibmFtZSI6Ik5hdXJ1IiwiZGlhbF9jb2RlIjoiKzY3NCIsImNvZGUiOiJOUiJ9LHsibmFtZSI6Ik5lcGFsIiwiZGlhbF9jb2RlIjoiKzk3NyIsImNvZGUiOiJOUCJ9LHsibmFtZSI6Ik5ldGhlcmxhbmRzIiwiZGlhbF9jb2RlIjoiKzMxIiwiY29kZSI6Ik5MIn0seyJuYW1lIjoiTmV0aGVybGFuZHMgQW50aWxsZXMiLCJkaWFsX2NvZGUiOiIrNTk5IiwiY29kZSI6IkFOIn0seyJuYW1lIjoiTmV3IENhbGVkb25pYSIsImRpYWxfY29kZSI6Iis2ODciLCJjb2RlIjoiTkMifSx7Im5hbWUiOiJOZXcgWmVhbGFuZCIsImRpYWxfY29kZSI6Iis2NCIsImNvZGUiOiJOWiJ9LHsibmFtZSI6Ik5pY2FyYWd1YSIsImRpYWxfY29kZSI6Iis1MDUiLCJjb2RlIjoiTkkifSx7Im5hbWUiOiJOaWdlciIsImRpYWxfY29kZSI6IisyMjciLCJjb2RlIjoiTkUifSx7Im5hbWUiOiJOaWdlcmlhIiwiZGlhbF9jb2RlIjoiKzIzNCIsImNvZGUiOiJORyJ9LHsibmFtZSI6Ik5pdWUiLCJkaWFsX2NvZGUiOiIrNjgzIiwiY29kZSI6Ik5VIn0seyJuYW1lIjoiTm9yZm9sayBJc2xhbmQiLCJkaWFsX2NvZGUiOiIrNjcyIiwiY29kZSI6Ik5GIn0seyJuYW1lIjoiTm9ydGhlcm4gTWFyaWFuYSBJc2xhbmRzIiwiZGlhbF9jb2RlIjoiKzEgNjcwIiwiY29kZSI6Ik1QIn0seyJuYW1lIjoiTm9yd2F5IiwiZGlhbF9jb2RlIjoiKzQ3IiwiY29kZSI6Ik5PIn0seyJuYW1lIjoiT21hbiIsImRpYWxfY29kZSI6Iis5NjgiLCJjb2RlIjoiT00ifSx7Im5hbWUiOiJQYWtpc3RhbiIsImRpYWxfY29kZSI6Iis5MiIsImNvZGUiOiJQSyJ9LHsibmFtZSI6IlBhbGF1IiwiZGlhbF9jb2RlIjoiKzY4MCIsImNvZGUiOiJQVyJ9LHsibmFtZSI6IlBhbGVzdGluaWFuIFRlcnJpdG9yeSwgT2NjdXBpZWQiLCJkaWFsX2NvZGUiOiIrOTcwIiwiY29kZSI6IlBTIn0seyJuYW1lIjoiUGFuYW1hIiwiZGlhbF9jb2RlIjoiKzUwNyIsImNvZGUiOiJQQSJ9LHsibmFtZSI6IlBhcHVhIE5ldyBHdWluZWEiLCJkaWFsX2NvZGUiOiIrNjc1IiwiY29kZSI6IlBHIn0seyJuYW1lIjoiUGFyYWd1YXkiLCJkaWFsX2NvZGUiOiIrNTk1IiwiY29kZSI6IlBZIn0seyJuYW1lIjoiUGVydSIsImRpYWxfY29kZSI6Iis1MSIsImNvZGUiOiJQRSJ9LHsibmFtZSI6IlBoaWxpcHBpbmVzIiwiZGlhbF9jb2RlIjoiKzYzIiwiY29kZSI6IlBIIn0seyJuYW1lIjoiUGl0Y2Fpcm4iLCJkaWFsX2NvZGUiOiIrODcyIiwiY29kZSI6IlBOIn0seyJuYW1lIjoiUG9sYW5kIiwiZGlhbF9jb2RlIjoiKzQ4IiwiY29kZSI6IlBMIn0seyJuYW1lIjoiUG9ydHVnYWwiLCJkaWFsX2NvZGUiOiIrMzUxIiwiY29kZSI6IlBUIn0seyJuYW1lIjoiUHVlcnRvIFJpY28iLCJkaWFsX2NvZGUiOiIrMSA5MzkiLCJjb2RlIjoiUFIifSx7Im5hbWUiOiJRYXRhciIsImRpYWxfY29kZSI6Iis5NzQiLCJjb2RlIjoiUUEifSx7Im5hbWUiOiJSb21hbmlhIiwiZGlhbF9jb2RlIjoiKzQwIiwiY29kZSI6IlJPIn0seyJuYW1lIjoiUnVzc2lhIiwiZGlhbF9jb2RlIjoiKzciLCJjb2RlIjoiUlUifSx7Im5hbWUiOiJSd2FuZGEiLCJkaWFsX2NvZGUiOiIrMjUwIiwiY29kZSI6IlJXIn0seyJuYW1lIjoiUsOpdW5pb24iLCJkaWFsX2NvZGUiOiIrMjYyIiwiY29kZSI6IlJFIn0seyJuYW1lIjoiU2FpbnQgQmFydGjDqWxlbXkiLCJkaWFsX2NvZGUiOiIrNTkwIiwiY29kZSI6IkJMIn0seyJuYW1lIjoiU2FpbnQgSGVsZW5hLCBBc2NlbnNpb24gYW5kIFRyaXN0YW4gRGEgQ3VuaGEiLCJkaWFsX2NvZGUiOiIrMjkwIiwiY29kZSI6IlNIIn0seyJuYW1lIjoiU2FpbnQgS2l0dHMgYW5kIE5ldmlzIiwiZGlhbF9jb2RlIjoiKzEgODY5IiwiY29kZSI6IktOIn0seyJuYW1lIjoiU2FpbnQgTHVjaWEiLCJkaWFsX2NvZGUiOiIrMSA3NTgiLCJjb2RlIjoiTEMifSx7Im5hbWUiOiJTYWludCBNYXJ0aW4iLCJkaWFsX2NvZGUiOiIrNTkwIiwiY29kZSI6Ik1GIn0seyJuYW1lIjoiU2FpbnQgUGllcnJlIGFuZCBNaXF1ZWxvbiIsImRpYWxfY29kZSI6Iis1MDgiLCJjb2RlIjoiUE0ifSx7Im5hbWUiOiJTYWludCBWaW5jZW50IGFuZCB0aGUgR3JlbmFkaW5lcyIsImRpYWxfY29kZSI6IisxIDc4NCIsImNvZGUiOiJWQyJ9LHsibmFtZSI6IlNhbW9hIiwiZGlhbF9jb2RlIjoiKzY4NSIsImNvZGUiOiJXUyJ9LHsibmFtZSI6IlNhbiBNYXJpbm8iLCJkaWFsX2NvZGUiOiIrMzc4IiwiY29kZSI6IlNNIn0seyJuYW1lIjoiU2FvIFRvbWUgYW5kIFByaW5jaXBlIiwiZGlhbF9jb2RlIjoiKzIzOSIsImNvZGUiOiJTVCJ9LHsibmFtZSI6IlNhdWRpIEFyYWJpYSIsImRpYWxfY29kZSI6Iis5NjYiLCJjb2RlIjoiU0EifSx7Im5hbWUiOiJTZW5lZ2FsIiwiZGlhbF9jb2RlIjoiKzIyMSIsImNvZGUiOiJTTiJ9LHsibmFtZSI6IlNlcmJpYSIsImRpYWxfY29kZSI6IiszODEiLCJjb2RlIjoiUlMifSx7Im5hbWUiOiJTZXljaGVsbGVzIiwiZGlhbF9jb2RlIjoiKzI0OCIsImNvZGUiOiJTQyJ9LHsibmFtZSI6IlNpZXJyYSBMZW9uZSIsImRpYWxfY29kZSI6IisyMzIiLCJjb2RlIjoiU0wifSx7Im5hbWUiOiJTaW5nYXBvcmUiLCJkaWFsX2NvZGUiOiIrNjUiLCJjb2RlIjoiU0cifSx7Im5hbWUiOiJTbG92YWtpYSIsImRpYWxfY29kZSI6Iis0MjEiLCJjb2RlIjoiU0sifSx7Im5hbWUiOiJTbG92ZW5pYSIsImRpYWxfY29kZSI6IiszODYiLCJjb2RlIjoiU0kifSx7Im5hbWUiOiJTb2xvbW9uIElzbGFuZHMiLCJkaWFsX2NvZGUiOiIrNjc3IiwiY29kZSI6IlNCIn0seyJuYW1lIjoiU29tYWxpYSIsImRpYWxfY29kZSI6IisyNTIiLCJjb2RlIjoiU08ifSx7Im5hbWUiOiJTb3V0aCBBZnJpY2EiLCJkaWFsX2NvZGUiOiIrMjciLCJjb2RlIjoiWkEifSx7Im5hbWUiOiJTb3V0aCBHZW9yZ2lhIGFuZCB0aGUgU291dGggU2FuZHdpY2ggSXNsYW5kcyIsImRpYWxfY29kZSI6Iis1MDAiLCJjb2RlIjoiR1MifSx7Im5hbWUiOiJTcGFpbiIsImRpYWxfY29kZSI6IiszNCIsImNvZGUiOiJFUyJ9LHsibmFtZSI6IlNyaSBMYW5rYSIsImRpYWxfY29kZSI6Iis5NCIsImNvZGUiOiJMSyJ9LHsibmFtZSI6IlN1ZGFuIiwiZGlhbF9jb2RlIjoiKzI0OSIsImNvZGUiOiJTRCJ9LHsibmFtZSI6IlN1cmluYW1lIiwiZGlhbF9jb2RlIjoiKzU5NyIsImNvZGUiOiJTUiJ9LHsibmFtZSI6IlN2YWxiYXJkIGFuZCBKYW4gTWF5ZW4iLCJkaWFsX2NvZGUiOiIrNDciLCJjb2RlIjoiU0oifSx7Im5hbWUiOiJTd2F6aWxhbmQiLCJkaWFsX2NvZGUiOiIrMjY4IiwiY29kZSI6IlNaIn0seyJuYW1lIjoiU3dlZGVuIiwiZGlhbF9jb2RlIjoiKzQ2IiwiY29kZSI6IlNFIn0seyJuYW1lIjoiU3dpdHplcmxhbmQiLCJkaWFsX2NvZGUiOiIrNDEiLCJjb2RlIjoiQ0gifSx7Im5hbWUiOiJTeXJpYW4gQXJhYiBSZXB1YmxpYyIsImRpYWxfY29kZSI6Iis5NjMiLCJjb2RlIjoiU1kifSx7Im5hbWUiOiJUYWl3YW4iLCJkaWFsX2NvZGUiOiIrODg2IiwiY29kZSI6IlRXIn0seyJuYW1lIjoiVGFqaWtpc3RhbiIsImRpYWxfY29kZSI6Iis5OTIiLCJjb2RlIjoiVEoifSx7Im5hbWUiOiJUYW56YW5pYSwgVW5pdGVkIFJlcHVibGljIG9mIiwiZGlhbF9jb2RlIjoiKzI1NSIsImNvZGUiOiJUWiJ9LHsibmFtZSI6IlRoYWlsYW5kIiwiZGlhbF9jb2RlIjoiKzY2IiwiY29kZSI6IlRIIn0seyJuYW1lIjoiVGltb3ItTGVzdGUiLCJkaWFsX2NvZGUiOiIrNjcwIiwiY29kZSI6IlRMIn0seyJuYW1lIjoiVG9nbyIsImRpYWxfY29kZSI6IisyMjgiLCJjb2RlIjoiVEcifSx7Im5hbWUiOiJUb2tlbGF1IiwiZGlhbF9jb2RlIjoiKzY5MCIsImNvZGUiOiJUSyJ9LHsibmFtZSI6IlRvbmdhIiwiZGlhbF9jb2RlIjoiKzY3NiIsImNvZGUiOiJUTyJ9LHsibmFtZSI6IlRyaW5pZGFkIGFuZCBUb2JhZ28iLCJkaWFsX2NvZGUiOiIrMSA4NjgiLCJjb2RlIjoiVFQifSx7Im5hbWUiOiJUdW5pc2lhIiwiZGlhbF9jb2RlIjoiKzIxNiIsImNvZGUiOiJUTiJ9LHsibmFtZSI6IlR1cmtleSIsImRpYWxfY29kZSI6Iis5MCIsImNvZGUiOiJUUiJ9LHsibmFtZSI6IlR1cmttZW5pc3RhbiIsImRpYWxfY29kZSI6Iis5OTMiLCJjb2RlIjoiVE0ifSx7Im5hbWUiOiJUdXJrcyBhbmQgQ2FpY29zIElzbGFuZHMiLCJkaWFsX2NvZGUiOiIrMSA2NDkiLCJjb2RlIjoiVEMifSx7Im5hbWUiOiJUdXZhbHUiLCJkaWFsX2NvZGUiOiIrNjg4IiwiY29kZSI6IlRWIn0seyJuYW1lIjoiVWdhbmRhIiwiZGlhbF9jb2RlIjoiKzI1NiIsImNvZGUiOiJVRyJ9LHsibmFtZSI6IlVrcmFpbmUiLCJkaWFsX2NvZGUiOiIrMzgwIiwiY29kZSI6IlVBIn0seyJuYW1lIjoiVW5pdGVkIEFyYWIgRW1pcmF0ZXMiLCJkaWFsX2NvZGUiOiIrOTcxIiwiY29kZSI6IkFFIn0seyJuYW1lIjoiVW5pdGVkIEtpbmdkb20iLCJkaWFsX2NvZGUiOiIrNDQiLCJjb2RlIjoiR0IifSx7Im5hbWUiOiJVbml0ZWQgU3RhdGVzIiwiZGlhbF9jb2RlIjoiKzEiLCJjb2RlIjoiVVMifSx7Im5hbWUiOiJVcnVndWF5IiwiZGlhbF9jb2RlIjoiKzU5OCIsImNvZGUiOiJVWSJ9LHsibmFtZSI6IlV6YmVraXN0YW4iLCJkaWFsX2NvZGUiOiIrOTk4IiwiY29kZSI6IlVaIn0seyJuYW1lIjoiVmFudWF0dSIsImRpYWxfY29kZSI6Iis2NzgiLCJjb2RlIjoiVlUifSx7Im5hbWUiOiJWZW5lenVlbGEsIEJvbGl2YXJpYW4gUmVwdWJsaWMgb2YiLCJkaWFsX2NvZGUiOiIrNTgiLCJjb2RlIjoiVkUifSx7Im5hbWUiOiJWaWV0IE5hbSIsImRpYWxfY29kZSI6Iis4NCIsImNvZGUiOiJWTiJ9LHsibmFtZSI6IlZpcmdpbiBJc2xhbmRzLCBCcml0aXNoIiwiZGlhbF9jb2RlIjoiKzEgMjg0IiwiY29kZSI6IlZHIn0seyJuYW1lIjoiVmlyZ2luIElzbGFuZHMsIFUuUy4iLCJkaWFsX2NvZGUiOiIrMSAzNDAiLCJjb2RlIjoiVkkifSx7Im5hbWUiOiJXYWxsaXMgYW5kIEZ1dHVuYSIsImRpYWxfY29kZSI6Iis2ODEiLCJjb2RlIjoiV0YifSx7Im5hbWUiOiJZZW1lbiIsImRpYWxfY29kZSI6Iis5NjciLCJjb2RlIjoiWUUifSx7Im5hbWUiOiJaYW1iaWEiLCJkaWFsX2NvZGUiOiIrMjYwIiwiY29kZSI6IlpNIn0seyJuYW1lIjoiWmltYmFid2UiLCJkaWFsX2NvZGUiOiIrMjYzIiwiY29kZSI6IlpXIn0seyJuYW1lIjoiw4VsYW5kIElzbGFuZHMiLCJkaWFsX2NvZGUiOiIrMzU4IiwiY29kZSI6IkFYIn1d";
        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        return new String(data, "UTF-8");
    }

    private void doneCousellor() {
        startActivity(new Intent(this, MainActivityCounsellor.class));
        finish();
    }

    public void submit() {
        //Validate and confirm number country codes selected
        if (spinnerCountryCodes.getSelectedItem() == null) {
            Toast.makeText(this, R.string.validation_req_country, Toast.LENGTH_LONG).show();
            return;
        }
//        if (TextUtils.isEmpty(edname.getText().toString())) {
//            Toast.makeText(this, R.string.validation_req_name, Toast.LENGTH_LONG).show();
//            return;
//        }
        if (TextUtils.isEmpty(etPhone.getText().toString())) {
            Toast.makeText(this, R.string.validation_req_phone, Toast.LENGTH_LONG).show();
            return;
        }
        final String phoneNumber = ((Country) spinnerCountryCodes.getSelectedItem()).getDialCode() + etPhone.getText().toString().replaceAll("\\s+", "");
        if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
            Toast.makeText(this, R.string.validation_req_phone_valid, Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(phoneNumber);
        builder.setMessage(R.string.confirm_phone);
        builder.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            Intent intent = new Intent(ConsultantSignInActivity.this, VerificationActivityCounsellor.class);
            intent.putExtra("name", "");
            intent.putExtra("phone", phoneNumber);
            intent.putExtra("email", "");
            intent.putExtra("type", "");
            startActivity(intent);
        });
        builder.setNegativeButton(R.string.edit, (dialogInterface, i) -> {
            etPhone.requestFocus();
            keyboardUtil.openKeyboard();
            dialogInterface.dismiss();
        });
        builder.create().show();
    }

    private void LoginCounsellor(String counsid) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.COUNSELLORAUTOLOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response=k========>>>>>", response);
//              {"result":true,"message":"data found","data":[{"id":"21","name":"Sambhu dayal","contact":"+919079149908","email":"shiv@gmail.com"},{"id":"22","name":"Rk Jangid","contact":"+919079149908","email":"cybercureofficial@gmail.com"}]}
                try {
                    JSONObject json = new JSONObject(response);

                    String str_result = json.getString("result");

                    if (str_result.equalsIgnoreCase("true")) {
                        JSONArray data = json.getJSONArray("data");
                        JSONObject data_obj = data.getJSONObject(0);
                        PreferenceConnector.writeString(context, PreferenceConnector.LOGINEDUSERID, data_obj.getString("id"));
                        PreferenceConnector.writeString(context, PreferenceConnector.LOGINEDNAME, data_obj.getString("name"));
                        PreferenceConnector.writeString(context, PreferenceConnector.LOGINEDPHONE, data_obj.getString("contact"));
                        PreferenceConnector.writeString(context, PreferenceConnector.LOGINEDEMAIL, data_obj.getString("email"));

                        doneCousellor();
                    } else {
                        helper.logout(context);
                        Toast.makeText(context, json.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("counsellor_id", counsid);
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


}