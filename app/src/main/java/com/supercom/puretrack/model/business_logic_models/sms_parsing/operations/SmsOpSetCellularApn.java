package com.supercom.puretrack.model.business_logic_models.sms_parsing.operations;

import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.base.SmsOperation;

public class SmsOpSetCellularApn extends SmsOperation {
    private String APN;
    private String name;
    private String username;
    private String password;
    private int authenticationType;

    @Override
    public void performSmsOperation() {
        KnoxUtil.getInstance().getKnoxSDKImplementation().createCellularAPN(APN, name, username, password, authenticationType);
    }
}
