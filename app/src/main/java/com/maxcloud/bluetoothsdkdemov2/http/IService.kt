package com.maxcloud.bluetoothsdkdemov2.http

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * 描    述：
 * 作    者：AleX.D.Tu
 * 时    间：2017/5/12
 * 版    权：迈斯云门禁网络科技有限公司
 */

interface IService {

    @Headers("Content-Type: application/json", "Accept: application/json")//需要添加头
    //    @POST("get_door")// 获取蓝牙开门信息
    @POST// 获取蓝牙开门信息
    fun getDoor(@Url url: String, @Body body: RequestBody): Call<GetDoorResult>

    @Headers("Content-Type: application/json", "Accept: application/json")
    //    @POST("get_opendoor_certificate")// 获取人员开门证书
    @POST// 获取人员开门证书
    fun getOpenDoorCert(@Url url: String, @Body body: RequestBody): Call<GetOpenDoorCert>

    @Headers("Content-Type: application/json", "Accept: application/json")
    //    @POST("reset_opendoor_certificate")// 重置人员开门证书
    @POST// 重置人员开门证书
    fun resetOpenDoorCert(@Url url: String, @Body body: RequestBody): Call<ResetOpenDoorCert>

    @Headers("Content-Type: application/json", "Accept: application/json")
    //    @POST("close_opendoor")// 关闭人员蓝牙开门
    @POST// 关闭人员蓝牙开门
    fun closeOpenDoor(@Url url: String, @Body body: RequestBody): Call<CloseOpenDoorResult>

}
