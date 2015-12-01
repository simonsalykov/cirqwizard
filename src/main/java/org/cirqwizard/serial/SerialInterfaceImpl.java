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


import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
import org.cirqwizard.logging.LoggerFactory;

import java.io.*;


public class SerialInterfaceImpl implements SerialInterface
{
    private SerialPort port;
    private int baudrate;
    private String portName;
    private int timeout = -1;

    public SerialInterfaceImpl(String commPortName, int baudrate) throws SerialException
    {
        this.baudrate = baudrate;
        try
        {
            initUSART(commPortName, baudrate, SerialPort.PARITY_NONE);
        }
        catch (SerialPortException e)
        {
            throw new SerialException(e);
        }
    }

    private void initUSART(String name, int baudrate, int parity) throws SerialPortException
    {
        if (port != null)
            port.closePort();

        portName = name;
        port = new SerialPort(portName);
        port.openPort();
        port.setParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, parity);
    }

    public void setBootloaderMode(boolean bootloader) throws SerialException
    {
        try
        {
            initUSART(portName, bootloader ? 57600 : baudrate, bootloader ? SerialPort.PARITY_EVEN : SerialPort.PARITY_NONE);
            timeout = bootloader ? 25000 : -1;
        }
        catch (Exception e)
        {
            LoggerFactory.logException("Could not switch to bootloader mode", e);
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
            port.closePort();
        }
        catch (SerialPortException e)
        {
            throw new SerialException(e);
        }
    }

    private void sendCommand(String command, long timeout, StringBuilder response, boolean suppressExceptions) throws SerialException, ExecutionException, InterruptedException
    {
        timeout += System.currentTimeMillis();

        try
        {
            port.readBytes();
            port.writeString(command);
            port.writeString("\n");
            LoggerFactory.getSerialLogger().fine(command + "\n");

            StringBuffer buffer = new StringBuffer();
            while (System.currentTimeMillis() < timeout)
            {
                String s = port.readString();
                if (s == null)
                {
                    Thread.sleep(10);
                    continue;
                }
                buffer.append(s);
                String str = buffer.toString();
                if (str.indexOf('\n') >= 0)
                {
                    if (response != null)
                        response.append(str);
                    LoggerFactory.getSerialLogger().fine(str);
                    if (str.startsWith("ok"))
                        return;
                    if (str.startsWith("nack") && !suppressExceptions)
                        throw new SerialException("Negative acknowledgement received: " + str);
                    if (str.startsWith("error") && !suppressExceptions)
                        throw new ExecutionException("Execution error received from controller: " + str);
                    if (!suppressExceptions)
                        throw new SerialException("Unexpected confirmation received from controller: " + str);
                    return;
                }
            }
        }
        catch (SerialPortException e)
        {
            throw new SerialException(e);
        }
        throw new SerialException("Timeout.");
    }

    public void send(String str, long timeout) throws SerialException, ExecutionException, InterruptedException
    {
        send(str, timeout, null, false);
    }

    public void send(String str, long timeout, StringBuilder response, boolean suppressExceptions) throws SerialException, ExecutionException, InterruptedException
    {
        try
        {
            LineNumberReader reader = new LineNumberReader(new StringReader(str));
            String line;
            while ((line = reader.readLine()) != null)
            {
                try
                {
                    sendCommand(line, timeout, response, suppressExceptions);
                }
                catch (SerialException e)
                {
                    LoggerFactory.logException("Communication error detected, resending command", e);
                    sendCommand(line, timeout, response, suppressExceptions);
                }
            }
        }
        catch (IOException e)
        {
            // It's not going to happen
            LoggerFactory.logException("Improbable exception", e);
        }

    }

    public String getPortName()
    {
        return portName;
    }
}
