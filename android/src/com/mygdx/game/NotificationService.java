package com.mygdx.game;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.badlogic.gdx.Gdx;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Random;

import static com.mygdx.game.app.cons;


public class NotificationService extends Service {
    DatagramSocket sock;
    Boolean reset;
    private static final int ONGOING_NOTIFICATION = 1;
    String msg;
    public static boolean isChecked = true;
    private boolean isAlarm = false;


    @Override
    public void onCreate(){
        Log.d("serviceCreated", "service");
    }

    void startListen() {
        new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    int port = 11111;
                    while (reset) {
                        continueListen(port);
                    }
                }catch(Exception e){}
            stopSelf();
            }
        }).start();


    }
    void continueListen(int port){
        try {
            byte[] recvBuf = new byte[15000];
            if (sock == null || sock.isClosed()) {
                sock = new DatagramSocket(port);
                sock.setBroadcast(true);
                Log.d("Socket", "SocketCreated");
            }
            DatagramPacket pack = new DatagramPacket(recvBuf, recvBuf.length);
            sock.receive(pack);
            msg = new String(pack.getData()).trim();
            if(msg.charAt(0) == '0') {
                if(isAlarm)
                    foregroundBuilder("Газоанализатор","Уровень газа в норме", R.drawable.alarm0, new long[] {0}, 0);
                isAlarm = false;
                app.number = 0;
                for (int i = 0; i < 6; i++){
                    app.pos2[i] = 0;
                }
                Gdx.graphics.requestRendering();
            }
            else if(msg.charAt(0) == '1'){
                if(Integer.valueOf(Build.VERSION.SDK) < 26) {
                    foregroundBuilderOreo("Газоанализатор","Уровень газа превышен", R.drawable.alarm1, new long[] {0, 1000, 500, 1000}, R.raw.alarm);
                }
                if(isChecked)
                    foregroundBuilder("Газоанализатор","Уровень газа превышен", R.drawable.alarm1, new long[] {0, 1000, 500, 1000}, R.raw.alarm);
                isAlarm = true;
                app.pos = Integer.valueOf(Character.toString(msg.charAt(2)));
                app.pos2[app.pos] = Float.valueOf(Character.toString(msg.charAt(1))) / 10;
                Gdx.graphics.requestRendering();
            }
            else if(msg.charAt(0) == '2'){
                cons[Integer.parseInt(String.valueOf(msg.charAt(1)))] = msg.substring(2);
                Gdx.graphics.requestRendering();
            }
            else{
                foregroundBuilder("Газоанализатор",msg, R.drawable.alarm0, new long[] {0}, 0);
            }


            Log.d("message", Float.toString(app.number));
        }catch(Exception e){
            Log.d("SocketError", e.getMessage());
            reset = false;
        }
    }
    @TargetApi(26)
    void foregroundBuilderOreo(String Message, String Content, int icon, long[] vibro, int sound){
        int notifyID = 1;
        String CHANNEL_ID = "my_channel_01";
        CharSequence name = "channel_name";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        mChannel.setDescription("Notifi");
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(vibro);
        NotificationManager manager =  (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(mChannel);

    }
    void foregroundBuilder(String Message, String Content, int icon, long[] vibro, int sound){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getService(this, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);//.getActivity(this, 0, notificationIntent, 0);
        Notification notify = new NotificationCompat.Builder(this, createChannel())
                .setChannelId(createChannel())
                .setSmallIcon(icon)
                .setPriority(5)
                .setContentIntent(pendingIntent)
                .setContentTitle(Message)
                .setContentText(Content)
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName()  + "/" + sound))
                .setVibrate(vibro)
                .build();

        startForeground(ONGOING_NOTIFICATION, notify);
    }

    @TargetApi(26)
    private String createChannel(){
        if(Integer.valueOf(Build.VERSION.SDK) < 26) {
           return "";
        }
        Random rand = new Random();
        String CHANNEL_ID =
                String.valueOf(rand.nextInt(50));
        NotificationManager notifyManager = getSystemService(NotificationManager.class);
        CharSequence channelName = "Alarm";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
        notifyManager.createNotificationChannel(channel);
        return CHANNEL_ID;

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        reset = true;
        foregroundBuilder("Газоанализатор","Уровень газа в норме", R.drawable.alarm0, new long[] {0}, 0);
        startListen();
        Log.d("service", "service started");
        return START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onDestroy(){

    }
}
