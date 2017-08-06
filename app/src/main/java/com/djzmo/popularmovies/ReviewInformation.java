package com.djzmo.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class ReviewInformation implements Parcelable {

    public String remoteId, author, content;

    public ReviewInformation() {}

    public ReviewInformation(String remoteId, String author, String content) {
        this.remoteId = remoteId;
        this.author = author;
        this.content = content;
    }

    public ReviewInformation(Parcel in) {
        remoteId = in.readString();
        author = in.readString();
        content = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(remoteId);
        parcel.writeString(author);
        parcel.writeString(content);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ReviewInformation> CREATOR = new Parcelable.Creator<ReviewInformation>() {
        @Override
        public ReviewInformation createFromParcel(Parcel in) {
            return new ReviewInformation(in);
        }

        @Override
        public ReviewInformation[] newArray(int size) {
            return new ReviewInformation[size];
        }
    };
}
