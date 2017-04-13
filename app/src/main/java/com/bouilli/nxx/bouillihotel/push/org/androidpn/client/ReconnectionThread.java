package com.bouilli.nxx.bouillihotel.push.org.androidpn.client;

import android.util.Log;

/** 
 * A thread class for recennecting the server.
 */
public class ReconnectionThread extends Thread {

    private static final String LOGTAG = LogUtil
            .makeLogTag(ReconnectionThread.class);

    private final XmppManager xmppManager;

    private int waiting;

    ReconnectionThread(XmppManager xmppManager) {
        this.xmppManager = xmppManager;
        this.waiting = 0;
    }

    public void run() {
        try {
            while (!isInterrupted() && !xmppManager.getConnection().isAuthenticated()) {
                Log.d(LOGTAG, "Trying to reconnect in " + waiting1()
                        + " seconds");
                Thread.sleep((long) waiting1() * 1000L);
                xmppManager.connect();
                waiting++;
            }
        } catch (final InterruptedException e) {
            xmppManager.getHandler().post(new Runnable() {
                public void run() {
                    xmppManager.getConnectionListener().reconnectionFailed(e);
                }
            });
        }
    }

    private int waiting() {
        if (waiting > 750) {
            return 600;
        }
        if (waiting > 700) {
            return 300;
        }
        if (waiting > 650) {
            return 100;
        }
        if (waiting > 600) {
            return 50;
        }
        return waiting <= 500 ? 10 : 20;
    }

    private int waiting1() {
        return 10;
    }
}
