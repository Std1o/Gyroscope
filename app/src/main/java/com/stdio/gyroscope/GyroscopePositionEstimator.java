package com.stdio.gyroscope;

import android.hardware.SensorEvent;

public class GyroscopePositionEstimator {

    // Create a constant to convert Meter to MilliMeter
    private static final float M2MM = 1000.0f;
    // Create a constant to convert nanoseconds to seconds.
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    private final static float EPSILON = 1;

    /**
     * Constructor
     */
    public GyroscopePositionEstimator() {
    }

    public void onSensorEvent(SensorEvent event) {
        // This timestep's delta rotation to be multiplied by the current
        // rotation
        // after computing it from the gyro sample data.
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY
                    * axisY + axisZ * axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            // (that is, EPSILON should represent your maximum allowable margin
            // of error)
            if (omegaMagnitude > EPSILON) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the
            // timestep
            // We will convert this axis-angle representation of the delta
            // rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
            deltaRotationVector[1] = sinThetaOverTwo * axisX * M2MM;
            deltaRotationVector[0] = sinThetaOverTwo * axisY * M2MM;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ * M2MM;
            deltaRotationVector[3] = cosThetaOverTwo;
        }
        timestamp = event.timestamp;
    }

    public float[] getPosition() {
        deltaRotationVector[0] = getValue(deltaRotationVector[0]);
        deltaRotationVector[1] = getValue(deltaRotationVector[1]);
        deltaRotationVector[2] = getValue(deltaRotationVector[2]);
        return deltaRotationVector;
    }

    private float getValue(float value) {
        if (value * value > 1) {
            return value;
        }
        return 0;
    }

    public void reset() {
        deltaRotationVector[0] = 0;
        deltaRotationVector[1] = 0;
        deltaRotationVector[2] = 0;
        deltaRotationVector[3] = 0;
    }
}