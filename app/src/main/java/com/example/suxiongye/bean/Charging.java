package com.example.suxiongye.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by suxiongye on 4/29/17.
 */
public class Charging implements Parcelable {
    private int id;
    private String name;
    private Double latitude;
    private Double longitude;
    private String status;
    private String used;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeString(status);
        dest.writeString(used);
    }

    public static final Parcelable.Creator<Charging> CREATOR = new Parcelable.Creator<Charging>() {

        @SuppressWarnings("unchecked")
        @Override
        public Charging createFromParcel(Parcel arg0) {
            // TODO Auto-generated method stub
            Charging charging = new Charging();
            charging.id = arg0.readInt();
            charging.name = arg0.readString();
            charging.longitude = arg0.readDouble();
            charging.latitude = arg0.readDouble();
            charging.status = arg0.readString();
            charging.used = arg0.readString();
            return charging;
        }

        @Override
        public Charging[] newArray(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }


    };
}
