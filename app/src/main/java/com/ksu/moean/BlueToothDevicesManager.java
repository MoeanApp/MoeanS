package com.ksu.moean;

import android.content.IntentSender.SendIntentException;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.BleDevice;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.BleScanCallback;
import com.google.android.gms.fitness.request.StartBleScanRequest;

public class BlueToothDevicesManager {

    private static final String TAG = "BlueToothDevicesManager";
    private static final int REQUEST_BLUETOOTH = 1001;
    private ActivityMonitorActivity mMonitor;
    private GoogleApiClient mClient;

    public BlueToothDevicesManager(ActivityMonitorActivity monitor, GoogleApiClient client) {
        mMonitor = monitor;
        mClient = client;
    }

    public void startBleScan() {

        BleScanCallback callback = new BleScanCallback() {
            @Override
            public void onDeviceFound(BleDevice device) {
                Log.i(TAG, "BLE Device Found: " + device.getName());
                claimDevice(device);
            }
            @Override
            public void onScanStopped() {
                Log.i(TAG, "BLE scan stopped");
            }
        };

        StartBleScanRequest request = new StartBleScanRequest.Builder()
                .setDataTypes(DataType.TYPE_HEART_RATE_BPM, DataType.TYPE_STEP_COUNT_DELTA)
                .setBleScanCallback(callback)
                .build();

        PendingResult<Status> result = Fitness.BleApi.startBleScan(mClient, request);
        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (!status.isSuccess()) {
                    switch (status.getStatusCode()) {
                        case FitnessStatusCodes.DISABLED_BLUETOOTH:
                            try {
                                status.startResolutionForResult(mMonitor, REQUEST_BLUETOOTH);
                            } catch (SendIntentException e) {
                                Log.i(TAG, "SendIntentException: " + e.getMessage());
                            }
                            break;
                    }
                    Log.i(TAG, "BLE scan unsuccessful");
                } else {
                    Log.i(TAG, "ble scan status message: " + status.describeContents());
                    Log.i(TAG, "BLE scan successful: " + status.toString());
                }
            }
        });
    }

    public void claimDevice(BleDevice device) {
        //Stop ble scan
        Fitness.BleApi.stopBleScan(mClient, new BleScanCallback() {
            @Override
            public void onDeviceFound(BleDevice arg0) {
                Log.i(TAG, "onDeviceFound, stopBleScan");
            }

            @Override
            public void onScanStopped() {
                Log.i(TAG, "onScanStopped, stopBleScan");
            }

        });
        //Claim device
        PendingResult<Status> pendingResult = Fitness.BleApi.claimBleDevice(mClient, device);
        pendingResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status st) {
                if (st.isSuccess()) {
                    Log.i(TAG, "Claimed device successfully");
                } else {
                    Log.e(TAG, "Did not successfully claim device");
                }
            }
        });
    }

}