package it.geosolutions.geocollect.android.core.wmc.wmc;

import android.bluetooth.BluetoothDevice;

import it.geosolutions.geocollect.android.core.wmc.model.Configuration;
import it.geosolutions.geocollect.android.core.wmc.model.WMCReadResult;

/**
 * Created by Robert Oehler on 27.10.16.
 *
 */

public interface WMCFacade {

    /**
     * connect to a Bluetooth device identified by its mac address
     * @param device the device to connect to
     */
    void connect(final BluetoothDevice device);

    /**
     * disconnect device
     */
    void disConnect();

    /**
     * @return current connection state
     */
    boolean isConnected();

    /**
     * read configuration
     * @return the configuration read from device or null if an error occurred
     */
    Configuration readConfig();

    /**
     * write a configuration to the device
     * @param configuration the configuration to write
     * @return if the operation was successful
     */
    boolean writeConfig(final Configuration configuration);

    WMCReadResult read();

    String getConnectionState();

    boolean sendSysReset();

    boolean syncTime();

    boolean activateGSM(final boolean on);

    boolean sendTestSMS(final String recipient);

    boolean presetOverallCounter(final double preset);

    boolean clear(final int week_day_index);

    interface ConnectionListener
    {
        public void onDeviceConnected(String name);
        public void onDeviceDisconnected();
        public void onDeviceConnectionFailed(String error);
    }

}
