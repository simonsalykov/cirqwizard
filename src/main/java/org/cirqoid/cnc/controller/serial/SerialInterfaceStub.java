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

import java.io.IOException;
import java.util.logging.Logger;


public class SerialInterfaceStub implements SerialInterface
{
    @Override
    public void setBootloaderMode(boolean bootloader) throws SerialException
    {
    }

    @Override
    public void write(int b) throws IOException
    {
    }

    @Override
    public void write(byte[] b) throws IOException
    {
    }

    @Override
    public int readByte() throws IOException
    {
        return -1;
    }

    @Override
    public void close() throws SerialException
    {
    }

    @Override
    public void send(Command packet) throws SerialException
    {
    }

    @Override
    public int getPacketId()
    {
        return 0;
    }

    @Override
    public String getPortName()
    {
        return "";
    }

    @Override
    public void addListener(Response.Code responseCode, ResponseListener listener)
    {

    }

    @Override
    public void removeListener(Response.Code responseCode, ResponseListener listener)
    {

    }

    @Override
    public Response getCurrentError()
    {
        return null;
    }

    @Override
    public void resetError()
    {

    }

    @Override
    public void setLogger(Logger logger)
    {

    }

    @Override
    public int getHardwareVersion()
    {
        return 0;
    }

    @Override
    public void setHardwareVersion(int hardwareVersion)
    {

    }

    @Override
    public int getSoftwareVersion()
    {
        return 0;
    }

    @Override
    public void setSoftwareVersion(int softwareVersion)
    {

    }
}
