package com.supercom.puretrack.util.shared_preferences;

import static com.supercom.puretrack.util.constants.shared_prefrences.SharedPreferencesKeys.CASE_TAMPER_OPERATOR;
import static com.supercom.puretrack.util.constants.shared_prefrences.SharedPreferencesKeys.IS_KNOX_LICENCE_ACTIVATED;
import static com.supercom.puretrack.util.constants.shared_prefrences.SharedPreferencesKeys.PHONE_CALL_TIME;
import static com.supercom.puretrack.util.constants.shared_prefrences.SharedPreferencesKeys.SELECTED_CUSTOM_URL;
import static com.supercom.puretrack.util.constants.shared_prefrences.SharedPreferencesKeys.SELECTED_SERVER_URL_NAME;
import static com.supercom.puretrack.util.constants.shared_prefrences.SharedPreferencesKeys.SHOULD_DELETE_DB;
import static com.supercom.puretrack.util.constants.shared_prefrences.SharedPreferencesKeys.SHOULD_WRITE_TO_FILE;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.supercom.puretrack.model.business_logic_models.enums.CaseTamperOperator;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.constants.network.ServerUrls;

public class PureTrackSharedPreferences {

    public static SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
    public static SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

    public static CaseTamperOperator getCaseTamperOperator() {
        String operatorValue = sharedPreferences.getString(CASE_TAMPER_OPERATOR, CaseTamperOperator.GREATER_EQUALS.name());
        if (operatorValue.equals(CaseTamperOperator.GREATER_EQUALS.name()))
            return CaseTamperOperator.GREATER_EQUALS;
        else
            return CaseTamperOperator.LESSER_THEN;
    }

    public static void setCaseTamperOperator(CaseTamperOperator caseTamperOperator) {
        sharedPreferencesEditor.putString(CASE_TAMPER_OPERATOR, caseTamperOperator.name());
        sharedPreferencesEditor.apply();
    }

    public static String getSelectedServerUrlName() {
        return sharedPreferences.getString(SELECTED_SERVER_URL_NAME, ServerUrls.getInstance().getDefaultUrl());
    }

    public static void setSelectedServerUrlName(String selectedServerUrlName) {
        sharedPreferencesEditor.putString(SELECTED_SERVER_URL_NAME, selectedServerUrlName);
        sharedPreferencesEditor.apply();
    }

    public static String getCustomUrl() {
        return sharedPreferences.getString(SELECTED_CUSTOM_URL, "");
    }

    public static void setCustomUrl(String selectedServerUrlName) {
        sharedPreferencesEditor.putString(SELECTED_CUSTOM_URL, selectedServerUrlName);
        sharedPreferencesEditor.apply();
    }

    public static long getLastPhoneCallTime() {
        return sharedPreferences.getLong(PHONE_CALL_TIME, 0);
    }

    public static void setLastPhoneCallTime(long phoneCallTime) {
        sharedPreferencesEditor.putLong(PHONE_CALL_TIME, phoneCallTime);
        sharedPreferencesEditor.apply();
    }

    public static boolean getIsFirstTimeUsingApplication() {
        return sharedPreferences.getBoolean(SHOULD_DELETE_DB, true);
    }

    public static void setShouldDeleteDb(boolean shouldDeleteDB) {
        sharedPreferencesEditor.putBoolean(SHOULD_DELETE_DB, shouldDeleteDB);
        sharedPreferencesEditor.apply();
    }

    public static boolean getShouldWriteToFile() {
        return sharedPreferences.getBoolean(SHOULD_WRITE_TO_FILE, false);
    }

    public static void setShouldWriteToFile(boolean shouldWriteToFile) {
        sharedPreferencesEditor.putBoolean(SHOULD_WRITE_TO_FILE, shouldWriteToFile);
        sharedPreferencesEditor.apply();
    }

    public static boolean isKnoxLicenceActivated() {
        return sharedPreferences.getBoolean(IS_KNOX_LICENCE_ACTIVATED, false);
    }

    public static void setKnoxLicenceActivatedStatus(boolean knoxLicenceStatus) {
        sharedPreferencesEditor.putBoolean(IS_KNOX_LICENCE_ACTIVATED, knoxLicenceStatus);
        sharedPreferencesEditor.apply();
    }

    public static void setLastMagneticsValue(int value) {
        sharedPreferencesEditor.putInt("MagneticsValue", value).apply();
    }

    public static int getMagneticsValue() {
        return sharedPreferences.getInt("MagneticsValue", 1);
    }

    public static void setShowSensors(boolean value) {
        sharedPreferencesEditor.putInt("ShowSensors", value ? 1 : 0).apply();
    }

    public static boolean getShowSensors() {
        return sharedPreferences.getInt("ShowSensors", 0)==1;
    }
}
