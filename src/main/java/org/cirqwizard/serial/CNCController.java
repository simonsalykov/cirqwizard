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

import org.cirqwizard.fx.MainApplication;
import org.cirqwizard.fx.util.ExceptionAlert;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.post.PostProcessorFactory;
import org.cirqwizard.post.Postprocessor;


public class CNCController
{
    private final static long PROGRAM_INTERRUPTION_TIMEOUT = 100000;
    private final static long COMMAND_TIMEOUT = 4000;

    private SerialInterface serial;
    private MainApplication mainApplication;

    public CNCController(SerialInterface serial, MainApplication mainApplication)
    {
        this.serial = serial;
        this.mainApplication = mainApplication;
    }

    private void send(String str, long timeout)
    {
        send(str, timeout, null, false);
    }

    private void send(String str, long timeout, StringBuilder response, boolean suppressExceptions)
    {
        try
        {
            serial.send(str, timeout, response, suppressExceptions);
        }
        catch (SerialException | ExecutionException e)
        {
            LoggerFactory.logException("Communication with controller failed: ", e);
            ExceptionAlert alert = new ExceptionAlert("Oops! That's embarrassing!", "Communication error",
                    "Something went wrong while communicating with the controller. " +
                    "The most sensible thing to do now would be to close the program and start over again. Sorry about that.", e);
            alert.showAndWait();
        }
        catch (InterruptedException e)
        {
            LoggerFactory.logException("InterruptedException in CNCController.send(): ", e);
        }
    }

    private void send(String str)
    {
        send(str, COMMAND_TIMEOUT);
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
        StringBuilder str = new StringBuilder();
        Postprocessor post = PostProcessorFactory.getPostProcessor();
        post.getFirmwareVersion(str);
        StringBuilder response = new StringBuilder();
        send(str.toString(), 500, response, true);
        String firmware = response.toString();
        if (firmware.indexOf('\n') > 0)
            firmware = firmware.substring(0, firmware.indexOf('\n'));
        return firmware;
    }
}
