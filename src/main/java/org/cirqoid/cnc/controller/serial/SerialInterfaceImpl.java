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


import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
import org.cirqoid.cnc.controller.commands.Command;
import org.cirqoid.cnc.controller.commands.PacketParsingException;
import org.cirqoid.cnc.controller.commands.Response;
import org.cirqwizard.logging.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SerialInterfaceImpl implements SerialInterface
{
    private final static int MAX_PACKET_SIZE = 2048;

    private SerialPort port;
    private int baudrate;
    private String portName;
    private int timeout = -1;
    private int packetId = 0;

    private byte buffer[] = new byte[MAX_PACKET_SIZE];
    private int bufferPointer = 0;

    private int lastAcknowledgedPacket = -1;
    private Response currentError;
    private boolean isShuttingDown = false;

    private HashMap<Response.Code, List<ResponseListener>> listenersMap = new HashMap<>();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private Logger logger;

    private int hardwareVersion;
    private int softwareVersion;

    public SerialInterfaceImpl(String commPortName, int baudrate) throws SerialException
    {
        this.logger = LoggerFactory.getSerialLogger();
        this.baudrate = baudrate;
        try
        {
            initUSART(commPortName, false);
            scheduler.schedule(() ->
            {
                try
                {
                    CirqoidInitializer.initDevice(this);
                }
                catch (SerialException e)
                {
                    e.printStackTrace();
                }
            }, 1, TimeUnit.SECONDS);
        }
        catch (SerialPortException e)
        {
            throw new SerialException(e);
        }
    }

    private Response parseBuffer()
    {
        int i = 0;
        // TODO: check for double AA
        while (buffer[i] != (byte)0xAA && i < bufferPointer)
            i++;
        if (i > 0)
        {
            logger.log(Level.INFO, "Skipping " + i + " bytes...");
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < i; j++)
                sb.append(String.format("%02x ", buffer[j]));
            logger.log(Level.INFO, sb.toString());
            System.arraycopy(buffer, i, buffer, 0, bufferPointer - i);
            bufferPointer -= i;
        }
        if (bufferPointer < 14)
            return null;
        ByteBuffer b = ByteBuffer.wrap(buffer, 10, 4);
        int expectedLength = b.getInt() + 2 + 4 + 4 + 4 + 4;
        if (expectedLength > MAX_PACKET_SIZE)
        {
            bufferPointer = 0;
            return null;
        }
        if (bufferPointer < expectedLength)
            return null;

        byte[] packet = new byte[expectedLength - 2];
        System.arraycopy(buffer, 2, packet, 0, expectedLength - 2);
        try
        {
            Response parsed = Response.parsePacket(packet);
            System.arraycopy(buffer, expectedLength, buffer, 0, bufferPointer - expectedLength);
            bufferPointer -= expectedLength;
            return parsed;
        }
        catch (PacketParsingException e)
        {
            System.out.println("Parsing failed: " + e.getMessage());
            System.arraycopy(buffer, bufferPointer, buffer, 0, buffer.length - bufferPointer);
            bufferPointer = 0;
        }
        return null;
    }

    private void initUSART(String name, boolean bootloader) throws SerialPortException
    {
        if (port != null)
        {
            try
            {
                port.removeEventListener();
            }
            catch (SerialPortException e) {}
            port.closePort();
        }

        portName = name;
        port = new SerialPort(portName);
        port.openPort();
        port.setParams(bootloader ? 57600 : baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                bootloader ? SerialPort.PARITY_EVEN : SerialPort.PARITY_NONE);
        isShuttingDown = false;
        if (!bootloader)
        {
            port.setEventsMask(SerialPort.MASK_RXCHAR);
            port.addEventListener(serialPortEvent ->
            {
                try
                {
                    byte[] b = port.readBytes();
                    if (bufferPointer + b.length < MAX_PACKET_SIZE)
                    {
                        System.arraycopy(b, 0, buffer, bufferPointer, b.length);
                        bufferPointer += b.length;
                    }
                    Response p;
                    while ((p = parseBuffer()) != null)
                        processParsedPacket(p);
                }
                catch (SerialPortException e)
                {
                    e.printStackTrace();
                }
            });
        }
    }

    private void processParsedPacket(Response response)
    {
        if (logger != null)
            logger.log(Level.FINE, "<<< " + response);
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

    public void setBootloaderMode(boolean bootloader) throws SerialException
    {
        try
        {
            initUSART(portName, bootloader);
            timeout = bootloader ? 30000 : -1;
            if (!bootloader)
                CirqoidInitializer.initDevice(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void write(int b) throws IOException
    {
        try
        {
            port.writeByte((byte) b);
        }
        catch (SerialPortException e)
        {
            throw new IOException(e);
        }
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        try
        {
            port.writeBytes(b);
        }
        catch (SerialPortException e)
        {
            throw new IOException(e);
        }
    }

    public int readByte() throws IOException
    {
        byte[] b;
        try
        {
            if (timeout > 0)
                b = port.readBytes(1, timeout);
            else
                b = port.readBytes(1);
            return b[0];
        }
        catch (SerialPortException | SerialPortTimeoutException e)
        {
            throw new IOException(e);
        }
    }

    public void close() throws SerialException
    {
        try
        {
            isShuttingDown = true;
            port.closePort();
            scheduler.shutdown();
        }
        catch (SerialPortException e)
        {
            throw new SerialException(e);
        }
    }

    public void send(Command packet) throws SerialException
    {
        if (currentError != null)
            throw new SerialException("Controller error is not reset");
        try
        {
            if (logger != null)
                logger.log(Level.FINE, ">>> " + packet);
            port.writeBytes(packet.serializePacket());
            long timeout = System.currentTimeMillis() + 20000;
            while (!isShuttingDown && System.currentTimeMillis() < timeout && lastAcknowledgedPacket < packet.getId())
                Thread.sleep(10);
            if (lastAcknowledgedPacket < packet.getId())
                throw new SerialException("Command timed out: " + packet.getId());
        }
        catch (SerialPortException e)
        {
            throw new SerialException(e);
        }
        catch (InterruptedException e) {}
    }

    @Override
    public int getPacketId()
    {
        return packetId++;
    }

    public String getPortName()
    {
        return portName;
    }

    @Override
    public void addListener(Response.Code responseCode, ResponseListener listener)
    {
        List<ResponseListener> list = listenersMap.computeIfAbsent(responseCode, k -> new ArrayList<>());
        list.add(listener);
    }

    @Override
    public void removeListener(Response.Code responseCode, ResponseListener listener)
    {
        if (listenersMap.get(responseCode) != null)
            listenersMap.get(responseCode).remove(listener);
    }

    @Override
    public Response getCurrentError()
    {
        return currentError;
    }

    @Override
    public void resetError()
    {
        currentError = null;
    }

    public int getHardwareVersion()
    {
        return hardwareVersion;
    }

    public void setHardwareVersion(int hardwareVersion)
    {
        this.hardwareVersion = hardwareVersion;
    }

    public int getSoftwareVersion()
    {
        return softwareVersion;
    }

    public void setSoftwareVersion(int softwareVersion)
    {
        this.softwareVersion = softwareVersion;
    }
}
