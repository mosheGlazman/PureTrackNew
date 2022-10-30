package com.supercom.puretrack.data.cycle.tasks

import android.util.Log
import com.supercom.puretrack.data.cycle.Cycle
import com.supercom.puretrack.data.cycle.CycleRepository
import com.supercom.puretrack.data.cycle.JsonKotlin
import com.supercom.puretrack.data.cycle.enums.E_TaskStatus
import com.supercom.puretrack.data.cycle.enums.E_TaskType
import com.supercom.puretrack.data.cycle.interfaces.APIInterface
import com.supercom.puretrack.data.cycle.interfaces.TaskInterface
import com.supercom.puretrack.data.cycle.resultObjects.base.*
import com.supercom.puretrack.data.repositories.KnoxProfileConfig
import com.supercom.puretrack.util.constants.network.ServerUrls
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.Serializable
import java.net.HttpURLConnection
import java.net.URL
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

abstract class TaskBase (var cycle : Cycle): Serializable {
    abstract val type: E_TaskType?
    abstract fun handledData(resultData: String = ""): BaseTaskResult
    abstract fun post(retrofitBuilder: APIInterface)
    abstract fun getMainPropertyName() : String
    abstract fun handledError()

    val TAG="TaskBase"

    var status = E_TaskStatus.none
    var error = ""
    var success: Boolean? = null
    var listener: TaskInterface? = null
    var nextTask: TaskBase? = null

    fun hasNextTask(): Boolean {
        return nextTask!= null
    }
    lateinit  var repository : CycleRepository

    fun addTask(task : TaskBase) {
        if(nextTask != null){
            nextTask!!.addTask(task)
        }else{
            nextTask=task
        }
    }

    fun run(listener: TaskInterface?) {
        this.listener = listener
        this.repository = cycle.repository

        setNextStatus(E_TaskStatus.connecting)
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                }

                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                }

                override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
            })
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

// Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory

           var fullUrlName =  ServerUrls.getInstance().selectedServerUrlAddress
            if (fullUrlName.contains("/UploadPhotoOnDemand") || fullUrlName.contains("/DeviceInstallation")) {
                fullUrlName = fullUrlName.replace("RestfulService.svc/", "")
            }
            if (fullUrlName.startsWith("Http")) {
                fullUrlName = fullUrlName.replace("Http", "http")
            }

           //val url = URL(fullUrlName)
           //if (fullUrlName.startsWith("https")) {
           //    httpURLConnection = url.openConnection() as HttpsURLConnection
           //} else {
           //    httpURLConnection = url.openConnection() as HttpURLConnection
           //}

            if (fullUrlName.contains("https")) {
              if (KnoxProfileConfig.getInstance().useClientCert) {
                    //useClientSSLCertificate();
                } else {
                    //disableSSLCertificateChecking()
                }
            }

// connect to server
            val client = OkHttpClient.Builder().sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager).hostnameVerifier{ _, _ -> true }.build()
            val retrofitBuilder = Retrofit.Builder()
             .addConverterFactory(GsonConverterFactory.create())
             .baseUrl(fullUrlName)
             .client(client)
             .build()
             .create(APIInterface::class.java)

            post(retrofitBuilder)
        } catch (e: Exception) {
            setErrorResponse(e)
        }
    }

    private fun setNextStatus(status: E_TaskStatus) {
        this.status = status
        Log.i(TAG,"$type -> $status")
        listener!!.onTaskStatusUpdated(this)
    }

    protected fun getCallBack(): Callback<Any?> {
        return object : Callback<Any?> {
            override fun onResponse(
                call: Call<Any?>,
                response: Response<Any?>
            ) {
                try {
                    setNextStatus(E_TaskStatus.handledError)
                    if (response == null) {
                        setErrorResponse("Connection failed")
                        return
                    }

                    if (response.code() != 200) {
                        setErrorResponse("Connection failed Code: ${response.code()} ${response.message()}")
                        return
                    }

                    val responseBody = response.body()
                    if (responseBody == null) {
                        setErrorResponse("Connection failed 2")
                        return
                    }

                    setNextStatus(E_TaskStatus.handledData)
                    Log.i(TAG,"$type data: $responseBody")

                    if (isError(responseBody.toString())) {
                        handledError()
                        return
                    }

                    var base: BaseTaskResult = handledData(responseBody.toString())
                    if (base == null) {
                        setErrorResponse("ERROR parse failed for task:${type} ")
                        return
                    }

                    if (!base.isSuccess()) {
                        setErrorResponse("ERROR status:${base.status} ${base.error}")
                        return
                    }
                } catch (e: java.lang.Exception) {
                    setErrorResponse(e)
                    return
                }

                success = true
                setNextStatus(E_TaskStatus.finish)
            }

            override fun onFailure(call: Call<Any?>, t: Throwable) {
                setErrorResponse(t)
            }
        }
    }

    private fun setErrorResponse(t: Throwable) {
        Log.e(TAG, "error", t)
        setErrorResponse(t.message!!)
    }

    private fun setErrorResponse(errorMessage: String) {
        error=errorMessage
        setNextStatus(E_TaskStatus.error)
        Log.e(TAG,"$type error: $errorMessage")
    }

    private fun isError(responseBody : String): Boolean {
        try {
            var data = responseBody.replace(getMainPropertyName(), "BaseObject")

            var error = JsonKotlin.toObject(data, BaseErrorResultObject::class.java)
            if(error == null){
                var error = JsonKotlin.toObject(data, BaseErrorListObject::class.java)
                if(error == null) {
                    setErrorResponse("parse error")
                    return true
                }
            }

            if (!error!!.BaseObject.isSuccess()) {
                setErrorResponse(error.BaseObject.error)
                return true
            }

            var errorList = JsonKotlin.toObject(data, BaseErrorListResultObject::class.java)
                ?: return false

            errorList.BaseObject.copyErrorFromChildren()
            if (!errorList.BaseObject.isSuccess()) {
                setErrorResponse(errorList.BaseObject.error)
                return true
            }

            return false
        }catch (ex : java.lang.Exception){
            setErrorResponse(ex)
            return true
        }
    }



    //protected fun replaceJsonChars(resultData: String): String {
    //    Log.i("replaceJsonChars", "from: $resultData")
//
    //    var res = resultData
//
    //    res = res.replace("\\n", "\n")
    //    res = res.replace("\\", "")
    //    res = res.replace("\"{", "{")
    //    res = res.replace("}\"", "}")
    //    res = res.replace("\"[", "[")
    //    res = res.replace("]\"", "]")
//
    //    Log.i("replaceJsonChars","to  : $res")
    //    return res
    //}
}