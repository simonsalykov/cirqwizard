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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;


public class ToolpathsCache implements Serializable
{
    private HashMap<ToolpathsCacheKey, List<Toolpath>> traces = new HashMap<>();

    public List<Toolpath> getToolpaths(ToolpathsCacheKey cacheKey)
    {
        if (traces.containsKey(cacheKey))
            return traces.get(cacheKey);
        LoggerFactory.getApplicationLogger().log(Level.INFO, "Cache did not contain key: " + cacheKey);
        return null;
    }

    public void setToolpaths(ToolpathsCacheKey cacheKey, List<Toolpath> topLayer)
    {
        this.traces.put(cacheKey, topLayer);
    }
}