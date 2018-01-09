package com.maxcloud.bluetoothsdkdemov2;

/**
 * Created by Administrator on 2018/1/4.
 */

public class doorList {
    private String openData;
    private String doorID;
    private String doorName;
    private String doorPath;
    private String connectionKey;
    private String keyID;

    public doorList(String doorID, String doorName, String doorPath, String connectionKey, String keyID, String openData){
        this.doorID = doorID;
        this.doorName = doorName;
        this.doorPath = doorPath;
        this.connectionKey = connectionKey;
        this.keyID = keyID;
        this.openData = openData;
    }

    public doorList(){
    }

    public String getOpenData() {
        return openData;
    }

    public void setOpenData(String openData) {
        this.openData = openData;
    }

    public String getKeyID() {
        return keyID;
    }

    public void setKeyID(String keyID) {
        this.keyID = keyID;
    }

    public String getDoorID() {
        return doorID;
    }

    public void setDoorID(String doorID) {
        this.doorID = doorID;
    }

    public String getDoorName() {
        return doorName;
    }

    public void setDoorName(String doorName) {
        this.doorName = doorName;
    }

    public String getDoorPath() {
        return doorPath;
    }

    public void setDoorPath(String doorPath) {
        this.doorPath = doorPath;
    }

    public String getConnectionKey() {
        return connectionKey;
    }

    public void setConnectionKey(String connectionKey) {
        this.connectionKey = connectionKey;
    }
}
