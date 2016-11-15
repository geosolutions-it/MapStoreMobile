package it.geosolutions.geocollect.android.core.wmc.service;

import it.geosolutions.geocollect.android.app.BuildConfig;
import it.geosolutions.geocollect.android.app.R;
import it.geosolutions.geocollect.android.core.wmc.model.Configuration;
import it.geosolutions.geocollect.android.core.wmc.model.WMCCommand;
import it.geosolutions.geocollect.android.core.wmc.model.WMCReadResult;
import it.geosolutions.geocollect.android.core.wmc.service.events.RequestWMCDataEvent;
import it.geosolutions.geocollect.android.core.wmc.service.events.WMCCommunicationResultEvent;
import it.geosolutions.geocollect.android.core.wmc.service.events.WMCConnectionStateChangedEvent;
import it.geosolutions.geocollect.android.core.wmc.wmc.WMCFacade;
import it.geosolutions.geocollect.android.core.wmc.wmc.WMCMock;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by Robert Oehler on 21.11.16.
 *
 *
 */

public class WMCService extends Service implements WMCFacade.ConnectionListener{

    private final static String TAG = "WMCService";

    public static final String PARAM_DEBUG  = "PARAM_DEBUG";
    public static final String PARAM_DEVICE = "PARAM_DEVICE";

    public static final int NOTIFICATION_ID = 42;

    private WMCFacade wmc;
    private Handler handler;
    private String deviceName;

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler();
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        final boolean debug = intent.getBooleanExtra(PARAM_DEBUG, false);
        final BluetoothDevice device = intent.getParcelableExtra(PARAM_DEVICE);

        if(debug) {
            wmc = new WMCMock(this);
        }else {
        	// not yet in this branch
            //wmc = new WMCImpl(getBaseContext(), this);
        }

        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                if (device != null) {
                	
                	if(BuildConfig.DEBUG){
                		Log.i(TAG, "onStartCommand in Debug "+Boolean.toString(debug)+ " with device "+ device.toString());
                	}
                	
                    wmc.connect(device);
                    
                } else {
                    Log.e(TAG, "no device to connect provided as parameter");
                }
                return null;
            }
        }.execute();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);

    }

    /**
     * receives @param RequestWMCDataEvent events
     * 
     * They are handled in a background thread
     * according to their command creating a response
     * containing the result of the operation with the WMC
     * this result is broadcasted 
     * 
     * @param event the event to handle
     */
    @Subscribe
    public void onEvent(final RequestWMCDataEvent event){

        new AsyncTask<Void,Void, WMCCommunicationResultEvent>(){
            @Override
            protected WMCCommunicationResultEvent doInBackground(Void... params) {

                WMCCommunicationResultEvent resultEvent = new WMCCommunicationResultEvent(event.getCommand());

                switch (event.getCommand()){

                    case READ_CONFIG:
                        final Configuration configuration = wmc.readConfig();
                        if(configuration != null) {
                            resultEvent.setSuccess(true);
                            resultEvent.setConfiguration(configuration);
                            resultEvent.setDeviceName(deviceName);
                        }else{
                        	Log.e(TAG, "error reading configuration from WMC");
                        }
                        break;
                    case WRITE_CONFIG:
                        if(event.getArg() != null && event.getArg() instanceof  Configuration){
                            Configuration conf = (Configuration) event.getArg();
                            resultEvent.setSuccess(wmc.writeConfig(conf));
                        }else{
                        	Log.e(TAG, "did not provide argument to write configuration to WMC");
                        }
                        break;
                    case READ_WATER:
                        
                    	WMCReadResult result = wmc.read();
                    	if(result != null){
                    		resultEvent.setSuccess(true);
                    		resultEvent.setReadResult(result);
                    	}else{
                    		Log.e(TAG, "error reading water data from WMC");
                    	}

                        break;
                    case RSSI:
                        boolean on = true;
                        if(event.getArg() != null && event.getArg() instanceof  Boolean){
                            on = (Boolean)  event.getArg();
                        }else{
                        	Log.w(TAG, "did not provide argument to activate gsm on WMC, using \"on\"");
                        }
                        resultEvent.setSuccess(wmc.activateGSM(on));

                        break;
                    case TEST_SMS:
                        String testReceiver;
                        if(event.getArg() != null && event.getArg() instanceof String){
                            testReceiver = (String)  event.getArg();
                            resultEvent.setSuccess(wmc.sendTestSMS(testReceiver));
                        }else{
                        	Log.e(TAG, "did not provide argument recipient to send test sms request to WMC");
                        }
                        break;
                    case WRITE_TIME:
                        resultEvent.setSuccess(wmc.syncTime());
                        break;
                    case PRESET:
                        if(event.getArg() != null && event.getArg() instanceof String){
                            try {
                                double preset = Double.parseDouble((String) event.getArg());
                                resultEvent.setSuccess(wmc.presetOverallCounter(preset));
                            }catch (NumberFormatException e){
                                Log.e(TAG, "error parsing preset arg to double", e);
                            }
                        }else{
                        	Log.e(TAG, "did not provide preset argument to write to WMC");
                        }

                        break;
                    case CLEAR_COUNTER:

                        if(event.getArg() != null && event.getArg() instanceof String){
                            try {
                                int week_day = Integer.parseInt((String) event.getArg());
                                resultEvent.setSuccess(wmc.clear(week_day));
                            }catch (NumberFormatException e){
                                Log.e(TAG, "error parsing week day arg to int", e);
                            }
                        }else{
                        	Log.e(TAG, "did not provide week day argument to clear counter on WMC");
                        }
                        break;
                    case DISCONNECT:

                        boolean sysResetSuccessful = wmc.sendSysReset();
                        //wait
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "delay failed");
                        }
                        if(!sysResetSuccessful){
                            Log.w(TAG, "sys reset failed");
                        }

                        //however, disconnect
                        wmc.disConnect();
                        //TODO report if an error occurred or ignore ?
                        resultEvent.setSuccess(true);
                        break;
                }

                return resultEvent;
            }

            @Override
            protected void onPostExecute(WMCCommunicationResultEvent resultEvent) {
                super.onPostExecute(resultEvent);

                EventBus.getDefault().post(resultEvent);

            }
        }.execute();
    }

    @Override
    public void onDeviceConnected(final String name) {

    	this.deviceName = name;
    	
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //state connected
                WMCConnectionStateChangedEvent stateChangedEvent = new WMCConnectionStateChangedEvent(WMCConnectionStateChangedEvent.ConnectionState.CONNECTED);
                stateChangedEvent.setWmcName(name);
                EventBus.getDefault().post(stateChangedEvent);
                showNotification(getString(R.string.state_connection_active,name),false);
                
            
            }
        });


    }

    @Override
    public void onDeviceDisconnected() {
    	
    	this.deviceName = null;

    	//inform the UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                WMCConnectionStateChangedEvent stateChangedEvent = new WMCConnectionStateChangedEvent(WMCConnectionStateChangedEvent.ConnectionState.DISCONNECTED);
                EventBus.getDefault().post(stateChangedEvent);
                ((NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
                stopSelf();
            }
        });

    }

    @Override
    public void onDeviceConnectionFailed(String error) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                WMCConnectionStateChangedEvent stateChangedEvent = new WMCConnectionStateChangedEvent(WMCConnectionStateChangedEvent.ConnectionState.CONNECTION_ERROR);
                EventBus.getDefault().post(stateChangedEvent);
                stopSelf();
            }
        });
        

    }

    /**
     * shows a notification to the user to indicate that there is an
     * ongoing connection to the WMC device
     * @param message the message to show
     * @param autoCancel if this notification is cancelable
     */
    private void showNotification(String message, boolean autoCancel){

    	final NotificationManager notificationManager = ((NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext())
                .setContentTitle(getString(R.string.app_name))
                .setAutoCancel(autoCancel)
                .setOngoing(!autoCancel)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }
}
