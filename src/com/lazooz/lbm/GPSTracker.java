package com.lazooz.lbm;

import java.util.Date;

import com.google.android.gms.maps.model.LatLng;
import com.lazooz.lbm.utils.BBUncaughtExceptionHandler;
import com.lazooz.lbm.utils.Utils;




import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class GPSTracker extends Service implements LocationListener {

	private static GPSTracker instance = null;

	private final Context mContext;

	// flag for GPS status
	private boolean isGPSEnabled = false;

	// flag for network status
	private boolean isNetworkEnabled = false;

	// flag for GPS status
	private boolean canGetLocation = false;
	
	
	protected LocationListener mOnLocationListener;
 	
 	
	public LocationListener getOnLocationListener() {
		return mOnLocationListener;
	}

	public void setOnLocationListener(LocationListener l) {
		this.mOnLocationListener = l;
	}
	
	

	Location location; // location
	double latitude; // latitude
	double longitude; // longitude

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

	// Declaring a Location Manager
	protected LocationManager locationManager;

	private float accuracy;

	private long theTime;
	
	public static GPSTracker getInstance(Context context) {
		
		Thread.setDefaultUncaughtExceptionHandler( new BBUncaughtExceptionHandler(context));
		
	      if(instance == null) {
	         instance  = new GPSTracker(context);
	      }

	      return instance;
	   }
	
	public static void removeInstance() {
	      instance = null;
	   }
	

	public GPSTracker(Context context) {
		this.mContext = context;
		getLocation();
	}

	
	public LatLng getLocationLL(){
		Location l = getLocation();
		if (l == null)
			return null;
		return new LatLng(l.getLatitude(), l.getLongitude());
	}
	
	public Location getLocation() {
		try {
			locationManager = (LocationManager) mContext
					.getSystemService(LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
			} else {
				this.canGetLocation = true;
				if (isNetworkEnabled) {
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("Network", "Network");
					if (locationManager != null) {
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d("GPS Enabled", "GPS Enabled");
						if (locationManager != null) {
							location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}
	
	/**
	 * Stop using GPS listener
	 * Calling this function will stop using GPS in your app
	 * */
	public void stopUsingGPS(){
		if(locationManager != null){
			locationManager.removeUpdates(GPSTracker.this);
		}		
	}
	
	/**
	 * Function to get latitude
	 * */
	public double getLatitude(){
		if(location != null){
			latitude = location.getLatitude();
		}
		
		// return latitude
		return latitude;
	}
	
	
	
	
	/**
	 * Function to get longitude
	 * */
	public double getLongitude(){
		if(location != null){
			longitude = location.getLongitude();
		}
		
		// return longitude
		return longitude;
	}
	
	
	public float getAccuracy(){
		if(location != null){
			accuracy = location.getAccuracy();
		}
		
		// return latitude
		return accuracy;
	}
	
	public String getTime(){
		String timeString = "";
		if(location != null){
			theTime = location.getTime();
			Date date = new Date(theTime);
			timeString = Utils.getTimeInGMT(date);
		}
		
		// return latitude
		return timeString;
	}
	

	
	
	/**
	 * Function to show settings alert dialog
	 * On pressing Settings button will lauch Settings Options
	 * */
	public void showSettingsAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
   	 
        // Setting Dialog Title
        alertDialog.setTitle(mContext.getString(R.string.gps_activate_gps_title));
 
        // Setting Dialog Message
        alertDialog.setMessage(mContext.getString(R.string.gps_activate_gps_body));
 
        // On pressing Settings button
        alertDialog.setPositiveButton(mContext.getString(R.string.gps_activate_gps_setting), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            	mContext.startActivity(intent);
            }
        });
 
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
 
        // Showing Alert Message
        alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location location) {
		if (mOnLocationListener != null)
			mOnLocationListener.onLocationChanged(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		if (mOnLocationListener != null)
			mOnLocationListener.onProviderDisabled(provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		if (mOnLocationListener != null)
			mOnLocationListener.onProviderEnabled(provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (mOnLocationListener != null)
			mOnLocationListener.onStatusChanged(provider, status, extras);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public boolean isGPSEnabled() {
		isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return isGPSEnabled;
	}

	public boolean isNetworkEnabled() {
		return isNetworkEnabled;
	}

	public boolean canGetLocation() {
		return canGetLocation;
	}

}
