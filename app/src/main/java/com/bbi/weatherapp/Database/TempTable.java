package com.bbi.weatherapp.Database;

import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by jay on 16/12/17.
 */


@Table(database = DatabaseModule.class)
public class TempTable extends BaseModel {
    @Column
    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public String region_code;
    @Column
    public String weather_param;
    @Column
    public String year;
    @Column
    public String key;
    @Column
    public String value;


}
