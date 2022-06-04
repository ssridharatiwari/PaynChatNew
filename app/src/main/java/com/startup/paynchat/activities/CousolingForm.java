package com.startup.paynchat.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.startup.paynchat.GlobalVariables;
import com.startup.paynchat.R;
import com.startup.paynchat.models.SpinnerModel;
import com.startup.paynchat.models.User;
import com.startup.paynchat.utils.Helper;
import com.startup.paynchat.utils.KeyboardUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CousolingForm extends AppCompatActivity {
    private EditText edName, edEmail, edAge, edQuery;
    private KeyboardUtil keyboardUtil;
    private ProgressDialog progressDialog;
    private final int MINAGE = 18, MAXAGE = 60;
    private User user;
    private String subCatId;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Helper helper = new Helper(this);
        context = this;
        user = helper.getLoggedInUser();
//        if (user != null) {    //Check if user if logged in
//            done();
//        } else {
        //init vars
        keyboardUtil = KeyboardUtil.getInstance(this);
        progressDialog = new ProgressDialog(this);

        //if there is number to authenticate in preferences then initiate
        setContentView(R.layout.activity_counsellingform);
        //setup number selection
        edName = findViewById(R.id.name);
        edAge = findViewById(R.id.age);
        edEmail = findViewById(R.id.email);
        edQuery = findViewById(R.id.query);

        findViewById(R.id.submit).setOnClickListener(view -> {
            submit();
        });

        findViewById(R.id.back_button).setOnClickListener(view -> {
            onBackPressed();
        });

        subCatId = getIntent().getStringExtra("subcat_id");

//            GetRoles();
//            permissionHandler.postDelayed(permissionRunnable, 200);
//        }

    }

    public void submit() {
        //Validate and confirm number country codes selected
        if (TextUtils.isEmpty(edAge.getText().toString())) {
            Toast.makeText(this, "Enter age", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(edName.getText().toString())) {
            Toast.makeText(this, R.string.validation_req_name, Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(edQuery.getText().toString())) {
            Toast.makeText(this, "Write your query/problem here", Toast.LENGTH_LONG).show();
            return;
        }

        int userAge = Integer.parseInt(edAge.getText().toString().trim());
        if (userAge < MINAGE) {
            Toast.makeText(this, "You are underage. There must be an adult need during consultancy", Toast.LENGTH_LONG).show();
            return;
        }

        if (userAge < MINAGE) {
            Toast.makeText(this, "You are above required age", Toast.LENGTH_LONG).show();
            return;
        }

        ShowProgressDialog("Loading", "Loading Data");
        SubmitQuery();
    }

    private Context context;
    private List<SpinnerModel> lstRoles = new ArrayList<>();

    private void SubmitQuery() {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.ADDAPPOINTMENT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response==========>>>>>", response);
                try {
                    lstRoles = new ArrayList<>();
                    JSONObject json = new JSONObject(response);

                    String str_result = json.getString("result");
                    if (str_result.equalsIgnoreCase("true")) {
                        Toast.makeText(context, json.getString("message"), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, MainActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    } else {
                        Toast.makeText(context, json.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                    HideProgressDialog();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Log.e("=====submit....", user.getId());
                Map<String, String> params = new HashMap<String, String>();
                params.put("age", edAge.getText().toString().trim());
                params.put("name", edName.getText().toString().trim());
                params.put("contact", user.getId());
                params.put("email", edEmail.getText().toString().trim());
                params.put("query", edQuery.getText().toString().trim());
                params.put("sub_cat", subCatId);
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

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        super.onDestroy();
    }

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
}
