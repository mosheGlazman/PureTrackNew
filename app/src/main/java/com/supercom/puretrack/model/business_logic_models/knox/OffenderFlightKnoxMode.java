package com.supercom.puretrack.model.business_logic_models.knox;

import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.R;

public class OffenderFlightKnoxMode extends OffenderNormalKnoxMode {

    @Override
    public void addMoreExtentions() {
        super.addMoreExtentions();
        knoxSdkManager.setKioskModeState(false);
        knoxSdkManager.setAirplaneModeChangeable(true);
        knoxSdkManager.setRecentButtonMode(false);
        knoxSdkManager.setAllowSVoiceMode(false);
        knoxSdkManager.setAllowStatusBarExpansion(false);
        knoxSdkManager.setFlightModeState(true);
        knoxSdkManager.setKeyboardMode();// block keyboard settings and prediction
    }

    @Override
    protected String getLogMessage() {
        return App.getContext().getString(R.string.launcher_text_enter_offender_flight_mode);
    }
}
