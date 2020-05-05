package com.example.gpstestwithservice;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import com.example.gpstestwithservice.Service.LocatorService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(MainActivity.this, LocatorService.class));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        check_gps_status();

        getCurrentCoordinates();
    }


    //    GPS on karalada kiyala balanna
    public void check_gps_status() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

// check if enabled and if not send user to the GSP settings
        if (!enabled) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("To use this service you have to enable GPS. Would you like to enable GPS?");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    });

            builder1.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        } else {
            Toast.makeText(this, "GPS Provider is Enabled", Toast.LENGTH_SHORT).show();
        }
    }

    public void getCurrentCoordinates() {
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String latitude = intent.getStringExtra(LocatorService.EXTRA_LATITUDE);
                double lat = Double.parseDouble(latitude);
                String longitude = intent.getStringExtra(LocatorService.EXTRA_LONGITUDE);
                double lng = Double.parseDouble(longitude);

                String time = intent.getStringExtra(LocatorService.EXTRA_TIME);
                String date = intent.getStringExtra(LocatorService.EXTRA_DATE);

                Toast.makeText(MainActivity.this, longitude + " / " + latitude + "/" + time + "/" + date, Toast.LENGTH_LONG).show();



            }
        }, new IntentFilter(LocatorService.ACTION_LOCATION_BROADCAST));
    }




}
