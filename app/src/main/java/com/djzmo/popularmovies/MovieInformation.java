package com.djzmo.popularmovies;

import android.graphics.Movie;
import android.os.Parcel;
import android.os.Parcelable;

public class MovieInformation implements Parcelable {

    public String remoteId;
    public String title;
    public String posterUrl;
    public String synopsis;
    public double userRating;
    public String releaseDate;
    public int runtime;

    public MovieInformation() {}

    public MovieInformation(String remoteId, String title, String posterUrl, String synopsis, double userRating, String releaseDate, int runtime) {
        this.remoteId = remoteId;
        this.title = title;
        this.posterUrl = posterUrl;
        this.synopsis = synopsis;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
    }

    public MovieInformation(Parcel in) {
        remoteId = in.readString();
        title = in.readString();
        posterUrl = in.readString();
        synopsis = in.readString();
        userRating = in.readDouble();
        releaseDate = in.readString();
        runtime = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(remoteId);
        parcel.writeString(title);
        parcel.writeString(posterUrl);
        parcel.writeString(synopsis);
        parcel.writeDouble(userRating);
        parcel.writeString(releaseDate);
        parcel.writeInt(runtime);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MovieInformation> CREATOR = new Parcelable.Creator<MovieInformation>() {
        @Override
        public MovieInformation createFromParcel(Parcel in) {
            return new MovieInformation(in);
        }

        @Override
        public MovieInformation[] newArray(int size) {
            return new MovieInformation[size];
        }
    };
}
