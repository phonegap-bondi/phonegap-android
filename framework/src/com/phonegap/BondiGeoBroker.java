package com.phonegap;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.webkit.WebView;

/*
 * This class is the interface to the Geolocation.  It's bound to the geo object.
 * 
 * This class only starts and stops various GeoListeners, which consist of a GPS and a Network Listener
 */

public class BondiGeoBroker {
    private WebView mAppView;
	private Context mCtx;
	private HashMap<String, BondiGeoListener> geoListeners = new HashMap<String, BondiGeoListener>();
	private Location cLoc;
	private LocationManager mLocMan;
	
	public BondiGeoBroker(WebView view, Context ctx)
	{
		mCtx = ctx;
		mAppView = view;
		mLocMan = (LocationManager) mCtx.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public void getCurrentLocation(final String id)
	{		
		try {
			List<String> providers = mLocMan.getAllProviders();

			boolean noProvider = true;
			boolean allProvidersDisabled = true;
			Location loc = null;
			for (String provName : providers){
				if (loc == null && (mLocMan.getProvider(provName) != null)){
					loc = mLocMan.getLastKnownLocation(provName);
					noProvider = false;
				} 
				if (mLocMan.isProviderEnabled(provName)){
					allProvidersDisabled = false;
				}
			}

			if (noProvider || allProvidersDisabled){
				mAppView.loadUrl("javascript:bondi.geolocation.fail(" + id + ",'Currently is no Location-Provider avaiable - check devicesettings')");
				return;
			}


			String params; 
			if (loc != null){
				/*
				 * Build the giant string to send back to Javascript!
				 */

				/*
				 * altitudeAccuracy: as this value isn't supported seperatedly by the Android.location.Location class
				 * so the general accuracy is used for it
				 */
				float altitudeAccuracy = loc.getAccuracy();

				params = loc.getLatitude() + "," + loc.getLongitude() + ", " + loc.getAltitude() + "," + loc.getAccuracy() + "," + altitudeAccuracy +  "," + loc.getBearing();
				params += "," + loc.getSpeed() + "," + loc.getTime();

				mAppView.loadUrl("javascript:bondi.geolocation.success(" + id + "," +  params + ")");
			} else {
				mAppView.loadUrl("javascript:bondi.geolocation.fail(" + id + ",'noLoc')");
			}

		} catch (Exception e) {
			e.printStackTrace();
			// call fail if an error was detected.
			mAppView.loadUrl("javascript:bondi.geolocation.fail(" + id + "," + e.getMessage() + ")");
		}
	}
	
	/**
	 * getLastKnownLocation.
	 * @return the last know location
	 */
	public Location getLastKnownLocation()
	{
		cLoc = mLocMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (cLoc == null){
			cLoc = mLocMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		
		return cLoc;
	}
	
	/**
	 * Start.
	 * @param freq the frequency
	 * @param key the key
	 * @return the key
	 */
	public String start(final int freq, final String key)
	{
		BondiGeoListener listener = new BondiGeoListener(key, mCtx, freq, mAppView);
		geoListeners.put(key, listener);
		return key;
	}
	
	
	
	/**
	 * Stop.
	 * @param key the key
	 */
	public void stop(String key)
	{
		BondiGeoListener geo = geoListeners.get(key);
		geo.stop();
		geo = null;
	}
	
}
