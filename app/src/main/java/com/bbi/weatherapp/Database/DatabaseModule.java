package com.bbi.weatherapp.Database;

/**
 * Created by jay on 23/01/17.
 */

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = DatabaseModule.NAME, version = DatabaseModule.VERSION)
public class DatabaseModule {

    public static final String NAME = "TextToCSVAppDB";
    public static final int VERSION = 1;
}