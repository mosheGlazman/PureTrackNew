package com.supercom.puretrack.ui.enrollment.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.location.GpsSatellite;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.ui.enrollment.activity.EnrollmentActivity;
import com.supercom.puretrack.util.constants.Enrollment;
import com.supercom.puretrack.util.runnable.BaseFutureRunnable;
import com.supercom.puretrack.data.R;
import com.supercom.puretrack.data.source.local.local_managers.hardware.LocationManager;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.local.local_managers.hardware.AccelerometerManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.model.database.entities.EntityGpsPoint;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.DeviceStateManager;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.date.TimeUtil;

import java.util.concurrent.TimeUnit;

public class LocationVerificationFragment extends Fragment implements OnClickListener, LocationListener, Listener {
    public static final String TAG = "LocationVerificationFragment";

    private enum POINT_STATUS {
        NO_POINT,
        POINT_RECEIVED,
        GOOD_POINT_RECEIVED
    }

    private static final long MIN_DISTANCE = 0;
    private static final long MIN_TIME_TO_GET_LOCATION = 5;
    private static final long MAX_TIME_TO_SEARCH_LOCATION = 70;

    private View view;
    private LocationVerificationEnrollmentListener locationVerificationEnrollmentListener;
    private AnimationDrawable locationAnimation;
    private android.location.LocationManager enrollmentLocationManager;
    private final TestLocation testLocation = new TestLocation();
    private final Handler futureTasksHandler = new Handler();

    private int satellitesNumber;

    private long timeOfTakenLastSatellitesUpdate;
    private long timeOfLastGpsLocationUpdate;

    private POINT_STATUS gpsPointStatus = POINT_STATUS.NO_POINT;
    private POINT_STATUS networkPointStatus = POINT_STATUS.NO_POINT;
    private Location chosenGpsLocation = null;
    private Location chosenNetworkLocation = null;
    private int chosenSatellitesNumber;

    public static LocationVerificationFragment newInstance() {
        LocationVerificationFragment locationVerificationFragment = new LocationVerificationFragment();
        return locationVerificationFragment;
    }

    public interface LocationVerificationEnrollmentListener {
        void onFinishedToTestLocationVerification(boolean didSkipScreen);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        testLocation.scheduleFutureRun(futureTasksHandler, TimeUnit.SECONDS.toMillis(MAX_TIME_TO_SEARCH_LOCATION));
        startEnrollmentLocationUpdate();
    }

    @SuppressLint("MissingPermission")
    private void startEnrollmentLocationUpdate() {
        enrollmentLocationManager = (android.location.LocationManager) App.getContext().getSystemService(Context.LOCATION_SERVICE);

        String locationType = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.LOCATION_TYPES);
        enrollmentLocationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, TimeUnit.SECONDS.toMillis(MIN_TIME_TO_GET_LOCATION),
                TimeUnit.SECONDS.toMillis(MIN_DISTANCE), this);
        // check if "network location" should be requested
        if (locationType.equals("GPS")) {
            // in GPS only, ignore "network location" status (fake good point)
            networkPointStatus = POINT_STATUS.GOOD_POINT_RECEIVED;
        }
        enrollmentLocationManager.addGpsStatusListener(this);
    }

    public void stopEnrollmentLocationUpdate() {
        enrollmentLocationManager.removeUpdates(this);
        enrollmentLocationManager.removeGpsStatusListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof LocationVerificationEnrollmentListener) {
            locationVerificationEnrollmentListener = (LocationVerificationEnrollmentListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement LocationVerificationListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.enrollment_location_verification_main, container, false);

        String firstName = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_FIRST_NAME);
        String lastName = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_LAST_NAME);

        View topLayout = view.findViewById(R.id.top_layout);

        TextView firstTitleTextView = topLayout.findViewById(R.id.first_title);
        firstTitleTextView.setText(R.string.enrolment_text_location_title_type);

        TextView secondTitleTextView = topLayout.findViewById(R.id.second_title);
        secondTitleTextView.setText(String.format("%s %s", firstName, lastName));

        ImageButton locationRetryImageButton = view.findViewById(R.id.location_retry_button);
        locationAnimation = (AnimationDrawable) locationRetryImageButton.getDrawable();
        locationAnimation.start();

        Button leftButton = view.findViewById(R.id.left_button);
        leftButton.setOnClickListener(this);

        Button rightButton = view.findViewById(R.id.right_button);
        rightButton.setOnClickListener(this);
        rightButton.setTag(EnrollmentActivity.SKIPPED);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // back button
            case R.id.left_button:
                getActivity().onBackPressed();
                break;
            // skip/finished button
            case R.id.right_button:

                stopEnrollmentLocationUpdate();

                String messageToUpload = "";
                int status = (int) v.getTag();
                switch (status) {

                    case EnrollmentActivity.FINISHED:
                        messageToUpload = "Finished location enrollment";
                        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                                messageToUpload, DebugInfoModuleId.Network.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

                        locationVerificationEnrollmentListener.onFinishedToTestLocationVerification(false);

                        break;

                    case EnrollmentActivity.SKIPPED:
                        messageToUpload = "Skipped location enrollment";
                        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                                messageToUpload, DebugInfoModuleId.Network.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

                        locationVerificationEnrollmentListener.onFinishedToTestLocationVerification(true);

                        break;

                    default:
                        break;

                }

                break;

            case R.id.location_retry_button:
                handleRetryButton();
                break;

        }
    }

    private void handleRetryButton() {

        gpsPointStatus = POINT_STATUS.NO_POINT;
        networkPointStatus = POINT_STATUS.NO_POINT;
        LoggingUtil.updateNetworkLog("\nCalling 'start new cycle' - handleRetryButton\n", false);
        NetworkRepository.getInstance().startNewCycle();

        TextView locationTitleTextView = view.findViewById(R.id.location_title_text);
        locationTitleTextView.setTextColor(getResources().getColor(R.color.grey_darker));
        locationTitleTextView.setText(R.string.enrolment_text_location_acquiring_Location);

        TextView locationStatusTextView = view.findViewById(R.id.location_status_text);
        locationStatusTextView.setTextColor(getResources().getColor(R.color.grey_darker));
        locationStatusTextView.setText(R.string.enrolment_text_location_order_message);

        ImageButton locationRetryImageButton = view.findViewById(R.id.location_retry_button);
        locationRetryImageButton.setImageResource(R.drawable.enrollment_location_testing_animation);
        locationRetryImageButton.setOnClickListener(null);

        locationAnimation = (AnimationDrawable) locationRetryImageButton.getDrawable();
        locationAnimation.start();

        testLocation.scheduleFutureRun(futureTasksHandler, TimeUnit.SECONDS.toMillis(MAX_TIME_TO_SEARCH_LOCATION));
        startEnrollmentLocationUpdate();
    }

    class TestLocation extends BaseFutureRunnable {

        @Override
        public void run() {
            handleFailedToGetGoodPoints();
        }
    }

    /**
     * If failed to receive Network point, GPS point, or both
     */
    private void handleFailedToGetGoodPoints() {
        showErrorMessage();
        stopEnrollmentLocationUpdate();
    }

    private void showErrorMessage() {
        switch (gpsPointStatus) {

            case NO_POINT:
                switch (networkPointStatus) {
                    case NO_POINT:            //No GPS or Network available
                        showLocationErrorMessage(R.string.enrolment_text_location_no_gps_network_point, R.drawable.ico_enrollment_location_validaiton_error_retry);
                        break;
                    case POINT_RECEIVED:    //Only Network available
                    case GOOD_POINT_RECEIVED:
                        showLocationErrorMessage(R.string.enrolment_text_location_no_gps_point, R.drawable.ico_enrollment_location_validaiton_error_retry);
                        break;
                    default:
                        break;
                }
                break;

            case POINT_RECEIVED:
                switch (networkPointStatus) {
                    case NO_POINT:            //Only GPS available
                        showLocationErrorMessage(R.string.enrolment_text_location_no_network_point, R.drawable.ico_enrollment_location_validaiton_error_retry);
                        break;
                    case POINT_RECEIVED:    //GPS and network available, but GPS  and network with bad accuracy
                        showLocationErrorMessage(R.string.enrolment_text_location_gps_network_doesnt_meet_threshold, R.drawable.ico_enrollment_location_validaiton_error_retry);
                        break;
                    case GOOD_POINT_RECEIVED://GPS and network available, but GPS with bad accuracy
                        showLocationErrorMessage(R.string.enrolment_text_location_gps_doesnt_meet_threshold, R.drawable.ico_enrollment_location_validaiton_error_retry);
                        break;
                    default:
                        break;
                }
                break;

            case GOOD_POINT_RECEIVED:
                switch (networkPointStatus) {
                    case NO_POINT:            //Only GPS available
                        showLocationErrorMessage(R.string.enrolment_text_location_no_network_point, R.drawable.ico_enrollment_location_validaiton_error_retry);
                        break;
                    case POINT_RECEIVED:    //GPS and network available, but network with bad accuracy
                        showLocationErrorMessage(R.string.enrolment_text_location_network_doesnt_meet_threshold, R.drawable.ico_enrollment_location_validaiton_error_retry);
                        break;
                    case GOOD_POINT_RECEIVED:
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    private void showLocationErrorMessage(int tagStatusTextResourceId, int locationRetryImageResourceId) {
        if (isAdded()) {
            TextView locationTitleTextView = view.findViewById(R.id.location_title_text);
            locationTitleTextView.setTextColor(getResources().getColor(R.color.enrollment_device_error_color));
            locationTitleTextView.setText(R.string.enrolment_text_location_location_failed);

            TextView locationStatusTextView = view.findViewById(R.id.location_status_text);
            locationStatusTextView.setTextColor(getResources().getColor(R.color.grey_darker));
            locationStatusTextView.setText(tagStatusTextResourceId);

            ImageButton locationRetryImageButton = view.findViewById(R.id.location_retry_button);
            locationRetryImageButton.setImageResource(locationRetryImageResourceId);
            locationRetryImageButton.setOnClickListener(this);

            locationAnimation.stop();
        }
    }

    private void handleOnGoodPointReceivedSuccess(Location currentLocation) {
        if (isAdded()) {
            TextView locationTitleTextView = view.findViewById(R.id.location_title_text);
            locationTitleTextView.setTextColor(getResources().getColor(R.color.green));
            locationTitleTextView.setText(R.string.enrolment_text_location_location_acquired);

            TextView locationStatusTextView = view.findViewById(R.id.location_status_text);
            locationStatusTextView.setTextColor(getResources().getColor(R.color.grey_darker));
            locationStatusTextView.setText(getResources().getString(R.string.enrolment_text_location_gps_network_points) + " " + satellitesNumber);

            ImageButton locationRetryImageButton = view.findViewById(R.id.location_retry_button);
            locationRetryImageButton.setOnClickListener(null);
            locationRetryImageButton.setImageResource(R.drawable.ico_enrollment_location_validaiton_ok);

            locationAnimation.stop();

            Button rightButton = view.findViewById(R.id.right_button);
            if (((EnrollmentActivity) getActivity()).getNextScreen(Enrollment.LOCATION_VALIDATION_STEP) == null) {
                rightButton.setText(R.string.enrolment_button_finish);
            } else {
                rightButton.setText(R.string.enrolment_button_next);
            }
            rightButton.setTag(EnrollmentActivity.FINISHED);

            futureTasksHandler.removeCallbacks(testLocation);

            addPointToDB(currentLocation);

            stopEnrollmentLocationUpdate();
        }
    }

    private void addPointToDB(Location currentLocation) {
        BatteryManager batteryManager = (BatteryManager) App.getAppContext().getSystemService(Context.BATTERY_SERVICE);
        int isDeviceChargingAsInt = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (batteryManager.isCharging()) {
                isDeviceChargingAsInt = 1;
            }
        }
        EntityGpsPoint recordGpsPoint = new EntityGpsPoint(
                DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().offenderId,
                currentLocation.getTime(), currentLocation.getLatitude(),
                currentLocation.getLongitude(), currentLocation.getAltitude(),
                currentLocation.getAccuracy(),
                getLocationSatellitesNumber(currentLocation),
                LocationManager.getProviderType(currentLocation.getProvider()),
                EntityGpsPoint.INITIAL_SYNC_RETRY_COUNT,
                DeviceStateManager.getInstance().getIsMobileDataAvaliable(),
                currentLocation.getSpeed(), currentLocation.getBearing(),
                LocationManager.isMockLocation(currentLocation),
                LocationManager.getMotionType(),
                isDeviceChargingAsInt,
                AccelerometerManager.getInstance().getIsDeviceLayingFlatAsInt(),
                AccelerometerManager.getInstance().getLatestAccelerometerValues());

        Gson gson = new Gson();
        String recordGpsPointJsonStr = gson.toJson(recordGpsPoint);
        TableOffenderStatusManager.sharedInstance().updateColumnString(TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFF_LAST_GPS_POINT,
                recordGpsPointJsonStr);
    }

    @Override
    public void onLocationChanged(Location location) {
       timeOfLastGpsLocationUpdate = location.getTime();
        if (location.getProvider().equals(android.location.LocationManager.NETWORK_PROVIDER)) {
            String messageToUpload = "OnLocationChanged() : Lat = " + location.getLatitude() +
                    " Long = " + location.getLongitude() +
                    " Accuracy = " + location.getAccuracy() +
                    " provider " + location.getProvider() +
                    " Time " + TimeUtil.formatFromMiliSecondToString(location.getTime(), TimeUtil.SIMPLE);
            App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.Zones);
            networkPointStatus = POINT_STATUS.POINT_RECEIVED;
            long badPointThreshold = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BAD_POINT_THRESHOLD);
            if (location.getAccuracy() < badPointThreshold) {
                chosenNetworkLocation = location;
                networkPointStatus = POINT_STATUS.GOOD_POINT_RECEIVED;
            }
        }

        if (location.getProvider().equals(android.location.LocationManager.GPS_PROVIDER)) {
            Location lastKnownLocation = enrollmentLocationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
            String messageToUpload = "OnGPSLocationChanged() : Lat = " + lastKnownLocation.getLatitude() +
                    " Long = " + lastKnownLocation.getLongitude() +
                    " Accuracy = " + lastKnownLocation.getAccuracy() +
                    " provider " + lastKnownLocation.getProvider() +
                    " Time " + TimeUtil.formatFromMiliSecondToString(lastKnownLocation.getTime(), TimeUtil.SIMPLE) +
                    " satellitesNumber " + satellitesNumber;
            App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.Zones);
            gpsPointStatus = POINT_STATUS.POINT_RECEIVED;
            long goodPointThreshold = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_GOOD_POINT_THRESHOLD);
            if ((location.getAccuracy() < goodPointThreshold) && (satellitesNumber > 0)) {
                chosenGpsLocation = location;
                gpsPointStatus = POINT_STATUS.GOOD_POINT_RECEIVED;
                chosenSatellitesNumber = satellitesNumber;
            } else {
                timeOfLastGpsLocationUpdate = 0;
                timeOfTakenLastSatellitesUpdate = 0;
                satellitesNumber = 0;
            }
        }

        if (networkPointStatus == POINT_STATUS.GOOD_POINT_RECEIVED && gpsPointStatus == POINT_STATUS.GOOD_POINT_RECEIVED) {
            long minSatellitesNumber = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                    (OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_SATELLITE_NUM);
            Location currentLocation;
            if (chosenNetworkLocation == null) {
                currentLocation = chosenGpsLocation;
            } else if (chosenGpsLocation.getAccuracy() < chosenNetworkLocation.getAccuracy() && chosenSatellitesNumber >= minSatellitesNumber) {
                currentLocation = chosenGpsLocation;
            } else {
                currentLocation = chosenNetworkLocation;
            }
            handleOnGoodPointReceivedSuccess(currentLocation);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onGpsStatusChanged(int event) {
        int satellitesInFix = 0;
        for (GpsSatellite sat : enrollmentLocationManager.getGpsStatus(null).getSatellites()) {
            if (sat.usedInFix()) {
                satellitesInFix++;
            }
        }

        Location lastKnownLocation = enrollmentLocationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
        long time = 0;
        if (lastKnownLocation != null) {
            time = System.currentTimeMillis();

            if (satellitesInFix > 0) {

                if (timeOfLastGpsLocationUpdate == 0 || timeOfTakenLastSatellitesUpdate == 0) {
                    satellitesNumber = satellitesInFix;
                    timeOfTakenLastSatellitesUpdate = time;
                } else {
                    if ((timeOfLastGpsLocationUpdate - timeOfTakenLastSatellitesUpdate) > (time - timeOfLastGpsLocationUpdate)) {
                        satellitesNumber = satellitesInFix;
                    }
                }
            }
        }
    }

    private int getLocationSatellitesNumber(Location location) {
        if (location.getProvider().equals(android.location.LocationManager.GPS_PROVIDER)) {
            return satellitesNumber;
        } else {
            return 0;
        }
    }
}
