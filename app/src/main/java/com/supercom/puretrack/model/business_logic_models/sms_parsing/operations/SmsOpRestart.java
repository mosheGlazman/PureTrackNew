package com.supercom.puretrack.model.business_logic_models.sms_parsing.operations;

import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.base.SmsOperation;

public class SmsOpRestart extends SmsOperation {

    @Override
    public void performSmsOperation() {
        NetworkRepository.getInstance().setShouldRestartAppInTheEndOfTheCycle(true);
    }
}
