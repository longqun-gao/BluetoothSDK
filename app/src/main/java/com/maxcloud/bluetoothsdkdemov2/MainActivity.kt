package com.maxcloud.bluetoothsdkdemov2

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ScrollView
import com.maxcloud.bluetoothreadersdkv2.*
import com.maxcloud.bluetoothsdkdemov2.http.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference
import java.util.*

class MainActivity : AppCompatActivity(), ProtocolListener.onOpenDoorListener,
        ScanDeviceHelper.OnBluetoothScanCallback, AdapterView.OnItemClickListener,
        View.OnClickListener {
    private val scanTime:Int = 20000// 扫描时间20s
    private val openTime:Long = 10000// 刷卡时间10s
    private var scanHelper = ScanDeviceHelper()
    private val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
    private var alertDialog: AlertDialog? = null
    private val msg = "<br />易回家需要以下权限<br /><br /><b>位置</b><br />搜索蓝牙<br />"
    private val REQUEST_FINE_LOCATION = 0
    private val REQUEST_OPEN_LOCATION = 1
    private val mStringBuilder = StringBuilder()

    private val mBluetoothDevices = ArrayList<String>()
    private var mBluetoothDevice: BleDevice? = null
    private var mCurDevice:BleDevice? = null
    private var mLeDeviceListAdapter: LeDeviceListAdapter? = null

    private var currentCardStr: String? = null
    private var isRefreshing = false
    private var openCert: ByteArray? = null
    private var connectKey:ByteArray? = null
    private val mBaseUrl = "http://%s:%s/%s"

    private val mBlueJni: BluetoothJni = BluetoothJni()

    override fun onClick(v: View?) {
        when (v) {
            mOpenBtn -> {
                openDoor()
            }
            mScanBtn -> {
                scanLeDevice(true)
            }
            mGetOpenInfoBtn -> {
                val buildIds = mBuildId.text.toString()
                if (TextUtils.isEmpty(buildIds)) {
                    appendString("楼栋Id 不能为空")
                    return
                }

                val buildId = Integer.parseInt(buildIds)
                val url1 = getFinalUrl("get_door")
                if (TextUtils.isEmpty(url1)) {
                    return
                }
                Log.w("url1", "url1----->" + url1)
                GetOpenInfoTask(this, url1).execute(buildId)
            }
            mGetOpenCertBtn -> {
                if (TextUtils.isEmpty(mPersonId.text.toString())) {
                    appendString("人员Id 不能为空")
                    return
                }
                val url2 = getFinalUrl("get_opendoor_certificate")
                if (TextUtils.isEmpty(url2)) {
                    return
                }
                GetOpenDoorCertTask(this, url2).execute(Integer.parseInt(mPersonId.text.toString()))
            }
            mResetOpenCertBtn -> {
                if (TextUtils.isEmpty(mBuildId.text.toString())) {
                    appendString("楼栋Id 不能为空")
                    return
                }
                val url3 = getFinalUrl("reset_opendoor_certificate")
                if (TextUtils.isEmpty(url3)) {
                    return
                }
                ResetOpenDoorCertTask(this, url3).execute(Integer.parseInt(mBuildId.text.toString()))
            }
            mCloseOpenDoorBtn -> {
                if (TextUtils.isEmpty(mBuildId.text.toString())) {
                    appendString("楼栋Id 不能为空")
                    return
                }
                val url4 = getFinalUrl("close_opendoor")
                if (TextUtils.isEmpty(url4)) {
                    return
                }
                CloseOpenDoorTask(this, url4).execute(Integer.parseInt(mBuildId.text.toString()))
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        currentCardStr = ""
        mBluetoothDevice = mLeDeviceListAdapter!!.getDevice(position)
        mLeDeviceListAdapter!!.chooseDevice(mBluetoothDevice!!.address)
        appendString("选择设备" + mBluetoothDevice!!.address)
        mOpenBtn.isEnabled = true
    }

    override fun onScanFinish() {
        scanLeDevice(false)
    }

    override fun onScanFailed(p0: String?) {
        appendString(p0!!)
        scanLeDevice(false)
        isRefreshing = false
    }

    override fun onScanDevice(p0: BleDevice?) {
        val address = p0!!.address
        appendString("搜索到设备" + address)
        if (!mBluetoothDevices.contains(address)) {
            mBluetoothDevices.add(address)
            mLeDeviceListAdapter!!.addDevice(p0)
            // 默认选中第一个
            if (mCurDevice == null) {
                mCurDevice = p0
                mBluetoothDevice = p0
                appendString("默认选中第一个设备" + address)
                mLeDeviceListAdapter!!.chooseDevice(p0.address)
                mOpenBtn.isEnabled = true
            }
        }
    }

    override fun onSuccess() {
        ProtocolHelper.getInstance(this).close()
        runOnUiThread {
            appendString("刷卡成功（请以设备为主）" + mBluetoothDevice!!.address)
            setEnable(true)
        }
    }

    override fun onFailed(p0: String?) {
        ProtocolHelper.getInstance(this).close()
        runOnUiThread {
            appendString("刷卡失败（请以设备为主）" + p0)
            setEnable(true)
            scanLeDevice(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mDeviceList.onItemClickListener = this

        mScanBtn.setOnClickListener(this)
        mOpenBtn.setOnClickListener(this)
        mGetOpenInfoBtn.setOnClickListener(this)
        mGetOpenCertBtn.setOnClickListener(this)
        mResetOpenCertBtn.setOnClickListener(this)
        mCloseOpenDoorBtn.setOnClickListener(this)

        mayRequestLocation()
        mLeDeviceListAdapter = LeDeviceListAdapter(this)
        mDeviceList.adapter = mLeDeviceListAdapter
        mDeviceList.onItemClickListener = this
        mIpEdt.setText(SPUtil.getIpAddress(this))
        mPortEdt.setText(SPUtil.getPort(this))

    }

    private fun mayRequestLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (findDeniedPermissions(this, *permissions).isNotEmpty()) {
                alertDialog = AlertDialog.Builder(this)
                        .setTitle("权限申请")
                        .setMessage(Html.fromHtml(msg))
                        .setPositiveButton("确定") { dialog, which ->
                            alertDialog!!.dismiss()
                            ActivityCompat.requestPermissions(this@MainActivity, permissions, REQUEST_FINE_LOCATION)
                        }
                        .setCancelable(false)
                        .show()
            } else {
                // 检查位置信息有没打开
                turnOnGPS()
            }
        }
    }

    private fun turnOnGPS() {
        if (!checkLocationOn()) {
            AlertDialog.Builder(this)
                    .setMessage("我们需要打开位置服务才可以搜索蓝牙设备，请打开位置服务")
                    .setCancelable(false)
                    .setNegativeButton("取消") { dialog, which ->
                        appendString("我们需要打开位置服务才可以搜索蓝牙设备，请打开位置服务")
                        finish()
                    }
                    .setPositiveButton("确定") { dialog, which -> openGPS() }.show()
        }
    }

    private fun openGPS() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivityForResult(intent, REQUEST_OPEN_LOCATION)
    }

    private fun checkLocationOn(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun findDeniedPermissions(activity: Activity, vararg permission: String): List<String> {
        val denyPermissions = permission.filter { ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED }
        return denyPermissions
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_OPEN_LOCATION ->
                if (!checkLocationOn()) {
                    //ToastHelper.showLongToast(this, "我们需要打开位置服务才可搜索到蓝牙设备，请打开位置服务")
                    finish()
                }
        }
    }

    private fun getFinalUrl(methodString: String): String {
        val ipString = mIpEdt.text.toString()
        if (TextUtils.isEmpty(ipString)) {
            appendString("IP地址不能为空！")
            return ""
        }
        SPUtil.saveIpAddress(this, ipString)
        val port = mPortEdt.text.toString()
        if (TextUtils.isEmpty(port)) {
            appendString("端口不能为空！")
            return ""
        }
        SPUtil.savePort(this, port)
        return String.format(Locale.CHINA, mBaseUrl, ipString, port, methodString)
    }

    private fun openDoor() {
        if (mBluetoothDevice == null) {
            appendString("还没选择设备！")
            return
        }

        if (connectKey == null || connectKey!!.isEmpty()) {
            appendString("还没获取通讯密钥！")
            return
        } else if (connectKey!!.size != 16) {
            appendString("通讯密钥不合法，请检查数据！")
            return
        }
        val connectKeyB = connectKey

        if (openCert == null || openCert!!.isEmpty()) {
            appendString("还没获取开门证书！")
            return
        } else if (openCert!!.size != 10) {
            appendString("开门证书不合法，请检查数据！")
            return
        }

        var openDataB = openCert
        openDataB = Conversion.bytesHighLowChange(openDataB)
        appendString("openDataB反一下：" + Conversion.Bytes2HexString(openDataB))

        scanLeDevice(false)

        appendString("正在刷卡..." + mBluetoothDevice!!.address)
        setEnable(false)
        ProtocolHelper.getInstance(this@MainActivity).openDoor(this, mBluetoothDevice!!.address, openDataB,
                Conversion.HexString2Bytes(mBluetoothDevice!!.randomCast), connectKeyB, openTime)
    }

    private class GetOpenInfoTask constructor(activity: Activity, private val mUrl: String) : AsyncTask<Int, Void, GetDoorResult>() {
        private val mActivityWeakReference: WeakReference<Activity> = WeakReference(activity)

        override fun onPreExecute() {
            super.onPreExecute()
            val activity = mActivityWeakReference.get() as MainActivity?
            ProgressDialogUtils.showProgressDialogNoCancel(activity, "正在获取蓝牙开门信息, 请稍候...")
        }

        override fun doInBackground(vararg params: Int?): GetDoorResult? {
            try {
                return HttpHelper.getInstance()!!.getDoor(mUrl, params[0]!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: GetDoorResult?) {
            super.onPostExecute(result)
            ProgressDialogUtils.dismissProgressDialog()
            val activity = mActivityWeakReference.get() as MainActivity?
            if (result != null) {
                activity!!.appendString(result.toString())
                activity.connectKey = result.certificate
            } else {
                activity!!.appendString("网络错误，请检查您的手机网络")
            }
        }
    }

    private class GetOpenDoorCertTask constructor(activity: Activity, private val mUrl: String): AsyncTask<Int, Void, GetOpenDoorCert>() {
        private val mActivityWeakReference: WeakReference<Activity> = WeakReference(activity)

        override fun onPreExecute() {
            super.onPreExecute()
            val activity = mActivityWeakReference.get() as MainActivity?
            ProgressDialogUtils.showProgressDialogNoCancel(activity, "正在获取人员开门证书, 请稍候...")
        }

        override fun doInBackground(vararg params: Int?): GetOpenDoorCert? {
            try {
                return HttpHelper.getInstance()!!.getOpenDoorCert(mUrl, params[0]!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: GetOpenDoorCert?) {
            super.onPostExecute(result)
            ProgressDialogUtils.dismissProgressDialog()
            val activity = mActivityWeakReference.get() as MainActivity?
            if (result != null) {
                activity!!.appendString(result.toString())
                activity.openCert = result.certificate
            } else {
                activity!!.appendString("网络错误，请检查您的手机网络")
            }
        }
    }

    private class ResetOpenDoorCertTask constructor(activity: Activity, private val mUrl: String): AsyncTask<Int, Void, ResetOpenDoorCert>() {
        private val mActivityWeakReference: WeakReference<Activity> = WeakReference(activity)

        override fun onPreExecute() {
            super.onPreExecute()
            val activity = mActivityWeakReference.get() as MainActivity?
            ProgressDialogUtils.showProgressDialogNoCancel(activity, "正在重置人员开门证书, 请稍候...")
        }

        override fun doInBackground(vararg params: Int?): ResetOpenDoorCert? {
            try {
                return HttpHelper.getInstance()!!.resetOpenDoorCert(mUrl, params[0]!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: ResetOpenDoorCert?) {
            super.onPostExecute(result)
            ProgressDialogUtils.dismissProgressDialog()
            val activity = mActivityWeakReference.get() as MainActivity?
            if (result != null) {
                activity!!.appendString(result.toString())
                activity.openCert = result.certificate
            } else {
                activity!!.appendString("网络错误，请检查您的手机网络")
            }
        }
    }

    private class CloseOpenDoorTask constructor(activity: Activity, private val mUrl: String): AsyncTask<Int, Void, CloseOpenDoorResult>() {
        private val mActivityWeakReference: WeakReference<Activity> = WeakReference(activity)

        override fun onPreExecute() {
            super.onPreExecute()
            val activity = mActivityWeakReference.get() as MainActivity?
            ProgressDialogUtils.showProgressDialogNoCancel(activity, "正在重置人员开门证书, 请稍候...")
        }

        override fun doInBackground(vararg params: Int?): CloseOpenDoorResult? {
            try {
                return HttpHelper.getInstance()!!.closeOpenDoor(mUrl, params[0]!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: CloseOpenDoorResult?) {
            super.onPostExecute(result)
            ProgressDialogUtils.dismissProgressDialog()
            val activity = mActivityWeakReference.get() as MainActivity?
            if (result != null) {
                activity!!.appendString(result.toString())
                activity.openCert = null
                activity.connectKey = null
            } else {
                activity!!.appendString("网络错误，请检查您的手机网络")
            }
        }
    }

    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            isRefreshing = true
            appendString("开始搜索蓝牙设备")

            setEnable(false)
            mBluetoothDevices.clear()
            mLeDeviceListAdapter!!.clear()
            mCurDevice = null

            scanHelper.startLeScan(this, scanTime)
        } else {
            appendString("结束搜索")
            mProgress.visibility = View.GONE
            setEnable(true)
            isRefreshing = false
            scanHelper.stopLeScan()
        }
    }

    private fun setEnable(isEnable: Boolean) {
        if (isEnable) {
            mProgress.visibility = View.GONE
        } else {
            mProgress.visibility = View.VISIBLE
        }
        mScanBtn.isEnabled = isEnable
        mOpenBtn.isEnabled = isEnable
    }

    private fun getSDKVersion() {
        ProtocolHelper.getInstance(this).sdkVersion
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRefreshing) {
            scanLeDevice(false)
        }
        ProtocolHelper.getInstance(this@MainActivity).close()
    }

    private fun appendString(s:String) {
        mStringBuilder.append(s)
        mStringBuilder.append("\n")
        mRecord.text = ""
        mRecord.text = mStringBuilder.toString()
        mScroll.post({
            mScroll.fullScroll(ScrollView.FOCUS_DOWN)
        })
    }
}
