package com.supercom.puretrack.data.source.local.local_managers.business_logic;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.model.database.entities.EntityScheduleOfZones;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.general.NumberComputationUtil;
import com.supercom.puretrack.util.date.TimeUtil;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BiometricScheduleManager {

    private final String TAG = "BiometricManager";
    private final Handler biometricScheduleHandler;
    private final BiometricScheduleManagerListener biometricScheduleManagerListener;

    public BiometricScheduleManager(BiometricScheduleManagerListener biometricScheduleManagerListener) {
        this.biometricScheduleHandler = new Handler();
        this.biometricScheduleManagerListener = biometricScheduleManagerListener;
    }

    public interface BiometricScheduleManagerListener {
        void createBiometricDialog();
    }

    public void handleBiometricScheudleIfExists() {

        int biometricTestsCounter = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                (OFFENDER_STATUS_CONS.OFF_SCHEDULE_OF_ZONES_BIOMETRIC_TESTS_COUNTER);

        long minBetweenScheduleIntervalFromServer =
                TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BIOMETRIC_MIN_BETWEEN);

        long lastTimeUserDidBiometricSchedule = TableOffenderStatusManager.sharedInstance().
                getLongValueByColumnName(OFFENDER_STATUS_CONS.OFF_SCHEDULE_OF_ZONES_BIOMETRIC_LAST_CHECK);

        EntityScheduleOfZones currentBiometricSchedule = DatabaseAccess.getInstance().tableScheduleOfZones.GetCurrentBiometricSchedule();

        biometricScheduleHandler.removeCallbacks(biometricScheduleRunnable);

        //Check for biometric scheudle in present appointment
        if (currentBiometricSchedule != null && biometricTestsCounter < currentBiometricSchedule.amountOfBiometricTests
                && currentBiometricSchedule.amountOfBiometricTests > 0) {

            boolean didCreatePresentBiometricScheudle = didCreatePresentBiometricScheudle(biometricTestsCounter, minBetweenScheduleIntervalFromServer, lastTimeUserDidBiometricSchedule,
                    currentBiometricSchedule);

            if (!didCreatePresentBiometricScheudle) {
                createFutureBiometricScheduleIfNeeded(minBetweenScheduleIntervalFromServer, lastTimeUserDidBiometricSchedule);
            }
        }
        //Check for biometric scheudle in future appointment
        else {
            createFutureBiometricScheduleIfNeeded(minBetweenScheduleIntervalFromServer, lastTimeUserDidBiometricSchedule);
        }

    }

    private boolean didCreatePresentBiometricScheudle(int biometricTestsCounter, long minBetweenScheduleIntervalFromServer, long lastTimeUserDidBiometricSchedule,
                                                      EntityScheduleOfZones currentBiometricSchedule) {
        int startOfBiometricScheduleInterval;
        int endOfBiometricScheduleInterval;
        int timeToActuallyWaitBetweenBiometricSchedule = 0;

        /* F.E: Biometric appointment 10:00 - 10:30 ,  amount of biometric test : 3 , LastBiometricTest 9:58, biometricTestCounter: 0 ,
         * Min between Biometric test : 5 minutes
         * The next interval will be (10:03 - 10:30) / 3 = 9 minutes
         * Therefor the next interval will be between 10:03 - 10:12
         * startOfScheduleInterval - 10:03 , endOfScheduleInterval - 10:12 */
        if (((System.currentTimeMillis() - lastTimeUserDidBiometricSchedule) / 1000) < minBetweenScheduleIntervalFromServer) {
            timeToActuallyWaitBetweenBiometricSchedule = (int)
                    ((minBetweenScheduleIntervalFromServer) - ((System.currentTimeMillis() - lastTimeUserDidBiometricSchedule) / 1000));
        }

        /* F.E: Biometric appointment 10:00 - 10:30 ,  amount of biometric test : 3 , LastBiometricTest 10:27, biometricTestCounter: 2 ,
         * Min between Biometric test : 5 minutes
         * Then we will not have enough time to do the next biometric test in this appointment, since the next check should start at least
         * at 10:32 */
        boolean needToCreateNextBiometricSchedule = (((int) (System.currentTimeMillis() / 1000) + timeToActuallyWaitBetweenBiometricSchedule)
                < currentBiometricSchedule.EndTime / 1000);
        if (needToCreateNextBiometricSchedule) {

            startOfBiometricScheduleInterval = (int) (System.currentTimeMillis() / 1000) + timeToActuallyWaitBetweenBiometricSchedule;
            endOfBiometricScheduleInterval = (int) (startOfBiometricScheduleInterval + (((currentBiometricSchedule.EndTime / 1000) - startOfBiometricScheduleInterval) /
                    (currentBiometricSchedule.amountOfBiometricTests - biometricTestsCounter)));

            Log.i(TAG, "On present appointment");
            LoggingUtil.fileLogZonesUpdate("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] \n" + "On present appointment");

            String messageToUpload = "On present appointment";
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Zones.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

            createBiometricSchedule(startOfBiometricScheduleInterval, endOfBiometricScheduleInterval);

            return true;
        }

        return false;
    }

    private void createFutureBiometricScheduleIfNeeded(long minBetweenScheduleIntervalFromServer, long lastTimeUserDidBiometricSchedule) {
        int startOfBiometricScheduleInterval;
        int endOfBiometricScheduleInterval;
        int biometricTestsCounter;
        int timeToActuallyWaitBetweenBiometricSchedule;
        TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_SCHEDULE_OF_ZONES_BIOMETRIC_TESTS_COUNTER, 0);

        //next scheudle
        EntityScheduleOfZones nextBiometricSchedule = DatabaseAccess.getInstance().tableScheduleOfZones.GetNextBiometricAppointment();
        if (nextBiometricSchedule != null && nextBiometricSchedule.amountOfBiometricTests > 0) {

            biometricTestsCounter = 0;
            timeToActuallyWaitBetweenBiometricSchedule = 0;

            /* F.E: Biometric appointment 10:00 - 10:30 ,  amount of biometric test : 3 , LastBiometricTest 9:58, biometricTestCounter: 0 ,
             * Min between Biometric test : 5 minutes
             * The next interval will be (10:03 - 10:30) / 3 = 9 minutes
             * Therefor the next interval will be between 10:03 - 10:12
             * startOfScheduleInterval - 10:03 , endOfScheduleInterval - 10:12 */
            if (nextBiometricSchedule.StartTime - lastTimeUserDidBiometricSchedule < (minBetweenScheduleIntervalFromServer * 1000)) {
                timeToActuallyWaitBetweenBiometricSchedule = (int)
                        ((minBetweenScheduleIntervalFromServer) - ((nextBiometricSchedule.StartTime - lastTimeUserDidBiometricSchedule) / 1000));
            }

            startOfBiometricScheduleInterval = (int) (nextBiometricSchedule.StartTime / 1000) + timeToActuallyWaitBetweenBiometricSchedule;
            endOfBiometricScheduleInterval = (int) (startOfBiometricScheduleInterval + (((nextBiometricSchedule.EndTime - nextBiometricSchedule.StartTime) / 1000) /
                    (nextBiometricSchedule.amountOfBiometricTests - biometricTestsCounter)));

            Log.i(TAG, "On future appointment");
            LoggingUtil.fileLogZonesUpdate("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] \n" + "On future appointment");

            String messageToUpload = "On future appointment";
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Zones.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

            createBiometricSchedule(startOfBiometricScheduleInterval, endOfBiometricScheduleInterval);
        } else {
            Log.i(TAG, "No Biometric Scheudle");
            LoggingUtil.fileLogZonesUpdate("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] \n" + "No Biometric Scheudle");

            String messageToUpload = "No Biometric Scheudle";
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Zones.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
        }
    }

    private void createBiometricSchedule(int startOfBiometricScheduleInterval, int endOfBiometricScheduleInterval) {
        Random random = new Random();

        int nextBiometricScheduleInervalToWakeUp = NumberComputationUtil.createRandomInteger(startOfBiometricScheduleInterval, endOfBiometricScheduleInterval, random);

        long timeForNextBiometricScheduleToWakeUp = (((long) nextBiometricScheduleInervalToWakeUp * 1000) - System.currentTimeMillis()) + SystemClock.uptimeMillis();
        biometricScheduleHandler.postAtTime(biometricScheduleRunnable, 0, timeForNextBiometricScheduleToWakeUp);

        Log.i(TAG, "  Next Biometric Scheudle : " + TimeUtil.GetTimeString(((long) nextBiometricScheduleInervalToWakeUp * 1000), TimeUtil.SIMPLE));
        LoggingUtil.fileLogZonesUpdate("\n  Next Biometric Scheudle : " + TimeUtil.GetTimeString(((long) nextBiometricScheduleInervalToWakeUp * 1000), TimeUtil.SIMPLE));

        String messageToUpload = "Next Biometric Scheudle : " + TimeUtil.GetTimeString(((long) nextBiometricScheduleInervalToWakeUp * 1000), TimeUtil.SIMPLE);
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                DebugInfoModuleId.Zones.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
    }

    private final Runnable biometricScheduleRunnable = new Runnable() {

        @Override
        public void run() {

            biometricScheduleManagerListener.createBiometricDialog();

            int biometricTestsCounter = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                    (OFFENDER_STATUS_CONS.OFF_SCHEDULE_OF_ZONES_BIOMETRIC_TESTS_COUNTER);
            biometricTestsCounter = biometricTestsCounter + 1;
            TableOffenderStatusManager.sharedInstance().updateColumnInt
                    (OFFENDER_STATUS_CONS.OFF_SCHEDULE_OF_ZONES_BIOMETRIC_TESTS_COUNTER, biometricTestsCounter);

            TableOffenderStatusManager.sharedInstance().updateColumnLong
                    (OFFENDER_STATUS_CONS.OFF_SCHEDULE_OF_ZONES_BIOMETRIC_LAST_CHECK, System.currentTimeMillis());

            handleBiometricScheudleIfExists();
        }
    };
}
