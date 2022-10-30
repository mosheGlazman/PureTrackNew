package com.supercom.puretrack.data.source.local.local_managers.hardware;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import com.supercom.puretrack.util.application.App;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CellularInfoManager {
    public class CellInfoObj {
        public String registration_type;
        public String network_id;
        public String cell_reception;
        public String sim_id;
        public String cell_mobile_data;
        public String device_phone_number;


        public CellInfoObj(String registration_type, String network_id, String cell_reception, String sim_id,
                           String cell_mobile_data, String device_phone_number) {
            super();
            this.registration_type = registration_type;
            this.network_id = network_id;
            this.cell_reception = cell_reception;
            this.sim_id = sim_id;
            this.cell_mobile_data = cell_mobile_data;
            this.device_phone_number = device_phone_number;
        }
    }

    public class LbsInfo {
        public int isServiceCell;
        public int Mcc;
        public int Mnc;
        public String iccid;
        public int NetworkType;
        public int CellId;
        public int Lac;
        public int Rssi;
        public int dBm;
        public int Tac; // LTE
        public int Pci; // LTE
        public int TimingAdvance; // LTE
    }

    private class CellObj {
        String mnc;
        String mcc;
        int dbm;
        int signalLevel;
        String netType;
    }

    private static final CellularInfoManager INSTANCE = new CellularInfoManager();

    private CellularInfoManager() {
    }

    public static CellularInfoManager sharedInstance() {
        return INSTANCE;
    }

    @SuppressLint("MissingPermission")
    public CellInfoObj getCellularInfoObj() {
        String registration_type = isDataRoamingEnabled() ? "roaming" : "home network";
        TelephonyManager telephonyManager = (TelephonyManager) App.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        ArrayList<CellObj> cellList = getCellInfo(telephonyManager);
        String network_id = getNetworkId(cellList);
        String cell_reception = getCellReception(cellList);
        String sim_id = "";
        try {
            sim_id = "iccid = " + telephonyManager.getSimSerialNumber() + "; imsi = " + telephonyManager.getSubscriberId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String cell_mobile_data = isMobileDateEnabled() ? "yes" : "no";
        String device_phone_number = "";
        try {
            device_phone_number = telephonyManager.getLine1Number();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new CellInfoObj(registration_type, network_id, cell_reception, sim_id, cell_mobile_data, device_phone_number);
    }

    @SuppressLint("MissingPermission")
    public JSONArray getCellInfo_CellInfo() {
        TelephonyManager tel = (TelephonyManager) App.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);

        JSONArray cellList = new JSONArray();

        // Type of the network
        int phoneTypeInt = tel.getPhoneType();
        String phoneType = null;
        phoneType = phoneTypeInt == TelephonyManager.PHONE_TYPE_GSM ? "gsm" : phoneType;
        phoneType = phoneTypeInt == TelephonyManager.PHONE_TYPE_CDMA ? "cdma" : phoneType;

        //from Android M up must use getAllCellInfo
        List<CellInfo> infos = tel.getAllCellInfo();
        for (int i = 0; i < infos.size(); ++i) {
            try {
                CellInfo info = infos.get(i);

                JSONObject cellObj = new JSONObject();
                cellObj.put("TimeStamp", info.getTimeStamp()); //Approximate time of this cell information in nanos since boot
                cellObj.put("Is Serving", info.isRegistered()); //True if this cell is registered to the mobile network

                if (info instanceof CellInfoGsm) {
                    CellIdentityGsm identityGsm = ((CellInfoGsm) info).getCellIdentity();
                    cellObj.put("netType", "GSM"); //Network type
                    cellObj.put("cellId", identityGsm.getCid()); //Cell identity
                    cellObj.put("lac", identityGsm.getLac()); //Location Area Code
                    cellObj.put("mnc", identityGsm.getMnc()); //Mobile network code
                    cellObj.put("mcc", identityGsm.getMcc()); //Mobile country  code
                    CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
                    cellObj.put("signalLevel", gsm.getLevel()); //Received signal level
                    cellObj.put("dbm", gsm.getDbm()); //Signal Strength as DBM
                    cellList.put(cellObj);
                } else if (info instanceof CellInfoLte) {
                    CellIdentityLte identityLte = ((CellInfoLte) info).getCellIdentity();
                    cellObj.put("netType", "LTE"); //Network type
                    cellObj.put("cellId", identityLte.getCi()); //Cell identity
                    cellObj.put("tac", identityLte.getTac()); //Tracking area code
                    cellObj.put("mnc", identityLte.getMnc()); //Mobile network code
                    cellObj.put("mcc", identityLte.getMcc()); //Mobile country  code
                    cellObj.put("pci", identityLte.getPci()); //physical Cell Id
                    CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                    cellObj.put("signalLevel", lte.getLevel()); //Received signal level
                    cellObj.put("dbm", lte.getDbm()); //Signal Strength as DBM
                    cellObj.put("timingAdvance", lte.getTimingAdvance()); //Timing advance
                    cellList.put(cellObj);
                } else if (info instanceof CellInfoWcdma) {
                    cellObj.put("netType", "WCDMA"); //Network type
                    CellIdentityWcdma identityLteWcdma = ((CellInfoWcdma) info).getCellIdentity();
                    cellObj.put("cellId", identityLteWcdma.getCid()); //Cell identity
                    cellObj.put("lac", identityLteWcdma.getLac()); //Location Area Code
                    cellObj.put("mnc", identityLteWcdma.getMnc()); //Mobile network code
                    cellObj.put("mcc", identityLteWcdma.getMcc()); //Mobile country  code
                    CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) info).getCellSignalStrength();
                    cellObj.put("signalLevel", wcdma.getLevel()); //Received signal level
                    cellObj.put("dbm", wcdma.getDbm()); //Signal Strength as DBM
                    cellList.put(cellObj);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return cellList;
    }

    @SuppressLint("MissingPermission")
    public List<LbsInfo> getLbsInfo() {
        LbsInfo lbsObj = new LbsInfo();
        List<LbsInfo> lbsList = new ArrayList<>();
        boolean Valid = false;

        try {
            TelephonyManager telephonyManager = (TelephonyManager) App.getContext().getSystemService(Context.TELEPHONY_SERVICE);
            List<CellInfo> infos = telephonyManager.getAllCellInfo();

            if (infos == null) {
                return null;
            }

            lbsObj.NetworkType = telephonyManager.getNetworkType();
            //if (TelephonyManager.NETWORK_TYPE_GPRS)
            lbsObj.iccid = telephonyManager.getSimSerialNumber();


            for (int i = 0; i < infos.size(); ++i) {
                CellInfo info = infos.get(i);

                // if (info. == 2147483647)

                lbsObj.isServiceCell = (info.isRegistered() == true) ? 1 : 0;
                Valid = true;

                if (info instanceof CellInfoGsm) {
                    CellIdentityGsm identityGsm = ((CellInfoGsm) info).getCellIdentity();
                    lbsObj.CellId = identityGsm.getCid(); //Cell identity
                    lbsObj.Lac = identityGsm.getLac(); //Location Area Code
                    lbsObj.Mcc = identityGsm.getMcc(); //Mobile country  code
                    if (lbsObj.Mcc == 2147483647) {
                        // empty object
                        continue;
                    }
                    lbsObj.Mnc = identityGsm.getMnc(); //Mobile network code

                    CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
                    lbsObj.Rssi = gsm.getLevel(); //Received signal level
                    lbsObj.dBm = gsm.getDbm(); //Signal Strength as DBM
                    // Clear LTE data
                    lbsObj.TimingAdvance = 0;
                    lbsObj.Pci = 0;
                    lbsObj.Tac = 0;
                    // add to list
                    lbsList.add(lbsObj);
                } else if (info instanceof CellInfoLte) {
                    CellIdentityLte identityLte = ((CellInfoLte) info).getCellIdentity();
                    lbsObj.CellId = identityLte.getCi(); //Cell identity
                    lbsObj.Tac = identityLte.getTac(); //Tracking area code
                    lbsObj.Mcc = identityLte.getMcc(); //Mobile country  code
                    if (lbsObj.Mcc == 2147483647) {
                        // empty object
                        continue;
                    }
                    lbsObj.Mnc = identityLte.getMnc(); //Mobile network code
                    lbsObj.Pci = identityLte.getPci(); //physical Cell Id
                    CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                    lbsObj.Rssi = lte.getLevel(); //Received signal level
                    lbsObj.dBm = lte.getDbm(); //Signal Strength as DBM
                    lbsObj.TimingAdvance = lte.getTimingAdvance(); //Timing advance
                    // Clear 3G data
                    lbsObj.Lac = lbsObj.Tac;

                    // add to list
                    lbsList.add(lbsObj);
                } else if (info instanceof CellInfoWcdma) {
                    CellIdentityWcdma identityLteWcdma = ((CellInfoWcdma) info).getCellIdentity();
                    lbsObj.CellId = identityLteWcdma.getCid(); //Cell identity
                    lbsObj.Lac = identityLteWcdma.getLac(); //Location Area Code
                    lbsObj.Mcc = identityLteWcdma.getMcc(); //Mobile country  code
                    if (lbsObj.Mcc == 2147483647) {
                        // empty object
                        continue;
                    }
                    lbsObj.Mnc = identityLteWcdma.getMnc(); //Mobile network code
                    CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) info).getCellSignalStrength();
                    lbsObj.Rssi = wcdma.getLevel(); //Received signal level
                    lbsObj.dBm = wcdma.getDbm(); //Signal Strength as DBM
                    // Clear LTE data
                    lbsObj.TimingAdvance = 0;
                    lbsObj.Pci = 0;
                    lbsObj.Tac = 0;

                    // add to list
                    lbsList.add(lbsObj);
                }

                if (lbsObj.isServiceCell == 1) {
                    break;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (Valid) {
            return lbsList;
        } else {
            return null;
        }
    }


   //public String getCellInfo_CellLocation() {
   //    TelephonyManager telephonyManager = (TelephonyManager) App.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);

   //    /** MCC and MNC. the check is required, otherwise crash when no coverage is available*/
   //    String mynetmccmnc = telephonyManager.getNetworkOperator();
   //    String mcc, mnc;
   //    if (mynetmccmnc != null && mynetmccmnc.length() >= 4) {
   //        mcc = mynetmccmnc.substring(0, 3);
   //        mnc = mynetmccmnc.substring(3);
   //    } else {
   //        mcc = "NA";
   //        mnc = "NA";
   //    }

   //    /** LAC and Cellid*/
   //    GsmCellLocation gsmLocation = (GsmCellLocation) telephonyManager.getCellLocation();
   //    int lac = gsmLocation.getLac();
   //    int cid = gsmLocation.getCid() & 0xffff;

   //    int nettypeINT = telephonyManager.getNetworkType();
   //    String nettype = "NA";
   //    switch (nettypeINT) {
   //        case (TelephonyManager.NETWORK_TYPE_GPRS):
   //            nettype = "GPRS";
   //            break;
   //        case (TelephonyManager.NETWORK_TYPE_1xRTT):
   //            nettype = "1xRTT";
   //            break;
   //        case (TelephonyManager.NETWORK_TYPE_CDMA):
   //            nettype = "CDMA";
   //            break;
   //        case (TelephonyManager.NETWORK_TYPE_EDGE):
   //            nettype = "EDGE";
   //            break;
   //        case (TelephonyManager.NETWORK_TYPE_EVDO_0):
   //            nettype = "EVDO_0";
   //            break;
   //        case (TelephonyManager.NETWORK_TYPE_EVDO_A):
   //            nettype = "EVDO_A";
   //            break;
   //        case (TelephonyManager.NETWORK_TYPE_HSDPA):
   //            nettype = "HSDPA";
   //            break;
   //        case (TelephonyManager.NETWORK_TYPE_HSPA):
   //            nettype = "HSPA";
   //            break;
   //        case (TelephonyManager.NETWORK_TYPE_HSUPA):
   //            nettype = "HSUPA";
   //            break;
   //        case (TelephonyManager.NETWORK_TYPE_UMTS):
   //            nettype = "UMTS";
   //            break;
   //        case (TelephonyManager.NETWORK_TYPE_UNKNOWN):
   //            nettype = "UNKNOWN";
   //            break;
   //        default:
   //            break;
   //    }

   //    Map<Integer, Integer> neighborsmapUMTS = new HashMap<Integer, Integer>();
   //    Map<String, Integer> neighborsmapGSM = new HashMap<String, Integer>();

   //    List<NeighboringCellInfo> neighboringCellInfo;
   //    neighboringCellInfo = telephonyManager.getNeighboringCellInfo();

   //    /** Fill the hash tables depending on the network type*/
   //    for (NeighboringCellInfo i : neighboringCellInfo) {
   //        int networktype = i.getNetworkType();
   //        if ((networktype == TelephonyManager.NETWORK_TYPE_UMTS) ||
   //                (networktype == TelephonyManager.NETWORK_TYPE_HSDPA) ||
   //                (networktype == TelephonyManager.NETWORK_TYPE_HSUPA) ||
   //                (networktype == TelephonyManager.NETWORK_TYPE_HSPA)
   //        )
   //            neighborsmapUMTS.put(i.getPsc(), i.getRssi() - 115);
   //        else
   //            neighborsmapGSM.put(i.getLac() + "-" + i.getCid(), (-113 + 2 * (i.getRssi())));
   //    }


   //    StringBuilder record = new StringBuilder();
   //    record.append("mcc : ").append(mcc).append("\n");
   //    record.append("mnc : ").append(mnc).append("\n");
   //    record.append("lac : ").append(lac).append("\n");
   //    record.append("cid : ").append(cid).append("\n");
   //    record.append("nettype : ").append(nettype).append("\n");

   //    int n_neighboringcells = neighborsmapUMTS.size() + neighborsmapGSM.size();
   //    record.append("n_neighboringcells : ").append(n_neighboringcells).append("\n");
   //    if (!neighborsmapUMTS.isEmpty()) {
   //        for (Object key : neighborsmapUMTS.keySet()) {
   //            record.append(" |UMTS -> PSC:").append(key).append(",RSCP:").append(neighborsmapUMTS.get(key));
   //        }
   //    }
   //    if (!neighborsmapGSM.isEmpty()) {
   //        for (Object key : neighborsmapGSM.keySet()) {
   //            record.append(" |GSM -> LAC-CID:").append(key).append(",RSSI:").append(neighborsmapGSM.get(key));
   //        }
   //    }
   //    return record.toString();
   //}


    @SuppressLint("MissingPermission")
    private ArrayList<CellObj> getCellInfo(TelephonyManager telephonyManager) {
        ArrayList<CellObj> cellList = new ArrayList<>();
        List<CellInfo> infos = telephonyManager.getAllCellInfo();
        if (infos != null) {
            for (int i = 0; i < infos.size(); ++i) {
                try {
                    CellInfo info = infos.get(i);
                    CellObj cellObj = new CellObj();

                    if (info instanceof CellInfoGsm) {
                        cellObj.netType = "GSM";
                        CellIdentityGsm identityGsm = ((CellInfoGsm) info).getCellIdentity();
                        cellObj.mnc = String.valueOf(identityGsm.getMnc());
                        cellObj.mcc = String.valueOf(identityGsm.getMcc());
                        CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
                        cellObj.dbm = gsm.getDbm();
                        cellObj.signalLevel = gsm.getLevel();
                        cellList.add(cellObj);
                    } else if (info instanceof CellInfoLte) {
                        cellObj.netType = "LTE";
                        CellIdentityLte identityLte = ((CellInfoLte) info).getCellIdentity();
                        cellObj.mnc = String.valueOf(identityLte.getMnc());
                        cellObj.mcc = String.valueOf(identityLte.getMcc());
                        CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                        cellObj.dbm = lte.getDbm();
                        cellObj.signalLevel = lte.getLevel();
                        cellList.add(cellObj);
                    } else if (info instanceof CellInfoWcdma) {
                        cellObj.netType = "WCDMA"; //Network type
                        CellIdentityWcdma identityLteWcdma = ((CellInfoWcdma) info).getCellIdentity();
                        cellObj.mnc = String.valueOf(identityLteWcdma.getMnc());
                        cellObj.mcc = String.valueOf(identityLteWcdma.getMcc());
                        CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) info).getCellSignalStrength();
                        cellObj.dbm = wcdma.getDbm();
                        cellObj.signalLevel = wcdma.getLevel();
                        cellList.add(cellObj);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return cellList;
    }

    private boolean isDataRoamingEnabled() {
        try {
            return Settings.Global.getInt(App.getContext().getContentResolver(), Settings.Global.DATA_ROAMING) == 1;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    private String getNetworkId(ArrayList<CellObj> cellList) {
        String network_id = "";
        for (CellObj cellObj : cellList) {
            network_id += "netType: " + cellObj.netType + ";  mnc: " + cellObj.mnc + "; mcc:" + cellObj.mcc + "; ";
        }
        return network_id;
    }

    private String getCellReception(ArrayList<CellObj> cellList) {
        String cell_reception = "";
        for (CellObj cellObj : cellList) {
            cell_reception += "dbm = " + cellObj.dbm + " ; signalLevel = " + cellObj.signalLevel + "; ";
        }
        return cell_reception;
    }

    private boolean isMobileDateEnabled() {
        boolean mobileDataEnabled = false;
        ConnectivityManager cm = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            mobileDataEnabled = (Boolean) method.invoke(cm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mobileDataEnabled;
    }
}
