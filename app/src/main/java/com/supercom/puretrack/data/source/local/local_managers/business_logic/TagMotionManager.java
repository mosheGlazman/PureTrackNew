package com.supercom.puretrack.data.source.local.local_managers.business_logic;

import android.util.Log;

import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.entities.EntityTagMotion;

public class TagMotionManager {

    public interface TagMotionListener {
        void clearData();
    }

    public void handleTagMotionsReceptions(int totalTagReceptions, int totalTagMotionReceptions, int totalTagNoMotionReceptions, TagMotionListener listener) {

        EntityTagMotion tagMotionEntity = DatabaseAccess.getInstance().tableTagMotion.getTagMotionEntity();
        boolean isTagInMotion = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(TableOffenderStatusManager.OFFENDER_STATUS_CONS.TAG_MOTION) > 0;
        Log.d("TagMotionManager", "handle: total - " + totalTagReceptions + " motion - " + totalTagMotionReceptions + " no motion - " + totalTagNoMotionReceptions);
        if (isTagInMotion) {
            Log.d("TagMotionManager", "totalTagReceptions - " + totalTagReceptions + " signalsToNoMotion - " + tagMotionEntity.signalsToNoMotion);
            if (totalTagReceptions < tagMotionEntity.signalsToNoMotion) return;
            int tagNoMotionPercentage = (int) (((float) totalTagNoMotionReceptions / (float) totalTagReceptions) * 100);
            Log.d("TagMotionManager", "tagNoMotionPercentage - " + tagNoMotionPercentage);
            if (tagNoMotionPercentage >= tagMotionEntity.noMotionPercentage) {
                //event - tag no motion
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.tagNoMotion, -1, -1);
                TableOffenderStatusManager.sharedInstance().updateColumnInt(TableOffenderStatusManager.OFFENDER_STATUS_CONS.TAG_MOTION, 0);
                Log.d("TagMotionManager", "added NO MOTION event");
            }
            listener.clearData();
            return;
        }
        Log.d("TagMotionManager", "totalTagReceptions - " + totalTagReceptions + " signalsToMotion - " + tagMotionEntity.signalsToMotion);
        if (totalTagReceptions < tagMotionEntity.signalsToMotion) return;
        int tagMotionPercentage = (int) (((float)totalTagMotionReceptions / (float) totalTagReceptions) * 100);
        Log.d("TagMotionManager", "tagNoMotionPercentage - " + tagMotionPercentage);
        if (tagMotionPercentage >= tagMotionEntity.motionPercentage) {
            //event - tag motion
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.tagMotion, -1, -1);
            TableOffenderStatusManager.sharedInstance().updateColumnInt(TableOffenderStatusManager.OFFENDER_STATUS_CONS.TAG_MOTION, 1);
            Log.d("TagMotionManager", "added MOTION event");
        }
        listener.clearData();
    }
}
