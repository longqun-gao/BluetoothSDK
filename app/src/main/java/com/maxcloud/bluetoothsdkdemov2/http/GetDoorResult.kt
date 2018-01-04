package com.maxcloud.bluetoothsdkdemov2.http

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.maxcloud.bluetoothreadersdkv2.Conversion

/**
 * 描    述：
 * 作    者：AleX.D.Tu
 * 时    间：2017/9/30
 * 版    权：迈斯云门禁网络科技有限公司
 */

class GetDoorResult {
    @SerializedName("ResultCode")
    @Expose
    var resultCode: Int = 0
    @SerializedName("Message")
    @Expose
    var message: String? = null
    @SerializedName("BuildingName")
    @Expose
    var buildingName: String? = null
    @SerializedName("Certificate")
    @Expose
    var certificate: ByteArray? = null
    @SerializedName("CertificateId")
    @Expose
    var certificateId: ByteArray? = null

    override fun toString(): String {
        return "GetDoorResult{" +
                "ResultCode=" + resultCode +
                ", Message='" + message + '\'' +
                ", BuildingName='" + buildingName + '\'' +
                ", Certificate=" + Conversion.Bytes2HexString(certificate!!) +
                ", CertificateId=" + Conversion.Bytes2HexString(certificateId!!) +
                '}'
    }
}
