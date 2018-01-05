package com.maxcloud.bluetoothsdkdemov2;

/**
 * Created by Administrator on 2018/1/4.
 */

public class doorList {
    private String doorID;
    private String doorName;
    private String doorPath;
    private String connectionKey;

    public doorList(String doorID, String doorName, String doorPath, String connectionKey){
        this.doorID = doorID;
        this.doorName = doorName;
        this.doorPath = doorPath;
        this.connectionKey = connectionKey;
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
