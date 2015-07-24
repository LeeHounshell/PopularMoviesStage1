package com.smartvariables.lee.popularmovies;

import java.util.ArrayList;

public interface MovieContext {
    public MovieAdapter getMovieAdapter();

    public ArrayList<MovieDbInfo> getDefaultMovieList();
}
