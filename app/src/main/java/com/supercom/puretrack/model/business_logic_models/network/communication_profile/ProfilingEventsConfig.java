package com.supercom.puretrack.model.business_logic_models.network.communication_profile;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.model.database.entities.EntityEventConfig;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfilingEventsConfig {


    private ArrayList<PmComProfiles> pmComProfilesArrayList;
    private ArrayList<ProfileEvents> profileEventsArrayList;

    public class PmComProfiles {
        public int ID;
        public int CommInterval;
        public int LocationInterval;
        public int MinDuration;
        public int MaxDuration;
    }

    public static class ProfileEvents {
        public int EventID;
        public int ProfileID;
        public int Restrictions;
    }

    public ProfilingEventsConfig() {
        updatePmComProfilesArrayListAfterNewConfiguration();
        updateProfileEventsArrayListAfterNewConfiguration();
    }

    public PmComProfiles getPmComObjectByType(int type) {

        ProfileEvents profile = getProfileEvent(type);
        if (profile != null) {
            for (PmComProfiles pmComProfiles : pmComProfilesArrayList) {
                if (profile.ProfileID == pmComProfiles.ID) {
                    return pmComProfiles;
                }
            }
        }

        return null;
    }

    public PmComProfiles getPmComProfileObjectByProfileId(int profileId) {

        for (PmComProfiles pmComProfiles : pmComProfilesArrayList) {
            if (profileId == pmComProfiles.ID) {
                return pmComProfiles;
            }
        }

        return null;
    }

    public ProfileEvents getProfileEvent(int type) {

        for (ProfileEvents profileEvents : profileEventsArrayList) {
            if (profileEvents.EventID == type) {
                return profileEvents;
            }
        }

        return null;
    }

    public void updatePmComProfilesArrayListAfterNewConfiguration() {
        pmComProfilesArrayList = prasePmComProfile();
    }

    public void updateProfileEventsArrayListAfterNewConfiguration() {
        profileEventsArrayList = parseProfileEvents();
    }

    private ArrayList<PmComProfiles> prasePmComProfile() {
        String pmComProfilesJson = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.PM_COM_PROFILE);

        ArrayList<PmComProfiles> prfileEventsArr = new Gson().fromJson(pmComProfilesJson, new TypeToken<ArrayList<PmComProfiles>>() {
        }.getType());

        return prfileEventsArr;
    }

    private ArrayList<ProfileEvents> parseProfileEvents() {
        ArrayList<ProfileEvents> profileEventsArray = new ArrayList<ProfileEvents>();
        String profileEventsJson = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.PROFILE_EVENTS);
        try {
            JSONArray profileEventsJsonArray = new JSONArray(profileEventsJson);
            HashMap<Integer, Integer> tempProfileEvents = new HashMap<Integer, Integer>();
            for (int m = 0; m < profileEventsJsonArray.length(); m++) {

                if (profileEventsJsonArray.getJSONObject(m).names() != null) {
                    int eventIdFromServer = profileEventsJsonArray.getJSONObject(m).getInt("EventID");
                    Integer tempProfileIdValue = tempProfileEvents.get(eventIdFromServer);

                    int profileIdFromServer = profileEventsJsonArray.getJSONObject(m).getInt("ProfileID");
					
					/* We want to save only record with the lowest profileID. F.E: if we get event 1020 twice with two profileID: 0 and 1,
					then we will take the lowest which is 0 in our case */
                    if (tempProfileIdValue == null || (tempProfileIdValue != null && profileIdFromServer < tempProfileIdValue.intValue())) {
                        tempProfileEvents.put(eventIdFromServer, profileIdFromServer);
                        EntityEventConfig recordEventConfig = DatabaseAccess.getInstance().tableEventConfig.getRecordByEventType(eventIdFromServer);
                        if (recordEventConfig != null) {

                            int restrictions = profileEventsJsonArray.getJSONObject(m).getInt("Restrictions");

                            ProfileEvents profileEvents = new ProfileEvents();
                            profileEvents.EventID = eventIdFromServer;
                            profileEvents.ProfileID = profileIdFromServer;
                            profileEvents.Restrictions = restrictions;
                            profileEventsArray.add(profileEvents);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return profileEventsArray;
    }


}
