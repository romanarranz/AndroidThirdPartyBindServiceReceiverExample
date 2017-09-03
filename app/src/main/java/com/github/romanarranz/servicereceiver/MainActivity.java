package com.github.romanarranz.servicereceiver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private final int   MSG_REGISTER_CLIENT = 1,
                        MSG_UNREGISTER_CLIENT = 2,
                        MSG_SET_INT_VALUE = 3;

    /**
     * Objetivo donde los clienes envian los mensajes al IncomingHandler
     */
    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * Messenger para comunicarnos con el servicio
     */
    private Messenger mService = null;

    /**
     * Flag que indica si hemos hecho bind con el servicio.
     */
    private boolean mIsBound;

    /**
     * Instancia para interctuar con la interfaz principal del servicio.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        /**
         * Metodo que se llama cuando la conexion con el servicio ha sido establecida, dandonos la instancia de servicio
         * que podemos usar para interactuar con el. Estamos comunicando con nuestro servicio a traves de una interfaz IDL.
         *
         * @param className
         * @param service
         */
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Log.i(LOG_TAG, "Attached to Service.");
            try {
                Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                // En el caso en el que el servicio se detenga no hacemos nada
            }
        }

        /**
         * Metodo que se llama cuando la conexion con el servicio ha sido desconectada inesperadamente.
         *
         * @param className
         */
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            Log.i(LOG_TAG, "Disconnected from Service.");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkIfServiceIsRunning();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            doUnbindService();
        } catch (Throwable t) {
            Log.e(LOG_TAG, "Failed to unbind from the service", t);
        }
    }

    /**
     * Envia un mensaje al servicio sin contenido
     *
     * @param value
     */
    public void sendMessageToService(int value) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, value);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                }
            }
        }
    }

    /**
     * Envia un mensaje al servicio con datos
     *
     * @param value
     */
    public void sendMessageToService(int value, Bundle bundle) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, value);
                    msg.replyTo = mMessenger;
                    msg.setData(bundle);
                    mService.send(msg);
                }
                catch (RemoteException e) {
                }
            }
        }
    }

    /**
     * Comprueba si el servicio esta corriendo
     */
    private void checkIfServiceIsRunning() {

        // enviar al Muse Bind Service de NeurReader una solicitud de conexion
        Intent intent = new Intent();
        intent.setClassName("com.github.romanarranz.androidserviceexample", "com.github.romanarranz.androidserviceexample.sync.MyService");

        if (Utility.isSafeIntentService(this, intent)) {
            doBindService(intent);
        }
    }

    /**
     * Establece una conexion con el servicio.
     */
    private void doBindService(Intent intent) {
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    /**
     * Cierra una conexion con el servicio.
     */
    private void doUnbindService() {
        if (mIsBound) {
            // Si hemos recibido el servicio, y con ello lo hemos registrado, entones ahora es el momento de desregistarlo
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                    // Noa hay nada especial que necesitemos hacer si el servicio se crashea
                }
            }

            // Desvincular nuestra conexion
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    /**
     * Clase para interactuar con la interfaz del servicio en el paso de mensajes
     */
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case MSG_SET_INT_VALUE:
                    int dataPacket = msg.getData().getInt("my_int");
                    Log.i(LOG_TAG, "llegando..."+dataPacket);

                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }
}
