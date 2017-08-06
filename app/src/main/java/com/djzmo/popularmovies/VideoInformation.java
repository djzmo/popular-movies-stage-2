package com.djzmo.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoInformation implements Parcelable {

    public String site, key, name;

    public VideoInformation() {}

    public VideoInformation(String site, String key, String name) {
        this.site = site;
        this.key = key;
        this.name = name;
    }

    public VideoInformation(Parcel in) {
        site = in.readString();
        key = in.readString();
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(site);
        parcel.writeString(key);
        parcel.writeString(name);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<VideoInformation> CREATOR = new Parcelable.Creator<VideoInformation>() {
        @Override
        public VideoInformation createFromParcel(Parcel in) {
            return new VideoInformation(in);
        }

        @Override
        public VideoInformation[] newArray(int size) {
            return new VideoInformation[size];
        }
    };
}
