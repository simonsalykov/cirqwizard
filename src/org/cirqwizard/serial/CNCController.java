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
import org.cirqwizard.fx.MainApplication;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.post.PostProcessorFactory;
import org.cirqwizard.post.Postprocessor;


public class CNCController
{
    private final static long PROGRAM_INTERRUPTION_TIMEOUT = 100000;
    private final static long COMMAND_TIMEOUT = 2000;

    private SerialInterface serial;
    private MainApplication mainApplication;

    public CNCController(SerialInterface serial, MainApplication mainApplication)
    {
        this.serial = serial;
        this.mainApplication = mainApplication;
    }

    private void send(String str, long timeout)
    {
        try
        {
            serial.send(str, timeout);
        }
        catch (SerialException e)
        {
            LoggerFactory.logException("Communication with controller failed: ", e);
            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    mainApplication.showInfoDialog("Oops! That's embarrassing!", "Something went wrong while communicating with the controller. " +
                            "The most sensible thing to do now would be to close the program and start over again. Sorry about that.");
                }
            });
        }
        catch (ExecutionException e)
        {
            LoggerFactory.logException("Controller returned an error", e);
            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    mainApplication.showInfoDialog("Oops! That's embarrassing!", "Something went wrong and controller returned and error. " +
                            "The most sensible thing to do now would be to close the program and start over again. Sorry about that.");
                }
            });
        }
        catch (InterruptedException e)
        {
            LoggerFactory.logException("InterruptedException in CNCController.send(): ", e);
        }
    }

    private String sendAndReadResponse(String req, long timeout)
    {
        try
        {
            return serial.sendAndReadResponse(req, timeout);
        }
        catch (SerialException e)
        {
            LoggerFactory.logException("Communication with controller failed: ", e);
        }
        catch (ExecutionException e)
        {
            LoggerFactory.logException("Controller returned an error", e);
        }
        return null;
    }

    private void send(String str)
    {
        send(str, COMMAND_TIMEOUT);
    }

    public void home(String yDiff)
    {
        StringBuilder str = new StringBuilder();
        PostProcessorFactory.getPostProcessor().home(str, new RealNumber(yDiff));
        send(str.toString());
    }

    public void moveTo(String x, String y)
    {
        moveTo(x, y, "0");
    }

    public void moveTo(String x, String y, String z)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.selectMachineWS(str);
        RealNumber _x = x == null ? null : new RealNumber(x);
        RealNumber _y = y == null ? null : new RealNumber(y);
        if (z != null)
            post.rapid(str, null, null, new RealNumber(z));
        post.rapid(str, _x, _y, null);
        send(str.toString());
    }

    public void moveZ(String z)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.selectMachineWS(str);
        post.rapid(str, null, null, new RealNumber(z));
        send(str.toString());
    }

    public void moveHeadAway(String y)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.selectMachineWS(str);
        post.rapid(str, null, null, new RealNumber(0));
        post.rapid(str, null, new RealNumber(y), null);
        send(str.toString());
    }

    public void testCut(String x, String y, String z, String clearance, String safetyHeight, String workingHeight, String xyFeed, String zFeed,
                        String spindleSpeed, boolean horizontalDirection)
    {
        StringBuilder str  = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.setupG54(str, new RealNumber(x), new RealNumber(y), new RealNumber(z));
        post.selectWCS(str);
        post.rapid(str, new RealNumber(0), new RealNumber(0), new RealNumber(clearance));
        post.spindleOn(str, spindleSpeed);
        post.rapid(str, null, null, new RealNumber(safetyHeight));
        post.linearInterpolation(str, new RealNumber(0), new RealNumber(0), new RealNumber(workingHeight), new RealNumber(zFeed));
        post.linearInterpolation(str, new RealNumber(horizontalDirection ? 5 : 0), new RealNumber(horizontalDirection ? 0 : 5), new RealNumber(workingHeight), new RealNumber(xyFeed));
        post.rapid(str, null, null, new RealNumber(clearance));
        post.spindleOff(str);
        send(str.toString());
    }

    public void interruptProgram()
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.selectMachineWS(str);
        post.rapid(str, null, null, new RealNumber(0));
        post.spindleOff(str);
        post.syringeOff(str);
        send(str.toString(), PROGRAM_INTERRUPTION_TIMEOUT);
    }

    public void dispensePaste(String duration)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.syringeOn(str);
        post.pause(str, new RealNumber(duration));
        post.syringeOff(str);
        send(str.toString());
    }

    public void testDispensing(String x, String y, String z, String prefeedPause, String length, String feed)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.selectMachineWS(str);
        post.rapid(str, null, null, new RealNumber(0));
        post.rapid(str, new RealNumber(x), new RealNumber(y), null);
        post.rapid(str, null, null, new RealNumber(z));
        post.syringeOn(str);
        post.pause(str, new RealNumber(prefeedPause));
        post.linearInterpolation(str, new RealNumber(x).add(new RealNumber(length)), new RealNumber(y),
                new RealNumber(z), new RealNumber(feed));
        post.syringeOff(str);
        post.rapid(str, null, null, new RealNumber(0));
        send(str.toString());
    }

    public void rotatePP(String angle, String feed)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.rotatePP(str, new RealNumber(angle), new RealNumber(feed));
        send(str.toString());
    }

    public void rotatePP(String angle)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.rotatePP(str, new RealNumber(angle));
        send(str.toString());
    }

    public void pickup(String pickupHeight, String moveHeight)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.rapid(str, null, null, new RealNumber(pickupHeight));
        post.vacuumOn(str);
        post.pause(str, new RealNumber("0.5"));
        post.rapid(str, null, null, new RealNumber(moveHeight));
        send(str.toString());
    }

    public void place(String placementHeight, String moveHeight)
    {
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.rapid(str, null, null, new RealNumber(placementHeight));
        post.vacuumOff(str);
        post.pause(str, new RealNumber("0.1"));
        post.rapid(str, null, null, new RealNumber(moveHeight));
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
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.getFirmwareVersion(str);
        return sendAndReadResponse(str.toString(), 500);
    }
}
