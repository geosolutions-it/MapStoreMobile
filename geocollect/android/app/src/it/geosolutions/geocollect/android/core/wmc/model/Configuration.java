package it.geosolutions.geocollect.android.core.wmc.model;


/**
 * Created by Robert Oehler on 29.10.16.
 *
 */

public class Configuration {

    //byte array max size on device
    public final static int BYTES_MAX_PROVIDER   = 24;
    public final static int BYTES_MAX_PINCODE    = 6;
    public final static int BYTES_MAX_RECIPIENT  = 14;
    public final static int BYTES_MAX_NTPADDRESS = 16;
    public final static int BYTES_MAX_ORIGINNUM  = 14;

    public final static int BYTES_MAX_TEST_SMS_RECIPIENT  = 13;

    public final static int NTP_MIN_LENGTH = 7;
    public final static int TEL_MIN_LENGTH = 13;

    public final static int WEEKDAY_ARRAY_LENGTH = 8;

    public int signature;
    public int version;

    public int siteCode;
    public int timeZone;

    public int timerSlot1Start;
    public int timerSlot1Stop;
    public int timerSlot2Start;
    public int timerSlot2Stop;

    public int sensorType;
    public int sensorLitresRound;
    public int sensor_LF_Const;

    public String provider;
    public String pinCode;
    public String recipientNum;
    public String ntpAddress;
    public String originNum;

    public int digits;

    /**
    
     * Configuration constructor
     * It sets some invalid default values, which need to be overwritten by the actual object
     * otherwise a subsequent validation will fail if it finds such negative values
     */
    public Configuration(){

        this.timerSlot1Start = -1;
        this.timerSlot1Stop = -1;
        this.timerSlot2Start = -1;
        this.timerSlot2Stop = -1;

        this.sensorLitresRound = -1;
        this.sensor_LF_Const = -1;

    }
}
