package it.geosolutions.geocollect.android.core.wmc.model;

import java.util.Date;

/**
 * Created by Robert Oehler on 05.11.16.
 *
 * Container for the data a WMC devices provides
 */

public class WMCReadResult {

    public double overall_total;
    public double overall_Slot1;
    public double overall_Slot2;

    public float[] week_total;
    public float[] week_Slot1;
    public float[] week_Slot2;

    public Date date;
    public int rssi;


    public WMCReadResult(final double o_t, double o_s1,double o_s2,float w_t[], float w_s1[], float w_s2[], Date _date, int _rssi){

        this.overall_total = o_t;
        this.overall_Slot1 = o_s1;
        this.overall_Slot2 = o_s2;
        this.week_total = w_t;
        this.week_Slot1 = w_s1;
        this.week_Slot2 = w_s2;

        this.date = _date;

        this.rssi = _rssi;
    }
}
