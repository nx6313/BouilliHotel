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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bouilli.nxx.bouillihotel.MainActivity;
import com.bouilli.nxx.bouillihotel.push.org.jivesoftware.smack.ConnectionConfiguration;
import com.bouilli.nxx.bouillihotel.push.org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import com.bouilli.nxx.bouillihotel.push.org.jivesoftware.smack.ConnectionListener;
import com.bouilli.nxx.bouillihotel.push.org.jivesoftware.smack.PacketListener;
import com.bouilli.nxx.bouillihotel.push.org.jivesoftware.smack.XMPPConnection;
import com.bouilli.nxx.bouillihotel.push.org.jivesoftware.smack.XMPPException;
import com.bouilli.nxx.bouillihotel.push.org.jivesoftware.smack.filter.AndFilter;
import com.bouilli.nxx.bouillihotel.push.org.jivesoftware.smack.filter.PacketFilter;
import com.bouilli.nxx.bouillihotel.push.org.jivesoftware.smack.filter.PacketIDFilter;
import com.bouilli.nxx.bouillihotel.push.org.jivesoftware.smack.filter.PacketTypeFilter;
import com.bouilli.nxx.bouillihotel.push.org.jivesoftware.smack.packet.IQ;
import com.bouilli.nxx.bouillihotel.push.org.jivesoftware.smack.packet.Packet;
import com.bouilli.nxx.bouillihotel.push.org.jivesoftware.smack.packet.Registration;
import com.bouilli.nxx.bouillihotel.push.org.jivesoftware.smack.provider.ProviderManager;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.L;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * This class is to manage the XMPP connection between client and server.
 * 
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class XmppManager {

    private static final String LOGTAG = LogUtil.makeLogTag(XmppManager.class);

    private static final String XMPP_RESOURCE_NAME = "BouilliHotelClient";

    private Context context;

    private NotificationService.TaskSubmitter taskSubmitter;

    private NotificationService.TaskTracker taskTracker;

    private SharedPreferences sharedPrefs;

    private String xmppHost;

    private int xmppPort;

    private XMPPConnection connection;

    private String username;

    private String password;

    private String name;

    private String userMobel;

    private ConnectionListener connectionListener;

    private PacketListener notificationPacketListener;

    private Handler handler;

    private List<Runnable> taskList;

    private boolean running = false;

    private Future<?> futureTask;

    private Thread reconnection;

    public XmppManager(NotificationService notificationService) {
        context = notificationService;
        taskSubmitter = notificationService.getTaskSubmitter();
        taskTracker = notificationService.getTaskTracker();
        sharedPrefs = notificationService.getSharedPreferences();

        xmppHost = sharedPrefs.getString(Constants.XMPP_HOST, "localhost");
        xmppPort = sharedPrefs.getInt(Constants.XMPP_PORT, 5222);
        username = sharedPrefs.getString(Constants.XMPP_USERNAME, "");
        password = sharedPrefs.getString(Constants.XMPP_PASSWORD, "");
        name = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userRealName", SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userLoginName", ""));
        userMobel = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userMobel", "");

        connectionListener = new PersistentConnectionListener(this);
        notificationPacketListener = new NotificationPacketListener(this);

        handler = new Handler();
        taskList = new ArrayList<Runnable>();
        reconnection = new ReconnectionThread(this);
    }

    public Context getContext() {
        return context;
    }

    public void connect() {
        Log.d(LOGTAG, "connect()...");
        submitLoginTask();
    }

    public void disconnect() {
        Log.d(LOGTAG, "disconnect()...");
        terminatePersistentConnection();
    }

    public void terminatePersistentConnection() {
        Log.d(LOGTAG, "terminatePersistentConnection()...");
        Runnable runnable = new Runnable() {

            final XmppManager xmppManager = XmppManager.this;

            public void run() {
                if (xmppManager.isConnected()) {
                    Log.d(LOGTAG, "terminatePersistentConnection()... run()");
                    xmppManager.getConnection().removePacketListener(
                            xmppManager.getNotificationPacketListener());
                    xmppManager.getConnection().disconnect();
                }
                xmppManager.runTask();
            }

        };
        addTask(runnable);
    }

    public XMPPConnection getConnection() {
        return connection;
    }

    public void setConnection(XMPPConnection connection) {
        this.connection = connection;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserMobel() {
        return userMobel;
    }

    public void setUserMobel(String userMobel) {
        this.userMobel = userMobel;
    }

    public ConnectionListener getConnectionListener() {
        return connectionListener;
    }

    public PacketListener getNotificationPacketListener() {
        return notificationPacketListener;
    }

    public void startReconnectionThread() {
        synchronized (reconnection) {
            if (reconnection == null || !reconnection.isAlive()) {
            	reconnection = new ReconnectionThread(this);
                reconnection.setName("Xmpp Reconnection Thread");
                reconnection.start();
            }
        }
    }

    public Handler getHandler() {
        return handler;
    }

    public void reregisterAccount() {
        removeAccount();
        submitLoginTask();
        runTask();
    }

    public List<Runnable> getTaskList() {
        return taskList;
    }

    public Future<?> getFutureTask() {
        return futureTask;
    }

    public void runTask() {
        Log.d(LOGTAG, "runTask()...");
        synchronized (taskList) {
            running = false;
            futureTask = null;
            if (!taskList.isEmpty()) {
                Runnable runnable = (Runnable) taskList.get(0);
                taskList.remove(0);
                running = true;
                futureTask = taskSubmitter.submit(runnable);
                if (futureTask == null) {
                    taskTracker.decrease();
                }
            }
        }
        taskTracker.decrease();
        Log.d(LOGTAG, "runTask()...done");
    }

    private String newRandomUUID() {
        String uuidRaw = UUID.randomUUID().toString();
        return uuidRaw.replaceAll("-", "");
    }

    private boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    private boolean isAuthenticated() {
        return connection != null && connection.isConnected()
                && connection.isAuthenticated();
    }

    private boolean isRegistered() {
        return sharedPrefs.contains(Constants.XMPP_AUTHORIZED)
                && sharedPrefs.getString(Constants.XMPP_AUTHORIZED, "").equals("hasAuth");
    }

    private void submitConnectTask() {
        Log.d(LOGTAG, "submitConnectTask()...");
        addTask(new ConnectTask());
    }

    private void submitRegisterTask() {
        Log.d(LOGTAG, "submitRegisterTask()...");
        submitConnectTask();
        addTask(new RegisterTask());
    }

    private void submitLoginTask() {
        Log.d(LOGTAG, "submitLoginTask()...");
        submitRegisterTask();
        addTask(new LoginTask());
    }

    private void addTask(Runnable runnable) {
        Log.d(LOGTAG, "addTask(runnable)...");
        taskTracker.increase();
        synchronized (taskList) {
            if (taskList.isEmpty() && !running) {
                running = true;
                futureTask = taskSubmitter.submit(runnable);
                if (futureTask == null) {
                    taskTracker.decrease();
                }
            } else {
                taskList.add(runnable);
            }
        }
        Log.d(LOGTAG, "addTask(runnable)... done");
    }

    private void removeAccount() {
        Editor editor = sharedPrefs.edit();
        editor.remove(Constants.XMPP_AUTHORIZED);
        //editor.remove(Constants.XMPP_PASSWORD);
        editor.commit();
    }

    private void dropTask(int dropCount) {
        synchronized (taskList) {
            if (taskList.size() >= dropCount) {
                for (int i = 0; i < dropCount; i++) {
                    taskList.remove(0);
                    taskTracker.decrease();
                }
            }
        }
    }

    /**
     * A runnable task to connect the server. 
     */
    private class ConnectTask implements Runnable {

        final XmppManager xmppManager;

        private ConnectTask() {
            this.xmppManager = XmppManager.this;
        }

        public void run() {
            Log.i(LOGTAG, "ConnectTask.run()...");

            if (!xmppManager.isConnected()) {
                // Create the configuration for this new connection
                ConnectionConfiguration connConfig = new ConnectionConfiguration(
                        xmppHost, xmppPort);
                // connConfig.setSecurityMode(SecurityMode.disabled);
                connConfig.setSecurityMode(SecurityMode.required);
                connConfig.setSASLAuthenticationEnabled(false);
                connConfig.setCompressionEnabled(false);

                XMPPConnection connection = new XMPPConnection(connConfig);
                xmppManager.setConnection(connection);

                try {
                    // Connect to the server
                    connection.connect();
                    Log.i(LOGTAG, "XMPP connected successfully");

                    // packet provider
                    ProviderManager.getInstance().addIQProvider("notification",
                            "androidpn:iq:notification",
                            new NotificationIQProvider());

                    xmppManager.runTask();

                } catch (XMPPException e) {
                    Log.e(LOGTAG, "XMPP connection failed", e);

                    xmppManager.dropTask(2);
                    xmppManager.runTask();

                    xmppManager.startReconnectionThread();
                } catch (Exception e) {
                    xmppManager.dropTask(2);
                    xmppManager.runTask();

                    xmppManager.startReconnectionThread();
                }
            } else {
                Log.i(LOGTAG, "XMPP connected already");
                xmppManager.runTask();
            }
        }
    }

    /**
     * A runnable task to register a new user onto the server. 
     */
    private class RegisterTask implements Runnable {

        final XmppManager xmppManager;

        boolean isRegisterSucceed;
        boolean hasDropTask;

        private RegisterTask() {
            xmppManager = XmppManager.this;
        }

        public void run() {
            Log.i(LOGTAG, "RegisterTask.run()...");

            if (!xmppManager.isRegistered()) {
                isRegisterSucceed = false;
                hasDropTask = false;

                String username = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userId", newRandomUUID());
                if(username == ""){
                    username = newRandomUUID();
                }else{
                    username = username + "|" + newRandomUUID();
                }
                final String newUsername = username;
                final String newPassword = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userPwd", newRandomUUID());
                final String name = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userRealName", SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userLoginName", ""));
                final String mobile = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userMobel", "");

                Registration registration = new Registration();

                PacketFilter packetFilter = new AndFilter(new PacketIDFilter(
                        registration.getPacketID()), new PacketTypeFilter(
                        IQ.class));

                PacketListener packetListener = new PacketListener() {

                    public void processPacket(Packet packet) {
                        synchronized (xmppManager) {
                            Log.d("RegisterTask.Listener",
                                    "processPacket().....");
                            Log.d("RegisterTask.Listener", "packet="
                                    + packet.toXML());

                            if (packet instanceof IQ) {
                                IQ response = (IQ) packet;
                                if (response.getType() == IQ.Type.ERROR) {
                                    if (!response.getError().toString().contains(
                                            "409")) {
                                        Log.e(LOGTAG,
                                                "Unknown error while registering XMPP account! "
                                                        + response.getError()
                                                        .getCondition());
                                    }
                                } else if (response.getType() == IQ.Type.RESULT) {
                                    xmppManager.setUsername(newUsername);
                                    xmppManager.setPassword(newPassword);
                                    xmppManager.setName(name);
                                    if(ComFun.strNull(userMobel)){
                                        xmppManager.setUserMobel(userMobel);
                                    }
                                    Log.d(LOGTAG, "username=" + newUsername);
                                    Log.d(LOGTAG, "password=" + newPassword);

                                    Editor editor = sharedPrefs.edit();
                                    editor.putString(Constants.XMPP_USERNAME, newUsername);
                                    editor.putString(Constants.XMPP_PASSWORD, newPassword);
                                    editor.putString(Constants.XMPP_AUTHORIZED, "hasAuth");
                                    editor.commit();

                                    isRegisterSucceed = true;

                                    Log.i(LOGTAG, "Account registered successfully");

                                    if(!hasDropTask) {
                                        xmppManager.runTask();
                                    }
                                }
                            }
                        }
                    }
                };

                connection.addPacketListener(packetListener, packetFilter);

                registration.setType(IQ.Type.SET);
                // registration.setTo(xmppHost);
                // Map<String, String> attributes = new HashMap<String, String>();
                // attributes.put("username", rUsername);
                // attributes.put("password", rPassword);
                // registration.setAttributes(attributes);
                registration.addAttribute("username", newUsername);
                registration.addAttribute("password", newPassword);
                registration.addAttribute("name", name);
                if(ComFun.strNull(mobile) && !mobile.equals("-")){
                    registration.addAttribute("email", mobile);
                }
                connection.sendPacket(registration);

                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (xmppManager) {
                    if(!isRegisterSucceed){
                        xmppManager.dropTask(1);
                        xmppManager.runTask();
                        xmppManager.startReconnectionThread();
                        hasDropTask = true;
                    }
                }
            } else {
                Log.i(LOGTAG, "Account registered already");
                xmppManager.runTask();
            }
        }
    }

    /**
     * A runnable task to log into the server. 
     */
    private class LoginTask implements Runnable {

        final XmppManager xmppManager;

        private LoginTask() {
            this.xmppManager = XmppManager.this;
        }

        public void run() {
            Log.i(LOGTAG, "LoginTask.run()...");

            if (!xmppManager.isAuthenticated()) {
                Log.d(LOGTAG, "username=" + username);
                Log.d(LOGTAG, "password=" + password);

                try {
                    xmppManager.getConnection().login(
                            xmppManager.getUsername(),
                            xmppManager.getPassword(), XMPP_RESOURCE_NAME);
                    Log.d(LOGTAG, "Loggedn in successfully");

                    // connection listener
                    if (xmppManager.getConnectionListener() != null) {
                        xmppManager.getConnection().addConnectionListener(
                                xmppManager.getConnectionListener());
                    }

                    // packet filter
                    PacketFilter packetFilter = new PacketTypeFilter(
                            NotificationIQ.class);
                    // packet listener
                    PacketListener packetListener = xmppManager
                            .getNotificationPacketListener();
                    connection.addPacketListener(packetListener, packetFilter);

                    // 发送首页广播，提示连接推送服务器中...
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    msg.what = MainActivity.MSG_PUSH_CONNECTION_LOADING;
                    data.putString("pushConnectionType", "connectionSuccess");
                    msg.setData(data);
                    MainActivity.mHandler.sendMessage(msg);
                    
                    connection.startHeartBeat();

                } catch (XMPPException e) {
                    Log.e(LOGTAG, "LoginTask.run()... xmpp error");
                    Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: "
                            + e.getMessage());
                    String INVALID_CREDENTIALS_ERROR_CODE = "401";
                    String errorMessage = e.getMessage();
                    if (errorMessage != null
                            && errorMessage
                                    .contains(INVALID_CREDENTIALS_ERROR_CODE)) {
                        xmppManager.reregisterAccount();
                        return;
                    }
                    xmppManager.startReconnectionThread();

                } catch (Exception e) {
                    Log.e(LOGTAG, "LoginTask.run()... other error");
                    Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: "
                            + e.getMessage());
                    xmppManager.startReconnectionThread();
                } finally {
                    xmppManager.runTask();
                }

            } else {
                Log.i(LOGTAG, "Logged in already");
                xmppManager.runTask();
            }

        }
    }

}
