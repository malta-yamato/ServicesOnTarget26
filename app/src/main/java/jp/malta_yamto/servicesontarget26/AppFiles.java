package jp.malta_yamto.servicesontarget26;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import static android.content.ContentValues.TAG;

public class AppFiles {

    public static FileInputStream openFileInput(Context context, Class<? extends Service> clazz)
            throws FileNotFoundException {
        return context.openFileInput(getCorrespondingName(clazz));
    }

    public static FileOutputStream openFileOutput(Context context, Class<? extends Service> clazz)
            throws FileNotFoundException {
        return context.openFileOutput(getCorrespondingName(clazz), Context.MODE_PRIVATE);
    }

    public static SharedPreferences getSharedPreferences(Context context,
            Class<? extends Service> clazz, int mode) {
        return context.getSharedPreferences(getCorrespondingName(clazz), mode);
    }

    private static String getCorrespondingName(Class<? extends Service> clazz) {
        String className = clazz.getName();
        Log.d(TAG, "getCorrespondingName: className = " + className);
        return className;
    }

}
