package it.geosolutions.geocollect.android.core.wmc.wmc;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import it.geosolutions.geocollect.android.core.wmc.model.Configuration;
import it.geosolutions.geocollect.android.core.wmc.model.WMCReadResult;

import static android.content.ContentValues.TAG;

/**
 * Created by Robert Oehler on 27.10.16.
 *
 * Represents a mock connection to a wmc device
 */

public class WMCMock implements WMCFacade {


    private ConnectionListener mConnectionListener;

    private Configuration mockConfiguration;

    private double mockOverallStartValue = 1234567.89;
    private float  mockWeekStartValue = 333f;
    private int rssi = 0;

    private final int DELAY_BETWEEN_GSM_REQUEST_AND_RSSI_UPDATE = 10000;
    private final int CONNECT_DELAY = 500;
    private final int DISCONNECT_DELAY = 0;
    private final int READ_CONFIG_DELAY = 500;

    private float[] weekTotalValues;
    private float[] weekSlot1Values;
    private float[] weekSlot2Values;

    private WMCReadResult readResult;

    private String mState = "NONE";

    private boolean isConnected = false;

    public WMCMock(final ConnectionListener listener){

        this.mConnectionListener = listener;

        weekTotalValues = new float[Configuration.WEEKDAY_ARRAY_LENGTH];
        weekSlot1Values = new float[Configuration.WEEKDAY_ARRAY_LENGTH];
        weekSlot2Values = new float[Configuration.WEEKDAY_ARRAY_LENGTH];


        for(int i = 0; i < Configuration.WEEKDAY_ARRAY_LENGTH; i++){

            if( i == 0 || i == 3){
                weekTotalValues[i] = mockWeekStartValue;
            }else{
                weekTotalValues[i] = 0;
            }
            weekSlot1Values[i] = 0;
            weekSlot2Values[i] = 0;
        }
        
        readResult = new WMCReadResult(
                mockOverallStartValue,
                0d,
                0d,
                weekTotalValues,
                weekSlot1Values,
                weekSlot2Values,
                new Date(),
                rssi);
    }

    @Override
    public void connect(final BluetoothDevice device) {

        try {
            Thread.sleep(CONNECT_DELAY);
        } catch (InterruptedException e) {
            Log.e(TAG, "delay failed");
        }


        if(mConnectionListener != null) {
        	mConnectionListener.onDeviceConnected(device.getName());
        }
        mState = "Connected";
        isConnected = true;


    }

    @Override
    public void disConnect() {

        mState = "Disconnected";
        isConnected = false;

        if(mConnectionListener != null) {
            mConnectionListener.onDeviceDisconnected();
        }
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public Configuration readConfig() {

        try {
            Thread.sleep(READ_CONFIG_DELAY);
        } catch (InterruptedException e) {
            Log.e(TAG, "delay failed");
        }

        if(mockConfiguration == null){
            mockConfiguration = getMockConfiguration();
        }
        mState = "Read Config Successful";

        return mockConfiguration;
    }

    @Override
    public boolean writeConfig(final Configuration configuration) {

        mState = "Write Config Successful";
        return true;
    }

    @Override
    public WMCReadResult read(){

        mState = "Read WMC Data Successful";

        readResult.overall_total++;
        readResult.week_total[0]++;
        readResult.date = new Date();
        readResult.rssi = rssi;
        
        return readResult;

    }

    @Override
    public boolean activateGSM(final boolean on) {

    	/**
    	 * after some delay set a rssi value
    	 */
        final HandlerThread handlerThread = new HandlerThread("BackgroundHandler");
        handlerThread.start();
        new Handler(handlerThread.getLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                rssi = 42;
                handlerThread.interrupt();
            }
        },DELAY_BETWEEN_GSM_REQUEST_AND_RSSI_UPDATE);

        mState = "GSM RSSI request sent";

        return true;
    }

    @Override
    public String getConnectionState() {
        return mState;
    }

    @Override
    public boolean sendSysReset() {

        return true;
    }

    @Override
    public boolean syncTime() {

        return true;
    }

    @Override
    public boolean sendTestSMS(String recipient) {

        return true;
    }

    @Override
    public boolean presetOverallCounter(double preset) {
    	if(readResult != null){
    		readResult.overall_total = preset;
    	}

        return true;
    }

    @Override
    public boolean clear(final int week_day_index) {

        readResult.week_total[week_day_index] = 0;

        return true;
    }

    public static Configuration getMockConfiguration(){

        final Configuration config = new Configuration();

        config.siteCode = 777;
        config.timeZone = 1;

        config.signature = 1;
        config.version = 656;

        config.timerSlot1Start = 7;
        config.timerSlot1Stop = 8;
        config.timerSlot2Start = 11;
        config.timerSlot2Stop = 12;

        config.sensorType = 1;
        config.sensorLitresRound = 3;
        config.sensor_LF_Const = 1;

        config.provider = "Mock provider";
        config.pinCode = "1234";
        config.recipientNum = "+391234567890";
        config.ntpAddress = "191.232.434.31";
        config.originNum = "orig-num";

        config.digits = 8;

        return config;
    }
}
