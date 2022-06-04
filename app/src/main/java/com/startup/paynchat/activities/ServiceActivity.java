package com.startup.paynchat.activities;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.startup.paynchat.GlobalVariables;
import com.startup.paynchat.R;
import com.startup.paynchat.adapters.OurServiceAdapter;
import com.startup.paynchat.models.SubCategoryModel;
import com.startup.paynchat.utils.PreferenceConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceActivity extends BaseActivity implements View.OnClickListener, OurServiceAdapter.OnItemClickListener {
    private Context context;
    private String serviceId, serviceName;
    List<SubCategoryModel> itemsSubCat = new ArrayList<>();

    @Override
    void onSinchConnected() {

    }

    @Override
    void onSinchDisconnected() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        context = this;

        serviceId = getIntent().getStringExtra("serviceid");
        serviceName = getIntent().getStringExtra("servicename");

        initViews();
        LoadSubServices();
        IsPlanSubscribed(PreferenceConnector.readString(mContext, PreferenceConnector.LOGINEDUSERID, ""));
    }

    private void initViews() {
        ImageView ivBackButton = findViewById(R.id.back_button);
        ivBackButton.setOnClickListener(this);

        TextView txtServiceName = findViewById(R.id.services_name);
        txtServiceName.setText(serviceName);

        TextView txtOurService = findViewById(R.id.txt_ourservice);
        txtOurService.setText("");
        txtOurService.setVisibility(View.GONE);
    }

    private void LoadSubServices() {
        ShowProgressDialog("Loading", "Loading Data");
        itemsSubCat = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.GETSUBCATEGORY, new Response.Listener<String>() {
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

                            //{"result":true,"message":"data found","data":[{"id":"10","category":"Legal Advice","sub_category":"Criminal Cases"}]}
                            String id = data_obj.getString("id");
                            String category = data_obj.getString("category");
                            String sub_category = data_obj.getString("sub_category");
                            itemsSubCat.add(new SubCategoryModel(R.drawable.girl, id, category, sub_category, ""));
                        }
                    } else {
                        Toast.makeText(context, str_message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_services);
                recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                recyclerView.setHasFixedSize(true);
                OurServiceAdapter mAdapter = new OurServiceAdapter(context, itemsSubCat, ServiceActivity.this);
                recyclerView.setAdapter(mAdapter);

                HideProgressDialog();
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
                params.put("id", serviceId);
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

    @Override
    public void onBookAppoimentClick(View view, SubCategoryModel obj, int position) {
        //App TODO by  ssridhara
        //Remove below three line to autodetect activetatus
//        isAudioActive = true;
//        isVideoActive = true;
//        isChatActive = true;
        if (isAudioActive || isVideoActive || isChatActive) {
            Intent intent = new Intent(context, CousolingForm.class);
            intent.putExtra("subcat_id", obj.getId());
            startActivity(intent);
        }else {
            Intent intent = new Intent(context, ViewPlansActivity.class);
            intent.putExtra("subcat_id", obj.getId());
            intent.putExtra("gotoform", "1");
            startActivity(intent);
        }
    }

    private ProgressDialog progressDialog;
    @Override
    protected void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        super.onDestroy();
    }

    private void ShowProgressDialog(String strShowTextTitle, String strShowMessage){
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(strShowTextTitle);
            progressDialog.setMessage(strShowMessage);
            progressDialog.setCancelable(false);
        }
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void HideProgressDialog(){
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();

    }

}