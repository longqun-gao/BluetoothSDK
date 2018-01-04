package com.maxcloud.bluetoothsdkdemov2.http

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

/**
 * 描    述：服务器返回基础类，包括返回码和错误原因。
 * 作    者：向晓阳
 * 时    间：2016/2/24
 * 版    权：迈斯云门禁网络科技有限公司
 */
class DataResult<T> {

    @SerializedName("msgId")
    @Expose
    var msgId: String? = null
    @SerializedName("reason")
    @Expose
    var reason: String? = null
    @SerializedName("msg")
    @Expose
    var msg: String? = null
    @SerializedName("btnText")
    @Expose
    var btnText: String? = null
    @SerializedName("nowTime")
    @Expose
    var nowTime: String? = null
    @SerializedName("data")
    @Expose
    var data: T? = null
    @SerializedName("message")
    @Expose
    var message: String? = null

    /**
     * 判断是否成功。
     *
     * @return 如果成功，则返回 true ，否则，返回 false 。
     */
    val isSuccess: Boolean
        get() = "OK" == reason

    /**
     * 判断是否为未登录.
     *
     * @return 如果为未登录，则返回 true ，否则，返回 false 。
     */
    val isNotLogin: Boolean
        get() = CODE_NOT_LOGIN == reason

    /**
     * 判断是否为凭证无效.
     *
     * @return 如果为未登录，则返回 true ，否则，返回 false 。
     */
    val isInvalidToken: Boolean
        get() = CODE_INVALID_TOKEN == reason

    override fun toString(): String {
        return "DataResult{" +
                "msgId='" + msgId + '\'' +
                ", reason='" + reason + '\'' +
                ", msg='" + msg + '\'' +
                ", btnText='" + btnText + '\'' +
                ", nowTime='" + nowTime + '\'' +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}'
    }

    companion object {
        private val CODE_NOT_LOGIN = "HasNotLogin"
        private val CODE_INVALID_TOKEN = "InvalidToken"
        private val DATE_FMT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA)
    }
}
