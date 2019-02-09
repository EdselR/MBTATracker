package com.edsel.mbtatracker;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {mbtaStops.class}, version = 4)
public abstract class StopsDatabase extends RoomDatabase{

    public abstract MBTAInterface MBTAInterface();
    public static final String DB_NAME = "MyStops";
    private static StopsDatabase INSTANCE;

    public static StopsDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, StopsDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();

        }
        return INSTANCE;
    }

}
