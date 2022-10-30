/**
 *
 */
package com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.base;

public class SmsOperationEntity {
    private long Crc;
    private String OpData;

    public long getCrc() {
        return Crc;
    }


    public String getOpData() {
        return OpData;
    }
}
