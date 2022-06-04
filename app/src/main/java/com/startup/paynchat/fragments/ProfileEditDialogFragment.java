package com.startup.paynchat.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.startup.paynchat.GlobalVariables;
import com.startup.paynchat.R;
import com.startup.paynchat.activities.ImageViewerActivity;
import com.startup.paynchat.models.Attachment;
import com.startup.paynchat.models.AttachmentTypes;
import com.startup.paynchat.models.User;
import com.startup.paynchat.utils.FirebaseUploader;
import com.startup.paynchat.utils.Helper;
import com.startup.paynchat.utils.PreferenceConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileEditDialogFragment extends BaseFullDialogFragment implements ImagePickerCallback {
    private static final int REQUEST_CODE_MEDIA_PERMISSION = 999;
    private static final int REQUEST_CODE_PICKER = 4321;
    private EditText userPhone, userNameEdit, userStatus;
    private ImageView userImage;
    private ProgressBar userImageProgress;
    private Helper helper;
    private String[] permissionsCamera = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private User userMe;
    private ImagePicker imagePicker;
    private CameraImagePicker cameraPicker;
    private String pickerPath;
    private ProgressDialog progressDialog;
    private DatabaseReference usersRef;
    private boolean firstEdit;
    private LinearLayout layemail;
    private Context mContext;

    public static ProfileEditDialogFragment newInstance(boolean firstEdit) {
        ProfileEditDialogFragment profileEditDialogFragment = new ProfileEditDialogFragment();
        profileEditDialogFragment.firstEdit = firstEdit;
        return profileEditDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        helper = new Helper(getActivity());
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference(Helper.REF_USERS);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_edit, container);
        userImage = view.findViewById(R.id.userImage);
        userImageProgress = view.findViewById(R.id.progressBar);
        userPhone = view.findViewById(R.id.userPhone);
        userNameEdit = view.findViewById(R.id.userNameEdit);
        userStatus = view.findViewById(R.id.userStatus);
        layemail = view.findViewById(R.id.lay_email);
        userPhone.setEnabled(false);
        view.findViewById(R.id.changeImage).setOnClickListener(view13 -> pickProfileImage());
        view.findViewById(R.id.back).setOnClickListener(view12 -> dismiss());
        view.findViewById(R.id.done).setOnClickListener(view1 -> {
            Helper.closeKeyboard(getContext(), view1);
            updateUserNameAndStatus(userNameEdit.getText().toString().trim(), userStatus.getText().toString().trim());
        });
        userImage.setOnClickListener(v -> {
            if (userMe != null && !TextUtils.isEmpty(userMe.getImage()))
                startActivity(ImageViewerActivity.newUrlInstance(getContext(), userMe.getImage()));
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper = new Helper(getContext());
        userMe = helper.getLoggedInUser();
        setUserDetails();
        if (userMe.isCounsellor()) {
            layemail.setVisibility(View.VISIBLE);
        } else {
            layemail.setVisibility(View.GONE);
        }
        if (userMe.isCounsellor()) {
            GetCounsellorDetail(PreferenceConnector.readString(getActivity(), PreferenceConnector.LOGINEDUSERID, ""));
        } else {
            GetUSerDetail(PreferenceConnector.readString(getActivity(), PreferenceConnector.LOGINEDUSERID, ""));
        }

    }

    private void setUserDetails() {
        userPhone.setText(userMe.getId());
//        userNameEdit.setText(userMe.getName());
//        userStatus.setText(userMe.getStatus());

        userNameEdit.setText(PreferenceConnector.readString(getActivity(), PreferenceConnector.LOGINEDNAME, ""));
        userStatus.setText(PreferenceConnector.readString(getActivity(), PreferenceConnector.LOGINEDEMAIL, ""));

        Glide.with(this).load(userMe.getImage()).apply(new RequestOptions().placeholder(R.drawable.y_placeholder)).into(userImage);
    }

    private void saveAndBroadcast() {
        helper.setLoggedInUser(userMe);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.sendBroadcast(new Intent(Helper.BROADCAST_USER_ME));
    }

    private void updateUserNameAndStatus(String updatedName, String updatedStatus) {
        if (TextUtils.isEmpty(updatedName)) {
            Helper.presentToast(getContext(), getString(R.string.validation_req_username), false);
        } else { /*else if (TextUtils.isEmpty(updatedStatus)) {
            Toast.makeText(getContext(), R.string.validation_req_status, Toast.LENGTH_SHORT).show();
        } else if (!userMe.getName().equals(updatedName) || !userMe.getStatus().equals(updatedStatus)) {*/
            userMe.setName(updatedName);
            userMe.setStatus(updatedStatus);
            usersRef.child(userMe.getId()).setValue(userMe);
            saveAndBroadcast();
//            if (firstEdit) {
                if (userMe.isCounsellor()) {
                    UpdateProfile(PreferenceConnector.readString(getActivity(), PreferenceConnector.LOGINEDUSERID, ""),
                            GlobalVariables.PREURL + GlobalVariables.UPDATECOUNSELLOR);
                } else {
                    UpdateProfile(PreferenceConnector.readString(getActivity(), PreferenceConnector.LOGINEDUSERID, ""),
                            GlobalVariables.PREURL + GlobalVariables.UPDATEUSER);
                }

//            }
        }
    }

    private void UpdateProfile(String userid, String updateUrl) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest request = new StringRequest(Request.Method.POST, updateUrl, response -> {
            Log.d("response==========>>>>>", response);
            try {
                JSONObject json = new JSONObject(response);
                String str_result = json.getString("result");

                if (str_result.equalsIgnoreCase("true")) {
                    Toast.makeText(getActivity(), json.getString("message"), Toast.LENGTH_SHORT).show();
                    LoginUser(PreferenceConnector.readString(getActivity(), PreferenceConnector.LOGINEDPHONE, ""));
                    dismiss();
                } else {
                    Toast.makeText(getActivity(), json.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", userid);
                params.put("name", userNameEdit.getText().toString().trim());
                if (userMe.isCounsellor()) {
                    params.put("contact", userMe.getId());
                    params.put("email", userStatus.getText().toString().trim());
                }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_MEDIA_PERMISSION) {
            if (mediaPermissions().isEmpty()) {
                pickProfileImage();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Picker.PICK_IMAGE_DEVICE:
                    if (imagePicker == null) {
                        imagePicker = new ImagePicker(this);
                    }
                    imagePicker.submit(data);
                    break;
                case Picker.PICK_IMAGE_CAMERA:
                    if (cameraPicker == null) {
                        cameraPicker = new CameraImagePicker(this);
                        cameraPicker.reinitialize(pickerPath);
                    }
                    cameraPicker.submit(data);
                    break;
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void userImageUploadTask(final File fileToUpload, @AttachmentTypes.AttachmentType final int attachmentType, final Attachment attachment) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle(getString(R.string.uploading));
            progressDialog.setMessage(getString(R.string.just_moment));
            progressDialog.setCancelable(false);
        }
        if (!progressDialog.isShowing())
            progressDialog.show();
        //Toast.makeText(getContext(), R.string.uploading, Toast.LENGTH_SHORT).show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(getString(R.string.app_name)).child("ProfileImage").child(userMe.getId());
        FirebaseUploader firebaseUploader = new FirebaseUploader(new FirebaseUploader.UploadListener() {
            @Override
            public void onUploadFail(String message) {
                if (userImageProgress != null)
                    userImageProgress.setVisibility(View.GONE);
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onUploadSuccess(String downloadUrl) {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                if (userImageProgress != null) {
                    userImageProgress.setVisibility(View.GONE);
                }
                if (usersRef != null && userMe != null) {
                    userMe.setImage(downloadUrl);
                    usersRef.child(userMe.getId()).child("image").setValue(userMe.getImage());
                    saveAndBroadcast();
                }
            }

            @Override
            public void onUploadProgress(int progress) {

            }

            @Override
            public void onUploadCancelled() {
                if (userImageProgress != null) {
                    userImageProgress.setVisibility(View.GONE);
                }
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }, storageReference);
        firebaseUploader.setReplace(true);
        firebaseUploader.uploadImage(getContext(), fileToUpload);
        //userImageProgress.setVisibility(View.VISIBLE);
    }

    private void pickProfileImage() {
        if (mediaPermissions().isEmpty()) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
            alertDialog.setMessage(R.string.get_image_title);
            alertDialog.setPositiveButton(R.string.get_image_camera, (dialogInterface, i) -> {
                dialogInterface.dismiss();

                cameraPicker = new CameraImagePicker(ProfileEditDialogFragment.this);
                cameraPicker.shouldGenerateMetadata(true);
                cameraPicker.shouldGenerateThumbnails(true);
                cameraPicker.setImagePickerCallback(ProfileEditDialogFragment.this);
                pickerPath = cameraPicker.pickImage();
            });
            alertDialog.setNegativeButton(R.string.get_image_gallery, (dialogInterface, i) -> {
                dialogInterface.dismiss();

                imagePicker = new ImagePicker(ProfileEditDialogFragment.this);
                imagePicker.shouldGenerateMetadata(true);
                imagePicker.shouldGenerateThumbnails(true);
                imagePicker.setImagePickerCallback(ProfileEditDialogFragment.this);
                imagePicker.pickImage();
            });
            alertDialog.create().show();
        } else {
            requestPermissions(permissionsCamera, REQUEST_CODE_MEDIA_PERMISSION);
        }
    }

    @Override
    public void onImagesChosen(List<ChosenImage> images) {
        File fileToUpload = new File(Uri.parse(images.get(0).getOriginalPath()).getPath());
        Glide.with(this).load(fileToUpload).apply(new RequestOptions().placeholder(R.drawable.y_placeholder)).into(userImage);
        userImageUploadTask(fileToUpload, AttachmentTypes.IMAGE, null);
    }

    @Override
    public void onError(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // You have to save path in case your activity is killed.
        // In such a scenario, you will need to re-initialize the CameraImagePicker
        outState.putString("picker_path", pickerPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("picker_path")) {
                pickerPath = savedInstanceState.getString("picker_path");
            }
        }
    }

    private List<String> mediaPermissions() {
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : permissionsCamera) {
            if (ActivityCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        return missingPermissions;
    }

    private void GetUSerDetail(String userid) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL+GlobalVariables.GETUSERDETAIL, response -> {
            Log.d("response==========>>>>>", response);
            try {
                JSONObject json = new JSONObject(response);
                String str_result = json.getString("result");

                if (str_result.equalsIgnoreCase("true")) {
                    JSONArray data = json.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject data_obj = data.getJSONObject(i);
                        PreferenceConnector.readString(getActivity(), PreferenceConnector.LOGINEDNAME, data_obj.getString("name"));
                    }
                } else {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setUserDetails();
        }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", userid);
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


//    {"result":true,"message":"data found","data":[{"id":"24","name":"Shridhar Tiwari","contact":"+917691001989","mail":"ssridharatiwari@gmail.com","role":"lawyer","status":"Active"}]}
    private void GetCounsellorDetail(String userid) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL+GlobalVariables.GETCOUNSELLORDETAIL, response -> {
            Log.d("response==========>>>>>", response);
            try {
                JSONObject json = new JSONObject(response);
                String str_result = json.getString("result");

                if (str_result.equalsIgnoreCase("true")) {
                    JSONArray data = json.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject data_obj = data.getJSONObject(i);
                        PreferenceConnector.readString(getActivity(), PreferenceConnector.LOGINEDNAME, data_obj.getString("name"));
                    }
                } else {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", userid);
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


    private void LoginUser(String userid) {
        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.USERAUTOLOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("res=LoginUser==>", response);
                try {
                    JSONObject json = new JSONObject(response);

                    String str_result = json.getString("result");

                    if (str_result.equalsIgnoreCase("true")) {
                        JSONArray data = json.getJSONArray("data");
                        JSONObject data_obj = data.getJSONObject(0);
                        PreferenceConnector.writeString(mContext, PreferenceConnector.LOGINEDUSERID, data_obj.getString("id"));
                        PreferenceConnector.writeString(mContext, PreferenceConnector.LOGINEDNAME, data_obj.getString("name"));
                        PreferenceConnector.writeString(mContext, PreferenceConnector.LOGINEDPHONE, data_obj.getString("contact"));
                        PreferenceConnector.writeString(mContext, PreferenceConnector.LOGINEDEMAIL, "");

                        if (data_obj.has("wallet_bal")) {
                            PreferenceConnector.writeInteger(mContext, PreferenceConnector.WALLETBAL, data_obj.getInt("wallet_bal"));
                        }
                    } else {
                        Toast.makeText(mContext, json.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", userid);
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
