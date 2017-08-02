/*
This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 3 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cirqwizard.serial;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.cirqoid.cnc.controller.commands.Command;
import org.cirqoid.cnc.controller.commands.Response;
import org.cirqoid.cnc.controller.commands.StatusResponse;
import org.cirqoid.cnc.controller.interpreter.Interpreter;
import org.cirqoid.cnc.controller.interpreter.ParsingException;
import org.cirqoid.cnc.controller.serial.SerialException;
import org.cirqoid.cnc.controller.serial.SerialInterface;
import org.cirqwizard.fx.MainApplication;
import org.cirqwizard.fx.util.ExceptionAlert;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.post.PostProcessorFactory;
import org.cirqwizard.post.Postprocessor;

import java.util.List;
import java.util.logging.Level;


public class CNCController
{
    private final static long PROGRAM_INTERRUPTION_TIMEOUT = 100000;
    private final static long COMMAND_TIMEOUT = 4000;

    public enum Status
    {
        NOT_CONNECTED, ERROR, NOT_HOMED, OK, RUNNING
    }

    private Interpreter interpreter;
    private SerialInterface serial;
    private SimpleObjectProperty<Status> status = new SimpleObjectProperty<>();
    private long lastStatusUpdate = System.currentTimeMillis();

    public CNCController(SerialInterface serial)
    {
        this.interpreter = new Interpreter();
        this.serial = serial;
        serial.addListener(null, l ->
        {
            if (l.getCode().isExecutionError())
            {
                try
                {
                    LoggerFactory.getApplicationLogger().log(Level.SEVERE, "Command execution failed. Command id:  " + l.getPacketId() + ", code: " + l.getCode());
                    Platform.runLater(() ->
                    {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Something went wrong while executing the command. " +
                                "Command #" + l.getPacketId() + " failed with code " + l.getCode() +
                                ". The most sensible thing to do now would be to close the program and start over again. Sorry about that.", ButtonType.OK);
                        alert.setHeaderText("Oops! That's embarrassing!");
                        alert.setTitle("Controller error");
                        alert.showAndWait();
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        serial.addListener(Response.Code.STATUS, response ->
        {
            int runLevel = ((StatusResponse) response).getRunLevel();
            switch (runLevel)
            {
                case 0: status.set(Status.ERROR); break;
                case 1: status.set(Status.NOT_HOMED); break;
                case 2: status.set(Status.OK); break;
                case 3: status.set(Status.RUNNING); break;
            }
            this.lastStatusUpdate = System.currentTimeMillis();
        });
        Thread t = new Thread(() ->
        {
            while (true)
            {
                if (lastStatusUpdate < System.currentTimeMillis() - 1000)
                    status.set(Status.NOT_CONNECTED);
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {}
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public Status getStatus()
    {
        return status.get();
    }

    public SimpleObjectProperty<Status> statusProperty()
    {
        return status;
    }

    public void send(String str, long timeout)
    {
        try
        {
            // TODO: this needs to be handled better
            if (serial.getCurrentError() != null && serial.getCurrentError().getCode() == Response.Code.NOT_HOMED)
                serial.resetError();
            List<Command> commands = interpreter.interpretBlocks(str);
            for (Command c : commands)
                send(c);
        }
        catch (SerialException | ParsingException e)
        {
            LoggerFactory.logException("Communication with controller failed: ", e);
            ExceptionAlert alert = new ExceptionAlert("Oops! That's embarrassing!", "Communication error",
                    "Something went wrong while communicating with the controller. " +
                    "The most sensible thing to do now would be to close the program and start over again. Sorry about that.", e);
            alert.showAndWait();
        }
    }

    public void send(Command c) throws SerialException
    {
        c.setId(serial.getPacketId());
        serial.send(c);
    }

    private void send(String str)
    {
        send(str, COMMAND_TIMEOUT);
    }

    public Interpreter getInterpreter()
    {
        return interpreter;
    }

    public void home(Integer yDiff)
    {
        StringBuilder str = new StringBuilder();
        PostProcessorFactory.getPostProcessor().home(str, yDiff);
        send(str.toString());
    }

    public void moveTo(Integer x, Integer y)
    {
        moveTo(x, y, 0);
    }

    public void moveTo(Integer x, Integer y, Integer z)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.selectMachineWS(str);
        if (z != null)
            post.rapid(str, null, null, z);
        post.rapid(str, x, y, null);
        send(str.toString());
    }

    public void moveZ(int z)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.selectMachineWS(str);
        post.rapid(str, null, null, z);
        send(str.toString());
    }

    public void moveHeadAway(int y)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.selectMachineWS(str);
        post.rapid(str, null, null, 0);
        post.rapid(str, null, y, null);
        send(str.toString());
    }

    public void testCut(int x, int y, int z, int clearance, int safetyHeight, int workingHeight, int xyFeed, int zFeed,
                        int spindleSpeed, boolean horizontalDirection)
    {
        StringBuilder str  = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.setupG54(str, x, y, z);
        post.selectWCS(str);
        post.rapid(str, 0, 0, clearance);
        post.spindleOn(str, spindleSpeed);
        post.rapid(str, null, null, safetyHeight);
        post.linearInterpolation(str, 0, 0, workingHeight, zFeed);
        post.linearInterpolation(str, horizontalDirection ? 5000 : 0, horizontalDirection ? 0 : 5000, workingHeight, xyFeed);
        post.rapid(str, null, null, clearance);
        post.spindleOff(str);
        send(str.toString());
    }

    public void interruptProgram()
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.selectMachineWS(str);
        post.rapid(str, null, null, 0);
        post.spindleOff(str);
        post.syringeOff(str);
        send(str.toString(), PROGRAM_INTERRUPTION_TIMEOUT);
    }

    public void dispensePaste(int duration)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.syringeOn(str);
        post.pause(str, duration);
        post.syringeOff(str);
        send(str.toString());
    }

    public void testDispensing(int x, int y, int z, int prefeedPause, int length, int feed)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.selectMachineWS(str);
        post.rapid(str, null, null, 0);
        post.rapid(str, x, y, null);
        post.rapid(str, null, null, z);
        post.syringeOn(str);
        post.pause(str, prefeedPause);
        post.linearInterpolation(str, x + length, y, z, feed);
        post.syringeOff(str);
        post.rapid(str, null, null, 0);
        send(str.toString());
    }

    public void rotatePP(int angle, int feed)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.rotatePP(str, angle, feed);
        send(str.toString());
    }

    public void rotatePP(int angle)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.rotatePP(str, angle);
        send(str.toString());
    }

    public void pickup(int pickupHeight, int moveHeight)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.rapid(str, null, null, pickupHeight);
        post.vacuumOn(str);
        post.pause(str, 500);
        post.rapid(str, null, null, moveHeight);
        send(str.toString());
    }

    public void place(int placementHeight, int moveHeight)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.rapid(str, null, null, placementHeight);
        post.vacuumOff(str);
        post.pause(str, 100);
        post.rapid(str, null, null, moveHeight);
        send(str.toString());
    }

    public void vacuumOff()
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.vacuumOff(str);
        send(str.toString());
    }

    public String getFirmwareVersion()
    {
        int v = serial.getSoftwareVersion();
        return "Cirqoid firmware v" + ((v >> 16) & 0xFF) + "." + ((v >> 8) & 0xFF) + "." + (v & 0xFF);
    }
}
