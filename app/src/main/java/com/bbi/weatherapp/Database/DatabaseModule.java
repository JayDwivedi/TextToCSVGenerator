package com.bbi.weatherapp.Database;

/**
 * Created by jay on 16/12/17.
 */

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = DatabaseModule.NAME, version = DatabaseModule.VERSION)
public class DatabaseModule {

    public static final String NAME = "TextToCSVApp";
    public static final int VERSION = 1;
}