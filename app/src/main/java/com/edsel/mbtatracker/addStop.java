package com.edsel.mbtatracker;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

public class addStop extends AsyncTask<Object, Void, Void> {

    Context myContext;

    addStop(Context myContext){
        this.myContext = myContext;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Object... params) {

        mbtaStops userStop = (mbtaStops)params[0];

        if(StopsDatabase.getDatabase(myContext).MBTAInterface().getStop(userStop.getId()) == null){
            StopsDatabase.getDatabase(myContext).MBTAInterface().insert(userStop);
        }

        return null;
    }


}
