package com.maxcloud.bluetoothsdkdemov2;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxcloud.bluetoothreadersdkv2.BleDevice;
import com.maxcloud.bluetoothsdkdemov2.net.HttpConnectionInter;
import com.maxcloud.bluetoothsdkdemov2.net.HttpConnectionTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 描    述：
 * 作    者：AleX.D.Tu
 * 时    间：2017/4/19
 * 版    权：迈斯云门禁网络科技有限公司
 */

public class LeDeviceListAdapter extends BaseAdapter {
    private List<BleDevice> mLeDevices;
    private LayoutInflater mInflator;
    private String mAddress;
    //private doorList doorList;
    private List<doorList> list = new ArrayList<>();
    ViewHolder viewHolder;
    String deviceName;
    BleDevice device = new BleDevice();
    String address;

    int rssi,maxRssi;
    List<Integer> rssiList = new ArrayList<>();
    List<BleDevice> result1;

    String doorID,doorName,doorPath,connectionKey,keyID,openData;

    private doorList doorList;
    private class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        ImageView deviceImg;
    }

    public LeDeviceListAdapter(Activity activity) {
        mLeDevices = new ArrayList<>();
        mInflator = activity.getLayoutInflater();
    }

    public void addDevice(BleDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
            notifyDataSetChanged();
        }
    }

    public BleDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
        notifyDataSetChanged();
    }

    public void chooseDevice(String address) {
        mAddress = address;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mInflator.inflate(R.layout.item_ble_device, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            viewHolder.deviceImg = (ImageView) view.findViewById(R.id.device_img);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        /**
         * 这里是向服务器获取我的门禁列表
         */
        String time = DateUtils.getCurrentTime_Today_Min();
        String token = MD5Utils.encode(time + "adminXH");
        String url = "http://202.105.104.105:8006/ssh/openDoor/getDoorByPhone";
        HttpConnectionTools.HttpServler(url,
                HttpConnectionTools.HttpData("projectCode", "123", "token", token,
                        "userName", "柳玉豹", "phone", "15107962485"), new HttpConnectionInter() {
                    @Override
                    public void onFinish(String content) {
                        try {
                            if(list.size() == 0){
                                JSONObject jsonObject = new JSONObject(content);
                                String result = jsonObject.getString("result");
                                jsonObject = new JSONObject(result);
                                openData = jsonObject.getString("openData");
                                JSONArray jsonArray = jsonObject.getJSONArray("doorList");
                                for(int i = 0;i<jsonArray.length();i++){
                                    jsonObject = jsonArray.getJSONObject(i);
                                    doorID =  jsonObject.getString("doorID");
                                    doorName =  jsonObject.getString("doorName");
                                    doorPath =  jsonObject.getString("doorPath");
                                    connectionKey =  jsonObject.getString("connectionKey");
                                    keyID =  jsonObject.getString("keyID");
                                    Constant.doorList = new doorList(doorID,doorName,doorPath,connectionKey,keyID,openData);

                                    list.add(Constant.doorList);
                                }
                            }
                            /**
                             * 这里判断我扫描到的门禁列表里
                             * 是否有和我的门禁列表相同的门
                             *
                             * 判断方法：判断Key Id是否相同
                             * 相同的则添加到result1
                             *
                             * result1中的数据是最终的结果，
                             * 里面的门禁列表我全部都有权限开门
                             */
                            result1 = new ArrayList<>();
                            for (BleDevice bleDevice : mLeDevices) {
                                for (doorList doorList : list) {
                                    if (bleDevice.getKeyId().equals(doorList.getKeyID())){
                                        Constant.ConnectionKey = doorList.getConnectionKey();
                                        result1.add(bleDevice);
                                        //将result1中的蓝牙门禁信号存储起来
                                        rssi = result1.get(i).getRssi();
                                        rssiList.add(rssi);
//                                        list.clear();
//                                        Constant.doorList = new doorList(doorID,doorName,doorPath,connectionKey,keyID,openData);
//                                        Log.e("Constant.doorList",Constant.doorList.getConnectionKey()+"");
//                                        list.add(doorList);
                                    }
                                }
                            }
                            //然后取信号最大值:
                            for(int i=0;i<rssiList.size();i++){
                                if(i==0){
                                    maxRssi = rssiList.get(i);
                                    System.out.println("所有的:" + maxRssi);
                                }else{
                                    Log.e("i",i+"");
                                    maxRssi = Math.max(maxRssi,rssiList.get(i));
                                    //这里表示已经获取了最大值的Rssi,并且锁定这一行；
                                    device = result1.get(i);
                                }
                            }
                            if(rssiList.size() > 0){
                                System.out.println("最大值:" + maxRssi);
                                Log.e("门名称",device.getName());
                                deviceName = device.getName();
                                if (deviceName != null && deviceName.length() > 0){
                                    mHandler.sendEmptyMessage(0x123);
                                }
                                else{
                                    mHandler.sendEmptyMessage(0x124);
                                }
                                address = device.getAddress();
                                mHandler.sendEmptyMessage(0x125);

                                if (!TextUtils.isEmpty(mAddress) && mAddress.equals(address)) {
                                    mHandler.sendEmptyMessage(0x126);
                                } else {
                                    mHandler.sendEmptyMessage(0x127);
                                }
                            }else{
                                System.out.println("当前list为空");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("error","失败");
                    }
                });

        //BleDevice device = mLeDevices.get(i);

        return view;
    }
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0x123){
                viewHolder.deviceName.setText(deviceName);
            }else if(msg.what == 0x124){
                viewHolder.deviceName.setText(R.string.unknown_device);
            }else if(msg.what == 0x125){
                viewHolder.deviceAddress.setText(address + "<->" + device.getRandomCast());
            }else if(msg.what == 0x126){
                viewHolder.deviceImg.setVisibility(View.VISIBLE);
            }else if(msg.what == 0x127){
                viewHolder.deviceImg.setVisibility(View.GONE);
            }
        }

    };

}
