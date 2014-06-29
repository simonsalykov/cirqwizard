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


import java.io.IOException;


public interface SerialInterface
{

    public void setBootloaderMode(boolean bootloader) throws SerialException;
    public void write(int b) throws IOException;
    public int readByte() throws IOException;
    public void close() throws SerialException;
    public void send(String str, long timeout) throws SerialException, ExecutionException, InterruptedException;
    public void send(String str, long timeout, StringBuilder response) throws SerialException, ExecutionException, InterruptedException;
    public String getPortName();
}
