package it.geosolutions.android.map.activities;


import it.geosolutions.android.map.BuildConfig;
import it.geosolutions.android.map.R;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
/**
 * shows a seekbar to select the opacity for MBTiles Layer
 * 
 * between 25 % and 100 %
 * 
 * @author Robert Oehler
 *
 */
public class MBTilesLayerOpacitySettingActivity  extends SherlockActivity implements OnSeekBarChangeListener{
	
	public static String MBTILES_OPACITY_ID = "mbtiles_opacity";
	private TextView seekbar_tv;
	private SeekBar seekbar;
  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			
		setContentView(R.layout.mbtiles_opacity_setting_layout);
		
		seekbar = (SeekBar) findViewById(R.id.opacity_setting_seekBar);
		
		seekbar_tv = (TextView) findViewById(R.id.opacity_setting_seekbar_textView);
		
		final Button confirmButton = (Button) findViewById(R.id.opacity_setting_confirmbutton);
		
		final int currentValue = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt(MBTILES_OPACITY_ID, 192);
		
		//there is no setMin, so the value has to be converted always
		seekbar.setMax(75);	
		
		final double percent =  ((double)  currentValue / 256) * 100;
		
		seekbar_tv.setText(String.format("%.0f %%", percent));
		
		seekbar.setProgress(((int) percent) - 25 );
		
		if(BuildConfig.DEBUG){
			Log.d(MBTilesLayerOpacitySettingActivity.class.getSimpleName(), "current "+currentValue+" percent "+percent);
		}
		
		seekbar.setOnSeekBarChangeListener(this);
		
		confirmButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				//save new 
				saveCurrentValue();
				
				finish();
			}
		});
		
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
		
		int newRealValue = progress + 25;

		seekbar_tv.setText(String.format("%d %%", newRealValue));
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {	}

	@Override
	public void onBackPressed() {

		saveCurrentValue();
		super.onBackPressed();
	}

	private void saveCurrentValue() {
		
		final Editor ed = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
		
		final int valueInPercent = seekbar.getProgress() + 25;
		
		final int valueInBit = (int) (256 * ((double)valueInPercent / 100));
		
		ed.putInt(MBTILES_OPACITY_ID, valueInBit);
		
		ed.commit();
		
		if(BuildConfig.DEBUG){
			Log.d(MBTilesLayerOpacitySettingActivity.class.getSimpleName(), "saved percent "+valueInPercent+" to bit "+valueInBit);
		}
		
		Intent returnIntent = new Intent();
		setResult(RESULT_OK,returnIntent);
		finish();
		
	}

}
