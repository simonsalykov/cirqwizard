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
import org.cirqoid.cnc.controller.commands.Response;
import org.cirqwizard.logging.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SerialInterfaceImpl extends SerialInterface
{
    private SerialPort port;
    private int baudrate;
    private String portName;
    private int timeout = -1;

    private SerialBuffer buffer = new SerialBuffer();

    private boolean isShuttingDown = false;

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
                    buffer.addBytes(port.readBytes());
                    Response p;
                    while ((p = buffer.parseBuffer()) != null)
                        processParsedPacket(p);
                }
                catch (SerialPortException e)
                {
                    LoggerFactory.logException("Exception caught while receiving data", e);
                    e.printStackTrace();
                }
            });
        }
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
            LoggerFactory.logException("Error setting bootloader mode", e);
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

    public String getPortName()
    {
        return portName;
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
