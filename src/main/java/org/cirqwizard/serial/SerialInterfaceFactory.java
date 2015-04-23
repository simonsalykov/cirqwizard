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


import jssc.SerialPortList;

import java.util.Arrays;
import java.util.List;


public class SerialInterfaceFactory
{

    public static List<String> getSerialInterfaces(SerialInterface currentSerialInterface)
    {
        return Arrays.asList(SerialPortList.getPortNames());
    }

    public static SerialInterface autodetect() throws SerialException
    {
        for (String port : SerialPortList.getPortNames())
        {
            if (port.startsWith("/dev/tty.usbserial") || port.startsWith("/dev/ttyUSB"))
                return new SerialInterfaceImpl(port, 38400);
        }

        return null;
    }

}
