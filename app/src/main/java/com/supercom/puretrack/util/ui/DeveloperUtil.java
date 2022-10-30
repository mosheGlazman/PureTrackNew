package com.supercom.puretrack.util.ui;

import static com.supercom.puretrack.data.source.local.table.TableOffenderDetails.LAUNCHER_CONFIG_SETTINGS_PASSWORD;

import android.widget.Toast;

import com.supercom.puretrack.data.BuildConfig;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.util.application.App;

/**
 * Util class that gets data only for 'debug' build type for faster developing.
 */
public class DeveloperUtil {

    public static void showDeveloperToastMessage(String message) {
        if (!BuildConfig.BUILD_TYPE.equals("debug")) return;
        Toast.makeText(App.getContext(), "Configuration parsing error - " + message, Toast.LENGTH_LONG).show();
    }

    public static String getSettingsDialogPassword(){
        if (BuildConfig.BUILD_TYPE == "debug") {
            return TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(LAUNCHER_CONFIG_SETTINGS_PASSWORD);
        }
        return "";
    }
}