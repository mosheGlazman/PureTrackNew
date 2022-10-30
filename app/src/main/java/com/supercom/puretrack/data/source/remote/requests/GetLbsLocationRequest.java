package com.supercom.puretrack.data.source.remote.requests;

import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.parsers.GetLbsLocationResultParser;
import com.supercom.puretrack.data.source.local.local_managers.hardware.CellularInfoManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class GetLbsLocationRequest extends BaseAsyncTaskRequest {
    private final GetLbsLocationResultParser lbsResponseHandler;
    private final List<CellularInfoManager.LbsInfo> lbsLocalList;

    public GetLbsLocationRequest(GetLbsLocationResultParser handler, List<CellularInfoManager.LbsInfo> lbsData) {
        this.lbsResponseHandler = handler;
        lbsLocalList = lbsData;
    }

    @Override
    protected String getHttpRequestType() {
        return "POST";
    }

    @Override
    protected String getServiceRequestString() {
        return "GetLbsLocation";
    }

    @Override
    protected String getBody() {
        String token = NetworkRepository.getInstance().getTokenKey();
        String lbsRequestString;
        String iccidValue;

        if (lbsLocalList.size() > 0) {
            iccidValue = lbsLocalList.get(0).iccid;
        } else {
            iccidValue = "0";
        }

        try {
            lbsRequestString =
                    "<root type=\"object\">" +
                            "<deviceId type=\"string\">" + NetworkRepository.getDeviceSerialNumber() + "</deviceId>" +
                            "<token type=\"string\">" + token + "</token>" +
                            "<offender_id type=\"string\">" + TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DETAILS_OFF_ID) + "</offender_id>" +
                            "<Utc_Time type=\"string\">" + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "</Utc_Time>" +
                            "<SIM_ID type=\"string\">" + iccidValue + "</SIM_ID>" +
                            "<LBS_Request type=\"array\">";
            // cell/wifi info array
            for (int i = 0; i < lbsLocalList.size(); i++) {
                lbsRequestString +=
                        "<LBS_Info type=\"object\">" +
                                "<Network_Type type=\"string\">" + lbsLocalList.get(i).NetworkType + "</Network_Type>" +
                                "<Serving type=\"string\">" + lbsLocalList.get(i).isServiceCell + "</Serving>" +
                                "<MCC type=\"string\">" + lbsLocalList.get(i).Mcc + "</MCC>" +
                                "<MNC type=\"string\">" + lbsLocalList.get(i).Mnc + "</MNC>" +
                                "<Cell_ID type=\"string\">" + lbsLocalList.get(i).CellId + "</Cell_ID>" +
                                "<LAC type=\"string\">" + lbsLocalList.get(i).Lac + "</LAC>" +
                                "<Rssi type=\"string\">" + lbsLocalList.get(i).Rssi + "</Rssi>" +
                                "<DBM type=\"string\">" + lbsLocalList.get(i).dBm + "</DBM>" +
                                "<TAC type=\"string\">" + lbsLocalList.get(i).Tac + "</TAC>" +
                                "<PCI type=\"string\">" + lbsLocalList.get(i).Pci + "</PCI>" +
                                "<TA type=\"string\">" + lbsLocalList.get(i).TimingAdvance + "</TA>" +
                                "</LBS_Info>";
            }
            lbsRequestString += "</LBS_Request>";
            lbsRequestString += "</root>";
        } catch (Exception e) {
            lbsRequestString = "<root type=\"object\">" +
                    "<deviceId type=\"string\">" + NetworkRepository.getDeviceSerialNumber() + "</deviceId>" +
                    "<token type=\"string\">" + token + "</token>" +
                    "<offender_id type=\"string\">" + TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DETAILS_OFF_ID) + "</offender_id>" +
                    "<Utc_Time type=\"string\">" + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "</Utc_Time>" +
                    "<SIM_ID type=\"string\">" + "0" + "</SIM_ID>" +
                    "<LBS_Request type=\"array\">" + "</LBS_Request>" + "</root>";
        }

        return lbsRequestString;
    }

    @Override
    protected void startHttpResponseHandle(String result, int responseCode) {
        lbsResponseHandler.handleResponse(result);
    }
}


