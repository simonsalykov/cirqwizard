package org.cirqoid.cnc.analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by simon on 21.06.17.
 */
public class DataAnalyzer
{
    public static final int X_STEP  = 1 << 0;
    public static final int X_DIR   = 1 << 1;
    public static final int Y1_STEP = 1 << 2;
    public static final int Y1_DIR  = 1 << 3;
    public static final int Y2_STEP = 1 << 4;
    public static final int Y2_DIR  = 1 << 5;
    public static final int SPI_SCK = 1 << 6;
    public static final int SPI_MISO= 1 << 7;
    public static final int X_CS    = 1 << 8;
    public static final int Y1_CS   = 1 << 9;
    public static final int Y2_CS   = 1 << 10;
    public static final int CYCLE   = 1 << 11;

    static final int STEPPER_COUNT = 3;

    List<StepperAnalyzer> steppers = Arrays.asList(new StepperAnalyzer(X_STEP, X_DIR), new StepperAnalyzer(Y1_STEP, Y1_DIR),
                new StepperAnalyzer(Y2_STEP, Y2_DIR));
    List<AS5311Analyzer> as5311 = Arrays.asList(new AS5311Analyzer(SPI_MISO, SPI_SCK, X_CS, true),
            new AS5311Analyzer(SPI_MISO, SPI_SCK, Y1_CS, false), new AS5311Analyzer(SPI_MISO, SPI_SCK, Y2_CS, false));

    public void analyze(Stream<String> lines)
    {
        FallingEdgeTrigger trigger = new FallingEdgeTrigger(CYCLE);
//        System.out.println("Time, X steps, X position, X step speed, Y1 steps, Y2 steps, X position, Y1 position, Y2 position, Y12 diff");
        // Output format:
        // Time,
        // X steps, X position, X step speed, X AS5311 speed
        // Y1 steps, Y1 position, Y1 step speed, Y1 AS5311 speed
        // Y2 steps, Y2 position, Y2 step speed, Y2 AS5311 speed
        // Y1 - Y1 diff
        double stepperPositions[] = new double[STEPPER_COUNT];
        double asPositions[] = new double[STEPPER_COUNT];
        double prevTime[] = new double[1];
        lines.forEach(l ->
        {
            double time = Double.valueOf(l.substring(0, l.indexOf(',')));
            int bits = Integer.valueOf(l.substring(l.indexOf(',') + 2, l.length()));
            steppers.forEach(s -> s.tick(bits));
            as5311.forEach(a -> a.tick(bits));
            double stepperSpeeds[] = new double[STEPPER_COUNT];
            double asSpeeds[] = new double[STEPPER_COUNT];
            if (trigger.tick(bits))
            {
                for (int i = 0; i < STEPPER_COUNT; i++)
                {
                    stepperSpeeds[i] = (steppers.get(i).getCounter() - stepperPositions[i]) / (time - prevTime[0]);
                    asSpeeds[i] = (as5311.get(i).getPosition() - asPositions[i]) / (time - prevTime[0]);
                    stepperPositions[i] = steppers.get(i).getCounter();
                    asPositions[i] = as5311.get(i).getPosition();
                }
                prevTime[0] = time;

                StringBuffer str = new StringBuffer(String.valueOf(time)).append(", ");
                for (int i = 0; i < STEPPER_COUNT; i++)
                {
                    str.append(steppers.get(i).getCounter()).append(", ").
                            append(as5311.get(i).getPosition()).append(", ").
                            append(stepperSpeeds[i]).append(", ").
                            append(asSpeeds[i]).append(", ");
                }
                str.append((as5311.get(1).getPosition() - as5311.get(2).getPosition()));
                System.out.println(str);
            }
        });
    }

    public static void main(String[] args)
    {
        try
        {
            new DataAnalyzer().analyze(Files.lines(Paths.get(args[0])));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
