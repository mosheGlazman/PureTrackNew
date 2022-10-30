package com.supercom.puretrack.data.source.remote.requests;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import com.supercom.puretrack.data.R;
import com.supercom.puretrack.data.repositories.KnoxProfileConfig;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.AutoRestartManager;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;
import com.supercom.puretrack.util.constants.network.ServerUrls;
import com.supercom.puretrack.util.date.TimeUtil;
import com.supercom.puretrack.util.general.LoggingUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public abstract class BaseAsyncTaskRequest extends AsyncTask<String, String, String> {
    public HttpURLConnection httpURLConnection = null;
    private int responseCode;

    public static final int TIMEOUT = 60000; //1 minute
    private static final int SPACES_TO_INDENT_EACH_LEVEL = 3;
    private int cycleState;

    protected String fullUrlName;

    public static String TAG = "CycleTaskBase";

    private static final int NUMNBER_OF_FAILED_SSL_CYCLE = 2;
    private final String SSL_CONST_ERROR = "OpenSSLSocketImpl";
    private static int currentTimesCycleFailedSinceSSLFailedCounter = -1;

    @Override
    protected String doInBackground(String... uri) {
        StringBuilder result = new StringBuilder();
        try {
            fullUrlName = ServerUrls.getInstance().getSelectedServerUrlAddress() + getServiceRequestString();
            // fullUrlName =  KnoxProfileConfig.getInstance().getSelectedServerUrl().getUrl() + getServiceRequestString();

            InputStream is = getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine);
            }
            is.close();
            in.close();
            currentTimesCycleFailedSinceSSLFailedCounter = -1;
        } catch (Exception e) {
            if(this instanceof GetAuthenticationTokenRequest){
                if (cycleState==0) {
                    cycleState = 2;
                    AutoRestartManager.getInstance().cycleFailed(-1);
                }
            }

            writeToFilesAndDB(e);

            //in order to resolve a problem while sometimes server return error, then we would like to start a new cycle
            if (isStuckTraceContainsWord(e)) {
                TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                        "abort and restart cycle " + SSL_CONST_ERROR, DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
                currentTimesCycleFailedSinceSSLFailedCounter++;
            }
        } finally {
            if (httpURLConnection != null)
                httpURLConnection.disconnect();
        }

        return result.toString();
    }

    private boolean isStuckTraceContainsWord(Exception e) {
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.toString().contains(SSL_CONST_ERROR)) {
                return true;
            }
        }
        return false;

    }

    private void writeToFilesAndDB(Exception e) {
        e.printStackTrace();

        StringBuilder stuckTraceBuilder = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace())
            stuckTraceBuilder.append(element.toString()).append("\n");

        boolean networkAvaliableStatus = NetworkRepository.getInstance().isNetworkAvailable();

        String messageToUpload = fullUrlName + "\nResponseCode: " + responseCode
                + " , IsNetworkAvaliable: " + networkAvaliableStatus
                + "\n" + e.getMessage() + "\n" + stuckTraceBuilder + "\nLength: ";

        LoggingUtil.updateNetworkLog(TAG + "\n\n" + "Error in " + fullUrlName + "\n" + messageToUpload, false);

        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                messageToUpload, DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
    }
    String body;
    private InputStream getInputStream() throws IOException {
        long startSendingRequestTimeStamp = 0;
        long packetRTT = 0;

        if (fullUrlName.contains("/UploadPhotoOnDemand") || fullUrlName.contains("/DeviceInstallation")) {
            fullUrlName = fullUrlName.replace("RestfulService.svc/","");
        }
        if(fullUrlName.startsWith("Http")) {
            fullUrlName = fullUrlName.replace("Http", "http");
        }

        URL url = new URL(fullUrlName);
        if(fullUrlName.startsWith("https")) {
            httpURLConnection = (HttpsURLConnection) url.openConnection();
        }else{
            httpURLConnection = (HttpURLConnection) url.openConnection();
        }


        if (fullUrlName.contains("https")) {
            //int shouldIgnoreSSLCertPatch = (TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.IGNORE_SSL_CERT));
            //if(shouldIgnoreSSLCertPatch > -1){
            //    // The server param "IgnoreSSLCert" is not empty
            //    if(shouldIgnoreSSLCertPatch  == 1){
            //        disableSSLCertificateChecking();
            //    }else{
            //        useClientSSLCertificate();
            //    }
            //}else{
                // The server param "IgnoreSSLCert" is empty. check dialog setting param

                // change by moshe at 19/09/2022 - read from knox config
                //boolean useClientCertificate_settingsDialog = TableOffenderDetailsManager.sharedInstance()
                //        .getIntValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DEVICE_CONFIG_USE_CLIENT_CERT) == 1;


                if(KnoxProfileConfig.getInstance().getUseClientCert()){
                    //useClientSSLCertificate();
                 }else{
                    disableSSLCertificateChecking();
                }
            //}
        }

        // set Timeout and method
        httpURLConnection.setReadTimeout(TIMEOUT);
        httpURLConnection.setConnectTimeout(TIMEOUT);
        httpURLConnection.setRequestMethod(getHttpRequestType());
        httpURLConnection.setDoInput(true);

        body = getBody();

        printBasicRequestLogs(body);

        if (!body.isEmpty()) {
            byte[] buffer = body.getBytes(Xml.Encoding.UTF_8.name());

            httpURLConnection.addRequestProperty("Content-Length", "" + buffer.length);
            if (fullUrlName.contains("/UploadPhotoOnDemand")) {
                httpURLConnection.addRequestProperty("Content-Type", "application/json");
            } else {
                httpURLConnection.addRequestProperty("Content-Type", "text/xml");
            }

            startSendingRequestTimeStamp = System.currentTimeMillis();
            BufferedOutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());

            outputStream.write(buffer);
            outputStream.flush();

            outputStream.close();
        }

        httpURLConnection.connect();

        responseCode = httpURLConnection.getResponseCode();
        if(this instanceof GetAuthenticationTokenRequest){
            if(responseCode==200){
                cycleState=1;
                AutoRestartManager.getInstance().cycleSuccess();
            }else{
                cycleState=2;
                AutoRestartManager.getInstance().cycleFailed(responseCode);
            }
        }
        packetRTT = System.currentTimeMillis() - startSendingRequestTimeStamp;
        printResponseLog(responseCode, packetRTT);

        return httpURLConnection.getInputStream();
    }

    private void disableSSLCertificateChecking() {

        TrustManager[] trustAllCerts = new X509TrustManager[]{new X509TrustManager() {

            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {

            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {

            }

            public X509Certificate[] getAcceptedIssuers() {

                return new X509Certificate[]{};
            }
        }};


        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
            ((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(sc.getSocketFactory());
            ((HttpsURLConnection) httpURLConnection).setHostnameVerifier(hostnameVerifier);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    private void useClientSSLCertificate() {
        TrustManager[] trustAllCerts = new X509TrustManager[]{new X509TrustManager() {

            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {

            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {

            }

            public X509Certificate[] getAcceptedIssuers() {

                return new X509Certificate[]{};
            }
        }};
        class TrivialTrustManager implements X509TrustManager {
            public void checkClientTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
                // do some checks on the chain here
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }
        class TrivialHostVerifier implements HostnameVerifier {

            @Override
            public boolean verify(String host, SSLSession session) {
                // check host and return true if verified
                return true;
            }

        }
        try {
            KeyStore ks = loadKeyStore();

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            kmf.init(ks, ("A12345678").toCharArray());
            KeyManager[] keyManagers = kmf.getKeyManagers();
            SSLContext sc = SSLContext.getInstance("TLSv1");
            sc.init(keyManagers, trustAllCerts, null);

            ((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(sc.getSocketFactory());
            ((HttpsURLConnection) httpURLConnection).setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.setDoInput(true);

        } catch (KeyManagementException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | KeyStoreException | IOException e) {
//			Toast.makeText(super.this, "Incorrect Password", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    private KeyStore loadKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {

        String KEY_STORE_FORMAT = "PKCS12";
        String KEY_STORE_PASSWORD = "Password1";

        KeyStore keyStore = null;

        // Create a new keyStore object
        try {

            keyStore = KeyStore.getInstance(KEY_STORE_FORMAT);

        } catch (KeyStoreException e) {

            throw e;
        }

        // Open the keyStore file for reading
		//AssetManager assetManager = this.getAssets();

        InputStream inputStream;
        try {

            inputStream = App.getAppContext().getResources().openRawResource(R.raw.clientcertsecond);


        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }

        // Load the keyStore
        try {
            keyStore.load(inputStream, KEY_STORE_PASSWORD.toCharArray());

        } catch (NoSuchAlgorithmException | CertificateException e) {

            throw e;

        }

        inputStream.close();

        return keyStore;

    }

    @Override
    public void onPostExecute(String result) {



        if (NetworkRepositoryConstants.IS_NETWORK_LOG_ON) {
            try {
                String prettyJson = new JSONObject(getResultAfterDecode(result)).toString(getSpacesToIndentEachLevel());
                Log.i(TAG,  getTagName()+ "   Response result : " + prettyJson);
            } catch (JSONException ignored) {

            }
            LoggingUtil.updateNetworkLog("\n   Response Result : " + result, false);
        }

        if (currentTimesCycleFailedSinceSSLFailedCounter >= 0 && currentTimesCycleFailedSinceSSLFailedCounter < NUMNBER_OF_FAILED_SSL_CYCLE) {
            LoggingUtil.updateNetworkLog("\nCalling 'start new cycle' - SSL failure" + "\n", true);
            NetworkRepository.getInstance().initForNewCycle();
            NetworkRepository.getInstance().startNewCycle();
        } else {
            currentTimesCycleFailedSinceSSLFailedCounter = -1;
            startHttpResponseHandle(result, responseCode);
        }
        if(this instanceof GetAuthenticationTokenRequest) {
            Log.i("CycleLog", cycleLogId() + "           url:" + fullUrlName);
        }
        Log.i("CycleLog",cycleLogId()+"           body:" +body);
        Log.i("CycleLog",cycleLogId()+"           responseCode:" +responseCode);
        Log.i("CycleLog",cycleLogId()+"           result:" +result);
        if(fixCycleBug) {
            CycleManager.getInstance().removeTask(this);
        }

        if(this instanceof GetAuthenticationTokenRequest){
            if (cycleState==0) {
                cycleState = 2;
                AutoRestartManager.getInstance().cycleFailed(-1);
            }
        }
    }
static int cycleLogId_static;
    Integer cycleLogId;
    private String cycleLogId(){
        if(cycleLogId==null){
            cycleLogId= ++cycleLogId_static;
        }

        return "["+cycleLogId+"] ";
    }

    protected int getSpacesToIndentEachLevel() {
        return SPACES_TO_INDENT_EACH_LEVEL;
    }

    private void printResponseLog(int responseCode, long packetRTT) {
        if (NetworkRepositoryConstants.IS_NETWORK_LOG_ON) {
            Log.i(TAG,  getTagName()+ "<- Response : " + responseCode + "#   RTT in Mils.: " + packetRTT);
            LoggingUtil.updateNetworkLog("\n<- Response : " + getTagName() + " " + responseCode + "\n   RTT in Mils.: " + packetRTT, false);
        }
    }

    private String getResultAfterDecode(String result) {
        result = result.replace("\\", "");
        result = result.replace("\"{", "{");
        result = result.replace("}\"", "}");
        result = result.replace("\"[", "[");
        result = result.replace("]\"", "]");
        return result;
    }

    protected abstract String getHttpRequestType();


    protected String getTagName() {
        return this.getClass().getSimpleName();
    }

    protected abstract String getServiceRequestString();

    protected abstract String getBody();

    protected void printBasicRequestLogs(String body) {
        if (NetworkRepositoryConstants.IS_NETWORK_LOG_ON) {
            //Log.i("ServerLog","Function: "+body);
            LoggingUtil.updateNetworkLog("\n\n" + getTagName() + "\n" + TimeUtil.getCurrentTimeStr() + " : " + getXmlLogsToShow(body, true), false);
            Log.i(TAG,  getTagName()+ "##" + getXmlLogsToShow(body, false));
        }
    }

    private String getXmlLogsToShow(String body, boolean shouldShowPrettyPrint) {
        String bodyToPrint = (body.isEmpty() ? "" : (shouldShowPrettyPrint ? body : getPrettyXmlPrint(body, "4")));
        return "##" + "-> Request : " + getTagName() + "#   " + getAdditionalInfo() + fullUrlName
                + "#   DeviceId = " + NetworkRepository.getDeviceSerialNumber() + ", Token = " + NetworkRepository.getInstance().getTokenKey()
                + "#   Body: " + bodyToPrint + "# ";
    }

    protected String getAdditionalInfo() {
        return "";
    }

    public static String getPrettyXmlPrint(String input, String indent) {
        Source xmlInput = new StreamSource(new StringReader(input));
        StringWriter stringWriter = new StringWriter();
        String pretty = "";
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indent);
            transformer.transform(xmlInput, new StreamResult(stringWriter));

            pretty = stringWriter.toString();
            pretty = pretty.replace("\r\n", "\n");
            return pretty;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pretty;
    }

    protected void startHttpResponseHandle(String result, int responseCode) {

    }

    boolean fixCycleBug = false;
    UUID executeUid;
    public void execute(){
        if(!fixCycleBug){

            if(this instanceof GetAuthenticationTokenRequest){
                Log.i("CycleLog","");
                Log.i("CycleLog","-------------------------------------------------");
                Log.i("CycleLog","-------------------START CYCLE-------------------");
                Log.i("CycleLog","-------------------------------------------------");
            }else  if(this instanceof PostTerminateRequest){
                Log.i("CycleLog","-------------------END CYCLE---------------------");
                Log.i("CycleLog","");
            }

            Log.i("CycleLog",cycleLogId()+"start ---> "+   getTagName());
            executeSuper();
            return;
        }
        executeUid = UUID.randomUUID();
        //Log.i("executeLogT", getTagName());
        //Log.i("executeLog",Log.getStackTraceString(new Throwable()));

        if(this instanceof GetAuthenticationTokenRequest){
            executeStartCycleTask();
        }else  if(this instanceof PostTerminateRequest){
            executeEndCycleTask();
        }else{
            executeRegularCycleTask();
        }
    }

    public void executeStartCycleTask() {
        int number = CycleManager.getInstance().startNew();
        Log.i("CycleLog",cycleLogId()+  " ");
        Log.i("CycleLog",cycleLogId()+  "-----------------------------------------");
        Log.i("CycleLog",cycleLogId()+  "          Start Cycle number "+number+"           ");
        Log.i("CycleLog",cycleLogId()+  "-----------------------------------------");
        executeRegularCycleTask();
    }

    public void executeEndCycleTask(){
       if(!CycleManager.getInstance().setTerminateTask()){
           Log.i("CycleLog",cycleLogId()+  "cancel twice terminate task");
           return;
       }

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean canRunTerminateTask=CycleManager.getInstance().canRunTerminateTask();
                while (!canRunTerminateTask){
                    sleep(1000);
                    canRunTerminateTask=CycleManager.getInstance().canRunTerminateTask();
                }
                executeSuper();
                Log.i("CycleLog",cycleLogId()+  "execute "+getTagName());
                Log.i("CycleLog",cycleLogId()+  "-----------------------------------------");
                Log.i("CycleLog",cycleLogId()+  "               End Cycle                 ");
                Log.i("CycleLog",cycleLogId()+  "-----------------------------------------");
            }
        }).start();
    }
    void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void executeRegularCycleTask(){
        Log.i("CycleLog",cycleLogId()+  "execute "+getTagName());
        CycleManager.getInstance().putTask(this);
        executeSuper();
    }
    public void executeSuper(){
         super.execute();
    }
}