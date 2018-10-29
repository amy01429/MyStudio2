package com.example.amy01.mystudio;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by amy01 on 2018/6/12.
 */

class MyService extends Service {

    public class LocalBinder extends Binder //宣告一個繼承 Binder 的類別 LocalBinder
    {
        MyService getService()
        {
            return  MyService.this;
        }
    }
    private LocalBinder mLocBin = new LocalBinder();

    public void myMethod()
    {
       // Log.d(LOG_TAG, "myMethod()");
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        // TODO Auto-generated method stub
        return mLocBin;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        // TODO Auto-generated method stub
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        // TODO Auto-generated method stub
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // TODO Auto-generated method stub
    }
}
