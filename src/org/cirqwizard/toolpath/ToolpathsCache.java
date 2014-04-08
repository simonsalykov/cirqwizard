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


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;


public class ToolpathsCache implements Serializable
{
    private long lastModified;
    private HashMap<ToolpathsCacheKey, List<Toolpath>> traces = new HashMap<>();

    public boolean hasValidData(long lastModified)
    {
        return this.lastModified == lastModified;
    }

    public List<Toolpath> getToolpaths(ToolpathsCacheKey cacheKey)
    {
        if (traces.containsKey(cacheKey))
            return traces.get(cacheKey);
        return null;
    }

    public void setLastModified(long lastModified)
    {
        this.lastModified = lastModified;
    }

    public void setToolpaths(ToolpathsCacheKey cacheKey, List<Toolpath> topLayer)
    {
        this.traces.put(cacheKey, topLayer);
    }
}