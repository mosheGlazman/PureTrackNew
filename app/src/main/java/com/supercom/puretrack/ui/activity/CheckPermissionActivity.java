package com.supercom.puretrack.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;

import com.supercom.puretrack.data.R;

public class CheckPermissionActivity extends Activity {
    final  int allPermissionArrayRequestCode=12349;
    final  int backgroundLocationPermissionArrayRequestCode=12359;

   static String[] allPermissionArray = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_PHONE_NUMBERS
    };
    static  String[] backgroundLocationPermissionArray = new String[]{
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_permission_activity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        managePermission();
    }

    private void managePermission() {
        if(!hasPermission(getApplicationContext(), allPermissionArray)){
            ActivityCompat.requestPermissions(this,
                    allPermissionArray,
                    allPermissionArrayRequestCode);
            return;
        }

        if(!hasBackgroundLocation(getApplicationContext())){
            ActivityCompat.requestPermissions(this,
                    backgroundLocationPermissionArray,
                    backgroundLocationPermissionArrayRequestCode);
            return;
        }

        finish();
    }

    private static boolean hasBackgroundLocation(Context context) {
        if (Build.VERSION.SDK_INT < 29) {
            return true;
        }

        int i = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        return i == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    public static void startIfRequired(Context context){
        if(hasPermission(context, allPermissionArray) && hasBackgroundLocation(context)){
            return;
        }

        Intent intent = new Intent(context,CheckPermissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static boolean hasPermission(Context context,String[] permissionArray){
       for(String p : permissionArray){
           if(ActivityCompat.checkSelfPermission(context, p) !=  PackageManager.PERMISSION_GRANTED ){
               return false;
           }
       }

       return true;
    }
}
