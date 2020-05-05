package com.example.gpstestwithservice.Service;


import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class LocatorService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    GoogleApiClient mLocationClient;
    public static final int LOCATION_INTERVAL = 10000;
    public static final int FASTEST_LOCATION_INTERVAL = 5000;
    LocationRequest mLocationRequest = new LocationRequest();

    public static final String ACTION_LOCATION_BROADCAST = LocatorService.class.getName() + "LocationBroadcast";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_TIME = "extra_time";
    public static final String EXTRA_DATE = "extra_date";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;


    private static final String TAG = LocatorService.class.getSimpleName();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest.setInterval(LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_LOCATION_INTERVAL);
        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        mLocationRequest.setPriority(priority);
        mLocationClient.connect();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "== Error On onConnected() Permission not granted");
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);

        Log.d(TAG, "Connected to Google API");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect to Google API");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed");


        if (location != null) {
            Log.d(TAG, "== location != null");

            long epochSeconds = location.getTime();

            String latitude = String.valueOf(location.getLatitude());
            String longitude = String.valueOf(location.getLongitude());
            String time = epoch_to_time_string(epochSeconds);
            String date = epoch_to_date_string(epochSeconds);
            //Send result to activities
            sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), time, date);


            String printing_text = latitude + "," + longitude + "," + time + "," + date;
            String directory_name = "GPS-Locator-Zee";
            String file_name_with_file_extension = "my_locations.csv";
            String appending_data = printing_text;
            make_new_file_and_append_data_to_it(directory_name, file_name_with_file_extension, appending_data);
        }

    }

    private void sendMessageToUI(String lat, String lng, String time, String date) {

        Log.d(TAG, "Sending info...");
        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LATITUDE, lat);
        intent.putExtra(EXTRA_LONGITUDE, lng);
        intent.putExtra(EXTRA_DATE, date);
        intent.putExtra(EXTRA_TIME, time);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationClient.disconnect();
    }

    public static String epoch_to_time_string(long epochSeconds) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(epochSeconds);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);
        return (hour + ":" + min + ":" + sec);
    }

    public static String epoch_to_date_string(long epochSeconds) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(epochSeconds);
//        int month=calendar.da
        Date date = new Date(epochSeconds);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String myDate = format.format(date);
        System.out.println(myDate);
        return (myDate);
    }


//    File Ekata Write karana tika

    /*file handling tika*/
    public void make_new_file_and_append_data_to_it(String directory_name, String file_name_with_file_extension, String appending_data) {
        if (isExternalStorageWritable() && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            /* make directory */
            File root_path = Environment.getExternalStorageDirectory();
            File directory = null;
            directory = new File(root_path + "/" + directory_name + "/");
            directory.mkdirs();

            File my_file = new File(root_path + "/" + directory_name + "/" + file_name_with_file_extension);
            if (my_file.exists()) {
                zee("have file");
                String data = appending_data + "\n";
                append_data_to_file(my_file, data);
            } else {
                String data = appending_data + "\n";
                File new_file = make_new_file(root_path + "/" + directory_name + "/", file_name_with_file_extension);
                append_data_to_file(new_file, data);
            }

        } else {
            zee("no permission");
//            request_permission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

    }


    /* append data to available file */
    private void append_data_to_file(File file, String data) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
            writer.append(data);
            writer.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* make new file */
    private File make_new_file(String path, String file_name) {
        File new_file = new File(path, file_name);
        try {
            new_file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new_file;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /*for check permissions*/

    public boolean checkPermission(String permission) {
        int check = ContextCompat.checkSelfPermission(this, permission);
        return check == PackageManager.PERMISSION_GRANTED;
    }


    /*request permission*/

    public void request_permission(Activity activity, String permission, int my_permission_request_code) {
        ActivityCompat.requestPermissions(activity,
                new String[]{permission},
                my_permission_request_code);
    }

    /*for System.out.println()*/

    public void zee(String text) {
        System.out.println(text);
    }

}
