/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2018 The LineageOS Project
 *		 2026 Aless Microsystems
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

package org.lineageos.settings.doze;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PickupSensor extends TriggerEventListener {
    private static final boolean DEBUG = false;
    private static final String TAG = "PickupSensor";

    private static final int MIN_PULSE_INTERVAL_MS = 2500;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Context mContext;
    private ExecutorService mExecutorService;

    private long mEntryTimestamp;

    public PickupSensor(Context context) {
        mContext = context;
        mSensorManager = mContext.getSystemService(SensorManager.class);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PICK_UP_GESTURE, true);
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    private Future<?> submit(Runnable runnable) { return mExecutorService.submit(runnable); }

    @Override
    public void onTrigger(TriggerEvent event) {
    	long now = SystemClock.elapsedRealtime();

    	if (now - mEntryTimestamp >= MIN_PULSE_INTERVAL_MS) {
    	    mEntryTimestamp = now;
    	    DozeUtils.wakeOrLaunchDozePulse(mContext);
    	}

    	// Always re-arm, even if we ignored this event
    	mSensorManager.requestTriggerSensor(this, mSensor);
	}



    protected void enable() {
    if (mSensor == null) return;

    	mEntryTimestamp = SystemClock.elapsedRealtime();
    	mSensorManager.requestTriggerSensor(this, mSensor);
	}

    protected void disable() {
        if (DEBUG)
            Log.d(TAG, "Disabling");
        submit(() -> { mSensorManager.cancelTriggerSensor(this, mSensor); });
    }
}
