package com.startup.paynchat.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.startup.paynchat.R;
import com.startup.paynchat.adapters.AppIntroPagerAdapter;
import com.startup.paynchat.utils.PreferenceConnector;
import java.util.Objects;

public class ActivityIntro extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    private ViewPager mViewPager;
    private AppIntroPagerAdapter mAdapter;
    private LinearLayout viewPagerCountDots;
    private int dotsCount;
    private ImageView[] dots;
    private Context svContext;
    private Button buttonSign;
    private int[] mResources = {R.drawable.slide_one, R.drawable.slide_two, R.drawable.slide_three};
    private ViewGroup root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fullscreen(ActivityIntro.this);
        setContentView(R.layout.act_intro);
        svContext = ActivityIntro.this;
        root = (ViewGroup) findViewById(R.id.activity_foster);

        buttonSign.setOnClickListener(view -> ShowLoginScreen());

        removeNotif();
        mViewPager = findViewById(R.id.viewpager);
        viewPagerCountDots = findViewById(R.id.viewPagerCountDots);
        mAdapter = new AppIntroPagerAdapter(ActivityIntro.this, svContext, mResources);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOnPageChangeListener(this);
        setPageViewIndicator();

        PreferenceConnector.writeBoolean(svContext, PreferenceConnector.ISINTROSHOWED, true);
    }

    public static void Fullscreen(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void ShowLoginScreen() {
        Intent svIntent = new Intent(svContext, SignInActivity.class);
        startActivity(svIntent);
        finish();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setPageViewIndicator() {
        dotsCount = mAdapter.getCount();
        dots = new ImageView[dotsCount];

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(svContext);
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    25,
                    25
            );

            params.setMargins(4, 20, 4, 0);
            final int presentPosition = i;
            dots[presentPosition].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mViewPager.setCurrentItem(presentPosition);
                    return true;
                }
            });
            viewPagerCountDots.addView(dots[i], params);
        }
        dots[0].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.e("###onPageSelected, pos ", String.valueOf(position));
        for (int i = 0; i < dotsCount; i++) {
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));
        }
        dots[position].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public void scrollPage(int position) {
        mViewPager.setCurrentItem(position);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void removeNotif() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Objects.requireNonNull(notificationManager).cancel(0);
    }

}
