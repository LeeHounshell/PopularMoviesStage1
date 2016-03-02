package com.smartvariables.lee.popularmovies1;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import info.movito.themoviedbapi.model.MovieDb;

public class MovieDbInfo implements Parcelable {
    private static String TAG = "LEE: <" + MovieDbInfo.class.getSimpleName() + ">";
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MovieDbInfo> CREATOR = new Parcelable.Creator<MovieDbInfo>() {
        @Override
        public MovieDbInfo createFromParcel(Parcel in) {
            Log.v(TAG, "createFromParcel");
            return new MovieDbInfo(in);
        }

        @Override
        public MovieDbInfo[] newArray(int size) {
            Log.v(TAG, "newArray");
            return new MovieDbInfo[size];
        }
    };
    private String title;
    private String posterPath;
    private Float popularity;
    private Float voteAverage;
    private Integer voteCount;
    private String releaseDate;
    private String overview;

    public MovieDbInfo(MovieDb movie) {
        //Log.v(TAG, "MovieDbInfo(MovieDb)");
        this.title = movie.getTitle();
        this.posterPath = movie.getPosterPath();
        this.popularity = movie.getPopularity();
        this.voteAverage = movie.getVoteAverage();
        this.voteCount = movie.getVoteCount();
        this.releaseDate = movie.getReleaseDate();
        this.overview = movie.getOverview();
    }

    protected MovieDbInfo(Parcel in) {
        //Log.v(TAG, "MovieDbInfo(Parcel)");
        title = in.readString();
        posterPath = in.readString();
        popularity = in.readFloat();
        voteAverage = in.readFloat();
        voteCount = in.readInt();
        releaseDate = in.readString();
        overview = in.readString();
    }

    public MovieDbInfo(String title, Float populatity, Float voteAverage, Integer voteCount, String releaseDate, String posterPath, String overview) {
        //Log.v(TAG, "MovieDbInfo(...)");
        this.title = title;
        this.popularity = populatity;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.releaseDate = releaseDate;
        this.posterPath = posterPath;
        this.overview = overview;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //Log.v(TAG, "writeToParcel");
        dest.writeString(title);
        dest.writeString(posterPath);
        dest.writeFloat(popularity);
        dest.writeFloat(voteAverage);
        dest.writeInt(voteCount);
        dest.writeString(releaseDate);
        dest.writeString(overview);
    }

    public String getTitle() {
        return title;
    }

    public Float getPopularity() {
        return popularity;
    }

    public Float getVoteAverage() {
        return voteAverage;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getOverview() {
        return overview;
    }

    @Override
    public String toString() {
        String movieInfo = "title:" + title + "\nrelease date: " + releaseDate + "\n" + voteAverage / (float) 2.0 + " stars (out of 5)\n\n" + overview + "\n";
        return movieInfo;
    }

}