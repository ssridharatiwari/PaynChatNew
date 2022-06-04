package com.startup.paynchat.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.startup.paynchat.GlobalVariables;
import com.startup.paynchat.R;
import com.startup.paynchat.models.User;
import com.startup.paynchat.utils.Helper;
import com.startup.paynchat.utils.KeyboardUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VerificationActivityCounsellor extends AppCompatActivity {
    private DatabaseReference userRef;
    private Helper helper;
    private AppCompatEditText otpCode;
    private KeyboardUtil keyboardUtil;
    private String phoneNumberInPrefs = null, nameInPrefs = null, emailInPrefs = null, typeInPrefs = null;
    private ProgressDialog progressDialog;
    private TextView verificationMessage, retryTimer;
    private CountDownTimer countDownTimer;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private boolean authInProgress;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init vars
        mContext = this;
        phoneNumberInPrefs = getIntent().getStringExtra("phone");
        nameInPrefs = getIntent().getStringExtra("name");
        emailInPrefs = getIntent().getStringExtra("email");
        typeInPrefs = getIntent().getStringExtra("type");
        helper = new Helper(this);
        keyboardUtil = KeyboardUtil.getInstance(this);
        progressDialog = new ProgressDialog(this);
        setContentView(R.layout.activity_sign_in_2);
        //initiate authentication
        mAuth = FirebaseAuth.getInstance();
        retryTimer = findViewById(R.id.resend);
        verificationMessage = findViewById(R.id.verificationMessage);
        otpCode = findViewById(R.id.otp);
        findViewById(R.id.back).setOnClickListener(view -> back());
        initiateAuth(phoneNumberInPrefs);

        findViewById(R.id.submit).setOnClickListener(view -> {
            //force authenticate
            String otp = otpCode.getText().toString();
            if (!TextUtils.isEmpty(otp) && !TextUtils.isEmpty(mVerificationId))
                signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(mVerificationId, otp));
            //verifyOtp(otpCode[0].getText().toString() + otpCode[1].getText().toString() + otpCode[2].getText().toString() + otpCode[3].getText().toString());
        });
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        mContext = null;
        super.onDestroy();
    }

    private void showProgress(int i) {
        String title = (i == 1) ? getString(R.string.otp_sending) : getString(R.string.otp_verifying);
        String message = (i == 1) ? (getString(R.string.otp_sending_msg) + " " + phoneNumberInPrefs) : getString(R.string.otp_verifying_msg);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void initiateAuth(String phone) {
        if (mContext != null) {
            showProgress(1);
            PhoneAuthProvider.getInstance().verifyPhoneNumber(phone, 60, TimeUnit.SECONDS, this,
                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onCodeAutoRetrievalTimeOut(String s) {
                            super.onCodeAutoRetrievalTimeOut(s);
                        }

                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                            progressDialog.dismiss();
                            signInWithPhoneAuthCredential(phoneAuthCredential);
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            authInProgress = false;
                            progressDialog.dismiss();
                            countDownTimer.cancel();
                            verificationMessage.setText(getString(R.string.error_detail) + ((e.getMessage() != null) ? ("\n" + e.getMessage()) : ""));
                            retryTimer.setVisibility(View.VISIBLE);
                            retryTimer.setText(getString(R.string.resend));
                            retryTimer.setOnClickListener(view -> initiateAuth(phoneNumberInPrefs));
                            Toast.makeText(VerificationActivityCounsellor.this, getString(R.string.error_detail) + (e.getMessage() != null ? "\n" + e.getMessage() : ""), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onCodeSent(@NonNull String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            super.onCodeSent(verificationId, forceResendingToken);
                            authInProgress = true;
                            progressDialog.dismiss();
                            mVerificationId = verificationId;
                            mResendToken = forceResendingToken;

                            verificationMessage.setText(getString(R.string.otp_sent) + " " + phoneNumberInPrefs);
                            retryTimer.setVisibility(View.GONE);
                        }
                    });
            startCountdown();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        showProgress(2);
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(task -> {
            progressDialog.setMessage(getString(R.string.logging_in));
            login();
        }).addOnFailureListener(e -> {
            Toast.makeText(VerificationActivityCounsellor.this, getString(R.string.error_detail) + (e.getMessage() != null ? "\n" + e.getMessage() : ""), Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            authInProgress = false;
        });
    }

    private void login() {
        authInProgress = true;
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
                            done(false);
                            helper.setLoggedInUser(user);
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

            }
        });

    }

    private void createUser(final User newUser) {
        userRef.setValue(newUser).addOnSuccessListener(aVoid -> {
            helper.setLoggedInUser(newUser);
            done(true);
//            SignUpCounsellor(nameInPrefs, phoneNumberInPrefs, emailInPrefs, typeInPrefs);
        }).addOnFailureListener(e -> Toast.makeText(VerificationActivityCounsellor.this, R.string.error_create_user, Toast.LENGTH_LONG).show());
    }

    private void SignUpCounsellor(String name, String contact, String email, String cno) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.COUNSELLERSIGNUP, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response=====s=====>>>>", response);
                try {
                    JSONObject json = new JSONObject(response);
                    done(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("contact", contact);
                params.put("email", email);
                params.put("role", cno);
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
    }

    //Go to main activity
    private void done(boolean newUser) {
        Intent intent = new Intent(this, MainActivityCounsellor.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("newUser", newUser);
        startActivity(intent);
        VerificationActivityCounsellor.this.finish();
    }

    private void back() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.verification_cancel_title);
        builder.setMessage(R.string.verification_cancel_message);
        builder.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            onBackPressed();
        });
        builder.setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.dismiss());

        if (progressDialog.isShowing() || authInProgress) {
            builder.create().show();
        } else {
            onBackPressed();
        }
    }

    private void startCountdown() {
        retryTimer.setOnClickListener(null);
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {
                if (retryTimer != null) {
                    retryTimer.setText(String.valueOf(l / 1000));
                }
            }

            @Override
            public void onFinish() {
                if (retryTimer != null) {
                    retryTimer.setText(getText(R.string.resend));
                    retryTimer.setOnClickListener(view -> initiateAuth(phoneNumberInPrefs));
                }
            }
        }.start();
    }

}
