package com.pshegger.gpslogger;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity implements LocationListener {
	private TextView txtLongitude, txtLatitude, txtAltitude, txtAccuracy;
	private LocationManager lm;
	private PrintStream output;
	
	public static final int UPDATE_INTERVAL = 10; //seconds
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		txtLongitude = (TextView) findViewById(R.id.longitude);
		txtLatitude = (TextView) findViewById(R.id.latitude);
		txtAltitude = (TextView) findViewById(R.id.altitude);
		txtAccuracy = (TextView) findViewById(R.id.accuracy);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		File baseDir = new File(Environment.getExternalStorageDirectory().getPath()+"/gpslogs/");
		if (!baseDir.exists())
			baseDir.mkdir();
		
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
	}
	
	private String getFilename() {
		StringBuilder filename = new StringBuilder();
		Date date = Calendar.getInstance().getTime();
		
		filename.append(Environment.getExternalStorageDirectory().getPath());
		filename.append("/gpslogs/log-");
		filename.append(date.getYear()+1900);
		filename.append(String.format("%02d", date.getMonth()));
		filename.append(String.format("%02d", date.getDay()));
		filename.append("-");
		filename.append(String.format("%02d", date.getHours()));
		filename.append(String.format("%02d", date.getMinutes()));
		filename.append(".txt");
		
		return filename.toString();
	}
	
	private String getLocationRow(Location location) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(System.currentTimeMillis());
		builder.append(";");
		builder.append(location.getLatitude());
		builder.append(";");
		builder.append(location.getLongitude());
		builder.append(";");
		builder.append(location.getAltitude());
		builder.append(";");
		builder.append(location.getAccuracy());
		
		return builder.toString();
	}

	@Override
	protected void onPause() {
		if (output != null)
			output.close();
		lm.removeUpdates(this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		try {
			output = new PrintStream(new File(getFilename()));
		} catch (IOException e) {
			output = null;
			Log.e("GPSLogger", e.toString());
		}
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL*1000, 10f, this);
		super.onResume();
	}

	@Override
	public void onLocationChanged(Location location) {
		txtLongitude.setText(Double.toString(location.getLongitude()));
		txtLatitude.setText(Double.toString(location.getLatitude()));
		txtAltitude.setText(Double.toString(location.getAltitude()));
		txtAccuracy.setText(Double.toString(location.getAccuracy()));
		
		if (output != null)
			output.println(getLocationRow(location));
	}

	@Override
	public void onProviderDisabled(String provider) {
		Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(intent);
	}

	@Override
	public void onProviderEnabled(String provider) {}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

}
