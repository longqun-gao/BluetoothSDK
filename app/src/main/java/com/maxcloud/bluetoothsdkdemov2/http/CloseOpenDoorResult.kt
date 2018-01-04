package com.maxcloud.bluetoothsdkdemov2.http

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * 描    述：
 * 作    者：AleX.D.Tu
 * 时    间：2017/10/9
 * 版    权：迈斯云门禁网络科技有限公司
 */

class CloseOpenDoorResult {
    @SerializedName("ResultCode")
    @Expose
    var resultCode: Int = 0
    @SerializedName("Message")
    @Expose
    var message: String? = null

    override fun toString(): String {
        return "CloseOpenDoorResult{" +
                "ResultCode=" + resultCode +
                ", Message='" + message + '\'' +
                '}'
    }
}
