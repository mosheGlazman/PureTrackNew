package com.supercom.puretrack.model.business_logic_models.sms_parsing.factory;

import android.renderscript.Int2;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.SmsOpOffenderLeftPureComZone;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.SmsOpResetDB;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.SmsOpRestart;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.SmsOpSetCellularApn;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.SmsOpStartEarlyCycle;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.SmsOpStartFlightMode;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.SmsOpStartPmComProfile;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.SmsOpStopPmComProfile;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.base.SmsMsg;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.base.SmsOperation;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.base.SmsOperationEntity;
import com.supercom.puretrack.util.encryption.AESUtils;
import com.supercom.puretrack.util.encryption.ConversionUtil;

import java.security.GeneralSecurityException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class SmsOperationFactory {

    public interface SmsOperationType {
        Int2 TYPES_RANGE = new Int2(1, 7);
        int SYNC_NOW = 1;
        int RESTART = 2;
        int RESET_DB_STATE = 3;
        int START_PM_COM_PROFILE_BY_ID = 4;
        int STOP_PM_COM_PROFILE_BY_ID = 5;
        int SET_CELLULAR_APN = 6;
        int FLIGHT_MODE_ENABLE = 7;
        int OFFENDER_LEFT_PURECOM_ZONE = 8;
    }


    private String decryptSmsOperationDataToJsonFormat(SmsOperationEntity smsOperationEntity) {
        String smsOperationJsonStr = null;
        byte[] AESkeyBytes = ConversionUtil.convertHexStringToByteArray(TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_ENCRYPTION));
        try {
            smsOperationJsonStr = AESUtils.decrypt(AESkeyBytes, smsOperationEntity.getOpData());
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        //Validate the expected CRC received in SmsOperationEntity with the CRC of the decrypted JSON object: smsOperationJsonStr.
        boolean isCrcMatches = isCrcMatches(smsOperationJsonStr, smsOperationEntity.getCrc());
        return (isCrcMatches) ? smsOperationJsonStr : null;
    }

    private boolean isCrcMatches(String data, long crcExpectedValue) {
        byte[] dataByteArr = data.getBytes();
        Checksum checksum = new CRC32();

        // update the current checksum with the specified array of bytes
        checksum.update(dataByteArr, 0, dataByteArr.length);
        long crcValue = checksum.getValue();

        return crcValue == crcExpectedValue;
    }

    public SmsOperation parseSmsOperation(SmsMsg smsMessage) {
        SmsOperationEntity smsOperationEntity = null;
        String smsOperationJsonStr = null;
        SmsOperation smsOperation = null;

        try {
            Gson gson = new Gson();
            String smsOperationEntityJsonStr = smsMessage.getMsgBody().substring(2);
            smsOperationEntity = gson.fromJson(smsOperationEntityJsonStr, SmsOperationEntity.class);

            smsOperationJsonStr = decryptSmsOperationDataToJsonFormat(smsOperationEntity);
            if (smsOperationJsonStr != null) {
                smsOperation = gson.fromJson(smsOperationJsonStr, SmsOperation.class);
                switch (smsOperation.getOpCode()) {
                    case SmsOperationType.SYNC_NOW:
                        //Need to build manually because of LocationHandler usage in the SmsOpStartEarlyCycle class.
                        SmsOpStartEarlyCycle SmsOpStartEarlyCycle = new SmsOpStartEarlyCycle();
                        SmsOpStartEarlyCycle.setIndex(smsOperation.getIndex());
                        SmsOpStartEarlyCycle.setOpCode(smsOperation.getOpCode());
                        smsOperation = SmsOpStartEarlyCycle;
                        break;

                    case SmsOperationType.RESTART:
                        smsOperation = gson.fromJson(smsOperationJsonStr, SmsOpRestart.class);
                        break;

                    case SmsOperationType.RESET_DB_STATE:
                        smsOperation = gson.fromJson(smsOperationJsonStr, SmsOpResetDB.class);
                        break;

                    case SmsOperationType.START_PM_COM_PROFILE_BY_ID:
                        smsOperation = gson.fromJson(smsOperationJsonStr, SmsOpStartPmComProfile.class);
                        break;

                    case SmsOperationType.STOP_PM_COM_PROFILE_BY_ID:
                        smsOperation = gson.fromJson(smsOperationJsonStr, SmsOpStopPmComProfile.class);
                        break;

                    case SmsOperationType.SET_CELLULAR_APN:
                        smsOperation = gson.fromJson(smsOperationJsonStr, SmsOpSetCellularApn.class);
                        break;

                    case SmsOperationType.FLIGHT_MODE_ENABLE:
                        smsOperation = gson.fromJson(smsOperationJsonStr, SmsOpStartFlightMode.class);
                        break;

                    case SmsOperationType.OFFENDER_LEFT_PURECOM_ZONE:
                        smsOperation = gson.fromJson(smsOperationJsonStr, SmsOpOffenderLeftPureComZone.class);
                        break;
                }

                if (smsOperation != null) {
                    smsOperation.setSenderNumber(smsMessage.getSenderNumber());
                }
            }

        } catch (IllegalArgumentException | NullPointerException | JsonSyntaxException e) {
            e.printStackTrace();
        }


        return smsOperation;
    }
}
