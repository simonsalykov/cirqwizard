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

package org.cirqwizard.fx;

public class ScreenGroup extends ScreenController
{
    private String name;
    private boolean visible = true;

    public ScreenGroup(String name)
    {
        super();
        this.name = name;
    }

    @Override
    protected String getName()
    {
        return name;
    }

    public void select()
    {
        for (ScreenController c : getChildren())
        {
            if (c.isMandatory() && c.isEnabled())
            {
                c.select();
                return;
            }
        }
    }

    public boolean isVisible()
    {
        return visible;
    }

    public ScreenGroup setVisible(boolean visible)
    {
        this.visible = visible;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScreenGroup that = (ScreenGroup) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (getParent() != null ? !getParent().equals(that.getParent()) : that.getParent() != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return name != null ? name.hashCode() : 0;
    }
}
