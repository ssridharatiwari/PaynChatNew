package com.startup.paynchat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.asksira.webviewsuite.WebViewSuite;
import com.startup.paynchat.utils.PreferenceConnector;

public class WebViewActivity extends AppCompatActivity implements View.OnClickListener{
    private WebViewSuite webViewSuite;
    private ImageView imgBack;
    private TextView txtTitle;
    private Context svContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_webview);
        svContext = this;

        webViewSuite    = findViewById(R.id.webViewSuite);
        imgBack         = (ImageView) findViewById(R.id.img_back);
        txtTitle        = (TextView) findViewById(R.id.heading);
        txtTitle.setText(PreferenceConnector.readString(this, PreferenceConnector.WEBHEADING, ""));

        imgBack.setOnClickListener(this);

        ProgressBar pDialog = new ProgressBar(this);

        Log.e("---code-url---", PreferenceConnector.readString(this, PreferenceConnector.WEBURL, ""));
        webViewSuite.startLoading(PreferenceConnector.readString(this, PreferenceConnector.WEBURL, ""));
        webViewSuite.setCustomProgressBar(pDialog);

        webViewSuite.setOpenPDFCallback(new WebViewSuite.WebViewOpenPDFCallback() {
            @Override
            public void onOpenPDF() {
                finish();
            }
        });

        webViewSuite.customizeClient(new WebViewSuite.WebViewSuiteCallback() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //Do your own stuffs. These will be executed after default onPageStarted().
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //Do your own stuffs. These will be executed after default onPageFinished().
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //Override those URLs you need and return true.
                //Return false if you don't need to override that URL.
                if (url.contains("wa.me")) {
                    openWhatsApp();
                    onBackPressed();
                }else if (url.contains("upi://pay?")) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                    finish();
                }else {
                    view.loadUrl(url);
                }
                return false;
            }
        });
    }

    private void openWhatsApp() {
        String smsNumber = "910000000000"; // E164 format without '+' sign
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "I want to know about your services.");
        sendIntent.putExtra("jid", smsNumber + "@s.whatsapp.net"); //phone number without "+" prefix
        sendIntent.setPackage("com.whatsapp");
        if (sendIntent.resolveActivity(getPackageManager()) == null) {
//            new ShowCustomToast(WebViewActivity.this).showToast("Whatsapp have not been installed.", WebViewActivity.this);
            return;
        }
        startActivity(sendIntent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
        }
    }

    private void onRefreshWebView() {
        webViewSuite.refresh();
    }

    @Override
    public void onBackPressed() {
        if (!webViewSuite.goBackIfPossible()) super.onBackPressed();
    }
}
