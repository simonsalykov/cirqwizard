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


import org.cirqwizard.logging.LoggerFactory;
import purejavacomm.*;

import java.io.*;
import java.util.TooManyListenersException;


public class SerialInterfaceImpl implements SerialInterface
{
    private SerialPort port;
    private int baudrate;
    private String portName;
    private InputStream inputStream;
    private OutputStream outputStream;

    public SerialInterfaceImpl(String commPortName, int baudrate) throws SerialException
    {
        try
        {
            this.baudrate = baudrate;
            initUSART(commPortName, baudrate, SerialPort.PARITY_NONE);
        }
        catch (NoSuchPortException e)
        {
            throw new SerialException(e);
        }
        catch (PortInUseException e)
        {
            throw new SerialException(e);
        }
        catch (IOException e)
        {
            throw new SerialException(e);
        }
        catch (UnsupportedCommOperationException e)
        {
            throw new SerialException(e);
        }
        catch (TooManyListenersException e)
        {
            throw new SerialException(e);
        }
    }

    private void initUSART(String name, int baudrate, int parity) throws PortInUseException, IOException, UnsupportedCommOperationException, TooManyListenersException, NoSuchPortException
    {
        if (port != null)
            port.close();

        portName = name;
        CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(portName);
        port = (SerialPort) portId.open(this.getClass().getName(), 5000);
        inputStream = port.getInputStream();
        outputStream = port.getOutputStream();
        port.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, parity);
    }

    public void setBootloaderMode(boolean bootloader) throws SerialException
    {
        close();
        try
        {
            initUSART(portName, bootloader ? 57600 : baudrate, bootloader ? SerialPort.PARITY_EVEN : SerialPort.PARITY_NONE);
            if (bootloader)
                port.enableReceiveTimeout(25000);
        }
        catch (Exception e)
        {
            LoggerFactory.logException("Could not switch to bootloader mode", e);
        }
    }

    public void write(int b) throws IOException
    {
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        outputStream.write(b);
    }

    public int readByte() throws IOException
    {
        return inputStream.read();
    }

    public void close() throws SerialException
    {
        try
        {
            inputStream.close();
            outputStream.close();
            port.close();
        }
        catch (IOException e)
        {
            throw new SerialException(e);
        }
    }

    private void sendCommand(String command, long timeout, StringBuilder response, boolean suppressExceptions) throws SerialException, ExecutionException, InterruptedException
    {
        timeout+= System.currentTimeMillis();
        try
        {
            byte[] b = new byte[1024];
            while (inputStream.available() > 0)
            {
                int i = inputStream.read(b);
                LoggerFactory.getSerialLogger().fine(new String(b, 0, i));
            }

            outputStream.write((command).getBytes());
            outputStream.write('\n');
            LoggerFactory.getSerialLogger().fine(command + "\n");

            int offset = 0;
            while (System.currentTimeMillis() < timeout)
            {
                if (inputStream.available() > 0)
                    offset += inputStream.read(b, offset, b.length - offset);
                String str = new String(b, 0, offset);
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
                Thread.sleep(10);
            }
        }
        catch (IOException e)
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
