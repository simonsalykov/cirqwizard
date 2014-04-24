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

import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Enumeration;


public class SerialInterfaceFactory
{

    public static ArrayList<String> getSerialInterfaces(SerialInterface currentSerialInterface)
    {
        ArrayList<String> serialInterfaceList = new ArrayList<String>();
        Enumeration e = CommPortIdentifier.getPortIdentifiers();
        while (e.hasMoreElements())
        {
            CommPortIdentifier port = (CommPortIdentifier) e.nextElement();
            if (port.getPortType() == CommPortIdentifier.PORT_SERIAL)
                serialInterfaceList.add(port.getName());
        }
        // Linux bug - getPortIdentifiers() can't see ports being in use
        if (currentSerialInterface != null && !serialInterfaceList.contains(currentSerialInterface.getPortName()))
            serialInterfaceList.add(currentSerialInterface.getPortName());

        return serialInterfaceList;
    }

    public static SerialInterface autodetect() throws SerialException
    {
        Enumeration e = CommPortIdentifier.getPortIdentifiers();
        while (e.hasMoreElements())
        {
            CommPortIdentifier port = (CommPortIdentifier) e.nextElement();
            if (port.getName().startsWith("/dev/tty.usbserial") || port.getName().startsWith("/dev/ttyUSB"))
                return new SerialInterfaceImpl(port.getName(), 38400);
        }

        return null;
    }

}
