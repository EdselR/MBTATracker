package com.edsel.mbtatracker;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RawQuery;

import java.util.List;

@Dao
public interface MBTAInterface {

    @Query("SELECT * FROM mbtaStops")
    List<mbtaStops> getAllItems();

    @Query("SELECT * FROM mbtaStops WHERE id = :userID")
    mbtaStops getStop(String userID);

    @Insert
    void insert(mbtaStops stops);

    //@Delete
    @Delete
    void delete(mbtaStops stop);


}
