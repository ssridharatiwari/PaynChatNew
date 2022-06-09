package com.startup.paynchat.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.startup.paynchat.GlobalVariables;
import com.startup.paynchat.adapters.MenuUsersRecyclerAdapter;
import com.startup.paynchat.utils.PreferenceConnector;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;
import com.startup.paynchat.R;
import com.startup.paynchat.models.LogCall;
import com.startup.paynchat.models.User;
import com.startup.paynchat.utils.AudioPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CallScreenActivity extends BaseActivity implements SensorEventListener {
    public static final int MAXCALLTIMEINSEC = 3600;
    static final String TAG = CallScreenActivity.class.getSimpleName();
    static final String ADDED_LISTENER = "addedListener";
    public static String userInOut = "";
    public static String userCallId = "";
    public static int callSecond;
    private static String EXTRA_DATA_USER = "extradatauser";
    private static String EXTRA_DATA_IN_OR_OUT = "extradatainorout";

    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;
    public static User userCounsellor;
    private int maxCallSec;
    private String mCallId, inOrOut;
    private boolean mAddedListener, mLocalVideoViewAdded, mRemoteVideoViewAdded, isVideo, isMute, isSpeaker, alphaInvisible, logSaved;
    private int mCallDurationSecond = 0;
    private User user;
    private boolean isCallStart = false;
    private TextView mCallDuration, mCallState, mCallerName, yoohoo_calling;
    private ImageView userImage1, userImage2, logo, switchVideo, switchMic, switchVolume, hangupButton;
    private View tintBlue, bottomButtons;
    private RelativeLayout localVideo, remoteVideo;

    private SensorManager mSensorManager;
    private Sensor mProximity;
    PowerManager.WakeLock wlOff = null, wlOn = null;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (wlOff != null && wlOff.isHeld()) {
                wlOff.release();
            } else if (wlOn != null && wlOn.isHeld()) {
                wlOn.release();
            }
        } catch (RuntimeException ex) {
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float distance = sensorEvent.values[0];
        if (!isVideo && !isSpeaker) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (distance < 4) {
                if (wlOn != null && wlOn.isHeld()) {
                    wlOn.release();
                }
                if (pm != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        if (wlOff == null)
                            wlOff = pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "tag");
                        if (!wlOff.isHeld()) wlOff.acquire();
                    }
                }
            } else {
                if (wlOff != null && wlOff.isHeld()) {
                    wlOff.release();
                }
                if (pm != null) {
                    if (wlOn == null)
                        wlOn = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
                    if (!wlOn.isHeld()) wlOn.acquire();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private class UpdateCallDurationTask extends TimerTask {
        @Override
        public void run() {
            CallScreenActivity.this.runOnUiThread(() -> updateCallDuration());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(ADDED_LISTENER, mAddedListener);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mAddedListener = savedInstanceState.getBoolean(ADDED_LISTENER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_screen);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

//        Intent intent = getIntent();
        user = userCounsellor;
        inOrOut = userInOut;
        mCallId = userCallId;
        maxCallSec = callSecond;
//        user = (User)intent.getParcelableExtra(EXTRA_DATA_USER);
//        mCallId = (String)intent.getStringExtra(SinchService.CALL_ID);
//        inOrOut = (String)intent.getStringExtra(EXTRA_DATA_IN_OR_OUT);


//        usersRef.child(user.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
        usersRef.child(PreferenceConnector.readString(this, PreferenceConnector.LOGINEDUSERID, "")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mContext != null) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        if (CallScreenActivity.this.user == null) {
                            CallScreenActivity.this.user = new User(CallScreenActivity.this.user.getId(),
                                    (CallScreenActivity.this.user.getName()), user.getStatus(),
                                    user.getImage(), user.getUserId());
                        } else {
                            CallScreenActivity.this.user.setName((CallScreenActivity.this.user.getName()));
                            CallScreenActivity.this.user.setImage(user.getImage());
                            CallScreenActivity.this.user.setStatus(user.getStatus());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mAudioPlayer = new AudioPlayer(this);
        mCallDuration = findViewById(R.id.callDuration);
        mCallerName = findViewById(R.id.remoteUser);
        mCallState = findViewById(R.id.callState);
        userImage1 = findViewById(R.id.userImage1);
        userImage2 = findViewById(R.id.userImage2);
        logo = findViewById(R.id.logo);
        yoohoo_calling = findViewById(R.id.paynchat_calling);
        tintBlue = findViewById(R.id.tintBlue);
        localVideo = findViewById(R.id.localVideo);
        remoteVideo = findViewById(R.id.remoteVideo);
        switchVideo = findViewById(R.id.switchVideo);
        switchMic = findViewById(R.id.switchMic);
        switchVolume = findViewById(R.id.switchVolume);
        bottomButtons = findViewById(R.id.layout_btns);
        hangupButton = findViewById(R.id.hangupButton);

        hangupButton.setOnClickListener(v -> endCall());
        remoteVideo.setOnClickListener(v -> startAlphaAnimation());
        switchMic.setOnClickListener(view -> {
            isMute = !isMute;
            setMuteUnmute();
        });
        switchVolume.setOnClickListener(view -> {
            isSpeaker = !isSpeaker;
            enableSpeaker(isSpeaker);
        });
        switchVideo.setClickable(false);


        if (!user.isCounsellor()) {
            if (isVideo) {
                if (!IsVideoPurchased()) {
                    OpenPurchaseActivity(this);
                    finish();
                }
            } else {
                if (!IsAudioPurchased()) {
                    OpenPurchaseActivity(this);
                    finish();
                }
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        mDurationTask.cancel();
        mTimer.cancel();
        mSensorManager.unregisterListener(this);
        removeVideoViews();
    }

    private void finishAct() {
//        onPause();
//        Intent intent = new Intent(CallScreenActivity.this, MainActivity.class);
//        startActivity(intent);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
        updateUI();
    }

    @Override
    public void onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }

    private void endCall() {
        mAudioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(mCallId);

        user = helper.getLoggedInUser();
        if (call != null) {
            if (user == null) {
                user = new User(call.getRemoteUserId(), (user.getName()), getString(R.string.app_name), user.getImage(), user.getUserId());
            }
            call.hangup();
        }
        saveLog();
        finishAct();
    }

    private void saveLog() {
        if (!logSaved) {
            if (isCallStart) {
                DeductAmount(GlobalVariables.ADDTRANSCATION, mCallDurationSecond + "");
                isCallStart = false;
            }

            helper.saveCallLog(new LogCall(user, System.currentTimeMillis(), mCallDurationSecond, isVideo, inOrOut, userMe.getId()));
            logSaved = true;
        }
    }

    private void SaveLogServer() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + GlobalVariables.DEACTIVATESUBSCRIPTION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response=====j====>>>>>", response);
                try {
                    JSONObject json = new JSONObject(response);
                    String str_result = json.getString("result");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("TAG", "--Value::" + "error");
                }
            }
        }, error -> Log.d("error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("sub_id", PreferenceConnector.readString(mContext, PreferenceConnector.callingdata, "").split("#:#")[0]);
                params.put("sub_status", "Deactivate");
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

    private String formatTimespan(int totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            if (call.getDetails().getDuration() >= 5) {
                isCallStart = true;
            }
            if (call.getDetails().getDuration() >= maxCallSec) {
                endCall();
                Toast.makeText(mContext, "Coin expired. Purchase coin", Toast.LENGTH_LONG).show();
            } else {
                mCallDurationSecond = call.getDetails().getDuration();
                mCallDuration.setText(formatTimespan(call.getDetails().getDuration()));
            }


            if (mCallDurationSecond % 60 == 5) {
                DeductAmount(GlobalVariables.ADDTRANSCATIONEVERYSECOND, "1");
            }
        }
    }

    private boolean isCallAPi = true;
    private void DeductAmount(String POSTURL, String duration) {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isCallAPi = true;
            }
        }, 3000);

        if (isCallAPi) {
            isCallAPi = false;
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest request = new StringRequest(Request.Method.POST, GlobalVariables.PREURL + POSTURL, response -> Log.d("response=====api>>>>>", response + "......." + GlobalVariables.PREURL + POSTURL), error -> Log.d("error", error.toString() + GlobalVariables.PREURL + POSTURL)) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("user_id", PreferenceConnector.readString(mContext, PreferenceConnector.LOGINEDUSERID, ""));
                    params.put("duration", duration);
                    params.put("counselors", (CallScreenActivity.userCounsellor).getUserId());
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

    @Override
    void onSinchConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            if (!mAddedListener) {
                call.addCallListener(new SinchCallListener());
                mAddedListener = true;
            }
            mCallerName.setText(user != null ? user.getName() : call.getRemoteUserId());
            mCallState.setText(call.getState().toString());
            if (user != null) {
                Glide.with(this).load(user.getImage()).apply(new RequestOptions().placeholder(R.drawable.y_placeholder)).into(userImage1);
                Glide.with(this).load(user.getImage()).apply(RequestOptions.circleCropTransform().placeholder(R.drawable.y_placeholder)).into(userImage2);
            }
        } else {
            Log.e(TAG, "Started with invalid callId, aborting.");
            finishAct();
        }

        updateUI();
    }

    private void updateUI() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            mCallerName.setText(user != null ? user.getName() : call.getRemoteUserId());
            mCallState.setText(call.getState().toString());
            isVideo = call.getDetails().isVideoOffered();
            if (isVideo) {
                addLocalView();
                if (call.getState() == CallState.ESTABLISHED) {
                    addRemoteView();
                }
            }

            yoohoo_calling.setText(isVideo ? getString(R.string.video_calling) : getString(R.string.voice_calling));
            tintBlue.setVisibility(isVideo ? View.GONE : View.VISIBLE);
            localVideo.setVisibility(!isVideo ? View.GONE : View.VISIBLE);
        }
    }

    private void removeVideoViews() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            remoteVideo.removeView(vc.getRemoteView());

            localVideo.removeView(vc.getLocalView());
            mLocalVideoViewAdded = false;
            mRemoteVideoViewAdded = false;
        }
    }

    private void addLocalView() {
        if (mLocalVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            localVideo.addView(vc.getLocalView());
            switchVideo.setOnClickListener(v -> vc.toggleCaptureDevicePosition());
            mLocalVideoViewAdded = true;
        }
    }

    private void addRemoteView() {
        if (mRemoteVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            RelativeLayout view = (RelativeLayout) findViewById(R.id.remoteVideo);
            view.addView(vc.getRemoteView());
            mRemoteVideoViewAdded = true;
        }
    }

    private void startAlphaAnimation() {
        AlphaAnimation animation1 = new AlphaAnimation(alphaInvisible ? 0.0f : 1.0f, alphaInvisible ? 1.0f : 0.0f);
        animation1.setDuration(500);
        animation1.setStartOffset(25);
        animation1.setFillAfter(true);

        logo.startAnimation(animation1);
        yoohoo_calling.startAnimation(animation1);
        userImage2.startAnimation(animation1);
        mCallerName.startAnimation(animation1);
        mCallState.startAnimation(animation1);
        mCallDuration.startAnimation(animation1);
        bottomButtons.startAnimation(animation1);
        hangupButton.startAnimation(animation1);

        alphaInvisible = !alphaInvisible;
    }

    private void enableSpeaker(boolean enable) {
        AudioController audioController = getSinchServiceInterface().getAudioController();
        if (enable)
            audioController.enableSpeaker();
        else
            audioController.disableSpeaker();
        switchVolume.setImageDrawable(ContextCompat.getDrawable(this, isSpeaker ? R.drawable.ic_volume_on_white_24dp : R.drawable.ic_volume_off_white_24dp));
    }

    private void setMuteUnmute() {
        AudioController audioController = getSinchServiceInterface().getAudioController();
        if (isMute)
            audioController.mute();
        else
            audioController.unmute();
        switchMic.setImageDrawable(ContextCompat.getDrawable(this, isMute ? R.drawable.ic_mic_white_24dp : R.drawable.ic_mic_off_white_24dp));
    }

    @Override
    void onSinchDisconnected() {

    }

    private class SinchCallListener implements VideoCallListener {
        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = call.getDetails().toString();
            Log.d(TAG, "Call ended. Reason: " + endMsg);

            user = helper.getLoggedInUser();
            if (user == null) {
                user = new User(call.getRemoteUserId(), (user.getName()), getString(R.string.app_name), user.getImage(), user.getUserId());
            }
            endCall();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
            mAudioPlayer.stopProgressTone();
            mCallState.setText(call.getState().toString());
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            isSpeaker = call.getDetails().isVideoOffered();
            enableSpeaker(isSpeaker);
            switchVideo.setClickable(call.getDetails().isVideoOffered());
            switchVideo.setAlpha(call.getDetails().isVideoOffered() ? 1f : 0.4f);
            userImage1.setVisibility(isVideo ? View.GONE : View.VISIBLE);
            Log.d(TAG, "Call offered video: " + call.getDetails().isVideoOffered());
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
            mAudioPlayer.playProgressTone();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // Send a push through your push provider here, e.g. GCM
        }

        @Override
        public void onVideoTrackAdded(Call call) {
            Log.d(TAG, "Video track added");
            addRemoteView();
        }

        @Override
        public void onVideoTrackPaused(Call call) {

        }

        @Override
        public void onVideoTrackResumed(Call call) {

        }

    }

    public static Intent newIntent(Context context, User user, String callId, String inOrOut, int callSecond) {
        if (BaseActivity.IsPlanSubscribed(context)) {
            Intent intent = new Intent(context, CallScreenActivity.class);
            CallScreenActivity.userCounsellor = user;
            CallScreenActivity.userInOut = inOrOut;
            CallScreenActivity.userCallId = callId;
            CallScreenActivity.callSecond = callSecond;
            return intent;
        } else {
            Intent svIntent = new Intent(context, ViewPlansActivity.class);
            svIntent.putExtra("gotoform", "0");
            context.startActivity(svIntent);
            return null;
        }
    }
}
