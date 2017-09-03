package com.github.romanarranz.servicereceiver;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by romanarranzguerrero on 3/9/17.
 */

public final class Utility {

    /**
     * Comprobamos si es seguro mandar el intent ya que puede que no sea recibido por el Service
     * que se estableció con el Intent
     *
     * http://codetheory.in/android-intents/
     *
     * @param intent
     * @return
     */
    public static boolean isSafeIntentService(Context context, Intent intent) {
        boolean result = false;

        // obtenemos el gestor de paquetes de android
        PackageManager packageManager = context.getPackageManager();

        // obtenemos los servicios que pueden responder a dicho intent
        List<ResolveInfo> services = packageManager.queryIntentServices(intent, 0);

        // comprobamos que haya al menos uno
        result = services.size() > 0;

        return result;
    }

    /**
     * Devuelve una lista de String con el CanonicalName de todos los servicios disponibles
     * que pueden responder a ese Intent
     *
     * @param context
     * @param intent
     * @return
     */
    public static List<String> getAviablesServices(Context context, Intent intent) {
        List<String> aviablesServices = new ArrayList<>();

        // obtenemos el gestor de paquetes de android
        PackageManager packageManager = context.getPackageManager();

        // obtenemos los servicios que pueden responder a dicho intent
        List<ResolveInfo> services = packageManager.queryIntentServices(intent, 0);

        for (int i = 0; i<services.size(); i++) {
            aviablesServices.add(services.get(i).toString());
        }

        return aviablesServices;
    }
}
