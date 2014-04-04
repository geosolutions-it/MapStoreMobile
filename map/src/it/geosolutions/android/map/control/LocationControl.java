/*
 * GeoSolutions map - Digital field mapping on Android based devices
 * Copyright (C) 2013  GeoSolutions (www.geo-solutions.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.android.map.control;

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.view.AdvancedMapView;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.Marker;
import org.mapsforge.android.maps.overlay.MyLocationOverlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Control that manage GeoLocation overlay.
 * Add and remove the <MyLocationOverlay> on the map.
 * Allow controlling snap to location, using long click. 
 * 
 * NOTE: This control does not receives gps position events
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class LocationControl extends MapControl implements OnLongClickListener, OnTouchListener {
	
	// MapsForge Overlay to display device position
MyLocationOverlay overlay = null;

boolean centerAtFirst = false;
/** flag to disable messages **/
boolean restoring = false;
/** preserve the previous status to choose message and icon change */
int previousStatus = 0;

private static final int MESSAGE_UNAVAILABLE = R.string.location_unavailable_message;

private static final int MESSAGE_DEACTIVATED = R.string.location_deactivated_message;

private static final int MESSAGE_ACTIVATED = R.string.location_activated_message;

private static final int MESSAGE_SNAP_ACTIVATED = R.string.location_snap_activated_message;

private static final int MESSAGE_SNAP_DEACTIVATED = R.string.location_snap_deactivated_message;

private static final int MESSAGE_PROMPT_POSITION_ACCESS = R.string.location_promt_position_permission;

private static final int MESSAGE_NETWORK_LOCATION_AVAILABLE = R.string.location_network_available_message;

private static final String DEFAULT_CONTROL_ID = "LOCATION_CONTROL_STATE";

private static final String ENABLED = "ENABLED";

private static final String SNAP = "SNAP";

/** 
 * Location provider status 
 */
private int locationProviderStatus = LocationProvider.OUT_OF_SERVICE;

public Location lastLocation;

/**
 * Animation for the fixing gps icon
 */
AnimationDrawable animation;


/**
 * @param view
 */
public LocationControl(AdvancedMapView view) {
    super(view);
    animation = new AnimationDrawable();
    animation.addFrame(view.getResources().getDrawable(R.drawable.ic_device_access_location_searching), 500);
    animation.addFrame(view.getResources().getDrawable(R.drawable.ic_device_access_location_found), 500);
    animation.setOneShot(false);
}

/**
 * Called when control status changes
 */
@Override
public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);

    Log.v("Location", "Setting to "+enabled);
    if (enabled) {
        // check if GPS is activated
        initMyLocationOverlay();
        //addOverlay();
        locationProviderStatus=LocationProvider.TEMPORARILY_UNAVAILABLE;
        checkLocationEnabled(true);
        if(lastLocation != null){
        	overlay.onLocationChanged(lastLocation);
        }
        refreshIcon();
    } else {
        if(overlay != null){
            overlay.disableMyLocation();
            //removeOverlay();
            setSnap(false);
            int currentStatus = MESSAGE_DEACTIVATED;
            sendMessageIfNeeded(currentStatus);
            // refresh icon
            refreshIcon();
        }
        
    }
    activationButton.setSelected(false);
}

/**
 * remove location overlay from the map
 */
public void removeOverlay() {
    view.overlayManger.removeOverlay(overlay);
}

/**
 * initialize the overlay. Add to the map if not present. Overrides the
 * <MyLocationOverlay> methods to change icon when the location provider is
 * unavailable or they are manually disabled.
 */
private void initMyLocationOverlay() {

    if (overlay == null) {
        // create the icon
        Drawable drawable = view.getResources().getDrawable(
                R.drawable.ic_maps_indicator_current_position_anim1);
        drawable = Marker.boundCenter(drawable);
        overlay = new MyLocationOverlay(view.getContext(), (MapView) view,
                drawable, getDefaultCircleFill(), getDefaultCircleStroke()) {


			/*
        	 * TODO: JAVADOC (non-Javadoc)
        	 * @see org.mapsforge.android.maps.overlay.MyLocationOverlay#onLocationChanged(android.location.Location)
        	 */
            @Override
            public void onLocationChanged(Location location) {
                try{
                    super.onLocationChanged(location);
                    lastLocation = location;
                    Log.v("LOCATION","Location changed, setting AVAILABLE");
                    locationProviderStatus = LocationProvider.AVAILABLE;
                    refreshIcon();
                }catch(IllegalStateException e){
                    //avoid exception and centering the map
                    Log.w("LOCATION","problem during map redraw");
                }

            }

        	/*
        	 * TODO: JAVADOC (non-Javadoc)
        	 * @see org.mapsforge.android.maps.overlay.MyLocationOverlay#onLocationChanged(android.location.Location)
        	 */
            @Override
            public void onProviderDisabled(String provider) {
                super.onProviderDisabled(provider);
                if (isEnabled()) {
                    Log.v("LOCATION", "provider disabled: " + provider);
                    enableMyLocation(false);
                    // TODO: better management of this short lived object
                    LocationManager locationManager = (LocationManager) view.getContext().getSystemService(Activity.LOCATION_SERVICE);
                    if (  !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                       && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    	locationProviderStatus = LocationProvider.OUT_OF_SERVICE;
                    } else{
                    	// TODO check confidence
                    	locationProviderStatus = LocationProvider.TEMPORARILY_UNAVAILABLE;
                    }

                    refreshStatus();
                }

            }

        	/*
        	 * TODO: JAVADOC (non-Javadoc)
        	 * @see org.mapsforge.android.maps.overlay.MyLocationOverlay#onLocationChanged(android.location.Location)
        	 */
            @Override
            public void onProviderEnabled(String provider) {
                super.onProviderEnabled(provider);
                if (isEnabled()) {
                    enableMyLocation(false);
                    enableMyLocation(true);
                    locationProviderStatus = LocationProvider.TEMPORARILY_UNAVAILABLE;
                    Log.v("LOCATION", "provider enabled: " + provider);
                    refreshStatus();
                }

            }

        	/*
        	 * TODO: JAVADOC (non-Javadoc)
        	 * @see org.mapsforge.android.maps.overlay.MyLocationOverlay#onLocationChanged(android.location.Location)
        	 */
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                super.onStatusChanged(provider, status, extras);
                if (isEnabled()) {
                    enableMyLocation(false);
                    Log.v("LOCATION","Status changed, setting "+status);
                    locationProviderStatus = status;
                    refreshStatus();
                }

            }
        };
        // If overlay is correctly created, add to the map
        addOverlay();
    }

}

/**
 * Add the overlay to the top of layers
 */
private void addOverlay() {
    view.getOverlayManger().addLocationOverlay(overlay);
};

/*
 * (non-Javadoc)
 * @see
 * it.geosolutions.android.map.control.MapControl#draw(android.graphics.Canvas)
 */
@Override
public void draw(Canvas canvas) {
    // TODO Auto-generated method stub

}

@Override
public void setActivationButton(ImageButton imageButton) {
    super.setActivationButton(imageButton);
    super.setMapListener(this);
    imageButton.setLongClickable(true);
    imageButton.setOnLongClickListener(this);
}

/*
 * (non-Javadoc)
 * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
 */
@Override
public boolean onLongClick(View view) {
    // if the control is not enable, enable it and set snapToLocation
	
	// TODO: higlight button as pressed
    if (isEnabled() && overlay.isMyLocationEnabled()) {
        toggleSnap();
        int currentStatus = overlay.isSnapToLocationEnabled() ? MESSAGE_SNAP_ACTIVATED
                : MESSAGE_SNAP_DEACTIVATED;
        sendMessageIfNeeded(currentStatus);
        activationButton.setSelected(true);
        refreshIcon();
        return true;
    }
    activationButton.setSelected(false);
    return false;
}

/**
 * Toggle the snap to location option
 */
private void toggleSnap() {
    setSnap(!overlay.isSnapToLocationEnabled());

}

/**
 * set snap to location and changes icon of the button
 */
private void setSnap(boolean snap) {
    if(overlay!=null){
        overlay.setSnapToLocationEnabled(snap);
        refreshIcon();
    }
}

/**
 * changes the icon checking the snap mode and the location service availability
 * returns an icon with the new status
 */
private int refreshIcon() {

    int currentStatus = previousStatus;
    if (!isEnabled()) {
    	// Location is turned off
        currentStatus = MESSAGE_DEACTIVATED;
		animation.stop();
        activationButton
                .setImageResource(R.drawable.ic_device_access_location_searching);
    } else if (overlay == null) {
    	// Location is turned on but no overlay is found
    	Log.w("LOCATION", "Location is turned on but no overlay is found");
    	currentStatus = 0;
		animation.stop();
        activationButton.setImageResource(R.drawable.ic_device_access_location_off);
    } else if (overlay.isMyLocationEnabled()) {
        // Check LocationProvider status
    	switch (locationProviderStatus) {
		case LocationProvider.OUT_OF_SERVICE:
			animation.stop();
			activationButton.setImageResource(R.drawable.ic_device_access_location_off);
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
            Log.v("LOCATION","This should be blinking now");
			activationButton.setImageDrawable(animation);
			animation.start();
			break;
		case LocationProvider.AVAILABLE:
			animation.stop();
			activationButton.setImageResource(R.drawable.ic_device_access_location_found);
	    	break;
		default:
			animation.stop();
			Log.w("LOCATION", "LocationProviderStatus is "+locationProviderStatus+" but value does not means anything");
			break;
		}
    	
      // my location unavailable
    } else {
        currentStatus = MESSAGE_UNAVAILABLE;
        Log.v("LOCATION","MESSAGE_UNAVAILABLE");
        activationButton.setImageResource(R.drawable.ic_device_access_location_off);

    }
    /*
     * Why a view method act as a control method?
    if(isEnabled()){
        activationButton.setSelected(true);
    }else{
        activationButton.setSelected(false);
    }
    */
	activationButton.setSelected(overlay.isSnapToLocationEnabled());
    return currentStatus;

}

/**
 * @return a default Paint for the location precision circle
 */
private static Paint getDefaultCircleFill() {
    return getPaint(Style.FILL, Color.BLUE, 30);
}

/**
 * @return a default Stroke for the location precision circle
 */
private static Paint getDefaultCircleStroke() {
    Paint paint = getPaint(Style.STROKE, Color.BLUE, 128);
    paint.setStrokeWidth(2);
    return paint;
}

/**
 * creates a <Paint> using a <Style>, a color and the trasperency
 * 
 * @param style the Paint style
 * @param color the color
 * @param alpha the trasparency index
 * @return
 */
private static Paint getPaint(Style style, int color, int alpha) {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setStyle(style);
    paint.setColor(color);
    paint.setAlpha(alpha);
    return paint;
}

/**
 * Check if Gps is enabled. If it is not, a dialog suggests to enable it. if the
 * GPS is available, simply activate the location and shows a <Toast> message
 * about the activation
 * @param b 
 */
private void checkLocationEnabled(boolean enable) {
    LocationManager locationManager = (LocationManager) view.getContext().getSystemService(Activity.LOCATION_SERVICE);

    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
    	!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
    	
        buildAlertMessageNoLocation(enable);

    } else {
        // if a location provider is enabled, activate the location overlay
        if(enable){
            overlay.enableMyLocation(centerAtFirst);
        }
        if (overlay.isMyLocationEnabled()) {
        	// default snap on click
        	setSnap(true);
            int currentStatus = overlay.isSnapToLocationEnabled() ? MESSAGE_SNAP_ACTIVATED : MESSAGE_ACTIVATED;
            sendMessageIfNeeded(currentStatus);
        }
    }

    return;
}

/**
 * Create a dialog to ask the user if he wants to activate a location provider. 
 * If the user agree, the control is disabled and the location source
 * setting window of the system is opened 
 * If no provider is available, the tool will be disabled
 * @param enable 
 */
private void buildAlertMessageNoLocation(final boolean enable) {
    if(restoring){
        overlay.enableMyLocation(enable);
        return;
    }
    final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
    builder.setMessage(MESSAGE_PROMPT_POSITION_ACCESS)
            .setCancelable(false)
            // the positive button event
            .setPositiveButton(android.R.string.yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog,
                                final int id) {
                            Context c = view.getContext();
                            
                            // TODO: use startActivityForResult
                            // Can't use startactivityforResult because this is not an activity
                            c.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                            overlay.enableMyLocation(true);
                            // activationButton.setSelected(false);

                            //refreshIcon();
                        }
                    })
            // the negative button event
            .setNegativeButton(android.R.string.no,
                    new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                        	
                            dialog.cancel();
                            if(enable){
                                overlay.enableMyLocation(true);
                            }
                            // TODO: This part is not needed
                            // TODO: better management of this short lived object
                            LocationManager locationManager = (LocationManager) view.getContext().getSystemService(Activity.LOCATION_SERVICE);
                            if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            	locationProviderStatus = LocationProvider.OUT_OF_SERVICE;
                            	sendMessageIfNeeded(MESSAGE_UNAVAILABLE);
                            	// TODO: disable button?
                            } else{
                            	locationProviderStatus = LocationProvider.TEMPORARILY_UNAVAILABLE;
                                if (!overlay.isMyLocationEnabled()) {
                                    sendMessageIfNeeded(MESSAGE_UNAVAILABLE);

                                } else {
                                    int currentStatus = overlay.isSnapToLocationEnabled() ? MESSAGE_SNAP_ACTIVATED : MESSAGE_NETWORK_LOCATION_AVAILABLE;
                                    sendMessageIfNeeded(currentStatus);
                                }
                            }
                            refreshIcon();

                        }
                    });
    final AlertDialog alert = builder.create();
    alert.show();
}

/**
 * refresh the icon and show a message if needed
 */
public void refreshStatus() {
    sendMessageIfNeeded(refreshIcon());

}

/**
 * show the message if the status changed and it is useful
 * 
 * @param changeIcon
 */
private void sendMessageIfNeeded(int status) {
    if(restoring) return;
    if (previousStatus == status) {
        return;
    }
    if (isEnabled()) {
        Toast.makeText(view.getContext(), status, Toast.LENGTH_LONG).show();

    } else if (status == MESSAGE_DEACTIVATED) {
        Toast.makeText(view.getContext(), MESSAGE_DEACTIVATED,
                Toast.LENGTH_LONG).show();
    }
    previousStatus = status;

}
// SAVE AND RESTORE STATE OF THE CONTROL
/**
 * Add a bundle with the control state by id
 * supposes to use the same id on restore
 */
@Override
public void saveState(Bundle savedInstanceState) {
    super.saveState(savedInstanceState);
    String id = controlId !=null ? controlId : DEFAULT_CONTROL_ID;
    Bundle b = new Bundle();
    b.putBoolean(ENABLED, isEnabled());
    if(overlay !=null){
        b.putBoolean(SNAP,overlay.isSnapToLocationEnabled());
    }
   
    savedInstanceState.putBundle(id, b);
    Log.d("LOCATION", "State saved");
}

@Override
public void restoreState(Bundle savedInstanceState) {
    super.restoreState(savedInstanceState);
    Log.d("LOCATION", "State restored");
    if(savedInstanceState==null){
        return;
    }
    String id = controlId !=null ? controlId : DEFAULT_CONTROL_ID;
    Bundle b = savedInstanceState.getBundle(id);
    if(b!=null){
        restoring = true;
        setEnabled(b.getBoolean(ENABLED,false));
        setSnap(b.getBoolean(SNAP,false));
        restoring = false;
    }

}


@Override
public void refreshControl(int requestCode, int resultCode, Intent data) {
    Log.d("LOCATION", "Resfreshing control");
    // I cannot use the result of the "enable gps" activity 
    // so I re-set the icon each time the control is refreshed
    if(isEnabled()){
        LocationManager locationManager = (LocationManager) view.getContext().getSystemService(Activity.LOCATION_SERVICE);
        if (  !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
           && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        	locationProviderStatus = LocationProvider.OUT_OF_SERVICE;
        } else{
        	// TODO check confidence
        	locationProviderStatus = LocationProvider.TEMPORARILY_UNAVAILABLE;
        }
        refreshStatus();
    }
}


@Override
public boolean onTouch(View v, MotionEvent event) {
	// Disable the button on map move
	if(event.getAction() == MotionEvent.ACTION_MOVE){
		if(overlay.isSnapToLocationEnabled()){
			// reset Toast message 
			previousStatus = 0;
			setSnap(false);
			refreshStatus();
		}
	}
	return false;
}

}
