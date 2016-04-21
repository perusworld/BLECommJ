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

import android.bluetooth.BluetoothDevice;

import java.util.Comparator;

/**
 * @author Saravana Perumal Shanmugam
 *
 */
public class BLEObject implements Comparator<BLEObject> {


    protected BluetoothDevice device;
    protected String address;
    protected String name;
    protected String manufacturerName;
    protected String modelNumber;
    protected String serialNumber;
    protected String hardwareRevision;
    protected String firmwareRevision;
    protected String softwareRevision;
    protected int rssi = Integer.MIN_VALUE;
    protected int attempsToReadDeviceInfo = 0;

    public BLEObject() {

    }

    public BLEObject(BluetoothDevice device, String address, String name, int rssi) {
        this(device, address, name);
        this.rssi = rssi;
    }

    public BLEObject(BluetoothDevice device, String address, String name) {
        this.device = device;
        this.address = address;
        this.name = name;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getHardwareRevision() {
        return hardwareRevision;
    }

    public String getFirmwareRevision() {
        return firmwareRevision;
    }

    public String getSoftwareRevision() {
        return softwareRevision;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setHardwareRevision(String hardwareRevision) {
        this.hardwareRevision = hardwareRevision;
    }

    public void setFirmwareRevision(String firmwareRevision) {
        this.firmwareRevision = firmwareRevision;
    }

    public void setSoftwareRevision(String softwareRevision) {
        this.softwareRevision = softwareRevision;
    }

    public boolean hasDeviceInfo() {
        return null != manufacturerName && null != modelNumber && null != serialNumber && null != hardwareRevision && null != firmwareRevision && null != softwareRevision;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getAttempsToReadDeviceInfo() {
        return attempsToReadDeviceInfo;
    }

    public void setAttempsToReadDeviceInfo(int attempsToReadDeviceInfo) {
        this.attempsToReadDeviceInfo = attempsToReadDeviceInfo;
    }

    public int compare(BLEObject o1, BLEObject o2) {
        int a = Math.abs(o1.getRssi());
        int b = Math.abs(o2.getRssi());
        return a > b ? +1 : a < b ? -1 : 0;
    }

    public int compareTo(BLEObject value) {
        return compare(this, value);
    }
}
