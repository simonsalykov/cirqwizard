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
package org.cirqwizard.gerber.appertures.macro;

import org.cirqwizard.geom.Point;
import org.cirqwizard.geom.Polygon;
import org.cirqwizard.geom.Rect;
import org.cirqwizard.gerber.appertures.Aperture;

import java.util.ArrayList;
import java.util.List;

public class ApertureMacro extends Aperture
{
    private ArrayList<MacroPrimitive> primitives = new ArrayList<>();

    public void addPrimitive(MacroPrimitive primitive)
    {
        primitives.add(primitive);
    }

    public List<MacroPrimitive> getPrimitives()
    {
        return primitives;
    }


    @Override
    public Aperture rotate(boolean clockwise)
    {
        ApertureMacro clone = new ApertureMacro();
        for (MacroPrimitive p : primitives)
        {
            p = p.clone();
            p.setRotationAngle(p.getRotationAngle() + 90 * (clockwise ? -1 : 1));
            clone.addPrimitive(p);
        }
        return clone;
    }

    @Override
    public boolean isVisible()
    {
        return true;
    }

    @Override
    public int getWidth()
    {
        return 0;
    }

    @Override
    public int getHeight()
    {
        return 0;
    }

    @Override
    public int getCircumRadius()
    {
        return 2000;
    }

    public Polygon getMinInsideRectangular()
    {
        if (primitives.size() == 1 && primitives.get(0) instanceof MacroOutline)
        {
            MacroOutline macroOutline = (MacroOutline) primitives.get(0);
            return new Polygon(macroOutline.getTranslatedPoints());
        }

        List<Rect> rects = findMinRectsInPrimitives();

        if (rects.size() == 0)
            return null;

        // find the rect with the biggest square
        Rect biggestRect = rects.stream().max((r1, r2) ->
                Integer.compare(r1.getWidth() * r2.getHeight(), r2.getWidth() * r2.getHeight())).get();

        // connect the biggest rect with the smaller ones, check their common square
        for(final Rect smallRect : rects)
        {
            if (smallRect == biggestRect)
                continue;

            int mergedLeftX, mergedRightX, mergedTopY, mergedBottomY;

            // 4 cases, left, right, top, bottom
            if (smallRect.getLeftX() > biggestRect.getLeftX() && smallRect.getRightX() > biggestRect.getRightX())
            {
                // right attachment
                mergedLeftX = biggestRect.getLeftX();
                mergedRightX = smallRect.getRightX();
                mergedTopY = Math.min(biggestRect.getTopY(), smallRect.getTopY());
                mergedBottomY = Math.max(biggestRect.getBottomY(), smallRect.getBottomY());;
            }
            else if (smallRect.getLeftX() < biggestRect.getLeftX() && smallRect.getRightX() < biggestRect.getRightX())
            {
                // left attachment
                mergedLeftX = smallRect.getLeftX();
                mergedRightX = biggestRect.getRightX();
                mergedTopY = Math.min(biggestRect.getTopY(), smallRect.getTopY());
                mergedBottomY = Math.max(biggestRect.getBottomY(), smallRect.getBottomY());
            }
            else if (smallRect.getTopY() > biggestRect.getTopY() && smallRect.getBottomY() > biggestRect.getBottomY())
            {
                // top attachment
                mergedLeftX = Math.max(biggestRect.getLeftX(), smallRect.getLeftX());
                mergedRightX = Math.min(biggestRect.getRightX(), smallRect.getRightX());
                mergedTopY = smallRect.getTopY();
                mergedBottomY = biggestRect.getBottomY();
            }
            else
            {
                // bottom attachment
                mergedLeftX = Math.max(biggestRect.getLeftX(), smallRect.getRightX());
                mergedRightX = Math.max(biggestRect.getRightX(), smallRect.getRightX());
                mergedTopY = biggestRect.getTopY();
                mergedBottomY = smallRect.getBottomY();
            }

            int mergedSquare = Math.abs(mergedRightX - mergedLeftX) * Math.abs(mergedBottomY - mergedTopY);
            int currentSquare = biggestRect.getWidth() * biggestRect.getHeight();
            if (mergedSquare > currentSquare)
            {
                Point mergedCenter = new Point((mergedLeftX + mergedRightX) / 2, (mergedTopY + mergedBottomY) / 2);
                biggestRect = new Rect(mergedCenter, Math.abs(mergedRightX - mergedLeftX), Math.abs(mergedTopY - mergedBottomY));
            }
        }

        Polygon polygon = new Polygon();
        polygon.addVertice(new Point(biggestRect.getLeftX(), biggestRect.getTopY()));
        polygon.addVertice(new Point(biggestRect.getRightX(), biggestRect.getTopY()));
        polygon.addVertice(new Point(biggestRect.getRightX(), biggestRect.getBottomY()));
        polygon.addVertice(new Point(biggestRect.getLeftX(), biggestRect.getBottomY()));
        return polygon;
    }

    private List<Rect> findMinRectsInPrimitives()
    {
        List<Rect> rects = new ArrayList(primitives.size());

        for (MacroPrimitive macroPrimitive : primitives)
        {
            if (macroPrimitive instanceof MacroCenterLine)
            {
                MacroCenterLine macroCenterLine = (MacroCenterLine) macroPrimitive;
                int width = macroCenterLine.getRotationAngle() == 90 ||  macroCenterLine.getRotationAngle() == 270 ?
                         macroCenterLine.getHeight() : macroCenterLine.getWidth();

                int height = macroCenterLine.getRotationAngle() == 90 ||  macroCenterLine.getRotationAngle() == 270 ?
                        macroCenterLine.getWidth() : macroCenterLine.getHeight();

                rects.add(new Rect(macroCenterLine.translate(macroCenterLine.getCenter()),
                    width,
                    height));
            }
            else if (macroPrimitive instanceof MacroCircle)
            {
                MacroCircle macroCircle = (MacroCircle) macroPrimitive;
                int diameter = macroCircle.getDiameter();
                // pythagorean theorem
                int width = (int) Math.sqrt(diameter * diameter / 2);
                rects.add(new Rect(macroCircle.getCenter(), width, width));
            }
            else
            {
                System.out.println("Given macro primitive is not supported at the moment: " + macroPrimitive);
            }
        }

        return rects;
    }
}
