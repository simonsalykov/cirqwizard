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


public interface SerialInterface
{
    void setBootloaderMode(boolean bootloader) throws SerialException;
    void write(int b) throws IOException;
    void write(byte[] b) throws IOException;
    int readByte() throws IOException;
    void close() throws SerialException;
    void send(Command packet) throws SerialException;
    String getPortName();
    int getPacketId();
    void addListener(Response.Code responseCode, ResponseListener listener);
    void removeListener(Response.Code responseCode, ResponseListener listener);
    Response getCurrentError();
    void resetError();
    int getHardwareVersion();
    void setHardwareVersion(int hardwareVersion);
    int getSoftwareVersion();
    void setSoftwareVersion(int softwareVersion);
}
