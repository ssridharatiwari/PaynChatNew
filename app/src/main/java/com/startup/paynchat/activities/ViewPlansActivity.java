package com.startup.paynchat.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.startup.paynchat.GlobalVariables;
import com.startup.paynchat.R;
import com.startup.paynchat.WebViewActivity;
import com.startup.paynchat.adapters.OurPlansAdapter;
import com.startup.paynchat.models.PlansItemsModel;
import com.startup.paynchat.models.PlansModel;
import com.startup.paynchat.utils.Helper;
import com.startup.paynchat.utils.PreferenceConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
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
    private Context svContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewplans);
        helper = new Helper(this);
        svContext = this;
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
        customeProgressDialog = new CustomeProgressDialog(svContext, R.layout.lay_customprogessdialog);
        TextView textView = (TextView) customeProgressDialog.findViewById(R.id.loader_showtext);
        textView.setVisibility(View.GONE);

        customeProgressDialog.setCancelable(false);
        customeProgressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.GETPLANS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response=====a====>>>>>", response);
                try {
                    plansItem = new ArrayList<>();

                    JSONObject json = new JSONObject(response);
                    JSONArray all_packages = json.getJSONArray("all_packages");
                    for(int all_packages_i = 0; all_packages_i < all_packages.length(); all_packages_i++){
                        JSONObject all_packages_obj=all_packages.getJSONObject(all_packages_i);
                        JSONArray data = all_packages_obj.getJSONArray("data");
                        for(int data_i = 0; data_i < data.length(); data_i++){
                            JSONObject jsonPlansData=data.getJSONObject(data_i);
                            String plan_id = jsonPlansData.getString("plan_id");
                            String discount = jsonPlansData.getString("discount");
                            String category = jsonPlansData.getString("coins");
                            String price = jsonPlansData.getString("price");

                            plansItem.add(new PlansItemsModel(plan_id, discount, category, price));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (null != customeProgressDialog && customeProgressDialog.isShowing()) {
                    customeProgressDialog.dismiss();
                }

                RecyclerView recyclerView = findViewById(R.id.rv_services);
                recyclerView.setLayoutManager(new GridLayoutManager(ViewPlansActivity.this, 3));
                OurPlansAdapter mAdapter = new OurPlansAdapter(ViewPlansActivity.this, plansItem);
                recyclerView.setAdapter(mAdapter);
                mAdapter.setOnItemClickListener((view, obj, position) -> {
                    plan_id = obj.getPlan_id();
                    startPayment(obj);
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
                try {
                    JSONObject json = new JSONObject(response);
                    String str_message = json.getString("message");
                    String str_result = json.getString("result");
                    if (str_result.equalsIgnoreCase("true")) {
                        onBackPressed();
                    }
                    Toast.makeText(ViewPlansActivity.this, str_message, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("plan_id", plan_id);
                params.put("user_id", PreferenceConnector.readString(getApplicationContext(), PreferenceConnector.LOGINEDUSERID, ""));
                params.put("cr_dr", "CR");
                params.put("amount", curAMount);
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
    private String curAMount;
    public void startPayment(PlansItemsModel model) {
        curModel = model;
        curAMount = curModel.getPrice().replace("/-", "");
        StartUpiPayment(curAMount);
//        String strRazorPayId = getString(R.string.razorpay_key_id);
//        String strRazorPayKey = getString(R.string.razorpay_key_secret);
//        Checkout co = new Checkout();
//        co.setKeyID(strRazorPayId);
//        if (strRazorPayId.length() == 0) {
//            Toast.makeText(this, "Razorpay Key Not Available", Toast.LENGTH_SHORT).show();
//        } else {
//            try {
//                JSONObject options = new JSONObject();
//                options.put("name", (helper.getLoggedInUser()).getName());
//                options.put("description", "Add Coins for " + (helper.getLoggedInUser()).getName());
//                options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
//                options.put("currency", "INR");
//                options.put("amount", (Integer.parseInt(curAMount) * 100) + "");
//
//                JSONObject preFill = new JSONObject();
//                preFill.put("email", "");
//                preFill.put("contact", (helper.getLoggedInUser()).getId());
//
//                options.put("prefill", preFill);
//
//                co.open(this, options);
//            } catch (Exception e) {
//                Toast.makeText(this, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void onResume() {
        if (isPaymentTried) {
            String str_order_id = PreferenceConnector.readString(svContext, PreferenceConnector.ORDERID, "");
            if (! str_order_id.equals("")) {
                CheckPaymentStatus(str_order_id);

            }
        }
        super.onResume();
    }
//            "data": {
//        "id": 66,
//                "customer_vpa": "",
//                "amount": 100,
//                "client_txn_id": "abcd1234",
//                "customer_name": "Jon Doe",
//                "customer_email": "jondoe@gmail.com",
//                "customer_mobile": "9876543210",
//                "p_info": "Product Name",
//                "upi_txn_id": "209876543210",
//                "status": "success",
//                "remark": "transaction successful",
//                "udf1": "",
//                "udf2": "",
//                "udf3": "",
//                "redirect_url": "http://google.com",
//                "txnAt": "2022-01-15",
//                "createdAt": "2022-01-15T14:43:30.000Z",
//                "Merchant": {
//            "name": "Merchant Name",
//                    "upi_id": "Q1234XXXX@ybl"
//        }
//    }
    private void CheckPaymentStatus(String transId) {
        RequestQueue queue = Volley.newRequestQueue(svContext);
        StringRequest request = new StringRequest(Request.Method.POST, "https://merchant.upigateway.com/api/check_order_status",
                response -> {
                    Log.d("res=LoginUser==>", response);
                    try {
                        JSONObject json = new JSONObject(response);
                        String str_result = json.getString("status");
                        if (str_result.equalsIgnoreCase("true")) {
                            JSONObject logindetail_obj = json.getJSONObject("data");
                            String str_status = logindetail_obj.getString("status");
                            if (str_status.equals("success")) {
                                Toast.makeText(svContext, logindetail_obj.getString("remark"), Toast.LENGTH_LONG).show();
                                isPaymentTried = false;
                                AddCoins();
                            }
                        } else {
//                            Toast.makeText(svContext,  json.getString("msg"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (null != customeProgressDialog && customeProgressDialog.isShowing()) {
                        customeProgressDialog.dismiss();
                    }
                }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("key", "92eb94ef-73c1-4605-9577-9a81d5a9dc1a");
                params.put("client_txn_id", transId);
                params.put("txn_date", getcurrentDate());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(request);
    }

    private String pay_user_txn_id;
    private CustomeProgressDialog customeProgressDialog;
    private static boolean isPaymentTried = false;
    private void StartUpiPayment(String amountAdd){
        customeProgressDialog = new CustomeProgressDialog(svContext, R.layout.lay_customprogessdialog);
        TextView textView = (TextView) customeProgressDialog.findViewById(R.id.loader_showtext);
        textView.setVisibility(View.GONE);

        customeProgressDialog.setCancelable(false);
        customeProgressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(svContext);
        StringRequest request = new StringRequest(Request.Method.POST, "https://merchant.upigateway.com/api/create_order",
                response -> {
                    Log.d("res=Upiresult==>", response);
                    try {
                        JSONObject json = new JSONObject(response);
                        String str_result = json.getString("status");
                        if (str_result.equalsIgnoreCase("true")) {
                            JSONObject logindetail_obj = json.getJSONObject("data");

//                            PreferenceConnector.writeString(svContext, PreferenceConnector.ORDERID, logindetail_obj.getString("order_id"));
                            PreferenceConnector.writeString(svContext, PreferenceConnector.ORDERID, pay_user_txn_id);
                            String str_payment_url = logindetail_obj.getString("payment_url");

                            isPaymentTried = true;
                            Intent browserIntent = new Intent(svContext, WebViewActivity.class);
                            PreferenceConnector.writeString(svContext, PreferenceConnector.WEBHEADING, "");
                            PreferenceConnector.writeString(svContext, PreferenceConnector.WEBURL, str_payment_url);
                            startActivity(browserIntent);
                        } else {
                            Toast.makeText(svContext, json.getString("msg"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (null != customeProgressDialog && customeProgressDialog.isShowing()) {
                        customeProgressDialog.dismiss();
                    }
                }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                String strName = PreferenceConnector.readString(svContext, PreferenceConnector.LOGINEDNAME, "");
                if (strName.equals("")) {
                    strName = "User_" + ((PreferenceConnector.readString(svContext, PreferenceConnector.LOGINEDPHONE, "")).replace("+", ""));
                }
                String strEmail = PreferenceConnector.readString(svContext, PreferenceConnector.LOGINEDEMAIL, "");
                if (strEmail.equals("")) {
                    strEmail = "userpaynchat@paynchat.online";
                }
                pay_user_txn_id = "PC" + getDateTimeForLog().replaceAll(" ", "");
                Map<String, String> params = new HashMap<>();
                params.put("key", "92eb94ef-73c1-4605-9577-9a81d5a9dc1a");
                params.put("client_txn_id", pay_user_txn_id);
                params.put("amount", amountAdd);
                params.put("p_info", "Paynchat ");
                params.put("customer_name", strName);
                params.put("customer_email",  strEmail);
                params.put("customer_mobile", PreferenceConnector.readString(svContext, PreferenceConnector.LOGINEDPHONE, "").replace("+91", ""));
                params.put("redirect_url", "https://paynchat.online");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(request);
    }

    public static String getDateTimeForLog() {
        return getStraightcurrentDate() + "_" + getStrightFormatedcurrentTime();
    }

    public static String getcurrentDate() {
        Calendar today = Calendar.getInstance();
        int date = today.get(Calendar.DATE);
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);

        return (date >= 10 ? date : "0" + date) + "-" + (month >= 10 ? month : "0" + month) + "-" + year;
    }

    public static String getCurrentTime() {
        Calendar today = Calendar.getInstance();
        int hour = today.get(Calendar.HOUR);
        int minute = today.get(Calendar.MINUTE);
        int second 	= today.get(Calendar.SECOND);
        int amorpm = today.get(Calendar.AM_PM);

        String strAMORPM = "";
        if (amorpm == 0) {
            strAMORPM = "am";
        } else {
            strAMORPM = "pm";
        }

        if (hour == 0) {
            hour = 12;
        }

        return (hour >= 10 ? hour : "0" + hour) + " " + (minute >= 10 ? minute : "0" + minute) + " " + second;
    }

    public static String getStraightcurrentDate() {
        Calendar today = Calendar.getInstance();
        int date = today.get(Calendar.DATE);
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);

        return year + "" + (month >= 10 ? month : "0" + month) + "" + (date >= 10 ? date : "0" + date);
    }

    public static String getStrightFormatedcurrentTime() {
        Calendar today = Calendar.getInstance();
        int hour = today.get(Calendar.HOUR);
        int minute = today.get(Calendar.MINUTE);
        int second 	= today.get(Calendar.SECOND);
        int amorpm = today.get(Calendar.AM_PM);

        String strAMORPM = "";
        if (amorpm == 0) {
            strAMORPM = "am";
        } else {
            strAMORPM = "pm";
        }

        if (hour == 0) {
            hour = 12;
        }

        return (hour >= 10 ? hour : "0" + hour) + "" + (minute >= 10 ? minute : "0" + minute) + "" + second;
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