package com.example.bletest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

////////////////////////////////////////////////Properties//////////////////////////////////////////////////////

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    ScanSettings scanSettings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build();
    private boolean isScanning = false;
    ArrayList<ScanResultModel> results = new ArrayList<>();
    RecyclerView recyclerView;
    ScanResultAdapter adapter;
    LinearLayoutManager layoutManager;

    ActivityResultLauncher<Intent> promptBluetooth = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result != null) {
                if (result.getResultCode() == RESULT_OK) {
                    Toast.makeText(MainActivity.this, "bluetooth enabled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "permission declined", Toast.LENGTH_SHORT).show();
                    promptEnableBluetooth();
                }
            }
        }
    });

//////////////////////////////////////////////OnCreate/////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "This Device doesn't support Bluetooth",
                    Toast.LENGTH_LONG).show();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "bluetooth off",
                    Toast.LENGTH_LONG).show();
            promptEnableBluetooth();
        } else {
            Toast.makeText(this, "bluetooth on",
                    Toast.LENGTH_LONG).show();
        }

        Button scanButton = findViewById(R.id.scan_button);
        scanButton.setText("Start Scan");
//        recyclerView = findViewById(R.id.recyclerView);
//        layoutManager = new LinearLayoutManager(this);
//        layoutManager.setOrientation(RecyclerView.VERTICAL);
//        recyclerView.setLayoutManager(layoutManager);


        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isScanning) {
                    stopBtScan();
                    scanButton.setText("Start Scan");
                } else {
                    startBtScan();
                    scanButton.setText("Stop Scan");

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        results = new ArrayList<>();
        adapter = new ScanResultAdapter(results);
        recyclerView.setAdapter(adapter);
    }

    //////////////////////////////////////////////functions//////////////////////////////////////////////////////

    public void promptEnableBluetooth() {
        Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        promptBluetooth.launch(bluetoothIntent);
    }

    public boolean isPermissionGranted(String permissionType) {
        return (ContextCompat.checkSelfPermission(this, permissionType) == PackageManager.PERMISSION_GRANTED);
    }

    public void startBtScan() {
        boolean isLocationPermissionGranted = isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission();
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetoothLeScanner.startScan(null, scanSettings, scanCallback);
            isScanning = true;
        }
    }

    private void stopBtScan() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        results.clear();
        bluetoothLeScanner.stopScan(scanCallback);
        isScanning = false;
    }

    public void requestLocationPermission() {
        boolean isLocationPermissionGranted = isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION);
        if (isLocationPermissionGranted) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Location permission required")
                        .setMessage("starting from Android M (6.0), the system requires apps to be granted " +
                                "location access in order to scan for BLE devices.")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_REQUEST_CODE);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialogBuilder.show();
            }
        });
    }

    public void requestPermission(String permissionType, Integer requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permissionType}, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    startBtScan();
                }
            }
        }
    }


    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult scanResult) {
            super.onScanResult(callbackType, scanResult);
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

//            ScanResultModel result = new ScanResultModel(scanResult.getDevice().getName(), scanResult.getDevice().getAddress(), scanResult.getRssi());
//
//            for(int i=0; i <results.size(); i++) {
//                if(results.get(i).getMacAddress() == result.getMacAddress()){
//                    results.set(i, result);
//                    adapter.notifyItemChanged(i);
//                }else {
//                    Log.i("ScanCallback", "Found BLE device! Name: "+result.getDeviceName()+" address: "+result.getMacAddress());
//                    results.add(result);
//                    adapter.notifyItemInserted(results.size()-1);
//                }
//                Log.i("ScanCallback", "Found BLE device! Name: "+result.getDeviceName()+" address: "+result.getMacAddress());
//            }
//
            results.add(new ScanResultModel(scanResult.getDevice().getName(), scanResult.getDevice().getAddress(), scanResult.getRssi()));
            adapter.notifyDataSetChanged();

            Log.i("ScanCallback", "Found BLE device! Name: "+results.get(0).getDeviceName()+" address: "+results.get(0).getMacAddress());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("ScanCallback", "Scan failed error code: "+errorCode);
        }
    };
}

