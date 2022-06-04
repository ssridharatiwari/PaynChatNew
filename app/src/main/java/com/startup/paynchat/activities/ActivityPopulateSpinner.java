package com.startup.paynchat.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.startup.paynchat.R;
import com.startup.paynchat.adapters.PopulateSpinnerAdapter;
import com.startup.paynchat.models.SpinnerModel;

import java.util.List;

public class ActivityPopulateSpinner extends AppCompatActivity implements View.OnClickListener,
        PopulateSpinnerAdapter.OnItemClickListener {
    private final View[] allViewWithClick = {};
    private final int[] allViewWithClickId = {0};

    private final EditText[] edTexts = {};
    private final String[] edTextsError = {};
    private final int[] editTextsClickId = {0};

    private ProgressBar progressbarLoad;
    private PopulateSpinnerAdapter mAdapter;
    public static final String TAG_DATA = "data";
    public static final String TAG_MESSAGE = "message";
    public static final String TAG_STATUS = "status";

    public static String spinnerTitle = "";
    public static SpinnerModel selectedSpinnerModel = null;
    public static int itemPosition = -1;
    public static List<SpinnerModel> itemList;
    public static String defaultSpinnerTitle = "Choose your role";

    public static void showSpinnerData(Context context, List<SpinnerModel> listSpinner) {
        showSpinnerData(context, listSpinner, defaultSpinnerTitle);
    }

    public static void showSpinnerData(Context context, List<SpinnerModel> listSpinner, String spinnerTitle) {
        ActivityPopulateSpinner.itemList = listSpinner;
        ActivityPopulateSpinner.spinnerTitle = spinnerTitle;
        Intent intent = new Intent(context, ActivityPopulateSpinner.class);
        context.startActivity(intent);
    }

    private static SpinnerModel getSpinnerSelection() {
        if (ActivityPopulateSpinner.selectedSpinnerModel == null) {
            return null;
        } else {
            SpinnerModel selectedSpinner = ActivityPopulateSpinner.selectedSpinnerModel;
            ActivityPopulateSpinner.selectedSpinnerModel = null;
            return selectedSpinner;
        }
    }

    public static SpinnerModel setSpinnerTitle(TextView textView, String defValue) {
        SpinnerModel selectedStateSpinner = getSpinnerSelection();
        if (selectedStateSpinner == null) {
            textView.setText(defValue);
        } else {
            textView.setText(selectedStateSpinner.getTitle());
        }
        return selectedStateSpinner;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_populatespinner);
        StartApp();

        OnClickCombineDeclare(allViewWithClick);
        EditTextDeclare(edTexts);

        resumeApp();
    }

    private String firstLetterInUpperCase(String str) {
        try {
            String strFinal = "";
            String[] lstStr = str.split(" ");
            for (String string : lstStr) {
                String firstLetStr = string.substring(0, 1).toUpperCase();
                String remLetStr = string.substring(1).toLowerCase();
                strFinal += firstLetStr + remLetStr + " ";
            }
            return strFinal;
        } catch (Exception ignored) {
            return str;
        }
    }

    public void resumeApp() {
        ((ImageView)findViewById(R.id.back_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.searchview);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        mAdapter = new PopulateSpinnerAdapter(svContext, itemList, this, false, false);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        progressbarLoad.setVisibility(View.GONE);

        if (itemList.size() == 0) {
            Toast.makeText(svContext, "No items to show", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }


    private void EditTextDeclare(EditText[] editTexts) {
        for (int j = 0; j < editTexts.length; j++) {
            editTexts[j] = findViewById(editTextsClickId[j]);
        }
    }

    private void OnClickCombineDeclare(View[] allViewWithClick) {
        for (int j = 0; j < allViewWithClick.length; j++) {
            allViewWithClick[j] = findViewById(allViewWithClickId[j]);
            allViewWithClick[j].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
//                        case R.id.btn_finish:
//                            CheckData();
//                            break;
//                        case R.id.btn_backform:
//                            ShowBackCardView();
//                            break;
                    }
                }
            });
        }
//        btnBack = (Button) allViewWithClick[0];
    }


    private Context svContext;
    private void StartApp() {
        svContext = this;
        ViewGroup root = (ViewGroup) findViewById(R.id.headlayout);
        hideKeyboard();

        progressbarLoad = (ProgressBar) findViewById(R.id.progressbar_load);
        progressbarLoad.setVisibility(View.GONE);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        // check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void hideFragmentkeyboard(Context meraContext, View meraView) {
        final InputMethodManager imm = (InputMethodManager) meraContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(meraView.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onItemClick(View view, SpinnerModel selectedSpinnerModel, int position) {
        ActivityPopulateSpinner.itemPosition = position;
        ActivityPopulateSpinner.selectedSpinnerModel = selectedSpinnerModel;
        finish();
    }

}