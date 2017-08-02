package org.cirqoid.cnc.controller.interpreter;

import org.cirqoid.cnc.controller.commands.Command;
import org.cirqoid.cnc.controller.commands.LinearInterpolationCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simon on 22.06.17.
 */
public class JointsOptimizer
{
    private List<Command> queue = new ArrayList<>();

    public List<Command> enqueue(List<Command> commands)
    {
        queue.addAll(commands);
        return optimize();
    }

    private boolean isOptimizable(Command command)
    {
        return command instanceof LinearInterpolationCommand;
    }

    private List<Command> optimize()
    {
        List<Command> result = new ArrayList<>();
        result.addAll(removeNonOptimizable());

        LinearInterpolationCommand firstCommand = (LinearInterpolationCommand) queue.get(0);
        double firstCommandAngle = Math.hypot(firstCommand.getTarget()[0] - firstCommand.getStart()[0],
                firstCommand.getTarget()[1] - firstCommand.getStart()[1]);


        return result;
    }

    private List<Command> removeNonOptimizable()
    {
        List<Command> nonOptimizable = new ArrayList<>();
        while (!queue.isEmpty() && !isOptimizable(queue.get(0)))
            nonOptimizable.add(queue.remove(0));
        return nonOptimizable;
    }
}
