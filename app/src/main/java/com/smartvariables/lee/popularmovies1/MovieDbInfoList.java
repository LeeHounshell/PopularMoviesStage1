package com.smartvariables.lee.popularmovies1;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/*
 * source: http://stackoverflow.com/questions/10953121/android-arraylistmyobject-pass-as-parcelable
 */
public class MovieDbInfoList extends ArrayList<MovieDbInfo> implements Parcelable {

    private static final long serialVersionUID = 931854767798790961L;

    public final Parcelable.Creator<MovieDbInfoList> CREATOR = new Parcelable.Creator<MovieDbInfoList>() {
        public MovieDbInfoList createFromParcel(Parcel in) {
            return new MovieDbInfoList(in);
        }

        public MovieDbInfoList[] newArray(int size) {
            return new MovieDbInfoList[size];
        }
    };

    public MovieDbInfoList() {
    }

    @SuppressWarnings("unused")
    public MovieDbInfoList(Parcel in) {
        this();
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        this.clear();

        // First we have to read the list size
        int size = in.readInt();

        for (int i = 0; i < size; i++) {
            MovieDbInfo r = new MovieDbInfo(in.readString(), in.readFloat(), in.readFloat(), in.readInt(), in.readString(), in.readString(), in.readString());
            this.add(r);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int size = this.size();

        // We have to write the list size, we need him recreating the list
        dest.writeInt(size);

        for (int i = 0; i < size; i++) {
            MovieDbInfo r = this.get(i);

            dest.writeString(r.getTitle());
            dest.writeFloat(r.getPopularity());
            dest.writeFloat(r.getVoteAverage());
            dest.writeInt(r.getVoteCount());
            dest.writeString(r.getReleaseDate());
            dest.writeString(r.getPosterPath());
            dest.writeString(r.getOverview());
        }
    }
}
