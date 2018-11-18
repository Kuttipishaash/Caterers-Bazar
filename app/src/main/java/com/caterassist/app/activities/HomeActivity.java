package com.caterassist.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.fragments.BottomNavigationDrawerFragment;
import com.caterassist.app.fragments.CatererDashboardFragment;
import com.caterassist.app.fragments.VendorDashboardFragments;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.utils.AppUtils;
import com.caterassist.app.utils.Constants;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import es.dmoral.toasty.Toasty;

public class HomeActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG = "HomeActivity";
    private static final int CALL_PERMISSION_REQ_CODE = 101;
    BottomAppBar bottomAppBar;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private UserDetails userDetails;
    private String currentUserID;
    private SharedPreferences sharedPreferences;

    private FloatingActionButton searchFAB;
    private boolean isFABVisible;
    private EditText vendorSearchEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUserID = checkLogin();
        setContentView(R.layout.activity_home);
        initViews();
        getPermissions();


    }

    private void getPermissions() {
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            }
        }
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQ_CODE);

    }

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CALL_PERMISSION_REQ_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doIfPermissionGranted();
                } else {
                    Toasty.warning(this, "You need to provide call permissions to use all the features of this app.", Toast.LENGTH_SHORT).show();
                    AppUtils.cleanUpAndLogout(this);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void doIfPermissionGranted() {
        sharedPreferences = this.getSharedPreferences(Constants.SharedPref.PREF_FILE, MODE_PRIVATE);
        userDetails = new UserDetails();
        userDetails.setUserID(sharedPreferences.getString(Constants.SharedPref.USER_ID, ""));
        userDetails.setUserEmail(sharedPreferences.getString(Constants.SharedPref.USER_EMAIL, ""));
        userDetails.setIsVendor(sharedPreferences.getBoolean(Constants.SharedPref.USER_IS_VENDOR, false));
        userDetails.setUserName(sharedPreferences.getString(Constants.SharedPref.USER_NAME, ""));
        userDetails.setUserLat(sharedPreferences.getFloat(Constants.SharedPref.USER_LAT, 0.0f));
        userDetails.setUserLng(sharedPreferences.getFloat(Constants.SharedPref.USER_LNG, 0.0f));
        userDetails.setUserImageUrl(sharedPreferences.getString(Constants.SharedPref.USER_IMG_URL, ""));
        setupBottomAppBar();
        if (userDetails.getIsVendor()) {
            loadVendorViews();
        } else {
            loadCatererViews();
        }
        searchFAB.setOnClickListener(this);
    }

    private void setupBottomAppBar() {
        if (userDetails.getIsVendor()) {
            bottomAppBar.replaceMenu(R.menu.bottom_bar_overflow_menu_vendor);
        } else {
            bottomAppBar.replaceMenu(R.menu.bottom_bar_overflow_menu_caterer);
        }
        bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialogFragment bottomSheetDialogFragment = new BottomNavigationDrawerFragment();
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });
        bottomAppBar.setOnMenuItemClickListener(new androidx.appcompat.widget.Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.btm_sheet_option_cart:
                        startActivity(new Intent(HomeActivity.this, CartActivity.class));
                        break;
                }
                return true;
            }
        });
    }


    private void loadCatererViews() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.act_home_fragment, new CatererDashboardFragment());
        fragmentTransaction.commit();
    }


    private void loadVendorViews() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.act_home_fragment, new VendorDashboardFragments());
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        if (userDetails != null) {
            if (!userDetails.getIsVendor()) {
                hideSearchBar();
            }
        }
        super.onResume();
    }

    private void hideSearchBar() {
        vendorSearchEditText.setVisibility(View.GONE);
        searchFAB.show();
        isFABVisible = true;
    }

    private void showSearchBar() {
        searchFAB.hide();
        vendorSearchEditText.setVisibility(View.VISIBLE);
        isFABVisible = false;
    }

    private String checkLogin() {
        if (firebaseAuth.getCurrentUser() == null) {
            AppUtils.cleanUpAndLogout(this);
            return "";
        } else {
            return firebaseAuth.getUid();
        }
    }

    private void initViews() {
        searchFAB = findViewById(R.id.act_home_fab_search_vendor);
        vendorSearchEditText = findViewById(R.id.act_home_edt_txt_vendor_search);
        bottomAppBar = findViewById(R.id.bottom_app_bar);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.act_home_fab_search_vendor:
                if (userDetails.getIsVendor()) {
                    //TODO: Vendor fab
                } else {
                    showSearchBar();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (!userDetails.getIsVendor() && !isFABVisible) {
            hideSearchBar();
        } else {
            super.onBackPressed();
        }
    }
}
