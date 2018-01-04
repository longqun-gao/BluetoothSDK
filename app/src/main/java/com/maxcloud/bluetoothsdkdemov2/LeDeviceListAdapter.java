package com.maxcloud.bluetoothsdkdemov2;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxcloud.bluetoothreadersdkv2.BleDevice;

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
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
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

        BleDevice device = mLeDevices.get(i);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText(R.string.unknown_device);
        String add = device.getAddress();
        viewHolder.deviceAddress.setText(add + "<->" + device.getRandomCast());
        if (!TextUtils.isEmpty(mAddress) && mAddress.equals(add)) {
            viewHolder.deviceImg.setVisibility(View.VISIBLE);
        } else {
            viewHolder.deviceImg.setVisibility(View.GONE);
        }

        return view;
    }
}
