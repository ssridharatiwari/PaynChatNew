package com.startup.paynchat.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.startup.paynchat.BuildConfig;
import com.startup.paynchat.GlobalVariables;
import com.startup.paynchat.R;
import com.startup.paynchat.WebViewActivity;
import com.startup.paynchat.activities.SignInActivity;
import com.startup.paynchat.activities.UseHistoryActivity;
import com.startup.paynchat.activities.ViewPlansActivity;
import com.startup.paynchat.models.User;
import com.startup.paynchat.services.SinchService;
import com.startup.paynchat.utils.ConfirmationDialogFragment;
import com.startup.paynchat.utils.Helper;
import com.startup.paynchat.utils.PreferenceConnector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by a_man on 01-01-2018.
 */

public class OptionsFragment extends BaseFullDialogFragment {
    private static final String CONFIRM_TAG = "confirmtag";
    private static final String PRIVACY_TAG = "privacytag";
    private static final String PROFILE_EDIT_TAG = "profileedittag";
    private ImageView userImage;
    private TextView userName, userStatus;
    private Helper helper;
    private SinchService.SinchServiceInterface sinchServiceInterface;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_options, container);
        userImage = view.findViewById(R.id.userImage);
        userName = view.findViewById(R.id.userName);
        userStatus = view.findViewById(R.id.userStatus);

        helper = new Helper(getContext());
        setUserDetails(helper.getLoggedInUser());

        view.findViewById(R.id.userDetailContainer).setOnClickListener(view19 -> new ProfileEditDialogFragment().show(getChildFragmentManager(), PROFILE_EDIT_TAG));
        view.findViewById(R.id.back).setOnClickListener(view18 -> {
            Helper.closeKeyboard(getContext(), view18);
            dismiss();
        });

        view.findViewById(R.id.txt_back).setOnClickListener(view18 -> {
            Helper.closeKeyboard(getContext(), view18);
            dismiss();
        });

        view.findViewById(R.id.counselling_his).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent svIntent = new Intent(getActivity(), UseHistoryActivity.class);
                startActivity(svIntent);
            }
        });
        view.findViewById(R.id.purchaseplan).setOnClickListener(view18 -> OpenPurchaseIntent());
        view.findViewById(R.id.share).setOnClickListener(view17 -> Helper.openShareIntent(getContext(), null, (getString(R.string.hey_there) + " " + getString(R.string.app_name) + "\n" + getString(R.string.download_now) + ": " + ("https://play.google.com/store/apps/details?id=" + getContext().getPackageName()))));
        view.findViewById(R.id.rate).setOnClickListener(view16 -> Helper.openPlayStore(getContext()));
        view.findViewById(R.id.contact).setOnClickListener(view15 -> Helper.openSupportMail(getContext()));
        view.findViewById(R.id.privacy).setOnClickListener(view14 -> {
            PreferenceConnector.writeString(getActivity(), PreferenceConnector.WEBHEADING, "");
            PreferenceConnector.writeString(getActivity(), PreferenceConnector.WEBURL, GlobalVariables.PRIVACYPOLICY);
            startActivity(new Intent(getActivity(), WebViewActivity.class));
        });
        view.findViewById(R.id.deleteaccount).setOnClickListener(view14 -> {
            DeleteAccount();
        });

        view.findViewById(R.id.logout).setOnClickListener(view13 -> {
            ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newConfirmInstance(getString(R.string.logout_title),
                    getString(R.string.logout_message), null, null,
                    view12 -> {
                        FirebaseAuth.getInstance().signOut();
                        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Helper.BROADCAST_LOGOUT));
                        sinchServiceInterface.stopClient();
                        helper.logout(getActivity());
                        startActivity(new Intent(getActivity(), SignInActivity.class));
                        getActivity().finishAffinity();
                        PreferenceConnector.cleanPrefrences(getActivity());
                    },
                    view1 -> {
                    });
            confirmationDialogFragment.show(getChildFragmentManager(), CONFIRM_TAG);
        });

        view.findViewById(R.id.editprofile).setOnClickListener(view13 -> {
            ProfileEditDialogFragment.newInstance(true).show(
                    getActivity().getSupportFragmentManager(), PROFILE_EDIT_TAG);
        });

        if (BuildConfig.IS_DEMO) {
        }

        return view;
    }

    private void DeleteAccount() {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.DELETEACOOUNT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("res=LoginUser==>", response);
                try {
                    JSONObject json = new JSONObject(response);

                    String str_result = json.getString("result");

                    if (str_result.equalsIgnoreCase("true")) {
                        Toast.makeText(getActivity(), json.getString("message"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), json.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", PreferenceConnector.readString(getActivity(), PreferenceConnector.LOGINEDUSERID, ""));
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


    public void OpenPurchaseIntent() {
        Intent intent = new Intent(getActivity(), ViewPlansActivity.class);
        intent.putExtra("gotoform", "0");
        getActivity().startActivity(intent);
    }

    public void setUserDetails(User user) {
        userName.setText(PreferenceConnector.readString(getActivity(), PreferenceConnector.LOGINEDNAME, ""));
        userStatus.setText(user.getId());
        //  Glide.with(this).load(user.getImage()).apply(new RequestOptions().placeholder(R.drawable.profile)).into(userImage);
    }

    public static OptionsFragment newInstance(SinchService.SinchServiceInterface sinchServiceInterface) {
        OptionsFragment fragment = new OptionsFragment();
        fragment.sinchServiceInterface = sinchServiceInterface;
        return fragment;
    }
}
