package com.startup.paynchat.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.startup.paynchat.BuildConfig;
import com.startup.paynchat.R;
import com.startup.paynchat.activities.AllEnquiries;
import com.startup.paynchat.activities.SignInActivity;
import com.startup.paynchat.activities.ViewPlansActivity;
import com.startup.paynchat.models.User;
import com.startup.paynchat.services.SinchService;
import com.startup.paynchat.utils.ConfirmationDialogFragment;
import com.startup.paynchat.utils.Helper;
import com.startup.paynchat.utils.PreferenceConnector;

/**
 * Created by a_man on 01-01-2018.
 */

public class OptionsFragmentCounsellor extends BaseFullDialogFragment {
    private static String CONFIRM_TAG = "confirmtag";
    private static String PRIVACY_TAG = "privacytag";
    private static String PROFILE_EDIT_TAG = "profileedittag";
    private ImageView userImage;
    private TextView userName, userStatus, actionBuy;
    private Helper helper;
    private SinchService.SinchServiceInterface sinchServiceInterface;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_counsellor, container);
        userImage = view.findViewById(R.id.userImage);
        userName = view.findViewById(R.id.userName);
        userStatus = view.findViewById(R.id.userStatus);
        actionBuy = view.findViewById(R.id.actionBuy);

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
                Intent svIntent = new Intent(getActivity(), AllEnquiries.class);
                startActivity(svIntent);
            }
        });
//        view.findViewById(R.id.purchaseplan).setOnClickListener(view18 -> OpenPurchaseIntent());
        view.findViewById(R.id.share).setOnClickListener(view17 -> Helper.openShareIntent(getContext(), null, (getString(R.string.hey_there) + " " + getString(R.string.app_name) + "\n" + getString(R.string.download_now) + ": " + ("https://play.google.com/store/apps/details?id=" + getContext().getPackageName()))));
        view.findViewById(R.id.rate).setOnClickListener(view16 -> Helper.openPlayStore(getContext()));
        view.findViewById(R.id.contact).setOnClickListener(view15 -> Helper.openSupportMail(getContext()));
        view.findViewById(R.id.privacy).setOnClickListener(view14 -> new PrivacyPolicyDialogFragment().show(getChildFragmentManager(), PRIVACY_TAG));
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
            actionBuy.setVisibility(View.VISIBLE);
            actionBuy.setOnClickListener(actionView -> {
                if (!TextUtils.isEmpty(BuildConfig.DEMO_ACTION_LINK)) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.DEMO_ACTION_LINK)));
                }
            });
        }

        return view;
    }



    public void OpenPurchaseIntent(){
        Intent intent = new Intent(getActivity(), ViewPlansActivity.class);
        intent.putExtra("gotoform", "0");
        ((Activity)getActivity()).startActivity(intent);
    }


    public void setUserDetails(User user) {
        userName.setText(PreferenceConnector.readString(getActivity(), PreferenceConnector.LOGINEDNAME, ""));
        userStatus.setText(user.getId());
        Glide.with(this).load(user.getImage()).apply(new RequestOptions().placeholder(R.drawable.profile)).into(userImage);
    }

    public static OptionsFragmentCounsellor newInstance(SinchService.SinchServiceInterface sinchServiceInterface) {
        OptionsFragmentCounsellor fragment = new OptionsFragmentCounsellor();
        fragment.sinchServiceInterface = sinchServiceInterface;
        return fragment;
    }
}
