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


public class ComponentId
{
    private String packaging;
    private  String value;

    public ComponentId(String packaging, String value)
    {
        this.packaging = packaging;
        this.value = value;
    }

    public String getPackaging()
    {
        return packaging;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComponentId that = (ComponentId) o;

        if (packaging != null ? !packaging.equals(that.packaging) : that.packaging != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = packaging != null ? packaging.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "ComponentId{" +
                "packaging='" + packaging + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
