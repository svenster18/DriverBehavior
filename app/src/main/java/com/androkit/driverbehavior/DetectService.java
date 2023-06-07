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
import android.media.SoundPool;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.androkit.driverbehavior.ml.MobilproAbnormalDriving;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class DetectService extends Service implements SensorEventListener {
    private static final float ACCEL_MAX = 23.381857f;
    private static final float ACCEL_MIN = -13.673344f;
    private static final float GYRO_MAX = 0.894246f;
    private static final float GYRO_MIN = -0.9062498f;
    private final int SERVICE_NOTIFICATION_ID = 1;
    private final String SERVICE_CHANNEL_ID = "channel_01";
    private final String WARNING_CHANNEL_ID = "channel_02";
    private final String SERVICE_CHANNEL_NAME = "service channel";
    private final String WARNING_CHANNEL_NAME = "warning channel";
    final String TAG = DetectService.class.getSimpleName();
    private SensorManager sensorManager;
    private Sensor sensorAcc;
    private Sensor sensorGyro;

    private ArrayList<Float> ax = new ArrayList<>();
    private ArrayList<Float> ay = new ArrayList<>();
    private ArrayList<Float> az = new ArrayList<>();
    private ArrayList<Float> gx = new ArrayList<>();
    private ArrayList<Float> gy = new ArrayList<>();
    private ArrayList<Float> gz = new ArrayList<>();

    private int zigzag = 0;
    private int sleepy = 0;
    private int braking = 0;
    private int acceleration = 0;

    MutableLiveData<Integer> zigzagLiveData = new MutableLiveData<>();
    MutableLiveData<Integer> sleepyLiveData = new MutableLiveData<>();
    MutableLiveData<Integer> brakingLiveData = new MutableLiveData<>();
    MutableLiveData<Integer> accelerationLiveData = new MutableLiveData<>();

    private DetectBinder binder = new DetectBinder();

    public static SoundPool sp;
    public static int streamId = 0;
    private int soundId = 0;
    private boolean spLoaded = false;
    public static boolean spPlayed = false;

    float[] normalAx = new float[10];
    float[] normalAy = new float[10];
    float[] normalAz = new float[10];
    float[] normalGx = new float[10];
    float[] normalGy = new float[10];
    float[] normalGz = new float[10];

    int accel = 0;
    int gyro = 0;

    public DetectService() {
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, DetectActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int pendingFlags = PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingFlags);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Detect Service")
                .setContentText("Detecting Behavior Service Running");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(SERVICE_CHANNEL_ID,
                    SERVICE_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(SERVICE_CHANNEL_NAME);
            notificationBuilder.setChannelId(SERVICE_CHANNEL_ID);
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
        notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, sensorAcc, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorGyro, SensorManager.SENSOR_DELAY_NORMAL);

        startForeground(SERVICE_NOTIFICATION_ID, notification);
        Log.d(TAG, "Service dijalankan...");

        sp = new SoundPool.Builder()
                .setMaxStreams(10)
                .build();

        sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int status) {
                if (status == 0) {
                    spLoaded = true;
                } else {
                    Toast.makeText(DetectService.this, "Load failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        soundId = sp.load(this, R.raw.car_warning, 1);

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

        if(sensorType.getType()==Sensor.TYPE_ACCELEROMETER) {
            if (accel < 10) {
                // adding the accelerometer values inside the list
                ax.add(sensorEvent.values[0]);
                ay.add(sensorEvent.values[1]);
                az.add(sensorEvent.values[2]);
                accel++;
            }



        }else if(sensorType.getType()==Sensor.TYPE_GYROSCOPE){
            if (gyro < 10) {
                // adding the gyroscope values inside the list
                gx.add(sensorEvent.values[0]);
                gy.add(sensorEvent.values[1]);
                gz.add(sensorEvent.values[2]);
                gyro++;
            }

        }

        predict();
    }

    private void predict() {
        if( ax.size() >= 10 && ay.size() >= 10 && az.size() >= 10
                && gx.size() >= 10 && gy.size() >= 10 && gz.size() >= 10)
        {
            normalAx = normalizazionss(toFloatArray(ax), ACCEL_MIN, ACCEL_MAX);
            normalAy = normalizazionss(toFloatArray(ay), ACCEL_MIN, ACCEL_MAX);
            normalAz = normalizazionss(toFloatArray(az), ACCEL_MIN, ACCEL_MAX);
            normalGx = normalizazionss(toFloatArray(gx), GYRO_MIN, GYRO_MAX);
            normalGy = normalizazionss(toFloatArray(gy), GYRO_MIN, GYRO_MAX);
            normalGz = normalizazionss(toFloatArray(gz), GYRO_MIN, GYRO_MAX);
            ArrayList<Float> data = new ArrayList<>();
            for (float nAx : normalAx) {
                data.add(nAx);
            }
            for (float nAy : normalAy) {
                data.add(nAy);
            }
            for (float nAz : normalAz) {
                data.add(nAz);
            }
            for (float nGx : normalGx) {
                data.add(nGx);
            }
            for (float nGy : normalGy) {
                data.add(nGy);
            }
            for (float nGz : normalGz) {
                data.add(nGz);
            }

            Log.d(TAG, "predictActivities: Data in List ArrayList"+ data);
            Log.d(TAG, "predictActivities: ax size"+ ax.size());
            Log.d(TAG, "predictActivities: gx size"+ gx.size());

            try {

                MobilproAbnormalDriving model = MobilproAbnormalDriving.newInstance(this);

                // Creates inputs for reference.
                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 6, 10}, DataType.FLOAT32);
                Log.d("shape", inputFeature0.getBuffer().toString());
                inputFeature0.loadArray(toFloatArray(data));

                ax.clear();
                ay.clear();
                az.clear();
                gx.clear();
                gy.clear();
                gz.clear();
                data.clear();

                // Runs model inference and gets result.
                MobilproAbnormalDriving.Outputs outputs = model.process(inputFeature0);
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
                    if(floatOutputs[i] == max) {
                        maxIndex = i;
                        sameValue++;
                    }
                }
                    if (sameValue == 1) {
                        switch (maxIndex) {
                            case 0 : {
                                Log.d(TAG, "Normal");
                                break;
                            }
                            case 1 : {
                                Log.d(TAG, "Zigzag");
                                zigzag++;
                                zigzagLiveData.postValue(zigzag);
                                if (zigzag % 7 == 0 && zigzag > 0) {
                                    setAlarm(this, "Warning", "You have been driving outside normal limits", 100);
                                }
                                break;
                            }
                            case 2 : {
                                Log.d(TAG, "Sleepy");
                                sleepy++;
                                sleepyLiveData.postValue(sleepy);
                                if (sleepy % 7 == 0 && sleepy > 0) {
                                    setAlarm(this, "Warning", "You have been driving outside normal limits", 100);
                                }
                                break;
                            }
                            case 3 : {
                                Log.d(TAG, "Sudden Braking");
                                braking++;
                                brakingLiveData.postValue(braking);
                                if (braking % 7 ==0 && braking > 0) {
                                    setAlarm(this, "Warning", "You have been driving outside normal limits", 100);
                                }
                                break;
                            }
                            case 4 : {
                                Log.d(TAG, "Sudden Acceleration");
                                acceleration++;
                                accelerationLiveData.postValue(acceleration);
                                if (acceleration % 7 == 0 && acceleration > 0) {
                                    setAlarm(this, "Warning", "You have been driving outside normal limits", 100);
                                }

                                break;
                            }
                        }
                    }
                    else {
                        Log.d(TAG, "Normal");
                    }
//                }
                Log.d(TAG, "predictActivities: output array: " + Arrays.toString(outputFeature0.getFloatArray()));

                // Releases model resources if no longer used.
                model.close();

                //clear the list for the next prediction
                ax.clear(); ay.clear(); az.clear();
                gx.clear(); gy.clear(); gz.clear();data.clear();

                accel = 0;
                gyro = 0;
            } catch (IOException e) {
                // TODO Handle the exception
            }
        }
    }

    public float[] normalizazionss(float[] input_array, float arrayMin, float arrayMax){

        float maxValue = 1.0f;
        float minValue = 0f;

        // Create an output array of the same size as the input array
        float[] outputArray = new float[input_array.length];

        // Normalize each element of the input array and store it in the output array
        for (int i = 0; i < input_array.length; i++) {
            outputArray[i] = ((input_array[i] - arrayMin) / (arrayMax - arrayMin)) * (maxValue - minValue) + minValue;
        }

        return outputArray;

    }

    private float[] toFloatArray(List<Float> data){
        int i = 0;
        float[] array = new float[data.size()];
        for (Float f: data){
            array[i++] = (f !=null ? f: Float.NaN);
        }
        return array;
    }

    private void setAlarm(Context context, String title, String message, int notifId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), null);
            if (spLoaded && !spPlayed) {
                streamId = sp.play(soundId, 1f, 1f, 0, -1, 1f);
                spPlayed = true;
                showAlarmNotification(context, title, message, notifId);
            }
        }
    }

    private void showAlarmNotification(Context context, String title, String message, int notifId) {

        Intent intent = new Intent(DetectService.this, DetectActivity.class);
        intent.putExtra(DetectActivity.EXTRA_NOTIF, true);

        intent.setAction("Open Dialog");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent;

        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, WARNING_CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setColor(ContextCompat.getColor(context, android.R.color.transparent))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setAutoCancel(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(WARNING_CHANNEL_ID,
                    WARNING_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{1000, 1000, 1000, 1000, 1000});
            builder.setChannelId(WARNING_CHANNEL_ID);
            if (notificationManagerCompat != null) {
                notificationManagerCompat.createNotificationChannel(channel);
            }
        }
        Notification notification = builder.build();
        if (notificationManagerCompat != null) {
            notificationManagerCompat.notify(notifId, notification);
            Log.d(TAG, "Alarm started");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public class DetectBinder extends Binder {
        DetectService getService = DetectService.this;
    }
}