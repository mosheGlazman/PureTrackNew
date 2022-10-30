package com.supercom.puretrack.model.business_logic_models.sms_parsing.operations;

import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.base.SmsOperation;
import com.supercom.puretrack.ui.activity.MainActivity;

public class SmsOpStartPmComProfile extends SmsOperation {

    private int PMComProfileID;

    @Override
    public void performSmsOperation() {
        ((MainActivity) App.getAppContext()).startPmComProfileByProfileID(PMComProfileID);
    }
}
