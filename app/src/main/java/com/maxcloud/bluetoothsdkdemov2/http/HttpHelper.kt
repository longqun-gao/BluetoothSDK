package com.maxcloud.bluetoothsdkdemov2.http

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 描    述：
 * 作    者：AleX.D.Tu
 * 时    间：2017/5/12
 * 版    权：迈斯云门禁网络科技有限公司
 */
class HttpHelper private constructor() {
    private val mIService: IService
    private val mTimeOut: Long = 15//连接服务器超时时间（秒）

    init {
        mThis = this
        val loggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
            //打印retrofit日志
            try {
                Log.i("RetrofitLog", "RETROFIT===" + URLDecoder.decode(message, "UTF-8"))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        })
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(mTimeOut, TimeUnit.SECONDS)
        okHttpClient.cookieJar(object : CookieJar {
            private var cookies: List<Cookie> = ArrayList()

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                this.cookies = cookies
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookies
            }
        }).build()

        val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")//时间转化为特定格式
                .excludeFieldsWithoutExposeAnnotation()//不导出实体中没有用@Expose注解的属性
                //.setPrettyPrinting() //对json结果格式化
                .create()

        val retrofitMax = Retrofit.Builder()
                .baseUrl(SERVER_ADDRESS_D)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient.build())
                .build()

        mIService = retrofitMax.create(IService::class.java)
    }

    @Throws(Exception::class)
    fun getDoor(url: String, BuildingId: Int): GetDoorResult {
        synchronized(rwLock) {
            val body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), "{\"BuildingId\":$BuildingId}")
            return executeCall(mIService.getDoor(url, body))
        }
    }

    @Throws(Exception::class)
    fun getOpenDoorCert(url: String, PersonId: Int): GetOpenDoorCert {
        synchronized(rwLock) {
            val body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), "{\"PersonId\":$PersonId}")
            return executeCall(mIService.getOpenDoorCert(url, body))
        }
    }

    @Throws(Exception::class)
    fun resetOpenDoorCert(url: String, PersonId: Int): ResetOpenDoorCert {
        synchronized(rwLock) {
            val body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), "{\"PersonId\":$PersonId}")
            return executeCall(mIService.resetOpenDoorCert(url, body))
        }
    }

    @Throws(Exception::class)
    fun closeOpenDoor(url: String, PersonId: Int): CloseOpenDoorResult {
        synchronized(rwLock) {
            val body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), "{\"PersonId\":$PersonId}")
            return executeCall(mIService.closeOpenDoor(url, body))
        }
    }

    @Throws(Exception::class)
    private fun <T> executeCall(call: Call<T>): T {
        val response: Response<T> = call.execute()

        if (!response.isSuccessful) {
            throw IOException(String.format(Locale.getDefault(), "msg:%s,code:%s", response.message(), response.code()))
        }
        val result = response.body()

        Log.d("executeCall", result.toString())
        return result
    }

    companion object {
        private val TAG = HttpHelper::class.java.simpleName
        @Volatile private var mThis: HttpHelper? = null
        private val rwLock = Any()

        fun getInstance(): HttpHelper? {
            if (null == mThis) {
                synchronized(HttpHelper::class.java) {
                    if (null == mThis) {
                        mThis = HttpHelper()
                    }
                }
            }

            return mThis
        }

        private val timeDiff: Long = 0

        private val SERVER_ADDRESS_D = "http://10.1.1.201:19990/"
    }
}
