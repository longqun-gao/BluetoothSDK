package com.maxcloud.bluetoothsdkdemov2.net;



public interface HttpConnectionInter {
    void onFinish(String content);
    void onError(Exception e);
}
