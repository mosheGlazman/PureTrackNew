package com.supercom.puretrack.data.cycle

import android.annotation.SuppressLint
import android.util.Log
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*


object JsonKotlin {
    val TAG = "JSON"
    internal var format_Json_Date = "yyyy-MM-dd HH:mm:ss"

    inline fun <reified T> Gson.fromJson(json: String) =
        this.fromJson<T>(json, object : TypeToken<T>() {}.type)

    fun toString(obj: Any?): String {


        try {
            val gson = GsonBuilder()
                .registerTypeAdapter(
                    Date::class.java,
                    DateAdapter()
                )
                .create()

            return gson.toJson(obj)
        } catch (ex: Exception) {
            Log.e("JsonMAPPER", "failed to parse to json string$obj", ex)
            return ""
        }
    }

    fun <T> getObjectList(jsonString: String?, cls: Class<T>?): List<T>? {
        val list: MutableList<T> = ArrayList()
        try {
            val gson = Gson()
            val arry = JsonParser().parse(jsonString).asJsonArray
            for (jsonElement in arry) {
                list.add(gson.fromJson(jsonElement, cls))
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun <T> toObject(responseString: String?, valueType: Class<T>): T? {
        if (responseString == null || responseString.length == 0) {
            return null
        }
        try {
            val gson = GsonBuilder()
                .registerTypeAdapter(
                    Date::class.java,
                    DateDeAdapter()
                )
                .create()

            return gson.fromJson(responseString, valueType)
        } catch (e: Exception) {
           //Log.e(
           //    "JsonMAPPER",
           //    "failed to parse to :" + valueType.javaClass.name + "/n" + responseString + "/n",
           //    e
           //)
        }

        return null
    }

    fun <T> getJsonArray(responseString: String, valueType: Type): ArrayList<T> {
        if (responseString.isNullOrEmpty())
            return ArrayList<T>()

        try {

            val result: ArrayList<T> =
                parseArray<ArrayList<T>>(json = responseString, typeToken = valueType)
            return result
        } catch (ex: Exception) {
            Log.e(TAG, "error: " + ex.message)
        }
        return ArrayList<T>()
    }

    inline fun <reified T> parseArray(json: String, typeToken: Type): T {
        val gson = GsonBuilder().create()
        return gson.fromJson<T>(json, typeToken)
    }

    inline fun <reified T> getListObject(responseString: String?): List<T>? {
        try {
            return Gson().fromJson<List<T>>(responseString!!)
        } catch (e: Exception) {
            Log.i(TAG, "error:" + e.message)
        }
        return null
    }

    class DateAdapter : JsonSerializer<Date> {

        override fun serialize(
            src: Date, typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {

            val sDate = SimpleDateFormat(format_Json_Date).format(src)
            return JsonPrimitive(sDate)
        }
    }

    class DateDeAdapter : JsonDeserializer<Date> {

        @SuppressLint("SimpleDateFormat")
        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Date {

            var date = Date()
            try {
                json.asString?.let {
                    date = SimpleDateFormat(format_Json_Date).parse(it) ?: Date()
                }

            } catch (ex: Exception) {
            }

            return date
        }
    }


}



