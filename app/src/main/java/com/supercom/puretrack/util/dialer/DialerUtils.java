package com.supercom.puretrack.util.dialer;

import android.content.Intent;

import com.supercom.puretrack.util.application.App;

public class DialerUtils {

    public static void startSuperComDialer(String language, boolean changeLanguage) {
        if (language.equals("us")) language = "en";

        Intent intent = App.getContext().getPackageManager().getLaunchIntentForPackage("supercom.dialer");
        if (intent == null) {
            return;
        }
        if (changeLanguage)
            intent.putExtra("language", language);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        App.getAppContext().startActivity(intent);
    }
}
