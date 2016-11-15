package it.geosolutions.geocollect.android.core.wmc.ui;

import it.geosolutions.geocollect.android.app.BuildConfig;
import it.geosolutions.geocollect.android.app.R;
import it.geosolutions.geocollect.android.core.form.FormEditActivity;
import it.geosolutions.geocollect.android.core.form.FormPageFragment;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.wmc.bluetooth.BluetoothUtil;
import it.geosolutions.geocollect.android.core.wmc.model.Configuration;
import it.geosolutions.geocollect.android.core.wmc.model.WMCCommand;
import it.geosolutions.geocollect.android.core.wmc.model.WMCReadResult;
import it.geosolutions.geocollect.android.core.wmc.service.WMCService;
import it.geosolutions.geocollect.android.core.wmc.service.events.RequestWMCDataEvent;
import it.geosolutions.geocollect.android.core.wmc.service.events.WMCCommunicationResultEvent;
import it.geosolutions.geocollect.android.core.wmc.service.events.WMCConnectionStateChangedEvent;
import it.geosolutions.geocollect.android.core.wmc.util.TimeSlotInputFilter;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.viewmodel.Page;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Created by Robert Oehler on 05.11.16.
 *
 * WMC form is the main class to interact with a wmc device
 */

public class WMCForm extends SherlockFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, TextWatcher {

    private final static String TAG = "WMCForm";
    
    private final static int REQUEST_ENABLE_BT = 321;

    public final static String WMC_DEVICE_PREFIX = "wmc";
    
    public final static String ARG_INFLATED= "inflated";
    public final static String ARG_CONNECTED = "connected";
    public final static String ARG_RSSI  = "rssi";
    
    private static boolean debug = true; //for this UI only branch always use WMC mock

    private final static int POLLING_INTERVAL   = 1000;
    private final static int FIRST_POLLING_DELAY = 1000;

    private final static int TIME_SLOT_MIN = 0;
    private final static int TIME_SLOT_MAX = 23;

    public final static String EXPORT_FILE_NAME = "wmc_configuration.xml";

    //Spinner
    private Spinner deviceSpinner;
    private Spinner digitsSpinner;
    private Spinner weekdaySpinner;
    private Spinner typeSpinner;

    //Buttons
    private Button presetButton;
    private Button clearButton;
    private Button graphButton;
    private Button syncButton;
    private Button smsTestButton;
    private Button gsmOnButton;
    private Button connectButton;
    private ImageButton refreshButton;

    //EditTexts
    //Site
    private EditText sideCodeEt;
    private EditText currTimeEt;
    private EditText currDateEt;
    private EditText deliveryEt;

    //Overall
    private EditText overallTotalEt;
    private EditText overallTS1Et;
    private EditText overallTS2Et;
    private EditText overallPresetEt;

    //Counter
    private EditText counterTotalEt;
    private EditText counterTS1Et;
    private EditText counterTS2Et;

    //Sensor
    private EditText sensorLitresRoundEt;
    private EditText sensorLFConstEt;

    //Communication
    private EditText providerEt;
    private EditText pinCodeEt;
    private EditText smsRecipientEt;
    private EditText smsOriginEt;
    private EditText timeServerEt;

    //Time Slots
    private EditText timeSlot1StartEt;
    private EditText timeSlot1EndEt;
    private EditText timeSlot2StartEt;
    private EditText timeSlot2EndEt;

    //Extras
    private EditText gsmRssiEt;
    private EditText smsTestEt;

    //textviews
    private TextView versionTV;
    private TextView firmWareTV;
    private TextView statusTV;

    //model
    private Configuration currentConfiguration;
    private Mission mission;
    private Page page;

    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private Handler handler;
    private ArrayAdapter<BluetoothDevice> deviceAdapter;
    private BluetoothDevice selectedDevice;

    private SimpleDateFormat date_sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    private SimpleDateFormat time_sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private boolean connected = false;
    private boolean isPolling = true;
    private boolean inflated  = false;
    private boolean recreated = false;
    private boolean didAskToEnableBluetooth = false;
    private int rssi = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
            this.inflated  = savedInstanceState.getBoolean(ARG_INFLATED);
            this.rssi      = savedInstanceState.getInt(ARG_RSSI);
        }

        setHasOptionsMenu(true);
        
    	mission =  (Mission) getActivity().getIntent().getExtras().getSerializable(FormPageFragment.ARG_MISSION);
    	
    	if(getArguments() != null && getArguments().containsKey(FormPageFragment.ARG_OBJECT)){
    		Integer pageNumber = (Integer) getArguments().get(FormPageFragment.ARG_OBJECT);
    		if(pageNumber!=null){
    			MissionTemplate t = MissionUtils.getDefaultTemplate(getActivity());
    			//if page number exists i suppose pages is not empty
    			page = t.sop_form.pages.get(pageNumber);

    		}
    	}
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	Log.i(TAG,"onCreateView");

        final View view = inflater.inflate(R.layout.wmc_layout,container, false);

        //spinner
        deviceSpinner  = (Spinner) view.findViewById(R.id.device_spinner);
        digitsSpinner  = (Spinner) view.findViewById(R.id.digits_spinner);
        weekdaySpinner = (Spinner) view.findViewById(R.id.weekday_spinner);
        typeSpinner    = (Spinner) view.findViewById(R.id.type_spinner);

        //buttons
        presetButton  = (Button) view.findViewById(R.id.button_preset);
        clearButton   = (Button) view.findViewById(R.id.button_clear);
        graphButton   = (Button) view.findViewById(R.id.button_graph);
        syncButton = (Button) view.findViewById(R.id.sync_button);
        smsTestButton = (Button) view.findViewById(R.id.sms_test_button);
        gsmOnButton   = (Button) view.findViewById(R.id.gsm_on_button);
        connectButton   = (Button) view.findViewById(R.id.connect_device_button);
        refreshButton = (ImageButton) view.findViewById(R.id.refresh_devices_button);

        //edittexts
        sideCodeEt = (EditText) view.findViewById(R.id.code_et);
        currTimeEt = (EditText) view.findViewById(R.id.curr_time_et);
        currDateEt = (EditText) view.findViewById(R.id.curr_date_et);
        deliveryEt = (EditText) view.findViewById(R.id.delivery_et);

        overallTotalEt  = (EditText) view.findViewById(R.id.overall_total_et);
        overallTS1Et    = (EditText) view.findViewById(R.id.overall_ts1_et);
        overallTS2Et    = (EditText) view.findViewById(R.id.overall_ts2_et);
        overallPresetEt = (EditText) view.findViewById(R.id.preset_et);

        counterTotalEt = (EditText) view.findViewById(R.id.counter_total_et);
        counterTS1Et   = (EditText) view.findViewById(R.id.counter_ts1_et);
        counterTS2Et   = (EditText) view.findViewById(R.id.counter_ts2_et);

        sensorLitresRoundEt = (EditText) view.findViewById(R.id.hf_et);
        sensorLFConstEt = (EditText) view.findViewById(R.id.k_const_et);

        providerEt = (EditText) view.findViewById(R.id.provider_et);
        pinCodeEt = (EditText) view.findViewById(R.id.pin_code_et);
        smsRecipientEt = (EditText) view.findViewById(R.id.sms_rec_et);
        smsOriginEt = (EditText) view.findViewById(R.id.sms_origin_et);
        timeServerEt = (EditText) view.findViewById(R.id.time_server_et);

        final TimeSlotInputFilter filter = new TimeSlotInputFilter(TIME_SLOT_MIN,TIME_SLOT_MAX);
        timeSlot1StartEt = (EditText) view.findViewById(R.id.tslots_t1_b_et);
        timeSlot1StartEt.setFilters(new InputFilter[]{filter});
        timeSlot1EndEt = (EditText) view.findViewById(R.id.tslots_t1_e_et);
        timeSlot1EndEt.setFilters(new InputFilter[]{filter});
        timeSlot2StartEt = (EditText) view.findViewById(R.id.tslots_t2_b_et);
        timeSlot2StartEt.setFilters(new InputFilter[]{filter});
        timeSlot2EndEt = (EditText) view.findViewById(R.id.tslots_t2_e_et);
        timeSlot2EndEt.setFilters(new InputFilter[]{filter});

        gsmRssiEt = (EditText) view.findViewById(R.id.rssi_et);
        smsTestEt = (EditText) view.findViewById(R.id.sms_test_et);

        versionTV  = (TextView) view.findViewById(R.id.version_tv);
        firmWareTV = (TextView) view.findViewById(R.id.firmware_tv);
        statusTV   = (TextView) view.findViewById(R.id.state_tv);

        weekdaySpinner.setFocusable(false);

        presetButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);
        graphButton.setOnClickListener(this);
        syncButton.setOnClickListener(this);
        smsTestButton.setOnClickListener(this);
        gsmOnButton.setOnClickListener(this);
        connectButton.setOnClickListener(this);
        refreshButton.setOnClickListener(this);
        
        EventBus.getDefault().register(this);

        //update the connection state 
        this.connected = isServiceRunning(getActivity());
        
        Log.i(TAG, "view recreated connection state is "+ (this.connected ? "connected" : "not connected"));
        
        //update the UI according to the current state
        changeState(this.connected, false);
        if(this.connected){
            updatePairedDevices();
        }
        
        if(!inflated){
        	inflated = true;
        }else{
        	recreated = true;
        }
        
        return view;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    	if(bluetoothAdapter == null){
    		//this device does not have Bluetooth
    		 reportStatus(R.string.state_bt_not_available);
    		 connectButton.setEnabled(false);
    		 
    	}else{
    		//is bluetooth enabled ?
    		if(!bluetoothAdapter.isEnabled()){
    			
    			if(!didAskToEnableBluetooth){
    				//need to enable, ask user
    				startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
                	didAskToEnableBluetooth = true;
    			}else{
    				//asked earlier, did not want to enable
    				connectButton.setEnabled(false);
    			}
                 
    		}else{

    			if(isConnected()){
    				if(BuildConfig.DEBUG){
    					Log.d(TAG, "onResume starting polling");
    				}
    				startPolling();
    			}else{
    				if(BuildConfig.DEBUG){
    					Log.d(TAG, "onResume not connected");
    				}
    				if(recreated){
    					clearForm();
    				}
    				updatePairedDevices();
    				startDeviceSpinnerListening();
    			}
    		}
    	}
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	 if(isConnected()){
    		 if(BuildConfig.DEBUG){
    	    		Log.d(TAG, "onPause stopping polling");
    	    	}
             stopPolling();
         }else{
        	 if(BuildConfig.DEBUG){
   	    		Log.d(TAG, "onPause not connected");
   	    	 }
             stopDeviceSpinnerListening();
         }
    	 
    	persistConfigToDB();	
    }
    
    @Override
    public void onDestroyView() {
    	super.onDestroyView();
    	
    	  EventBus.getDefault().unregister(this);
    	
    	 if(BuildConfig.DEBUG){
	    	Log.d(TAG, "onDestroyView");
	     }
    	 
    	 if(alertDialog != null && alertDialog.isShowing()){
             alertDialog.dismiss();
         }
         if(progressDialog != null && progressDialog.isShowing()){
             progressDialog.dismiss();
         }
    }

    @Override 
    public void onSaveInstanceState(Bundle outState) {
    	
    	persistConfigToDB();	
    	
    	outState.putBoolean(ARG_INFLATED, this.inflated);
    	outState.putInt(ARG_RSSI, this.rssi);
    	
    	super.onSaveInstanceState(outState);
    }

	private void persistConfigToDB(){
		
    	if(mission != null && mission.db == null){
    		if(getActivity() instanceof FormEditActivity){
				Log.d(TAG, "Connecting to Activity database");
				mission.db = ((FormEditActivity)getActivity()).spatialiteDatabase;
			}
    	}else if(mission == null){
    		Log.w(TAG, "mission null, aborting...");
			return;
    	}
    	
    	//get table
    	String tableName = mission.getTemplate().id + MissionTemplate.DEFAULT_TABLE_DATA_SUFFIX;
    	if(mission.getTemplate().schema_sop != null 
    			&& mission.getTemplate().schema_sop.localFormStore != null
    			&& !mission.getTemplate().schema_sop.localFormStore.isEmpty()){
    		tableName = mission.getTemplate().schema_sop.localFormStore;
    	}
    	
    	if(tableName == null || tableName.isEmpty()){
			Log.w(TAG, "Empty tableName, aborting...");
			return;
		}
		
		if(mission.db != null){
			
			//TODO convert current config to String
			//persist String in table
			
			Log.i(TAG, "ready to persist wmc config");
		
		}else{
			Log.w(TAG, "Database not found, aborting...");
		}
	}

    /**
     * starts listening to device spinner and refresh button
     * when not connected to a device
     */
    private void startDeviceSpinnerListening(){

        postSpinner(deviceSpinner);
        deviceSpinner.setEnabled(true);
        refreshButton.setVisibility(View.VISIBLE);
    }

    /**
     * stops listening to device spinner and refresh button
     * when connected to a device
     */
    private void stopDeviceSpinnerListening(){

        deviceSpinner.setOnItemSelectedListener(null);
        deviceSpinner.setEnabled(false);
        refreshButton.setVisibility(View.GONE);
    }

    /**
     * starts listening to spinner selected / editText text changed events
     * done when connected to a device
     */
    private void startFormListening(){

        postSpinner(typeSpinner);

        sideCodeEt.addTextChangedListener(this);
        sensorLitresRoundEt.addTextChangedListener(this);
        sensorLFConstEt.addTextChangedListener(this);
        timeSlot1StartEt.addTextChangedListener(this);
        timeSlot1EndEt.addTextChangedListener(this);
        timeSlot2StartEt.addTextChangedListener(this);
        timeSlot2EndEt.addTextChangedListener(this);

        providerEt.addTextChangedListener(this);
        pinCodeEt.addTextChangedListener(this);
        smsRecipientEt.addTextChangedListener(this);
        smsOriginEt.addTextChangedListener(this);
        timeServerEt.addTextChangedListener(this);
    }

    /**
     * stops listening to spinner selected / editText text changed events
     * done when disconnected to a device
     */
    private void stopFormListening(){

        typeSpinner.setOnItemSelectedListener(null);

        sideCodeEt.removeTextChangedListener(this);
        sensorLitresRoundEt.removeTextChangedListener(this);
        sensorLFConstEt.removeTextChangedListener(this);
        timeSlot1StartEt.removeTextChangedListener(this);
        timeSlot1EndEt.removeTextChangedListener(this);
        timeSlot2StartEt.removeTextChangedListener(this);
        timeSlot2EndEt.removeTextChangedListener(this);

        providerEt.removeTextChangedListener(this);
        pinCodeEt.removeTextChangedListener(this);
        smsRecipientEt.removeTextChangedListener(this);
        smsOriginEt.removeTextChangedListener(this);
        timeServerEt.removeTextChangedListener(this);
    }

    /**
     * adds the item selected listener to the spinner when the UI was fully inflated
     * this avoids that the onItemSelected event is fired with non-human events
     * @param spinner the spinner to add the listener to
     */
    private void postSpinner(final Spinner spinner){

        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setOnItemSelectedListener(WMCForm.this);
            }
        });
    }


    private void startPolling(){
        getHandler().removeCallbacks(mPollingTask);
        getHandler().postDelayed(mPollingTask, FIRST_POLLING_DELAY);

    }
    private void stopPolling(){
        getHandler().removeCallbacks(mPollingTask);
    }

    /**
     /////////////// Form State //////////////////////////
     */

    public void changeState(final boolean connected, final boolean report){
    	
    	changeFormMode(connected);
    	deviceSpinner.setFocusable(!connected);
    	refreshButton.setFocusable(!connected);
    	
    	if(connected){
    		
    	    //change form state to connected
            startFormListening();
            stopDeviceSpinnerListening();
            
            connectButton.setText(getString(R.string.button_disconnect));

            //start polling
            isPolling = true;
            readWaterData();
            startPolling();

    	}else{
    		
    		//stop polling
    		isPolling = false;
    	    stopPolling();
    	    
    	    stopFormListening();
    	    startDeviceSpinnerListening();
    	
            //change form state to disconnected
            connectButton.setText(getString(R.string.button_connect));

            //clear
            clearForm();
            //report
            if(report){
            	reportStatus(getString(R.string.state_disconnected));
            } 
    	}
    	
    	getActivity().invalidateOptionsMenu();
    	
    }
    /**
     * changes the state of the form according to the @param isConnected mode
     * giving the possibility to use/edit the buttons spinners and editTexts
     * @param connected if a device is currently isConnected
     */
    private void changeFormMode(final boolean connected){

        //remove / add load/write conf option in menu
        getActivity().invalidateOptionsMenu();

        if(!connected) {
            weekdaySpinner.setSelection(0);
            gsmRssiEt.setText("---");
        }

        syncButton.setEnabled(connected);
        gsmOnButton.setEnabled(connected);
        
        //sms test button will only be enabled when rssi is received -> see readWaterData()
        if(this.rssi != 0){        	
        	smsTestButton.setEnabled(true);
        }else{
        	smsTestButton.setEnabled(false);
        }
        
        presetButton.setEnabled(connected);
        clearButton.setEnabled(connected);
        graphButton.setEnabled(connected);
        weekdaySpinner.setEnabled(connected);
        digitsSpinner.setEnabled(connected);
        typeSpinner.setEnabled(connected);

        //editable edit texts :

        sideCodeEt.setFocusable(connected);
        sideCodeEt.setFocusableInTouchMode(connected);
        sensorLitresRoundEt.setFocusable(connected);
        sensorLitresRoundEt.setFocusableInTouchMode(connected);
        sensorLFConstEt.setFocusable(connected);
        sensorLFConstEt.setFocusableInTouchMode(connected);
        timeSlot1StartEt.setFocusable(connected);
        timeSlot1StartEt.setFocusableInTouchMode(connected);
        timeSlot1EndEt.setFocusable(connected);
        timeSlot1EndEt.setFocusableInTouchMode(connected);
        timeSlot2StartEt.setFocusable(connected);
        timeSlot2StartEt.setFocusableInTouchMode(connected);
        timeSlot2EndEt.setFocusable(connected);
        timeSlot2EndEt.setFocusableInTouchMode(connected);

        //can also be edited
        providerEt.setFocusable(connected);
        providerEt.setFocusableInTouchMode(connected);
        pinCodeEt.setFocusable(connected);
        pinCodeEt.setFocusableInTouchMode(connected);
        smsRecipientEt.setFocusable(connected);
        smsRecipientEt.setFocusableInTouchMode(connected);
        smsOriginEt.setFocusable(connected);
        smsOriginEt.setFocusableInTouchMode(connected);
        timeServerEt.setFocusable(connected);
        timeServerEt.setFocusableInTouchMode(connected);
        smsTestEt.setFocusable(connected);
        smsTestEt.setFocusableInTouchMode(connected);
    }

    /**
     * populates the WMC form with a @param config
     * @param config the config to apply
     */
    private void populateForm(final Configuration config){

        //time slots
        timeSlot1StartEt.setText(String.format(Locale.getDefault(),"%d",config.timerSlot1Start));
        timeSlot1EndEt.setText(String.format(Locale.getDefault(),"%d",config.timerSlot1Stop));
        timeSlot2StartEt.setText(String.format(Locale.getDefault(),"%d",config.timerSlot2Start));
        timeSlot2EndEt.setText(String.format(Locale.getDefault(),"%d",config.timerSlot2Stop));

        //sensor type
        typeSpinner.setSelection(config.sensorType);
        sensorLitresRoundEt.setText(String.format(Locale.getDefault(),"%d",config.sensorLitresRound));
        sensorLFConstEt.setText(String.format(Locale.getDefault(),"%d",config.sensor_LF_Const));

        if (config.sensorType == 1) { //sensorType "HF & Dir" makes lf const non-editable
            sensorLFConstEt.setEnabled(false);
        } else { // with sensorType "LF" this const is editable
            sensorLFConstEt.setEnabled(true);
        }

        //Strings
        if(config.provider != null){
            providerEt.setText(config.provider);
        }
        if (config.pinCode != null){
            pinCodeEt.setText(config.pinCode);
        }
        if (config.recipientNum != null) {
            smsRecipientEt.setText(config.recipientNum);
        }
        if (config.ntpAddress != null) {
            timeServerEt.setText(config.ntpAddress);
        }
        if (config.originNum != null){
            smsOriginEt.setText(config.originNum);
        }

        //site code
        sideCodeEt.setText(String.format(Locale.getDefault(),"%04d",config.siteCode));
        updateDeliveryAccordingToSiteCode(config.siteCode);

        //digits
        if(config.version >= 0x200) {
            //enable and set according to config
            digitsSpinner.setEnabled(true);

            if (config.digits == 6) {
                digitsSpinner.setSelection(0);
            } else if (config.digits == 7){
                digitsSpinner.setSelection(1);
            } else {
                digitsSpinner.setSelection(2);
            }
        }else{
            //disable, allow 6
            digitsSpinner.setEnabled(false);
            digitsSpinner.setSelection(0);
        }

        //week day index , select TODAY
        weekdaySpinner.setSelection(0);

        //version and firmware
        firmWareTV.setText(String.format(Locale.getDefault(),"F.#%s",Integer.toString(config.version, 16)));
        
        String versionCode = "1.0";
        try {
            versionCode = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
          Log.e(TAG, "error getting current version name",e);
        }
        
        versionTV.setText(String.format(Locale.getDefault(),"V.%s", versionCode));
    }

    /**
     * clears the form
     */
    private void clearForm(){
    	
        //time slots
        timeSlot1StartEt.setText("");
        timeSlot1EndEt.setText("");
        timeSlot2StartEt.setText("");
        timeSlot2EndEt.setText("");

        //sensor type
        sensorLitresRoundEt.setText("");
        sensorLFConstEt.setText("");

        //"Strings"
        providerEt.setText("");
        pinCodeEt.setText("");
        smsRecipientEt.setText("");
        timeServerEt.setText("");
        smsOriginEt.setText("");

        //site code
        sideCodeEt.setText("");
        deliveryEt.setText("");

        overallTotalEt.setText("");
        overallTS1Et.setText("");
        overallTS2Et.setText("");

        counterTotalEt.setText("");
        counterTS1Et.setText("");
        counterTS2Et.setText("");

        //time update
        currDateEt.setText("");
        currTimeEt.setText("");

        gsmRssiEt.setText("---");
        overallPresetEt.setText("");

        //version and firmware
        firmWareTV.setText("");
    }

    private boolean updateDeliveryAccordingToSiteCode(final int site_Code){

        if (site_Code > 2999) {
            reportStatus(getString(R.string.state_site_code_invalid));
            sideCodeEt.setText("");
            return false;
        }
        final String[] weekdWithTODAY = getResources().getStringArray(R.array.weekday_values);
        //remove "TODAY"
        final String[] weekd = Arrays.copyOfRange(weekdWithTODAY, 1, weekdWithTODAY.length);

        //algorithm taken as it is from Windows source code Form1.cs line 1134 ff.
        boolean done = false;
        int count = site_Code;
        int h1 = 0;
        int d1 = 0;
        int h2;
        int d2;
        int wday_b;
        int hour_b;
        int wday_e;
        int hour_e;

        while (!done) {
            count -= 10;
            if (count >= 0) {
                h1++;
                if (h1 == 23) {
                    h1 = 0;
                    d1++;
                    if (d1 == 7)
                        d1 = 0;
                }
            } else {
                done = true;
            }
        }
        wday_b = d1;
        hour_b = h1;
        h2 = (char)(h1 + 1);
        d2 = d1;

        if (h2 >= 24) {
            h2 = 0;
            d2++;
        }
        wday_e = d2;
        hour_e = h2;

        String delivery = String.format(Locale.US,"%s,%d - %s,%d",weekd[wday_b], hour_b, weekd[wday_e], hour_e);

        deliveryEt.setText(delivery);

        return true;
    }

    /**
     * update the paired devices spinner
     */
    private void updatePairedDevices() {

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter == null){
            //no bluetooth available
            reportStatus(R.string.state_bt_not_available);
        } else {
            //get currently paired devices
            //TODO apply filtering ?
            final ArrayList<BluetoothDevice> pairedDevices = BluetoothUtil.getPairedDevices(WMC_DEVICE_PREFIX);

            if (pairedDevices != null && pairedDevices.size() > 0) {

                getBluetoothDeviceAdapter().clear();
                getBluetoothDeviceAdapter().addAll(pairedDevices);
                getBluetoothDeviceAdapter().setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                deviceSpinner.setAdapter(getBluetoothDeviceAdapter());

                //as in the windows application (Form1.cs line 241) we assume the first of the available devices as being currently selected
                selectedDevice = pairedDevices.get(0);

            } else {
                reportStatus(R.string.state_bt_none_available);
            }
        }
    }
    
/**
    ////////////// Spinner selection listener////////////////
 */
@Override
public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
	
	Log.i(TAG, "Spinner item selected " + position);

    if(spinner.equals(deviceSpinner)){

        final BluetoothDevice device = deviceAdapter.getItem(deviceSpinner.getSelectedItemPosition());
        String deviceName = device.getName() == null ? "null " : device.getName();
        statusTV.setText(getString(R.string.state_device_selected,deviceName));
        selectedDevice = device;

    }else if(spinner.equals(typeSpinner)){

        if(currentConfiguration != null) {

            if(BuildConfig.DEBUG) {
                Log.i(TAG, "sensor type changed to " + typeSpinner.getSelectedItemPosition() + " which is " + typeSpinner.getSelectedItem().toString());
            }
            currentConfiguration.sensorType = typeSpinner.getSelectedItemPosition();
            //enable/disable const editText according to device type
            sensorLFConstEt.setEnabled(currentConfiguration.sensorType != 1);
            reportStatus(R.string.state_sensor_type_changed);
        }
    }
}
@Override
public void onNothingSelected(AdapterView<?> parent) {    }

/**
    ////////////// EditText text changed watcher/////////////////
 */
@Override
public void afterTextChanged(Editable s) {

    if(currentConfiguration == null){
        return;
    }

    if(s.length() == 0){
        if(BuildConfig.DEBUG) {
            Log.i(TAG, "changed to length 0, returning");
        }
        return;
    }

    //identifying sender according to the hashCode of the senders content @source http://stackoverflow.com/questions/4283062/textwatcher-for-more-than-one-edittext
    if(sideCodeEt.getText().hashCode() == s.hashCode()){

        try{
            int newSiteCode = Integer.parseInt(sideCodeEt.getText().toString());
            if(updateDeliveryAccordingToSiteCode(newSiteCode)){
                currentConfiguration.siteCode = newSiteCode;
            }
        }catch (NumberFormatException e){
            reportStatus(getString(R.string.state_incorrect_input));
        }

    }else if(sensorLitresRoundEt.getText().hashCode() == s.hashCode()){

        try{
            currentConfiguration.sensorLitresRound = Integer.parseInt(sensorLitresRoundEt.getText().toString());
        }catch (NumberFormatException e){
            reportStatus(getString(R.string.state_incorrect_input));
        }

    }else if(sensorLFConstEt.getText().hashCode() == s.hashCode()){

        try{
            currentConfiguration.sensor_LF_Const = Integer.parseInt(sensorLFConstEt.getText().toString());
        }catch (NumberFormatException e){
            reportStatus(getString(R.string.state_incorrect_input));
        }

    }else if(timeSlot1StartEt.getText().hashCode() == s.hashCode()){

        validateTimeSlot(timeSlot1StartEt, true);

    }else if(timeSlot1EndEt.getText().hashCode() == s.hashCode()){

        validateTimeSlot(timeSlot1EndEt, false);

    }else if(timeSlot2StartEt.getText().hashCode() == s.hashCode()){

        validateTimeSlot(timeSlot2StartEt, true);

    }else if(timeSlot2EndEt.getText().hashCode() == s.hashCode()){

        validateTimeSlot(timeSlot2EndEt, false);
    }else if(providerEt.getText().hashCode() == s.hashCode()){

        if(TextUtils.isEmpty(providerEt.getText().toString()) || providerEt.getText().toString().length() > Configuration.BYTES_MAX_PROVIDER){
            reportStatus(getString(R.string.state_incorrect_input));
            return;
        }
        currentConfiguration.provider = providerEt.getText().toString();

    }else if(pinCodeEt.getText().hashCode() == s.hashCode()){

        if(TextUtils.isEmpty(pinCodeEt.getText().toString()) || pinCodeEt.getText().toString().length() > 5){
            reportStatus(getString(R.string.state_incorrect_input));
            return;
        }
        currentConfiguration.pinCode = pinCodeEt.getText().toString();

    }else if(smsRecipientEt.getText().hashCode() == s.hashCode()){

        if(TextUtils.isEmpty(smsRecipientEt.getText().toString()) || smsRecipientEt.getText().toString().length() > Configuration.BYTES_MAX_RECIPIENT){
            reportStatus(getString(R.string.state_incorrect_input));
            return;
        }
        currentConfiguration.recipientNum = smsRecipientEt.getText().toString();

    }else if(smsOriginEt.getText().hashCode() == s.hashCode()){

        if(TextUtils.isEmpty(smsOriginEt.getText().toString()) || smsOriginEt.getText().toString().length() > Configuration.BYTES_MAX_ORIGINNUM){
            reportStatus(getString(R.string.state_incorrect_input));
            return;
        }
        currentConfiguration.originNum = smsOriginEt.getText().toString();

    }else if(timeServerEt.getText().hashCode() == s.hashCode()){

        if(TextUtils.isEmpty(timeServerEt.getText().toString()) || timeServerEt.getText().toString().length() > Configuration.BYTES_MAX_NTPADDRESS){
            reportStatus(getString(R.string.state_incorrect_input));
            return;
        }
        currentConfiguration.ntpAddress = timeServerEt.getText().toString();

    }
}

/**
 * validates a new value for a time slot edittext
 * @param ed the editText to validate
 * @param isStart if it is a start slot
 */
private void validateTimeSlot(final EditText ed, final boolean isStart){

    final String name = isStart ? getString(R.string.wmc_ts_1) : getString(R.string.wmc_ts_2);

    //empty ?
    if (TextUtils.isEmpty(ed.getText().toString())) {
        statusTV.setText(isStart ? getString(R.string.state_time_slot_start_null, name) : getString(R.string.state_time_slot_end_null, name));
        return;
    }
    try{
        //valid ? due to filtering @TimeSlotInputFilter this should not be necessary, but better double check
        final int newValue = Integer.parseInt(ed.getText().toString());
        if(newValue > TIME_SLOT_MAX || newValue < TIME_SLOT_MIN){
            statusTV.setText(isStart ? getString(R.string.state_time_slot_start_oob, name) : getString(R.string.state_time_slot_end_oob, name));
            return;
        }

        //valid
        if(ed.hashCode() == timeSlot1StartEt.hashCode()) {
            currentConfiguration.timerSlot1Start = newValue;
        }else if(ed.hashCode() == timeSlot1EndEt.hashCode()){
            currentConfiguration.timerSlot1Stop = newValue;
        }else if(ed.hashCode() == timeSlot2StartEt.hashCode()){
            currentConfiguration.timerSlot2Start = newValue;
        }else if(ed.hashCode() == timeSlot2EndEt.hashCode()){
            currentConfiguration.timerSlot2Stop = newValue;
        }
        statusTV.setText(isStart ? getString(R.string.state_time_slot_start_edited, name, newValue) : getString(R.string.state_time_slot_end_edited, name, newValue));

    }catch (NumberFormatException e){
        statusTV.setText(getString(R.string.state_incorrect_input));
    }
}

@Override
public void beforeTextChanged(CharSequence s, int start, int count, int after) {    }

@Override
public void onTextChanged(CharSequence s, int start, int before, int count) {    }


    /**
     * ////////////// Click listener for all buttons////////////////
     *
     * In the windows app buttons are disabled during communication with a wmc.
     * In Android we do the communication in background and show meanwhile a progress view,
     * hence button deactivation is not necessary as the user cannot click another button
     */
    @Override
    public void onClick(View v) {

        if(v.equals(connectButton)){
            //already connected ?
            if(isConnected()){
                // --> disconnect
                disconnect(true);

            } else {
                // --> connect

                //not connected yet, do we have a device ?
                if(selectedDevice == null){
                    reportStatus(R.string.state_bt_none_selected);
                } else {

                    //okay, connect
                    showProgressDialog();
                    startService(selectedDevice, debug);
                }
            }

        }else if(v.equals(gsmOnButton)){

            if(isConnected()){
            	executeWMCCommand(WMCCommand.RSSI);
            }else{
                reportStatus(R.string.state_not_connected);
            }

        }else if(v.equals(smsTestButton)){

            final String phone = smsTestEt.getText().toString();

            if(!TextUtils.isEmpty(phone)){
                //is not empty, also valid ?
                if(phone.length() == 13 && android.util.Patterns.PHONE.matcher(phone).matches()){
                    //looks valid, go
                	 executeWMCCommand(WMCCommand.TEST_SMS, phone);
                }else{
                    reportStatus(R.string.state_test_sms_invalid);
                }
            }else{
                reportStatus(R.string.state_recipient_null);
            }

        }else if(v.equals(syncButton)){

            if(isConnected()){
                executeWMCCommand(WMCCommand.WRITE_TIME);
            }else{
                reportStatus(R.string.state_not_connected);
            }

        }else if(v.equals(presetButton)){

            final String preset = overallPresetEt.getText().toString();

            if(!TextUtils.isEmpty(preset)){
                //validate
                double  limit_lo;
                double  limit_hi;
                String s;
                if(digitsSpinner.getSelectedItemPosition() == 0 || currentConfiguration.version < 0x200){
                    limit_hi =  999999.99;
                    limit_lo = -999999.99;
                }else if (digitsSpinner.getSelectedItemPosition() == 1){
                    limit_hi =  9999999.99;
                    limit_lo = -9999999.99;
                } else {
                    limit_hi =  99999999.99;
                    limit_lo = -99999999.99;
                }
                s = getString(R.string.state_preset_failure,limit_lo,limit_hi);

                try{
                    double value = Double.parseDouble(preset);
                    if (value > limit_hi || value < limit_lo){
                        reportStatus(s);
                    }else{
                        executeWMCCommand(WMCCommand.PRESET, preset);
                    }
                }catch (NumberFormatException e){
                    Log.e(TAG, "exception parsing preset "+ preset, e );
                    reportStatus(R.string.state_incorrect_input);
                }
            }else{
                reportStatus(R.string.state_incorrect_input);
            }

        } else if(v.equals(clearButton)){

            if(isConnected()){
                final int week_day_index = weekdaySpinner.getSelectedItemPosition();
                executeWMCCommand(WMCCommand.CLEAR_COUNTER, Integer.toString(week_day_index));
            }else{
                reportStatus(R.string.state_not_connected);
            }

        } else if(v.equals(refreshButton)){

            updatePairedDevices();
        }
    }
    
    /**
     * Starts the WMC service
     * @param the device to connect to
     * @param useMock if to use a mock connection
     */
    private void startService(final BluetoothDevice device, final boolean useMock){

        Intent serviceIntent = new Intent(getActivity(), WMCService.class);
        serviceIntent.putExtra(WMCService.PARAM_DEVICE, device);
        serviceIntent.putExtra(WMCService.PARAM_DEBUG, useMock);
        getActivity().startService(serviceIntent);
    }
    
    /**
     * when a configuration was read this populates the form with the new configuration
     */
    public void onConfigurationRead(final Configuration configuration, final String name){
    	
        if (configuration != null) {
            currentConfiguration = configuration;
            //pause listening to form events during the new configuration is applied
            stopFormListening();
            populateForm(configuration);
            startFormListening();
            statusTV.setText(getString(R.string.state_read_success, name));
        } else {
            //config could not be read
            statusTV.setText(R.string.state_comm_error);
        }
    } 

    /**
     * disconnects from the current connected wmc
     * and changed the form into not listening mode
     * and clears its content
     * @param disconnectListener listener to be informed when disconnecting async
     */
    public void disconnect(boolean withUI){
    	
    	Log.i(TAG, "disconnect");

    	//disconnect
    	changeState(false, withUI);
    	
    	if(withUI){
    		showProgressDialog();
    	}else{
    		//wont receive events
    		this.connected = false;
    	}
    	
    	executeWMCCommand(WMCCommand.DISCONNECT);
    }

    /////////////// COMMUNICATION//////////////////
    /**
     * reads (water meter) data from the device and updates the UI
     * this is done frequently using POLLING_INTERVAL and therefore
     * checks if the last task finished which will nullify the reference to the task
     */
    private void readWaterData(){

        if(isPolling && isConnected()){
            
        	 EventBus.getDefault().post(new RequestWMCDataEvent(WMCCommand.READ_WATER, null));
            
        }
    }

    /**
     * executes a command to the wmc
     * @param command the command to execute
     * @param args arguments which may be necessary e.g. the telephone number for test sms
     */
    private void executeWMCCommand(final WMCCommand command, final String... args){
    	
    	   isPolling = false;
           showProgressDialog();
           
           switch (command){

           case READ_CONFIG:
        	   EventBus.getDefault().post(new RequestWMCDataEvent(WMCCommand.READ_CONFIG, null));
        	   break;
           case WRITE_CONFIG:
        	   EventBus.getDefault().post(new RequestWMCDataEvent(WMCCommand.WRITE_CONFIG, currentConfiguration));
        	   break;
           case READ_WATER:
        	   //is handled separately
        	   break;
           case DISCONNECT:
        	   EventBus.getDefault().post(new RequestWMCDataEvent(WMCCommand.DISCONNECT, null));
        	   break;
           case RSSI:
        	   //currently this is always an "on" request
        	   //no "off" in Windows source code
        	   EventBus.getDefault().post(new RequestWMCDataEvent(WMCCommand.RSSI, true));
        	   break;
           case TEST_SMS:
        	   if(args == null || args.length < 1 || args[0] == null){
        		   Log.w(TAG, "No telephone number provided for test sms");
        		   return;
        	   }
        	   EventBus.getDefault().post(new RequestWMCDataEvent(WMCCommand.TEST_SMS, args[0]));
        	   break;
           case WRITE_TIME:
        	   EventBus.getDefault().post(new RequestWMCDataEvent(WMCCommand.WRITE_TIME, null));
        	   break;
           case PRESET:
        	   if(args == null || args.length < 1 || args[0] == null){
        		   Log.w(TAG, "No preset provided");
        		   return;
        	   }
        	   EventBus.getDefault().post(new RequestWMCDataEvent(WMCCommand.PRESET, args[0]));
        	   break;
           case CLEAR_COUNTER:
        	   if(args == null || args.length < 1 || args[0] == null){
        		   Log.w(TAG, "No clear week day parameter provided");
        		   return;
        	   }
        	   EventBus.getDefault().post(new RequestWMCDataEvent(WMCCommand.CLEAR_COUNTER, args[0]));
        	   break;
           }
    }
    
    /**
     * receives the result of a data request from the service
     * @param event
     */
    @Subscribe
    public void onEvent(WMCCommunicationResultEvent event){

        hideProgress();

        switch (event.getCommand()){

            case READ_CONFIG:
                if(event.isSuccess()){
                    final Configuration configuration = event.getConfiguration();
                    if(configuration != null){
                        onConfigurationRead(configuration, event.getDeviceName());
                    }else{                    	
                    	reportStatus(R.string.state_read_failure);
                    }
                }else{
                    reportStatus(R.string.state_read_failure);
                }
                break;
            case WRITE_CONFIG:
                if (event.isSuccess()) {
                    showAlertDialog(getString(R.string.state_write_success));
                } else {
                    reportStatus(R.string.state_write_failure);
                }
                break;
            case READ_WATER:
                if(!event.isSuccess()) {
                    statusTV.setText(R.string.state_comm_error);
                    Log.w(TAG, "error polling");
                }else{
                    final WMCReadResult readResult = event.getReadResult();

                    overallTotalEt.setText(String.format(Locale.US, "%.2f", readResult.overall_total));
                    overallTS1Et.setText(String.format(Locale.US, "%.2f", readResult.overall_Slot1));
                    overallTS2Et.setText(String.format(Locale.US, "%.2f", readResult.overall_Slot2));

                    final int week_day_index = weekdaySpinner.getSelectedItemPosition();

                    counterTotalEt.setText(String.format(Locale.US, "%.2f", readResult.week_total[week_day_index]));
                    counterTS1Et.setText(String.format(Locale.US, "%.2f", readResult.week_Slot1[week_day_index]));
                    counterTS2Et.setText(String.format(Locale.US, "%.2f", readResult.week_Slot2[week_day_index]));

                    //time update
                    currDateEt.setText(String.format(Locale.getDefault(), "%s", date_sdf.format(readResult.date)));
                    currTimeEt.setText(String.format(Locale.getDefault(), "%s", time_sdf.format(readResult.date)));

                    //update the result of the rssi request
                    if (readResult.rssi == 0 || readResult.rssi == 99) {
                        gsmRssiEt.setText("---");
                    } else {
                        gsmRssiEt.setText(String.format(Locale.US, "%d", readResult.rssi));
                        smsTestButton.setEnabled(true);
                        this.rssi = readResult.rssi;
                    }

                }
                break;
            case RSSI:
                if(event.isSuccess()){
                    reportStatus(R.string.state_gms_request_sent);
                }else{
                    reportStatus(R.string.state_comm_error);
                }
                break;
            case TEST_SMS:
                if(event.isSuccess()){
                    reportStatus(R.string.state_test_sms_sent);
                }else{
                    reportStatus(R.string.state_comm_error);
                }
                break;
            case WRITE_TIME:
                if(event.isSuccess()){
                    reportStatus(R.string.state_sync_success);
                }else{
                    reportStatus(R.string.state_sync_failure);
                }
                break;
            case PRESET:
                if(event.isSuccess()){
                    reportStatus(R.string.state_preset_success);
                }else{
                    reportStatus(R.string.state_comm_error);
                }
                break;
            case CLEAR_COUNTER:
                if(event.isSuccess()){
                    reportStatus(R.string.state_clear_success);
                }else{
                    reportStatus(R.string.state_comm_error);
                }
                break;
            case DISCONNECT:
                //nothing, this is done by state listener
                break;
        }
        //event handling done, allow polling if this was not a disconnect event
        if(event.getCommand() != WMCCommand.DISCONNECT){
        	isPolling = true;
        }
    }

    /**
     * validates the current config and sends it to the device
     */
    public void sendConfigToDevice(){

    	//TODO implement "real" validation
    	//pair.first contains validation result (success), pair.second the error message if validation failed
        final Pair<Boolean,String> validationResult = new Pair<Boolean, String>(true, null);

        if(isConnected() && validationResult.first) {

        	executeWMCCommand(WMCCommand.WRITE_CONFIG);

        }else if(validationResult.second != null){
        	
            reportStatus(validationResult.second);
            
        }else if(!isConnected()){
            Log.w(TAG, "send config to device should not be available when not connected to a device");
        }
    }

    /**
     * reads the current configuration from the device
     */
    public void readConfigFromDevice(){

        if(isConnected()){

        	executeWMCCommand(WMCCommand.READ_CONFIG);
        	
        }else{
            Log.w(TAG, "read config from device should not be available when not connected to a device");
        }
    }
    
   /**
    * connection state event receiver coming from service
    * 
    * @param event new state 
    */
    @Subscribe
    public void onEvent(WMCConnectionStateChangedEvent event){

        this.connected = event.isConnected();

        switch (event.getState()){

            case CONNECTED:
            	
            	 hideProgress();
            	 changeState(true, true);
            	 
            	 //connected, request config
            	 executeWMCCommand(WMCCommand.READ_CONFIG);
            	 
                break;
            case DISCONNECTED:

                if(getActivity() == null || getActivity().isFinishing()){
                    //no need to update ui when we're closing
                    return;
                }

                hideProgress();
            	changeState(false, true);
                break;
            case CONNECTION_ERROR:

                hideProgress();
                reportStatus(getString(R.string.state_connect_failure, selectedDevice.getName()));
                break;
        }
    }

    /////////////// Utils //////////////////

    /**
     * shows a progress view
     */
    private void showProgressDialog() {

        try{
            if(progressDialog == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                @SuppressLint("InflateParams")
                View progress = inflater.inflate(R.layout.progress_layout, null);
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setView(progress);
            }
            progressDialog.setMessage(getString(R.string.progress_please_wait));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }catch(Exception e){
            Log.e(TAG, "error showing progress",e);
        }
    }

    /**
     * hides the progress view
     */
    public void hideProgress(){

    	try {
    		if (progressDialog != null && progressDialog.isShowing()) {
    			progressDialog.dismiss();
    		}
    	} catch (Exception e) {
    		Log.e(TAG, "error hiding progress", e);
    	}

    }

    /**
     * shows a generic alert dialog
     * @param text message to show
     */
    protected void showAlertDialog(final String text){

        if(alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(false);
            builder.setTitle(R.string.app_name);
            builder.setMessage(text);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    alertDialog.dismiss();
                }
            });

            alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
        }else{
            alertDialog.setMessage(text);
        }
        alertDialog.show();
    }

    /**
     * shows a dialog asking the user if he wants to disconnect the device
     * @param context context to show dialog
     * @param listener to inform subscribers that the user selected disconnection
     */
    public void showAskForDisconnectDialog(final Context context, final OnDisconnectListener listener){
    	
    	 AlertDialog.Builder builder = new AlertDialog.Builder(context);
         builder.setCancelable(false);
         builder.setTitle(R.string.app_name);
         builder.setMessage(R.string.state_active_connection);
         builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int id) {
                 dialog.dismiss();
                 disconnect(false);
                 if(listener != null){
                	 listener.onDisconnect();
                 }
                
             }
         })
         .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
             }
         });

         AlertDialog alertDialog = builder.create();
         alertDialog.setCanceledOnTouchOutside(false);
         alertDialog.show();
    	
    }
    
    /**
     * array adapter for Bluetooth devices
     * @return the adapter
     */
    private ArrayAdapter<BluetoothDevice> getBluetoothDeviceAdapter(){
        if(deviceAdapter == null){
            deviceAdapter = new ArrayAdapter<BluetoothDevice>(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<BluetoothDevice>()){
               
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {

                    return getViewForItem(position, convertView);
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {

                    return getViewForItem(position, convertView);
                }
                private View getViewForItem(int position, View convertView){
                    View view;
                    if (convertView == null) {
                        view = View.inflate(getActivity(), android.R.layout.simple_spinner_item, null);
                    } else {
                        view = convertView;
                    }
                    final BluetoothDevice device = getItem(position);

                    /**
                     * on windows the display name consists of serial port - bluetooth id - wmc id
                     * on Android exist only the latter ones - address and name
                     */
                    //TODO should the device name be built ???
                    ((TextView) view).setText(String.format(Locale.getDefault(),"%s - %s",device.getAddress(), device.getName() == null ? "null" : device.getName()));
                    return view;
                }
            };
        }
        return deviceAdapter;
    }

    /**
     * task to poll data from the device,
     * repeats execution after POLLING_INTERVAL
     */
    private Runnable mPollingTask = new Runnable() {
        public void run() {
            if(isConnected() && isPolling) {
                readWaterData();
            }
            getHandler().postDelayed(this, POLLING_INTERVAL);
        }
    };

    private Handler getHandler() {
        if(handler == null){
            handler = new Handler();
        }
        return handler;
    }

    protected void reportStatus(int stringResource){
        this.reportStatus(getString(stringResource));
    }

    /**
     * reports messages to the user, currently to both
     * -statsTV and
     * -alerdialog
     *
     * @param string message to report
     */
    protected void reportStatus(String string){
        statusTV.setText(string);
        showAlertDialog(string);
    }

    /**
     * applies the new config by setting it as the current config
     * and populating the UI using it
     * @param newConfig the new conf
     */
    public void applyNewConfig(final Configuration newConfig){

        this.currentConfiguration = newConfig;
        populateForm(currentConfiguration);
    }

    public Configuration getCurrentConfiguration(){
        return currentConfiguration;
    }

    public boolean isConnected() {
        return connected;
    }
    
    public boolean isServiceRunning(Context context) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("it.geosolutions.geocollect.android.core.wmc.service.WMCService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
    
    public void setDebug(final boolean debug){
    	this.debug = debug;
    }
    
    public void setTest(final boolean test){
    	
    	 EventBus.getDefault().unregister(this);
    }
    
    
    
    public interface OnDisconnectListener
    {
    	public void onDisconnect();
    }
}
