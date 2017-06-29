package org.cirqoid.cnc.controller.commands;

import org.cirqoid.cnc.controller.settings.ApplicationConstants;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by simon on 14.06.17.
 */
public class SetParametersCommand extends Command
{
    public static class Axis
    {
        public boolean enabled;
        public float stepsPerMilli;
        public int lowLimit;
        public int highLimit;
        public int seekrate;
        public boolean inverted;
        public boolean homingDirection;
        public int homingRate;
        public float seekAcceleration;
    }

    public enum FeedbackProvider
    {
        OPEN_LOOP(0), AS5311(1);

        public int i;

        FeedbackProvider(int i)
        {
            this.i = i;
        }
    }

    public enum HomingSensor
    {
        NONE(0), GPIO(1), AS5311(2), GPIO_HACK(3);

        public int i;

        HomingSensor(int i)
        {
            this.i = i;
        }
    }

    public static class Motor
    {
        public int axis;
        public FeedbackProvider feedbackProvider;
        public boolean invertFeedback;
        public HomingSensor homingSensor;
        public boolean invertSensor;
    }

    // MotionSettings
    private float acceleration;
    private int maxArcsFeed;
    private int motionCorrectionFrequency;
    private float motionAdjustmentTarget;
    private int accelerationStartSpeed;
    private int minimumSpeed;
    private float motionCorrecionLagThreshold;
    private int motionCorrectionMinLagThreshold;

    // MachineParameters
    private Axis axes[] = new Axis[ApplicationConstants.MAX_AXES_COUNT];
    private Motor motors[] = new Motor[ApplicationConstants.MAX_MOTORS_COUNT];

    public float getAcceleration()
    {
        return acceleration;
    }

    public void setAcceleration(float acceleration)
    {
        this.acceleration = acceleration;
    }

    public int getMaxArcsFeed()
    {
        return maxArcsFeed;
    }

    public void setMaxArcsFeed(int maxArcsFeed)
    {
        this.maxArcsFeed = maxArcsFeed;
    }

    public int getMotionCorrectionFrequency()
    {
        return motionCorrectionFrequency;
    }

    public void setMotionCorrectionFrequency(int motionCorrectionFrequency)
    {
        this.motionCorrectionFrequency = motionCorrectionFrequency;
    }

    public float getMotionAdjustmentTarget()
    {
        return motionAdjustmentTarget;
    }

    public void setMotionAdjustmentTarget(float motionAdjustmentTarget)
    {
        this.motionAdjustmentTarget = motionAdjustmentTarget;
    }

    public int getAccelerationStartSpeed()
    {
        return accelerationStartSpeed;
    }

    public void setAccelerationStartSpeed(int accelerationStartSpeed)
    {
        this.accelerationStartSpeed = accelerationStartSpeed;
    }

    public int getMinimumSpeed()
    {
        return minimumSpeed;
    }

    public void setMinimumSpeed(int minimumSpeed)
    {
        this.minimumSpeed = minimumSpeed;
    }

    public float getMotionCorrecionLagThreshold()
    {
        return motionCorrecionLagThreshold;
    }

    public void setMotionCorrecionLagThreshold(float motionCorrecionLagThreshold)
    {
        this.motionCorrecionLagThreshold = motionCorrecionLagThreshold;
    }

    public int getMotionCorrectionMinLagThreshold()
    {
        return motionCorrectionMinLagThreshold;
    }

    public void setMotionCorrectionMinLagThreshold(int motionCorrectionMinLagThreshold)
    {
        this.motionCorrectionMinLagThreshold = motionCorrectionMinLagThreshold;
    }

    public Axis getAxis(int i)
    {
        return axes[i];
    }

    public void setAxis(int i, Axis axis)
    {
        axes[i] = axis;
    }

    public Motor getMotor(int i)
    {
        return motors[i];
    }

    public void setMotor(int i, Motor motor)
    {
        motors[i] = motor;
    }

    @Override
    public Type getType()
    {
        return Type.SET_PARAMETERS;
    }

    @Override
    public byte[] getPayload()
    {
        ByteBuffer b = ByteBuffer.allocate(8 * 4 + 9 * 4 * ApplicationConstants.MAX_AXES_COUNT + 5 * 4 * ApplicationConstants.MAX_MOTORS_COUNT);
        b.putFloat(acceleration);
        b.putInt(maxArcsFeed);
        b.putInt(motionCorrectionFrequency);
        b.putFloat(motionAdjustmentTarget);
        b.putInt(accelerationStartSpeed);
        b.putInt(minimumSpeed);
        b.putFloat(motionCorrecionLagThreshold);
        b.putInt(motionCorrectionMinLagThreshold);
        for (Axis axis : axes)
        {
            b.putInt(axis.enabled ? 1 : 0);
            b.putFloat(axis.stepsPerMilli);
            b.putInt(axis.lowLimit);
            b.putInt(axis.highLimit);
            b.putInt(axis.seekrate);
            b.putInt(axis.inverted ? 1 : 0);
            b.putInt(axis.homingDirection ? 1 : 0);
            b.putInt(axis.homingRate);
            b.putFloat(axis.seekAcceleration);
        }
        for (Motor motor : motors)
        {
            if (motor != null)
            {
                b.putInt(motor.axis);
                b.putInt(motor.feedbackProvider.i);
                b.putInt(motor.invertFeedback ? 1 : 0);
                b.putInt(motor.homingSensor.i);
                b.putInt(motor.invertSensor ? 1 : 0);
            }
            else
                for (int i = 0; i < 5; i++)
                    b.putInt(-1);
        }
        return b.array();
    }

    @Override
    public String toString()
    {
        return "SetParametersCommand{" +
                "id=" + getId() +
                ", acceleration=" + acceleration +
                ", maxArcsFeed=" + maxArcsFeed +
                ", motionCorrectionFrequency=" + motionCorrectionFrequency +
                ", motionAdjustmentTarget=" + motionAdjustmentTarget +
                ", accelerationStartSpeed=" + accelerationStartSpeed +
                ", minimumSpeed=" + minimumSpeed +
                ", motionCorrecionLagThreshold=" + motionCorrecionLagThreshold +
                ", motionCorrectionMinLagThreshold=" + motionCorrectionMinLagThreshold +
                ", axes=" + Arrays.toString(axes) +
                ", motors=" + Arrays.toString(motors) +
                '}';
    }
}
