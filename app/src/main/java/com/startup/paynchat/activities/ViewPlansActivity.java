package com.startup.paynchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.startup.paynchat.GlobalVariables;
import com.startup.paynchat.R;
import com.startup.paynchat.adapters.OurPlansAdapter;
import com.startup.paynchat.models.PlansItemsModel;
import com.startup.paynchat.models.PlansModel;
import com.startup.paynchat.utils.Helper;
import com.startup.paynchat.utils.PreferenceConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewPlansActivity extends AppCompatActivity implements View.OnClickListener, PaymentResultListener {
    private TextView txtOurService, txtFooter;
    private List<PlansModel> planModel;
    private Helper helper;
    private boolean goBackToForm;
    private String subcatId;
    private String plan_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewplans);
        helper = new Helper(this);

        initViews();
        LoadOurServices();

        String isGoToForm = getIntent().getStringExtra("gotoform");
        subcatId = getIntent().getStringExtra("subcat_id");
        if (isGoToForm.equals("1")) {
            goBackToForm = true;
        }
    }

    private void initViews() {
        ImageView ivBackButton = findViewById(R.id.back_button);
        ivBackButton.setOnClickListener(this);

        txtOurService = findViewById(R.id.txt_ourservice);
        txtFooter = findViewById(R.id.txt_footer);
        ((TextView)findViewById(R.id.txt_mygems)).setText("My Gems: "+PreferenceConnector.readInteger(this, PreferenceConnector.WALLETBAL, 0)+"");

        txtOurService.setText("Subscribe the plans according to your need");

        txtFooter.setText("Note* Please click on the plan to purchase Gems for your account");
    }

    private ArrayList<PlansItemsModel> plansItem = new ArrayList<>();

    private void LoadOurServices() {
        planModel = new ArrayList<>();

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.GETPLANS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response=====a====>>>>>", response);
                try {
                    JSONObject json = new JSONObject(response);
                    plansItem = new ArrayList<>();
                    JSONArray jsonArray2 = json.getJSONArray("data");
                    for (int j = 0; j < jsonArray2.length(); j++) {
                        JSONObject jsonPlansData = jsonArray2.getJSONObject(j);

                        String plan_id = jsonPlansData.getString("plan_id");
                        String discount = jsonPlansData.getString("discount");
                        String category = jsonPlansData.getString("coins");
                        String price = jsonPlansData.getString("price");

                        plansItem.add(new PlansItemsModel(plan_id, discount, category, price));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RecyclerView recyclerView = findViewById(R.id.rv_services);
                recyclerView.setLayoutManager(new GridLayoutManager(ViewPlansActivity.this, 3));
                OurPlansAdapter mAdapter = new OurPlansAdapter(ViewPlansActivity.this, plansItem);
                recyclerView.setAdapter(mAdapter);
                mAdapter.setOnItemClickListener(new OurPlansAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, PlansItemsModel obj, int position) {
                        plan_id = obj.getPlan_id();
                        startPayment(obj);
                    }
                });
            }
        }, error -> Log.d("error", error.toString()));
        queue.add(request);
    }

    private void AddCoins() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.SUBSCRIBE_PACKAGE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response=====b====>>>>>", response);
                try {
                    JSONObject json = new JSONObject(response);
                    String str_message = json.getString("message");
                    String str_result = json.getString("result");
                    if (str_result.equalsIgnoreCase("true")) {
                        onBackPressed();
                    }
                    Toast.makeText(ViewPlansActivity.this, str_message, Toast.LENGTH_SHORT).show();

//                    if (goBackToForm) {
//                        Intent intent = new Intent(ViewPlansActivity.this, CousolingForm.class);
//                        intent.putExtra("subcat_id", subcatId);
//                        startActivity(intent);
//                    }
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
                params.put("plan_id", plan_id);
                params.put("user_id", PreferenceConnector.readString(getApplicationContext(), PreferenceConnector.LOGINEDUSERID, ""));
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_button:
                finish();
                break;
            default:
                break;
        }
    }

    PlansItemsModel curModel;

    public void startPayment(PlansItemsModel model) {

        curModel = model;
        String strRazorPayId = getString(R.string.razorpay_key_id);
        String strRazorPayKey = getString(R.string.razorpay_key_secret);
        Checkout co = new Checkout();
        co.setKeyID(strRazorPayId);
        if (strRazorPayId.length() == 0) {
            Toast.makeText(this, "Razorpay Key Not Available", Toast.LENGTH_SHORT).show();
        } else {
            try {
                JSONObject options = new JSONObject();
                options.put("name", (helper.getLoggedInUser()).getName());
                options.put("description", "Add Coins for " + (helper.getLoggedInUser()).getName());
                options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
                options.put("currency", "INR");
                options.put("amount", (Integer.parseInt(model.getPrice().replace("/-", "")) * 100) + "");

                JSONObject preFill = new JSONObject();
                preFill.put("email", "");
                preFill.put("contact", (helper.getLoggedInUser()).getId());

                options.put("prefill", preFill);

                co.open(this, options);
            } catch (Exception e) {
                Toast.makeText(this, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onPaymentSuccess(String s) {
        AddCoins();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Error.", Toast.LENGTH_SHORT).show();
    }
}