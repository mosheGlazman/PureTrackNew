package com.supercom.puretrack.model.business_logic_models.sms_parsing.operations;

import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.base.SmsOperation;
import com.supercom.puretrack.ui.activity.MainActivity;

public class SmsOpResetDB extends SmsOperation {

    private String ServerURL;
    private String DeviceSN;
    private String DevicePass;

    @Override
    public void performSmsOperation() {
        ((MainActivity) App.getAppContext()).tryResetOffenderParams(DeviceSN, ServerURL, DevicePass);
    }
}
