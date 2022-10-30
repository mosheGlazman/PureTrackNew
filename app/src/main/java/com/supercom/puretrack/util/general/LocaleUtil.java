package com.supercom.puretrack.util.general;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.R;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.date.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

public class LocaleUtil {


    public interface Real_Languages {
        String ENGLISH = "en";
    }

    public interface Real_Countries {
        String USA = "us";
        String GB = "gb";
        String FRENCH = "fr";
        String RUSSIAN = "ru";
        String SWEDEN = "sv";
        String FINLAND = "fi";
        String ROMANIA = "ro";
    }

    public class LocalLanguage {

        public String language;
        public String country;

        public LocalLanguage(String language, String country) {
            this.language = language;
            this.country = country;
        }
    }


    private final Hashtable<String, LocalLanguage> countryConvertorLanguage = new Hashtable<String, LocalLanguage>() {

        private static final long serialVersionUID = 1L;

        {
            put("us", new LocalLanguage(Real_Languages.ENGLISH, Real_Countries.USA));
            put("cz", new LocalLanguage(Real_Languages.ENGLISH, Real_Countries.GB));
            put("dk", new LocalLanguage(Real_Countries.FRENCH, Real_Countries.FRENCH));
            put("ru", new LocalLanguage(Real_Countries.RUSSIAN, Real_Countries.RUSSIAN));
            put("sv", new LocalLanguage(Real_Countries.SWEDEN, Real_Countries.SWEDEN));
            put("fi", new LocalLanguage(Real_Countries.FINLAND, Real_Countries.FINLAND));
            put("ro", new LocalLanguage(Real_Countries.ROMANIA, Real_Countries.ROMANIA));

        }
    };

    public void initialize(String countryShortcut) {
        LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "localeUtils initialize" + "\n" + DatabaseAccess.getInstance().getDbStates().toString(), false);
        LocalLanguage localLanguageObject = getLocalLanguageObject(countryShortcut);
        updateResources(localLanguageObject.language, localLanguageObject.country);
    }

    private boolean updateResources(String language, String country) {
        Log.i("updateResources","pt country: "+country);
        Locale locale = new Locale(language, country);

        LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "localeUtils update, language " + language + ", country " + country + "\n" + DatabaseAccess.getInstance().getDbStates().toString(), false);

        Resources resources = App.getContext().getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        Log.i("updateResources","pt locale: "+locale);

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return true;
    }

    private LocalLanguage getLocalLanguageObject(String country) {
        return countryConvertorLanguage.get(country);
    }

    public void changeApplicationLanguageIfNeeded() {
        String[] countiesShortcutArray = App.getContext().getResources().getStringArray(R.array.counties_shortcut);
        List<String> countriesShorcutList = new ArrayList<>(Arrays.asList(countiesShortcutArray));
        String appLanguage = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.APP_LANGUAGE).toLowerCase();
        int countryIndex = countriesShorcutList.indexOf(appLanguage);
        if (countryIndex == -1) {
            countryIndex = 0;
        }
        initialize(countiesShortcutArray[countryIndex]);
    }

    public int getCurrentCountryPosition() {
        String appLanguague = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.APP_LANGUAGE).toLowerCase();
        String[] countriesShortcutArray = App.getContext().getResources().getStringArray(R.array.counties_shortcut);
        for (int i = 0; i < countriesShortcutArray.length; i++) {
            if (appLanguague.equals(countriesShortcutArray[i])) {
                return i;
            }
        }
        return 0;
    }


}