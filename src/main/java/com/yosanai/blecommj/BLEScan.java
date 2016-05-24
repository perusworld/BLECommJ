/**
 *
 * This is the MIT License
 * http://www.opensource.org/licenses/mit-license.php
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package com.yosanai.blecommj;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Saravana Perumal Shanmugam
 *
 */
public class BLEScan {

    public static final String TAG = BLEScan.class.getName();

    public static final UUID DEVICE_INFO = BLEScan.getUUID("180A");
    public static final UUID MANU_NAME = BLEScan.getUUID("2A29");
    public static final UUID MODEL_NUM = BLEScan.getUUID("2A24");
    public static final UUID SERIAL_NUM = BLEScan.getUUID("2A25");
    public static final UUID HW_REV = BLEScan.getUUID("2A27");
    public static final UUID FW_REV = BLEScan.getUUID("2A26");
    public static final UUID SW_REV = BLEScan.getUUID("2A28");

    public static final int MAX_ATTEMPTS_READ_DEVICE_INFO = 3;
    public static final int CONNECTION_TIMEOUT = 5000;

    public static final UUID[] SCANABLES = new UUID[]{
            MANU_NAME, MODEL_NUM, SERIAL_NUM, HW_REV, FW_REV, SW_REV
    };

    Handler handler;
    BluetoothAdapter bluetoothAdapter;
    Map<String, BLEObject> scanned = new HashMap<>();
    UUID service;
    BLEScanCallback callback;
    Activity activity;
    String mRequestedSerial, mRequestedModel;
    boolean cancelled = false;
    private BluetoothLeScanner mLEScanner;
    private Object scanCallback21;

    public static UUID getUUID(String uuidStr) {
        UUID ret = null;
        if (null != uuidStr && 4 == uuidStr.trim().length()) {
            if (4 == uuidStr.trim().length()) {
                ret = UUID.fromString("0000" + uuidStr.trim() + "-0000-1000-8000-00805F9B34FB");
            } else {
                ret = UUID.fromString(uuidStr);
            }
        }
        return ret;
    }

    public boolean init(Activity activity, UUID service) {
        boolean ret = false;
        this.activity = activity;
        handler = new Handler();
        scanned.clear();
        this.service = service;
        if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (null != bluetoothAdapter) {
                ret = true;
            }
        }
        return ret;
    }


    public void disableBluetoothAdapter() {
        if(bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
            }
        }
    }

    public void enableBluetoothAdapter() {
        if(bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }
        }
    }

    public void scan(long timeout, BLEScanCallback callback) {
        scan(timeout, false, callback);
    }

    private boolean requestedDeviceFound() {
        if(mRequestedSerial != null && mRequestedModel != null) {
            for (BLEObject bleObj : scanned.values()) {
                if (null != bleObj.getSerialNumber() && null != bleObj.getModelNumber()) {
                    if(bleObj.getSerialNumber().equalsIgnoreCase(mRequestedSerial) &&
                            bleObj.getModelNumber().equalsIgnoreCase(mRequestedModel)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void updateDeviceInfo() {
        if(cancelled) return;

        // Running this on the main thread to prevent problems on some Samsung devices and older OSes
      //  Handler handler = new Handler(Looper.getMainLooper());
      //  handler.post(new Runnable() {
//            @Override
//            public void run() {
                // If we've found the device with the requested characteristics then we can finish
                // There is no need to connect to all other devices and read these values
                if(requestedDeviceFound() && callback != null) {
                    Log.d(TAG, "Requested device found, no need to get device info for all devices");
                    callback.onDone(scanned.values());
                    return;
                }

                boolean done = true;

                // Sort the list by proximity - connecting to the nearest first
                BLEDeviceInfoService infoService = new BLEDeviceInfoService(new BLEDeviceInfoServiceCallback() {
                    @Override
                    public void onDone() {
                        updateDeviceInfo();
                    }
                }, activity, CONNECTION_TIMEOUT);

                List<Map.Entry<String, BLEObject>> scannedList =
                        new LinkedList<Map.Entry<String, BLEObject>>(scanned.entrySet());
                Collections.sort(scannedList, new Comparator<Map.Entry<String, BLEObject>>() {
                    public int compare(Map.Entry<String, BLEObject> o1,
                                       Map.Entry<String, BLEObject> o2) {
                        return (o1.getValue()).compareTo(o2.getValue());
                    }
                });

                for (Map.Entry<String, BLEObject> res : scannedList) {
                    BLEObject bleObj = res.getValue();
                    if (null == bleObj.getSerialNumber() && bleObj.getAttempsToReadDeviceInfo() < MAX_ATTEMPTS_READ_DEVICE_INFO) {
                        Log.d(TAG, "readDeviceInfo");
                        infoService.readDeviceInfo(bleObj);
                        done = false;
                        break;
                    } else {
                        //NOOP
                    }
                }
                if (done && callback != null) {
                    callback.onDone(scanned.values());
                }

         //   }
//        });

    }


    /**
     * Cancel any ongoing scan
     */
    public void disconnect() {
        cancelled = true;
        Log.d(TAG, "Disconnect: cancelled:" + cancelled);
        if(bluetoothAdapter != null) {
            stopScan();
        }
        this.callback = null;
    }

    private void stopScan() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                bluetoothAdapter.stopLeScan(scanCallback);
            } else {
                if(mLEScanner != null) {
                    mLEScanner.stopScan((ScanCallback)scanCallback21);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "EX:", e);
            //NOOP - prevent NPE https://github.com/AltBeacon/android-beacon-library/issues/219
        }
    }



    /**
     * Allow a user to search for devices with particular characteristics
     *
     * @param timeout
     * @param updateInfo
     * @param callbck
     * @param requestedModel
     * @param requestedSerial
     */
    public void scan(long timeout, final boolean updateInfo, BLEScanCallback callbck, String requestedModel, String requestedSerial, boolean resetScannedList) {
        this.mRequestedModel = requestedModel;
        this.mRequestedSerial = requestedSerial;
        this.callback = callbck;
        if(resetScannedList) {
            scanned.clear();
        } else {
            for (Map.Entry<String, BLEObject> res : scanned.entrySet()) {
                BLEObject bleObj = res.getValue();
                if (null == bleObj.getSerialNumber()) {
                    bleObj.setAttempsToReadDeviceInfo(0);   //Reset
                }
            }
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                stopScan();

                if (updateInfo) {
                    Log.d(TAG, "Got advertisements from " + scanned.size() + " devices");
                    updateDeviceInfo();
                } else if (null != callback) {
                    callback.onDone(scanned.values());
                }
            }
        }, timeout);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Android 4.3 and 4.4 have bugs filtering based on UUID
            bluetoothAdapter.startLeScan(scanCallback);

        } else {
            if(scanCallback21 == null) {
                scanCallback21 = new ScanCallback() {
                    @Override
                    @SuppressLint("NewApi")
                    public void onScanResult(int callbackType, ScanResult result) {
                        try {
                            List<ParcelUuid> serviceIds = result.getScanRecord().getServiceUuids();
                            if (serviceIds != null && serviceIds.size() > 0) {
                                handleScanResult(result.getDevice(), result.getRssi());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing scan result:", e);
                        }
                    }

                    @Override
                    @SuppressLint("NewApi")
                    public void onBatchScanResults(List<ScanResult> results) {
                        for (ScanResult sr : results) {
                            Log.i("ScanResult - Results", sr.toString());
                        }
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        if(errorCode == SCAN_FAILED_ALREADY_STARTED) {
                            Log.d(TAG, "SCAN ALREADY STARTED");
                            return;
                        }
                        Log.e(TAG, "SCAN FAILED: Error Code: " + errorCode);
                    }
                };
            }
            mLEScanner = bluetoothAdapter.getBluetoothLeScanner();
            if(mLEScanner != null) {
                ScanSettings settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                ArrayList<ScanFilter> filters = new ArrayList<>();

                ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(service)).build();
                filters.add(filter);
                mLEScanner.startScan(filters, settings, (ScanCallback)scanCallback21);
            }
        }


    }

    public void scan(long timeout, final boolean updateInfo, BLEScanCallback callbck) {
        this.scan(timeout, updateInfo, callbck, null, null, true);
    }

    private void handleScanResult(BluetoothDevice device, int rssi) {
        if (scanned.containsKey(device.getAddress())) {
            BLEObject previousScan = scanned.get(device.getAddress());
            previousScan.device = device;
            if(Math.abs(previousScan.getRssi())> Math.abs(rssi)) {
                previousScan.setRssi(rssi);   //update rssi if new scan is closer
            }
        } else {
            scanned.put(device.getAddress(), new BLEObject(device, device.getAddress(), device.getName(), rssi));
        }
    }

    private BluetoothAdapter.LeScanCallback scanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    List<UUID> list = parseUuids(scanRecord);
                    if (list.contains(service)) {
                        handleScanResult(device, rssi);
                    }
                }
            };

    // Filtering by custom UUID is broken in Android 4.3 and 4.4, see:
    // http://stackoverflow.com/questions/18019161/startlescan-with-128-bit-uuids-doesnt-work-on-native-android-ble-implementation?noredirect=1#comment27879874_18019161
    // This is a workaround function from the SO thread to manually parse advertisement data.
    private List<UUID> parseUuids(byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<UUID>();

        ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            byte length = buffer.get();
            if (length == 0) break;

            byte type = buffer.get();
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (length >= 2) {
                        uuids.add(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
                        length -= 2;
                    }
                    break;

                case 0x06: // Partial list of 128-bit UUIDs
                case 0x07: // Complete list of 128-bit UUIDs
                    while (length >= 16) {
                        long lsb = buffer.getLong();
                        long msb = buffer.getLong();
                        uuids.add(new UUID(msb, lsb));
                        length -= 16;
                    }
                    break;

                default:
                    buffer.position(buffer.position() + length - 1);
                    break;
            }
        }

        return uuids;
    }




}
