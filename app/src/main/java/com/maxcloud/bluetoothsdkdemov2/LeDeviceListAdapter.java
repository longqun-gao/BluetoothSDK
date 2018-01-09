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
    private doorList doorList,doorList2;
    private List<doorList> list = new ArrayList<>();
    ViewHolder viewHolder;
    String deviceName;
    BleDevice device = new BleDevice();
    String add;
    int rssi;
    List<Integer> rssiList = new ArrayList<>();
    List<BleDevice> result1;

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

        String time = DateUtils.getCurrentTime_Today_Min();
        Log.e("time",""+time);
        String token = MD5Utils.encode(time + "adminXH");
        Log.e("token",""+token);
        String url = "http://202.105.104.105:8006/ssh/openDoor/getDoorByPhone";
        HttpConnectionTools.HttpServler(url,
                HttpConnectionTools.HttpData("projectCode", "123", "token", token,
                        "userName", "柳玉豹", "phone", "15107962485"), new HttpConnectionInter() {
                    @Override
                    public void onFinish(String content) {
                        Log.e("门列表",""+content);
                        try {
                            if(list.size() == 0){
                                JSONObject jsonObject = new JSONObject(content);
                                String result = jsonObject.getString("result");
                                jsonObject = new JSONObject(result);
                                String openData = jsonObject.getString("openData");
                                JSONArray jsonArray = jsonObject.getJSONArray("doorList");
                                for(int i = 0;i<jsonArray.length();i++){
                                    jsonObject = jsonArray.getJSONObject(i);
                                    String doorID =  jsonObject.getString("doorID");
                                    String doorName =  jsonObject.getString("doorName");
                                    String doorPath =  jsonObject.getString("doorPath");
                                    String connectionKey =  jsonObject.getString("connectionKey");
                                    String keyID =  jsonObject.getString("keyID");
                                    doorList = new doorList(doorID,doorName,doorPath,connectionKey,keyID,openData);

                                    list.add(doorList);
                                }
                            }
                            result1 = new ArrayList<>();
                            for (BleDevice bleDevice : mLeDevices) {
                                for (doorList doorList : list) {
                                    if (bleDevice.getKeyId().equals(doorList.getKeyID())){
                                        Log.e("rssi",bleDevice.getRssi()+"");
                                        Log.e("opendata",doorList.getOpenData());
                                        result1.add(bleDevice);
                                    }
                                }
                            }
                            device = result1.get(i);
                            deviceName = device.getName();
                            rssi = device.getRssi();

                            if (deviceName != null && deviceName.length() > 0){
                                mHandler.sendEmptyMessage(0x123);
                            }
                            else{
                                mHandler.sendEmptyMessage(0x124);
                            }
                            add = device.getAddress();
                            mHandler.sendEmptyMessage(0x125);


                            if (!TextUtils.isEmpty(mAddress) && mAddress.equals(add)) {
                                mHandler.sendEmptyMessage(0x126);

                            } else {
                                mHandler.sendEmptyMessage(0x127);

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
                viewHolder.deviceAddress.setText(add + "<->" + device.getRandomCast());
            }else if(msg.what == 0x126){
                viewHolder.deviceImg.setVisibility(View.VISIBLE);
            }else if(msg.what == 0x127){
                viewHolder.deviceImg.setVisibility(View.GONE);
            }
        }

    };

}
