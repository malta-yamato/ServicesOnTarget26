package jp.malta_yamto.servicesontarget26;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class AppFiles {
    private static final String TAG = "AppFiles";

    @SuppressWarnings("unused")
    public static FileInputStream openFileInput(Context context, Class<? extends Service> clazz,
            String name) throws FileNotFoundException {
        return context.openFileInput(getCorrespondingName(clazz, name));
    }

    @SuppressWarnings("unused")
    public static FileOutputStream openFileOutput(Context context, Class<? extends Service> clazz,
            String name, int mode) throws FileNotFoundException {
        return context.openFileOutput(getCorrespondingName(clazz, name), mode);
    }

    @SuppressWarnings("unused")
    public static SharedPreferences getSharedPreferences(Context context,
            Class<? extends Service> clazz) {
        return context.getSharedPreferences(getCorrespondingName(clazz), Context.MODE_PRIVATE);
    }

    private static String getCorrespondingName(Class<? extends Service> clazz, String suffix) {
        return getCorrespondingName(clazz) + "." + suffix;
    }

    private static String getCorrespondingName(Class<? extends Service> clazz) {
        String className = clazz.getName();
        Log.d(TAG, "getCorrespondingName: className = " + className);
        return className;
    }

}
