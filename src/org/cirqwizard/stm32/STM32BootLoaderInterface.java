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

package org.cirqwizard.stm32;

import org.cirqwizard.serial.ExecutionException;
import org.cirqwizard.serial.SerialException;
import org.cirqwizard.serial.SerialInterface;

import java.io.IOException;


public class STM32BootLoaderInterface
{
    private static final int CMD_INIT = 0x7F;
    private static final int CMD_GET_VERSION = 0x01;
    private static final int CMD_EXTENDED_ERASE = 0x44;
    private static final int CMD_GET_ID = 0x02;
    private static final int CMD_WRITE_FLASH = 0x31;
    private static final int CMD_WRITE_UNPROTECT = 0x73;
    private static final int CMD_READ_FLASH = 0x11;
    private static final int ACK = 0x79;
    private static final int CMD_GO = 0x21;
    private static final int NACK = 0x1F;

    SerialInterface serialInterface;

    public STM32BootLoaderInterface(SerialInterface serialInterface)
    {
        this.serialInterface = serialInterface;
    }

    public void reset() throws SerialException, ExecutionException
    {
        try
        {
            serialInterface.send("$$$reset\r\n", 2000);
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            // Interrupted. That's fine
        }
    }

    public void initBootloader() throws BootloaderException
    {
        try
        {
            serialInterface.setBootloaderMode(true);
            serialInterface.write(CMD_INIT);
            int response = serialInterface.readByte();
            if (response != ACK && response != NACK)
                throw new BootloaderException("Unexpected response received: " + response);
        }
        catch (Exception e)
        {
            throw new BootloaderException(e);
        }
    }

    public void switchOffBootloader() throws SerialException
    {
        try
        {
            serialInterface.setBootloaderMode(false);
        }
        catch (SerialException e)
        {
            throw new SerialException(e);
        }
    }

    private void sendCommand(int command) throws BootloaderException
    {
        try
        {
            serialInterface.write(command);
            serialInterface.write(~command);
        }
        catch (IOException e)
        {
            throw new BootloaderException(e);
        }
    }

    private void sendPacket(byte[] packet) throws BootloaderException
    {
        try
        {
            int checksum = 0;
            for (int i = 0; i < packet.length; i++)
            {
                checksum ^= packet[i];
                serialInterface.write(packet[i]);
            }
            serialInterface.write(checksum);
        }
        catch (IOException e)
        {
            throw new BootloaderException(e);
        }
    }

    private void sendAddress(int address) throws BootloaderException
    {
        byte[] b = new byte[4];
        b[0] = (byte)(address >> 24);
        b[1] = (byte)((address >> 16) & 0xFF);
        b[2] = (byte)((address >> 8) & 0xFF);
        b[3] = (byte)(address & 0xFF);
        sendPacket(b);
    }

    public int getChipId() throws BootloaderException
    {
        try
        {
            sendCommand(CMD_GET_ID);
            if (serialInterface.readByte() != ACK)
                throw new BootloaderException("Command not acknowledged");
            if (serialInterface.readByte() != 1)
                throw new BootloaderException("Unexpected response");
            int id = (serialInterface.readByte() << 8) | serialInterface.readByte();
            if (serialInterface.readByte() != ACK)
                throw new BootloaderException("Command not acknowledged");
            return id;
        }
        catch (IOException e)
        {
            throw new BootloaderException(e);
        }
    }

    public int getVersion() throws BootloaderException
    {
        try
        {
            sendCommand(CMD_GET_VERSION);
            if (serialInterface.readByte() != ACK)
                throw new BootloaderException("Command not acknowledged");
            int version = serialInterface.readByte();
            serialInterface.readByte();
            serialInterface.readByte();
            if (serialInterface.readByte() != ACK)
                throw new BootloaderException("Command not acknowledged");
            return version;
        }
        catch (IOException e)
        {
            throw new BootloaderException(e);
        }
    }

    public void unprotectWrite() throws BootloaderException
    {
        try
        {
            sendCommand(CMD_WRITE_UNPROTECT);
            if (serialInterface.readByte() != ACK)
                throw new BootloaderException("Command not acknowledged");
            if (serialInterface.readByte() != ACK)
                throw new BootloaderException("Command not acknowledged");
        }
        catch (IOException e)
        {
            throw new BootloaderException(e);
        }
    }

    public void eraseFlash() throws BootloaderException
    {
        try
        {
            sendCommand(CMD_EXTENDED_ERASE);
            if (serialInterface.readByte() != ACK)
                throw new BootloaderException("Command not acknowledged");
            serialInterface.write(0xFF);
            serialInterface.write(0xFF);
            serialInterface.write(0x00);
            if (serialInterface.readByte() != ACK)
                throw new BootloaderException("Command not acknowledged");
        }
        catch (IOException e)
        {
            throw new BootloaderException(e);
        }
    }

    public void restartController() throws BootloaderException
    {
        try
        {
            sendCommand(CMD_GO);
            if (serialInterface.readByte() != ACK)
                throw new BootloaderException("Command not acknowledged");
            sendAddress(0x08000000);
            if (serialInterface.readByte() != ACK)
                throw new BootloaderException("Command not acknowledged");
        }
        catch (IOException e)
        {
            throw new BootloaderException(e);
        }
    }

    public void writeSector(byte[] bin, int offset) throws BootloaderException
    {
        try
        {
            sendCommand(CMD_WRITE_FLASH);
            if (serialInterface.readByte() != ACK)
                throw new BootloaderException("Command not acknowledged");
            sendAddress(0x08000000 + offset);
            if (serialInterface.readByte() != ACK)
                throw new BootloaderException("Command not acknowledged");
            int bytesToWrite = Math.min(bin.length - offset, 256);
            byte[] b = new byte[bytesToWrite + 1];
            b[0] = (byte)(bytesToWrite - 1);
            System.arraycopy(bin, offset, b, 1, bytesToWrite);
            sendPacket(b);
            if (serialInterface.readByte() != ACK)
                throw new BootloaderException("Command not acknowledged");
        }
        catch (IOException e)
        {
            throw new BootloaderException(e);
        }
    }

}
