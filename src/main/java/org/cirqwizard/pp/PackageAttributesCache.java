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
package org.cirqwizard.pp;

import org.cirqwizard.logging.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.HashMap;

public class PackageAttributesCache
{
    private static PackageAttributesCache instance = new PackageAttributesCache();

    private HashMap<String, PackageAttributes> cache;

    public static PackageAttributesCache getInstance()
    {
        return instance;
    }

    public PackageAttributesCache()
    {
        try
        {
            ObjectInputStream inputStream = new ObjectInputStream(Files.newInputStream(
                    Paths.get(System.getProperty("user.home"), ".cirqwizard", "package attributes.tmp")));
            cache = (HashMap<String, PackageAttributes>) inputStream.readObject();
            inputStream.close();
        }
        catch (NoSuchFileException e) {}
        catch (IOException | ClassNotFoundException e)
        {
            LoggerFactory.logException("Could not load component attribute cache", e);
        }
        if (cache == null)
            cache = new HashMap<>();
    }

    public PackageAttributes getAttributes(String pkg)
    {
        return cache.get(pkg);
    }

    public void saveAttributes(String pkg, Feeder feeder, int row, int pitch)
    {
        cache.put(pkg, new PackageAttributes(feeder, row, pitch));
        try
        {
            ObjectOutputStream outputStream = new ObjectOutputStream(Files.newOutputStream(
                    Paths.get(System.getProperty("user.home"), ".cirqwizard", "package attributes.tmp")));
            outputStream.writeObject(cache);
            outputStream.close();
        }
        catch (IOException e)
        {
            LoggerFactory.logException("Could not update component attribute cache", e);
        }
    }

    public static class PackageAttributes implements Serializable
    {
        private Feeder feeder;
        private int row;
        private int pitch;

        public PackageAttributes()
        {
        }

        public PackageAttributes(Feeder feeder, int row, int pitch)
        {
            this.feeder = feeder;
            this.row = row;
            this.pitch = pitch;
        }

        public Feeder getFeeder()
        {
            return feeder;
        }

        public void setFeeder(Feeder feeder)
        {
            this.feeder = feeder;
        }

        public int getRow()
        {
            return row;
        }

        public void setRow(int row)
        {
            this.row = row;
        }

        public int getPitch()
        {
            return pitch;
        }

        public void setPitch(int pitch)
        {
            this.pitch = pitch;
        }
    }
}
