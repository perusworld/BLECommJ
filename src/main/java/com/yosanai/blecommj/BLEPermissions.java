/**
 * This is the MIT License
 * http://www.opensource.org/licenses/mit-license.php
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.yosanai.blecommj;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Saravana Perumal Shanmugam
 */
public class BLEPermissions {

    public static final int REQUEST_ENABLE_BLUETOOTH = 234;
    public static final int REQUEST_BLUETOOTH_PERMS = 235;

    public static final String[] PERMS = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    protected Collection<String> permsToCheck;

    public Status ensurePermissions(Activity activity) {
        Status ret = Status.FALSE;
        if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            if (null != bluetoothManager) {
                BluetoothAdapter adapter = bluetoothManager.getAdapter();
                if (null == adapter || !adapter.isEnabled()) {
                    ret = Status.PROCESSING;
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    activity.startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
                } else {
                    ret = requestPermissions(activity);
                }
            }
        }
        return ret;
    }

    public Status checkResultActivity(Activity activity, int requestCode, int resultCode) {
        Status ret = Status.UNKNOWN;
        if (BLEPermissions.REQUEST_ENABLE_BLUETOOTH == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                ret = requestPermissions(activity);
            } else {
                ret = Status.FALSE;
            }
        }
        return ret;
    }

    public Status checkRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Status ret = Status.UNKNOWN;
        if (null == permsToCheck || permsToCheck.isEmpty()) {
            // NOOP
        } else if (BLEPermissions.REQUEST_BLUETOOTH_PERMS == requestCode) {
            Set<String> all = new HashSet<>(permsToCheck);
            for (int index = 0; index < permissions.length; index++) {
                if (all.contains(permissions[index]) && grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                    all.remove(permissions[index]);
                }
            }
            ret = all.isEmpty() ? Status.TRUE : Status.FALSE;
        }
        return ret;
    }

    private Status requestPermissions(Activity activity) {
        Status ret = Status.FALSE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permsToCheck = getPermissionsToCheck(activity, PERMS);
            if (null == permsToCheck || permsToCheck.isEmpty()) {
                ret = Status.TRUE;
            } else {
                ret = Status.PROCESSING;
                activity.requestPermissions(permsToCheck.toArray(new String[]{}), REQUEST_BLUETOOTH_PERMS);
            }
        } else {
            ret = Status.TRUE;
        }
        return ret;
    }

    public Collection<String> getPermissionsToCheck(Activity activity, String... perms) {
        Collection<String> ret = new HashSet<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String perm : perms) {
                if (PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(perm)) {
                    //NOOP
                } else {
                    ret.add(perm);
                }
            }
        }
        return ret;
    }

    public enum Status {
        TRUE, FALSE, PROCESSING, UNKNOWN
    }

}
