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

package org.cirqwizard.toolpath;


import java.io.*;
import java.util.List;


public class ToolpathLoader
{
    private static ToolpathContainer toolpathContainer;
    private static String filename;

    public static void setFile(String path)
    {
        filename = path;

        try
        {
            FileInputStream fis = new FileInputStream(path + ".tmp");
            ObjectInputStream oin = new ObjectInputStream(fis);
            toolpathContainer = (ToolpathContainer) oin.readObject();

            System.out.println("Loaded ToolpathContainer from file");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (toolpathContainer == null)
        {
            toolpathContainer = new ToolpathContainer();
            System.out.println("Corrupted or missing file. Generating new ToolpathContainer");
        }
    }

    public static void saveToFile()
    {
        if (toolpathContainer != null)
        {
            try
            {
                FileOutputStream fos = new FileOutputStream(filename + ".tmp");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(toolpathContainer);
                oos.flush();
                oos.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static boolean hasValidData(int toolDiameter, long lastModified)
    {
        return toolpathContainer.toolDiameter == toolDiameter && toolpathContainer.lastModified == lastModified;
    }


    public static List<Toolpath> getTopLayer()
    {
        return toolpathContainer.topLayer;
    }

    public static List<Toolpath> getBottomLayer()
    {
        return toolpathContainer.bottomLayer;
    }

    public static void setLastModified(long lastModified)
    {
        toolpathContainer.lastModified = lastModified;
    }

    public static void setToolDiameter(int toolDiameter)
    {
        toolpathContainer.toolDiameter = toolDiameter;
    }

    public static void setTopLayer(List<Toolpath> topLayer)
    {
        toolpathContainer.topLayer = topLayer;
    }

    public static void setBottomLayer(List<Toolpath> bottomLayer)
    {
        toolpathContainer.bottomLayer = bottomLayer;
    }
}

class ToolpathContainer implements Serializable
{
    public int toolDiameter;
    public long lastModified;
    public List<Toolpath> bottomLayer;
    public List<Toolpath> topLayer;
}