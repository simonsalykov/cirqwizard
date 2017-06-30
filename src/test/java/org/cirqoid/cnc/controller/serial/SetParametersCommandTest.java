package org.cirqoid.cnc.controller.serial;

import org.cirqoid.cnc.controller.commands.SetParametersCommand;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by simon on 14.06.17.
 */
public class SetParametersCommandTest
{
    @Test
    public void testSerialization()
    {
        SetParametersCommand packet = new SetParametersCommand();
        packet.setId(12345);
        packet.setAcceleration(50);
        packet.setMaxArcsFeed(1000);
        packet.setMotionCorrectionFrequency(1);
        packet.setMotionAdjustmentTarget(0.1f);
        packet.setAccelerationStartSpeed(4000);
        packet.setMinimumSpeed(2000);
        packet.setMotionCorrecionLagThreshold(0.1f);
        packet.setMotionCorrectionMinLagThreshold(20);
        packet.setPositioningTolerance(5);
        packet.setMotionsJointTolerance(10);

        SetParametersCommand.Axis x = new SetParametersCommand.Axis();
        x.enabled = true;
        x.stepsPerMilli = 200;
        x.lowLimit = -4000;
        x.highLimit = 300000;
        x.seekrate = 3000;
        x.inverted = false;
        x.homingDirection = false;
        x.homingRate = 2000;
        x.seekAcceleration = 100;
        packet.setAxis(0, x);

        SetParametersCommand.Axis y = new SetParametersCommand.Axis();
        y.enabled = false;
        y.stepsPerMilli = 400;
        y.lowLimit = 4000;
        y.highLimit = -500000;
        y.seekrate = 30000;
        y.inverted = true;
        y.homingDirection = true;
        y.homingRate = 5000;
        y.seekAcceleration = 500;
        packet.setAxis(1, y);

        SetParametersCommand.Axis z = new SetParametersCommand.Axis();
        z.enabled = true;
        z.stepsPerMilli = 200;
        z.lowLimit = -4000;
        z.highLimit = 300000;
        z.seekrate = 3000;
        z.inverted = false;
        z.homingDirection = false;
        z.homingRate = 2000;
        z.seekAcceleration = 100;
        packet.setAxis(2, z);

        SetParametersCommand.Axis a = new SetParametersCommand.Axis();
        a.enabled = false;
        a.stepsPerMilli = 400;
        a.lowLimit = 4000;
        a.highLimit = -500000;
        a.seekrate = 30000;
        a.inverted = true;
        a.homingDirection = true;
        a.homingRate = 5000;
        a.seekAcceleration = 500;
        packet.setAxis(3, a);

        SetParametersCommand.Motor m0 = new SetParametersCommand.Motor();
        m0.axis = 0;
        m0.feedbackProvider = SetParametersCommand.FeedbackProvider.OPEN_LOOP;
        m0.invertFeedback = false;
        m0.homingSensor = SetParametersCommand.HomingSensor.NONE;
        m0.invertSensor = false;
        packet.setMotor(0 , m0);

        SetParametersCommand.Motor m1 = new SetParametersCommand.Motor();
        m1.axis = 1;
        m1.feedbackProvider = SetParametersCommand.FeedbackProvider.AS5311;
        m1.invertFeedback = true;
        m1.homingSensor = SetParametersCommand.HomingSensor.GPIO;
        m1.invertSensor = true;
        packet.setMotor(1 , m1);

        SetParametersCommand.Motor m2 = new SetParametersCommand.Motor();
        m2.axis = 2;
        m2.feedbackProvider = SetParametersCommand.FeedbackProvider.AS5311;
        m2.invertFeedback = true;
        m2.homingSensor = SetParametersCommand.HomingSensor.AS5311;
        m2.invertSensor = true;
        packet.setMotor(2 , m2);

        byte[] p = packet.serializePacket();
        assertEquals(302, p.length);
        int ptr = 0;

        assertEquals((byte)0xAA, p[ptr++]);
        assertEquals((byte)0xAA, p[ptr++]);

        // Id
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x30, p[ptr++]);
        assertEquals(0x39, p[ptr++]);

        // Type
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x01, p[ptr++]);

        // Length
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x01, p[ptr++]);
        assertEquals((byte)0x1C, p[ptr++]);

        // acceleration
        assertEquals(0x42, p[ptr++]);
        assertEquals(0x48, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);

        // maxArcsFeed
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x03, p[ptr++]);
        assertEquals((byte)0xE8, p[ptr++]);

        // motionCorrectionDelay
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x01, p[ptr++]);

        // motionAdjustmentTarget
        assertEquals((byte) 0x3D, p[ptr++]);
        assertEquals((byte) 0xCC, p[ptr++]);
        assertEquals((byte) 0xCC, p[ptr++]);
        assertEquals((byte) 0xCD, p[ptr++]);

        // accelerationStartSpeed
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x0F, p[ptr++]);
        assertEquals((byte) 0xA0, p[ptr++]);

        // minimumSpeed
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x07, p[ptr++]);
        assertEquals((byte) 0xD0, p[ptr++]);

        // motionCorrectionLagThreshold
        assertEquals(0x3D, p[ptr++]);
        assertEquals((byte) 0xCC, p[ptr++]);
        assertEquals((byte) 0xCC, p[ptr++]);
        assertEquals((byte) 0xCD, p[ptr++]);

        // motionCorrectionMinLagThreshold
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x14, p[ptr++]);

        // positioningTolerance
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x05, p[ptr++]);

        // motionsJointTolerance
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x0A, p[ptr++]);

        // axes[0].enabled
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x01, p[ptr++]);

        // axes[0].stepsPerMilli
        assertEquals(0x43, p[ptr++]);
        assertEquals(0x48, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);

        // axes[0].lowLimit
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xF0, p[ptr++]);
        assertEquals(0x60, p[ptr++]);

        // axes[0].highLimit
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x04, p[ptr++]);
        assertEquals((byte) 0x93, p[ptr++]);
        assertEquals((byte) 0xE0, p[ptr++]);

        // axes[0].seekRate
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x0B, p[ptr++]);
        assertEquals((byte) 0xB8, p[ptr++]);

        // axes[0].inverted
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);

        // axes[0].homingDirection
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);

        // axes[0].homingRate
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x07, p[ptr++]);
        assertEquals((byte) 0xD0, p[ptr++]);

        // axes[0].seekAcceleration
        assertEquals((byte) 0x42, p[ptr++]);
        assertEquals((byte) 0xC8, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);

        // axes[1].enabled
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);

        // axes[1].stepsPerMilli
        assertEquals(0x43, p[ptr++]);
        assertEquals((byte) 0xC8, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);

        // axes[1].lowLimit
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x0F, p[ptr++]);
        assertEquals((byte) 0xA0, p[ptr++]);

        // axes[1].highLimit
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xF8, p[ptr++]);
        assertEquals((byte) 0x5E, p[ptr++]);
        assertEquals((byte) 0xE0, p[ptr++]);

        // axes[1].seekRate
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x75, p[ptr++]);
        assertEquals((byte) 0x30, p[ptr++]);

        // axes[1].inverted
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x01, p[ptr++]);

        // axes[1].homingDirection
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x01, p[ptr++]);

        // axes[1].homingRate
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x13, p[ptr++]);
        assertEquals((byte) 0x88, p[ptr++]);

        // axes[1].seekAcceleration
        assertEquals((byte) 0x43, p[ptr++]);
        assertEquals((byte) 0xFA, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);

        // axes[2].enabled
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x01, p[ptr++]);

        // axes[2].stepsPerMilli
        assertEquals(0x43, p[ptr++]);
        assertEquals(0x48, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);

        // axes[2].lowLimit
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xF0, p[ptr++]);
        assertEquals(0x60, p[ptr++]);

        // axes[2].highLimit
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x04, p[ptr++]);
        assertEquals((byte) 0x93, p[ptr++]);
        assertEquals((byte) 0xE0, p[ptr++]);

        // axes[2].seekRate
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x0B, p[ptr++]);
        assertEquals((byte) 0xB8, p[ptr++]);

        // axes[2].inverted
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);

        // axes[2].homingDirection
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);

        // axes[2].homingRate
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x07, p[ptr++]);
        assertEquals((byte) 0xD0, p[ptr++]);

        // axes[2].seekAcceleration
        assertEquals((byte) 0x42, p[ptr++]);
        assertEquals((byte) 0xC8, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);

        // axes[3].enabled
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);

        // axes[3].stepsPerMilli
        assertEquals(0x43, p[ptr++]);
        assertEquals((byte) 0xC8, p[ptr++]);
        assertEquals(0x00, p[ptr++]);
        assertEquals(0x00, p[ptr++]);

        // axes[3].lowLimit
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x0F, p[ptr++]);
        assertEquals((byte) 0xA0, p[ptr++]);

        // axes[3].highLimit
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xF8, p[ptr++]);
        assertEquals((byte) 0x5E, p[ptr++]);
        assertEquals((byte) 0xE0, p[ptr++]);

        // axes[3].seekRate
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x75, p[ptr++]);
        assertEquals((byte) 0x30, p[ptr++]);

        // axes[3].inverted
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x01, p[ptr++]);

        // axes[3].homingDirection
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x01, p[ptr++]);

        // axes[3].homingRate
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x13, p[ptr++]);
        assertEquals((byte) 0x88, p[ptr++]);

        // axes[3].seekAcceleration
        assertEquals((byte) 0x43, p[ptr++]);
        assertEquals((byte) 0xFA, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);

        // motors[0].axis
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);

        // motors[0].feedbackProvider
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);

        // motors[0].invertFeedback
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);

        // motors[0].homingSensor
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);

        // motors[0].invertHoming
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);

        // motors[1].axis
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x01, p[ptr++]);

        // motors[1].feedbackProvider
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x01, p[ptr++]);

        // motors[1].invertFeedback
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x01, p[ptr++]);

        // motors[1].homingSensor
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x01, p[ptr++]);

        // motors[1].invertHoming
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x01, p[ptr++]);

        // motors[2].axis
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x02, p[ptr++]);

        // motors[2].feedbackProvider
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x01, p[ptr++]);

        // motors[2].invertFeedback
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x01, p[ptr++]);

        // motors[2].homingSensor
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x02, p[ptr++]);

        // motors[2].invertHoming
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x00, p[ptr++]);
        assertEquals((byte) 0x01, p[ptr++]);

        // motors[3].axis
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);

        // motors[3].feedbackProvider
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);

        // motors[3].invertFeedback
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);

        // motors[3].homingSensor
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);

        // motors[3].invertHoming
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);

        // motors[4].axis
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);

        // motors[4].feedbackProvider
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);

        // motors[4].invertFeedback
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);

        // motors[4].homingSensor
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);

        // motors[4].invertHoming
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);
        assertEquals((byte) 0xFF, p[ptr++]);

        // CRC
        assertEquals((byte) 0xEC, p[ptr++]);
        assertEquals((byte) 0xC7, p[ptr++]);
        assertEquals((byte) 0x51, p[ptr++]);
        assertEquals((byte) 0x02, p[ptr++]);
    }
}
