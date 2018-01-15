package org.cirqoid.cnc.controller.interpreter;

import org.cirqoid.cnc.controller.commands.CircularInterpolationCommand;
import org.cirqoid.cnc.controller.commands.Command;
import org.cirqoid.cnc.controller.commands.LinearInterpolationCommand;
import org.cirqoid.cnc.controller.commands.RapidMotionCommand;
import org.cirqoid.cnc.controller.interpreter.commands.CommandFactory;
import org.cirqoid.cnc.controller.settings.ApplicationConstants;
import org.cirqoid.cnc.controller.settings.HardwareSettings;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by simon on 16.06.17.
 */
public class Interpreter
{
    private Context context = new Context();

    public Context getContext()
    {
        return context;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }

    public List<Command> interpretBlocks(String blocks) throws ParsingException
    {
        LineNumberReader reader = new LineNumberReader(new StringReader(blocks));
        List<Command> result = new ArrayList<>();
        String str;
        try
        {
            while ((str = reader.readLine()) != null)
            {
                result.addAll(interpretBlock(str));
            }
        }
        catch (IOException e) {}

        return result;
    }

    public List<Command> interpretBlock(String block) throws ParsingException
    {
        int[] startPosition = new int[ApplicationConstants.MAX_AXES_COUNT];
        System.arraycopy(context.getCurrentPosition(), 0, startPosition, 0, startPosition.length);
        Context contextBackup = (Context) context.clone();
        List<Command> executionList = new ArrayList<>();
        List<Token> tokens = GCodeParser.parseBlock(block);
        boolean interpolationCommanded = false;

        try
        {
            while (!tokens.isEmpty())
            {
                Token t = tokens.remove(0);
                if (ParsingUtil.isCommandLetter(t.getLetter()))
                {
                    org.cirqoid.cnc.controller.interpreter.commands.Command command = CommandFactory.findCommand(t);
                    if (command == null)
                        throw new ParsingException("Unknown command: " + t.getLetter() + t.getIntegerParameter());
                    List<Command> packets = command.act(context, tokens);
                    if (packets != null)
                        executionList.addAll(packets);
                }
                else if (t.getLetter() == 'F')
                    context.setFeed(ParsingUtil.toInteger(t.getDecimalParameter()));
                else if (t.getLetter() == 'S')
                    context.setSpeed(t.getIntegerParameter());
                else
                {
                    int axis = ParsingUtil.getAxisNumber(t.getLetter());
                    if (ParsingUtil.isCenterOffsetLetter(t.getLetter()))
                        context.setArcCenterOffset(axis, ParsingUtil.toInteger(t.getDecimalParameter()));
                    else if (ParsingUtil.isAxisLetter(t.getLetter()))
                    {
                        interpolationCommanded = true;
                        context.setCurrentPosition(axis,
                                adjustCoordinateForOffset(axis, ParsingUtil.toInteger(t.getDecimalParameter())));
                    }
                }
            }
            if (interpolationCommanded)
            {
                if (!TravelRangeValidator.validate(context.getCurrentPosition(), HardwareSettings.getCirqoidSettings()))
                    throw new ParsingException("Axis overtravel");
                    executionList.add(createInterpolationCommand(startPosition));
            }
        }
        catch (ParsingException e)
        {
            context = contextBackup;
            throw new ParsingException(e.getMessage(), block);
        }

        return executionList;
    }

    private int adjustCoordinateForOffset(int axis, int coordinate)
    {
        return coordinate + context.getOffset(context.getCurrentWcs(), axis);
    }

    private Command createInterpolationCommand(int[] originalPosition) throws ParsingException
    {
        int[] target = new int[ApplicationConstants.MAX_AXES_COUNT];
        System.arraycopy(context.getCurrentPosition(), 0, target, 0, target.length);
        switch (context.getCurrentInterpolationMode())
        {
            case RAPID:
                return new RapidMotionCommand(target);
            case LINEAR:
                if (context.getFeed() == null)
                    throw new ParsingException("Feed is not selected");
                LinearInterpolationCommand lic = createLinearInterpolationCommand(originalPosition, target);
                return lic;
            case CIRCUAR_CW:
            case CIRCULAR_CCW:
                if (context.getFeed() == null)
                    throw new ParsingException("Feed is not selected");
                int[] axes = null;
                switch (context.getPlane())
                {
                    case XY: axes = new int[] {0, 1, 2}; break;
                    case YZ: axes = new int[] {1, 2, 0}; break;
                    case XZ: axes = new int[] {0, 2, 1}; break;
                }
                int[] centerCoordinates = new int[] {originalPosition[axes[0]] + context.getArcCenterOffset(axes[0]),
                    originalPosition[axes[1]] + context.getArcCenterOffset(axes[1])};
                int radius = (int) Math.hypot(context.getCurrentPosition(axes[0]) - centerCoordinates[0],
                        context.getCurrentPosition(axes[1]) - centerCoordinates[1]);
                return new CircularInterpolationCommand(target, radius, centerCoordinates, context.getPlane(),
                        context.getCurrentInterpolationMode() == Context.InterpolationMode.CIRCUAR_CW, context.getFeed());
            case NOT_SELECTED:
                throw new ParsingException("Interpolation mode is not selected");
        }
        return null;
    }

    private LinearInterpolationCommand createLinearInterpolationCommand(int[] originalPosition, int[] target)
    {
        LinearInterpolationCommand lic = new LinearInterpolationCommand(originalPosition, target, context.getFeed());
        if (originalPosition[2] == target[2] &&
                Math.hypot(target[0] - originalPosition[0], target[1] - originalPosition[1]) < 200)
            lic.setMaxExitSpeed(2000);
        return lic;
    }


}
