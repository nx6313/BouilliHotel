/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bouilli.nxx.bouillihotel.push.org.androidpn.client;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.bouilli.nxx.bouillihotel.MainActivity;

/** 
 * A thread class for recennecting the server.
 *
 * @author Sehwan Noh (devnoh@gmail.com)
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
            while (!isInterrupted()) {
                if(!xmppManager.getConnection().isAuthenticated()){
                    // 发送首页广播，提示连接推送服务器中...
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    msg.what = MainActivity.MSG_PUSH_CONNECTION_LOADING;
                    data.putString("pushConnectionType", "reconnection");
                    data.putInt("reconnectionAfterTime", waiting1());
                    msg.setData(data);
                    MainActivity.mHandler.sendMessage(msg);

                    Log.d(LOGTAG, "Trying to reconnect in " + waiting1()
                            + " seconds");
                    Thread.sleep((long) waiting1() * 1000L);
                    xmppManager.connect();
                    waiting++;
                }else{
                    // 发送首页广播，提示连接推送服务器中...
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    msg.what = MainActivity.MSG_PUSH_CONNECTION_LOADING;
                    data.putString("pushConnectionType", "reconnectionErr");
                    msg.setData(data);
                    MainActivity.mHandler.sendMessage(msg);
                }
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
