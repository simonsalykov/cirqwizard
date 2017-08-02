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

package org.cirqoid.cnc.controller.serial;


import org.cirqoid.cnc.controller.commands.Command;
import org.cirqoid.cnc.controller.commands.Response;
import org.cirqwizard.logging.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class SerialInterface
{
    private int packetId = 0;
    protected int lastAcknowledgedPacket = -1;
    protected Response currentError;

    private HashMap<Response.Code, List<ResponseListener>> listenersMap = new HashMap<>();
    protected ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public int getPacketId()
    {
        return packetId++;
    }

    protected void processParsedPacket(Response response)
    {
        LoggerFactory.getSerialLogger().log(Level.FINE, "<<< " + response);
        if (response.getCode() == Response.Code.OK)
            lastAcknowledgedPacket = response.getPacketId();
        else if (response.getCode().isExecutionError())
            currentError = response;
        if (listenersMap.get(response.getCode()) != null)
            listenersMap.get(response.getCode()).forEach(l ->
                    scheduler.schedule(() -> l.responseReceived(response), 0, TimeUnit.SECONDS));
        if (listenersMap.get(null) != null)
            listenersMap.get(null).forEach(l ->
                    scheduler.schedule(() -> l.responseReceived(response), 0, TimeUnit.SECONDS));
    }

    public void addListener(Response.Code responseCode, ResponseListener listener)
    {
        List<ResponseListener> list = listenersMap.computeIfAbsent(responseCode, k -> new ArrayList<>());
        list.add(listener);
    }

    public void removeListener(Response.Code responseCode, ResponseListener listener)
    {
        if (listenersMap.get(responseCode) != null)
            listenersMap.get(responseCode).remove(listener);
    }

    public abstract void setBootloaderMode(boolean bootloader) throws SerialException;
    public abstract void write(int b) throws IOException;
    public abstract void write(byte[] b) throws IOException;
    public abstract int readByte() throws IOException;
    public abstract void close() throws SerialException;
    public abstract void send(Command packet) throws SerialException;
    public abstract String getPortName();
    public abstract Response getCurrentError();
    public abstract void resetError();
    public abstract int getHardwareVersion();
    public abstract void setHardwareVersion(int hardwareVersion);
    public abstract int getSoftwareVersion();
    public abstract void setSoftwareVersion(int softwareVersion);
}
