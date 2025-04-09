package com.spop.poverlay.sensor.v1

import android.os.IBinder
import timber.log.Timber

/**
 * At low power values the sensor can sometimes send huge spikes in values returned
 *
 * These spikes are referred to as "Spurious Readings" in this implementation, and rejected
 */
class PowerSensor(binder: IBinder) : Sensor(Command.GetPowerRepeating, binder) {
    override fun mapValue(value: Float) = value 
    /* removing "/ 100"  since originally implementation doesn't look like this https://github.com/selalipop/grupetto/blob/d6f96d4d78598a4c2d33ce597e2b041891643d07/app/src/main/java/com/spop/poverlay/sensor/PowerSensor.kt */
}
