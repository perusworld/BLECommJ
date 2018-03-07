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

/**
 * Created by sarav on 4/18/2016.
 */
public class BLECommConfig {

    protected BLECommCallback callback;
    protected int packetSize;
    protected String sUUID;
    protected String rxUUID;
    protected String txUUID;
    protected String fUUID;
    protected long notificationDelay;

    public BLECommConfig(BLECommCallback callback, int packetSize, String sUUID, String rxUUID, String txUUID, String fUUID, long notificationDelay) {
        this.callback = callback;
        this.packetSize = packetSize;
        this.sUUID = sUUID;
        this.rxUUID = rxUUID;
        this.txUUID = txUUID;
        this.fUUID = fUUID;
        this.notificationDelay = notificationDelay;
    }

    public BLECommCallback getCallback() {
        return callback;
    }

    public void setCallback(BLECommCallback callback) {
        this.callback = callback;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public String getsUUID() {
        return sUUID;
    }

    public void setsUUID(String sUUID) {
        this.sUUID = sUUID;
    }

    public String getRxUUID() {
        return rxUUID;
    }

    public void setRxUUID(String rxUUID) {
        this.rxUUID = rxUUID;
    }

    public String getTxUUID() {
        return txUUID;
    }

    public void setTxUUID(String txUUID) {
        this.txUUID = txUUID;
    }

    public String getfUUID() {
        return fUUID;
    }

    public void setfUUID(String fUUID) {
        this.fUUID = fUUID;
    }

    public long getNotificationDelay() {
        return notificationDelay;
    }

    public void setNotificationDelay(long notificationDelay) {
        this.notificationDelay = notificationDelay;
    }
}
