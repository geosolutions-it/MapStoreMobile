package it.geosolutions.geocollect.android.core.wmc.bluetooth;



import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

import it.geosolutions.geocollect.android.app.BuildConfig;

/**
 * Created by Robert Oehler on 14.11.16.
 *
 */

public class BluetoothUtil {

    public static ArrayList<BluetoothDevice> getPairedDevices(){
        return getPairedDevices(null);
    }
    /**
     * acquires the list of currently bonded (paired) Bluetooth devices
     * 
     * if the device has no Bluetooth or no bonded devices an empty list is returned
     * 
     * @param filterPrefix if not null filters the list using the provided prefix
     * 
     * @return the list of paired devices (may be empty but not null)
     */
    public static ArrayList<BluetoothDevice> getPairedDevices(String filterPrefix){

        ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter == null){
            return devices;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {
            String deviceBTName = device.getName();
            String address  = device.getAddress();
            //could filter here devices according to mac address
            if(BuildConfig.DEBUG){
                Log.d("BluetoothUtil", "Paired device found : "+deviceBTName + " address : "+address);
            }

            if(filterPrefix != null && deviceBTName != null) {
                if (deviceBTName.toLowerCase(Locale.getDefault()).startsWith(filterPrefix)) {
                    devices.add(device);
                }
            }else{
                devices.add(device);
            }
        }

        return devices;
    }
}
