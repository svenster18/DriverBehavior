package com.androkit.driverbehavior;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.androkit.driverbehavior.ml.AbnormalDriving;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class DetectService extends Service implements SensorEventListener {
    private static final int TIME_STAMP = 34;
    final String TAG = DetectService.class.getSimpleName();
    private SensorManager sensorManager;

    final ArrayList<Float> ax = new ArrayList<>();
    final ArrayList<Float> ay = new ArrayList<>();
    final ArrayList<Float> az = new ArrayList<>();
    final ArrayList<Float> gx = new ArrayList<>();
    final ArrayList<Float> gy = new ArrayList<>();
    final ArrayList<Float> gz = new ArrayList<>();

    private int zigzag = 0;
    private int sleepy = 0;
    private int braking = 0;
    private int acceleration = 0;

    final MutableLiveData<Integer> zigzagLiveData = new MutableLiveData<>();
    final MutableLiveData<Integer> sleepyLiveData = new MutableLiveData<>();
    final MutableLiveData<Integer> brakingLiveData = new MutableLiveData<>();
    final MutableLiveData<Integer> accelerationLiveData = new MutableLiveData<>();

    private final DetectBinder binder = new DetectBinder();

    public DetectService() {
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, DetectActivity.class);
        int pendingFlags = PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingFlags);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "channel_01";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Detect Service")
                .setContentText("Detecting Behavior Service Running")
                .setAutoCancel(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_NAME = "dicoding channel";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_NAME);
            notificationBuilder.setChannelId(CHANNEL_ID);
            mNotificationManager.createNotificationChannel(channel);
        }

        return notificationBuilder.build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        zigzagLiveData.postValue(zigzag);
        sleepyLiveData.postValue(sleepy);
        brakingLiveData.postValue(braking);
        accelerationLiveData.postValue(acceleration);
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = buildNotification();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, sensorAcc, 50000);
        sensorManager.registerListener(this, sensorGyro, 50000);

        int NOTIFICATION_ID = 1;
        startForeground(NOTIFICATION_ID, notification);
        Log.d(TAG, "Service dijalankan...");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        zigzag = 0;
        sleepy = 0;
        braking = 0;
        acceleration = 0;
        sensorManager.unregisterListener(this);
        Log.d(TAG, "onDestroy: Service dihentikan");
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensorType = sensorEvent.sensor;

        if (sensorType.getType() == Sensor.TYPE_ACCELEROMETER) {

            // adding the accelerometer values inside the list
            ax.add(sensorEvent.values[0]);
            ay.add(sensorEvent.values[1]);
            az.add(sensorEvent.values[2]);

        } else if (sensorType.getType() == Sensor.TYPE_GYROSCOPE) {

            // adding the gyroscope values inside the list
            gx.add(sensorEvent.values[0]);
            gy.add(sensorEvent.values[1]);
            gz.add(sensorEvent.values[2]);
        }

        predict();
    }

    private void predict() {
        List<Float> data = new ArrayList<>();
        if (ax.size() >= TIME_STAMP && ay.size() >= TIME_STAMP && az.size() >= TIME_STAMP
                && gx.size() >= TIME_STAMP && gy.size() >= TIME_STAMP && gz.size() >= TIME_STAMP) {
            data.addAll(ax.subList(0, TIME_STAMP));
            data.addAll(ay.subList(0, TIME_STAMP));
            data.addAll(az.subList(0, TIME_STAMP));

            data.addAll(gx.subList(0, TIME_STAMP));
            data.addAll(gy.subList(0, TIME_STAMP));
            data.addAll(gz.subList(0, TIME_STAMP));

            Log.d(TAG, "predictActivities: Data in List ArrayList" + data);

            try {
                AbnormalDriving model = AbnormalDriving.newInstance(this);

                // Creates inputs for reference.
                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 6, TIME_STAMP}, DataType.FLOAT32);
                Log.d("shape", inputFeature0.getBuffer().toString());
                inputFeature0.loadArray(toFloatArray(data));

                // Runs model inference and gets result.
                AbnormalDriving.Outputs outputs = model.process(inputFeature0);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                float[] floatOutputs = outputFeature0.getFloatArray();
                float max = Float.MIN_VALUE;
                int maxIndex = 0;
                int sameValue = 0;
                for (float floatOutput : floatOutputs) {
                    if (floatOutput > max) {
                        max = floatOutput;
                    }
                }
                for (int i = 0; i < floatOutputs.length; i++) {
                    if (floatOutputs[i] == max) {
                        maxIndex = i;
                        sameValue++;
                    }
                }
                if (sameValue == 1) {
                    switch (maxIndex) {
                        case 0: {
                            Log.d(TAG, "Normal");
                            break;
                        }
                        case 1: {
                            Log.d(TAG, "Zigzag");
                            zigzag++;
                            zigzagLiveData.postValue(zigzag);
                            if (zigzag == 7) {
                                setAlarm(this);
                            }
                            break;
                        }
                        case 2: {
                            Log.d(TAG, "Sleepy");
                            sleepy++;
                            sleepyLiveData.postValue(sleepy);
                            if (sleepy == 7) {
                                setAlarm(this);
                            }
                            break;
                        }
                        case 3: {
                            Log.d(TAG, "Sudden Braking");
                            braking++;
                            brakingLiveData.postValue(braking);
                            if (braking == 7) {
                                setAlarm(this);
                            }
                            break;
                        }
                        case 4: {
                            Log.d(TAG, "Sudden Acceleration");
                            acceleration++;
                            accelerationLiveData.postValue(acceleration);
                            if (acceleration == 7) {
                                setAlarm(this);
                            }

                            break;
                        }
                    }
                } else {
                    Log.d(TAG, "Normal");
                }
                Log.d(TAG, "predictActivities: output array: " + Arrays.toString(outputFeature0.getFloatArray()));

                // Releases model resources if no longer used.
                model.close();

                //clear the list for the next prediction
                ax.clear();
                ay.clear();
                az.clear();
                gx.clear();
                gy.clear();
                gz.clear();
            } catch (IOException e) {
                // TODO Handle the exception
            }
        }
    }

    private float[] toFloatArray(List<Float> data) {
        int i = 0;
        float[] array = new float[data.size()];
        for (Float f : data) {
            array[i++] = (f != null ? f : Float.NaN);
        }
        return array;
    }

    private void setAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), null);
        }
        showAlarmNotification(context);
    }

    private void showAlarmNotification(Context context) {
        String CHANNEL_ID = "Channel_1";
        String CHANNEL_NAME = "AlarmManager channel";

        Intent intent = new Intent(DetectService.this, DetectActivity.class);
        intent.putExtra(DetectActivity.EXTRA_NOTIF, true);
        intent.setAction("Open Dialog");
        PendingIntent pendingIntent;

        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentIntent(pendingIntent).setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Warning")
                .setContentText("You have been driving outside normal limits")
                .setColor(ContextCompat.getColor(context, android.R.color.transparent))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(alarmSound);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{1000, 1000, 1000, 1000, 1000});
            channel.setSound(alarmSound, channel.getAudioAttributes());
            builder.setChannelId(CHANNEL_ID);
            if (notificationManagerCompat != null) {
                notificationManagerCompat.createNotificationChannel(channel);
            }
        }
        Notification notification = builder.build();
        if (notificationManagerCompat != null) {
            notificationManagerCompat.notify(100, notification);
            Log.d(TAG, "Alarm started");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public class DetectBinder extends Binder {
        final DetectService getService = DetectService.this;
    }
}