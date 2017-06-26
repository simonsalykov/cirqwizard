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

package org.cirqwizard.generation.toolpath;


import org.cirqwizard.logging.LoggerFactory;

import java.io.*;
import java.util.logging.Level;


public class ToolpathsPersistor
{
    public static ToolpathsCache loadFromFile(String filename) throws ToolpathPersistingException
    {
        if(!new File(filename).exists())
        {
            LoggerFactory.getApplicationLogger().log(Level.FINE, "Cache file was not found");
            return null;
        }

        ToolpathsCache toolpathsCache;
        try
        {
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream oin = new ObjectInputStream(fis);
            toolpathsCache = (ToolpathsCache) oin.readObject();
        }
        catch(IOException | ClassNotFoundException e)
        {
            throw new ToolpathPersistingException("Error loading toolpaths from file", e);
        }
        return toolpathsCache;
    }

    public static void saveToFile(ToolpathsCache cache, String filename) throws ToolpathPersistingException
    {
        if (cache != null)
        {
            try
            {
                FileOutputStream fos = new FileOutputStream(filename);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(cache);
                oos.flush();
                oos.close();
            }
            catch(IOException e)
            {
                throw new ToolpathPersistingException("Error saving toolpaths to file", e);
            }
        }
    }
}
