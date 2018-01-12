package com.maxcloud.bluetoothsdkdemov2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.maxcloud.bluetoothreadersdkv2.BleDevice;
import com.maxcloud.bluetoothreadersdkv2.Conversion;
import com.maxcloud.bluetoothreadersdkv2.ProtocolHelper;
import com.maxcloud.bluetoothreadersdkv2.ProtocolListener;
import com.maxcloud.bluetoothreadersdkv2.ScanDeviceHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 描    述：
 * 作    者：AleX.D.Tu
 * 时    间：2017/12/29
 * 版    权：迈斯云门禁网络科技有限公司
 */

public class SecondActivity extends AppCompatActivity implements View.OnClickListener,
        ScanDeviceHelper.OnBluetoothScanCallback, AdapterView.OnItemClickListener {
    @BindView(R.id.mDeviceList)
    ListView mDeviceList;

    @BindView(R.id.mScanBtn)
    Button mScanBtn;
    @BindView(R.id.mOpenBtn)
    Button mOpenBtn;

    @BindView(R.id.mProgress)
    ProgressBar mProgressBar;

    @BindView(R.id.mRecord)
    TextView mRecord;
    @BindView(R.id.mScroll)
    ScrollView mScrollView;

    private StringBuilder mStringBuilder = new StringBuilder();
    private ScanDeviceHelper mScanDeviceHelper = new ScanDeviceHelper();
    private int scanningTime = 5000;// 扫描时间20s
    private static final long OPEN_PERIOD = 5000;// 刷卡时间10s

    private ArrayList<String> mBluetoothDevices = new ArrayList<>();
    private BleDevice mBluetoothDevice, mCurDevice;
    private LeDeviceListAdapter mLeDeviceListAdapter;

    private String [] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
    private AlertDialog alertDialog;
    private String msg = "<br />易回家需要以下权限<br /><br /><b>位置</b><br />搜索蓝牙<br />";
    private static final int REQUEST_FINE_LOCATION = 0;
    private static final int REQUEST_OPEN_LOCATION = 1;
    private boolean isRefreshing = false;

    List<Integer> rssiList = new ArrayList<>();
    List<BleDevice> result1;
    int rssi,maxRssi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mScanBtn.setOnClickListener(this);
        mOpenBtn.setOnClickListener(this);

        mayRequestLocation();
        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        mDeviceList.setAdapter(mLeDeviceListAdapter);
        mDeviceList.setOnItemClickListener(this);
        scanLeDevice(true);
    }

    private void mayRequestLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (findDeniedPermissions(this, permissions).size() > 0) {
                alertDialog = new AlertDialog.Builder(this)
                        .setTitle("权限申请")
                        .setMessage(Html.fromHtml(msg))
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                                ActivityCompat.requestPermissions(SecondActivity.this, permissions, REQUEST_FINE_LOCATION);
                            }
                        })
                        .setCancelable(false)
                        .show();
            } else {
                // 检查位置信息有没打开
                turnOnGPS();
            }
        }
    }

    private void turnOnGPS() {
        if (!checkLocationOn()) {
            new AlertDialog.Builder(this)
                    .setMessage("我们需要打开位置服务才可以搜索蓝牙设备，请打开位置服务")
                    .setCancelable(false)
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            appendString("我们需要打开位置服务才可以搜索蓝牙设备，请打开位置服务");
                            finish();
                        }
                    })
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            openGPS();
                        }
                    }).show();
        }
    }

    private void openGPS() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, REQUEST_OPEN_LOCATION);
    }

    private boolean checkLocationOn() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private static List<String> findDeniedPermissions(Activity activity, String... permission) {
        List<String> denyPermissions = new ArrayList<>();
        for (String value : permission) {
            if (ContextCompat.checkSelfPermission(activity, value) != PackageManager.PERMISSION_GRANTED) {
                denyPermissions.add(value);
            }
        }
        return denyPermissions;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_OPEN_LOCATION:
                if (!checkLocationOn()) {
                    appendString("我们需要打开位置服务才可搜索到蓝牙设备，请打开位置服务");
                    finish();
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mScanBtn:

                break;
            case R.id.mOpenBtn:

                //openDoor();
                openDoor();
                break;
        }
    }

    private void scanLeDevice(boolean enable) {
        if (enable) {
            isRefreshing = true;
            appendString("开始搜索蓝牙设备");

            setEnable(false);
            mBluetoothDevices.clear();
            mLeDeviceListAdapter.clear();
            mCurDevice = null;

            mScanDeviceHelper.startLeScan(this, scanningTime);
        } else {
            appendString("结束搜索");
            mProgressBar.setVisibility(View.GONE);
            setEnable(true);
            isRefreshing = false;
            mScanDeviceHelper.stopLeScan();

        }
    }

    private void setEnable(boolean isEnable) {
        if (isEnable) {
            mProgressBar.setVisibility(View.GONE);
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        mScanBtn.setEnabled(isEnable);
        mOpenBtn.setEnabled(isEnable);
    }

    @Override
    public void onScanDevice(BleDevice bleDevice) {
        String address = bleDevice.getAddress();
        appendString("搜索到设备" + address);
        //将result1中的蓝牙门禁信号存储起来
        rssi = bleDevice.getRssi();
        rssiList.add(rssi);

        if (!mBluetoothDevices.contains(address)) {
            mBluetoothDevices.add(address);
            mLeDeviceListAdapter.addDevice(bleDevice);
            // 默认选中第一个
            if (mCurDevice == null) {
                mCurDevice = bleDevice;
                mBluetoothDevice = bleDevice;
                appendString("默认选中第一个设备" + address);
                mLeDeviceListAdapter.chooseDevice(mCurDevice.getAddress());
                mOpenBtn.setEnabled(true);
            }
        }
    }

    @Override
    public void onScanFinish() {
        scanLeDevice(false);
    }

    @Override
    public void onScanFailed(String s) {
        appendString(s);
        scanLeDevice(false);
        isRefreshing = false;
    }

    private void openDoor() {
        if (mBluetoothDevice == null) {
            appendString("还没选择设备！");
            return;
        }

//        if (connectKey == null || connectKey.length == 0) {
//            appendString("还没获取通讯密钥！");
//            return;
//        } else if (connectKey.length != 16){
//            appendString("通讯密钥不合法，请检查数据！");
//            return;
//        }

        /*
         * 此处填写从接口获取的通讯密钥
         */
        Log.e("通讯密钥",Constant.ConnectionKey+"");
        byte[] connectKeyB = Conversion.HexString2Bytes(Constant.ConnectionKey);

//        if (openCert == null || openCert.length == 0) {
//            appendString("还没获取开门证书！");
//            return;
//        } else if (openCert.length != 10){
//            appendString("开门证书不合法，请检查数据！");
//            return;
//        }C522FC0FA08440463A38DC89641F3A72

        /*
         * 此处填写从接口获取的开门证书
         */
        Log.e("开门证书",Constant.doorList.getOpenData()+"");
        byte[] openDataB = Conversion.HexString2Bytes(Constant.doorList.getOpenData());
        // 不需要反，开门方法里面封装了
        //openDataB = Conversion.bytesHighLowChange(openDataB);
        //appendString("openDataB反一下：" + Conversion.Bytes2HexString(openDataB));

        scanLeDevice(false);

        appendString("正在刷卡..." + mBluetoothDevice.getAddress());
        setEnable(false);
        ProtocolHelper.getInstance(SecondActivity.this).openDoor(new OnOpenDoor(), mBluetoothDevice.getAddress(), openDataB,
                Conversion.HexString2Bytes(mBluetoothDevice.getRandomCast()), connectKeyB, OPEN_PERIOD);
    }

    private class OnOpenDoor implements ProtocolListener.onOpenDoorListener {

        @Override
        public void onSuccess() {
            ProtocolHelper.getInstance(SecondActivity.this).close();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    appendString("刷卡成功（请以设备为主）" + mBluetoothDevice.getAddress());
//                    appendString("刷卡成功（请以设备为主）");
                    setEnable(true);
                }
            });
        }

        @Override
        public void onFailed(final String reason) {
            ProtocolHelper.getInstance(SecondActivity.this).close();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    appendString("刷卡失败（请以设备为主）" + reason);
                    setEnable(true);
                    // 刷卡失败有可能是广播随机数更新了，所以重新搜索一下
                    scanLeDevice(true);
                }
            });
        }
    }

    /**
     * 获取SDK版本
     */
    private void getSDKVersion() {
        ProtocolHelper.getInstance(this).getSDKVersion();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mScanDeviceHelper != null) {
            if (isRefreshing) {
                scanLeDevice(false);
            }
            mScanDeviceHelper = null;
        }
        ProtocolHelper.getInstance(SecondActivity.this).close();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mBluetoothDevice = mLeDeviceListAdapter.getDevice(i);
        mLeDeviceListAdapter.chooseDevice(mBluetoothDevice.getAddress());
        appendString("选择设备" + mBluetoothDevice.getAddress());
        mOpenBtn.setEnabled(true);
    }

    private void appendString(String string) {
        mStringBuilder.append(string);
        mStringBuilder.append("\n");
        mRecord.setText("");
        mRecord.setText(mStringBuilder.toString());
        mScrollView.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}
