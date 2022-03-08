package widget;

import android.app.Application;
import android.content.Context;

public class MainApplication extends Application {
    private static Context mContext;

    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getCustomApplicationContext(){
        return mContext;
    }
}
