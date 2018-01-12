package org.cirqwizard.test.geom;

import org.cirqwizard.geom.Line;
import org.cirqwizard.geom.Point;
import org.cirqwizard.geom.Polygon;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PolygonTest
{
    @Test
    public void testPointBelongsToPolygon()
    {
        Polygon polygon = new Polygon();
        polygon.addVertice(new Point(0, 0));
        polygon.addVertice(new Point(0, 10));
        polygon.addVertice(new Point(10, 10));
        polygon.addVertice(new Point(10, 0));

        Line insideLine = new Line(new Point(2, 2), new Point(4, 4));
        Line insideLineReverted = new Line(new Point(2, 2), new Point(4, 4));
        Line topLine = new Line(new Point(0, 10), new Point(10, 10));
        Line bottomLine = new Line(new Point(0, 0), new Point(10, 0));

        assertTrue(polygon.pointBelongsToPolygon(new Point(10, 10)));
        assertTrue(polygon.pointBelongsToPolygon(new Point(0, 10)));
        assertTrue(polygon.lineBelongsToPolygon(insideLine));
        assertTrue(polygon.lineBelongsToPolygon(insideLineReverted));
        assertTrue(polygon.lineBelongsToPolygon(topLine));
        assertTrue(polygon.lineBelongsToPolygon(bottomLine));
    }

    @Test
    public void testPointBelongsToPolygon2()
    {
        Polygon polygon = new Polygon();
        polygon.addVertice(new Point(10, 10));
        polygon.addVertice(new Point(15, 5));
        polygon.addVertice(new Point(10, 0));
        polygon.addVertice(new Point(0, 0));

        Line insideLine = new Line(new Point(0, 0), new Point(10, 10));
        Line insideLine2 = new Line(new Point(10, 0), new Point(0, 0));
        assertTrue(polygon.lineBelongsToPolygon(insideLine));
        assertTrue(polygon.lineBelongsToPolygon(insideLine2));
    }

    @Test
    public void testPointBelongsToPolygon3()
    {
        Polygon polygon = new Polygon();
        polygon.addVertice(new Point(8611, 66636));
        polygon.addVertice(new Point(8710, 66636));
        polygon.addVertice(new Point(8709, 66428));
        polygon.addVertice(new Point(8619, 66336));
        polygon.addVertice(new Point(8610, 66336));

        // point has the same Y as one of the vertices
        Point point = new Point(8513, 66428);
        assertFalse(polygon.pointBelongsToPolygon(point));
    }

    @Test
    public void testLineBelongsToPolygon3()
    {
        Polygon polygon = new Polygon();
        polygon.addVertice(new Point(8611, 66636));
        polygon.addVertice(new Point(8710, 66636));
        polygon.addVertice(new Point(8709, 66428));
        polygon.addVertice(new Point(8619, 66336));
        polygon.addVertice(new Point(8610, 66336));

        Line insideLine = new Line(new Point(8710, 66636), new Point(8709, 66428));

        double angle = 3.13;
        Point offset = new Point((int) (Math.cos(angle) * 50), (int) (Math.sin(angle) * 50));

        for (int i = 0; i < 2; ++i)
        {
            insideLine.setFrom(insideLine.getFrom().add(offset));
            insideLine.setTo(insideLine.getTo().add(offset));
            assertTrue(polygon.lineBelongsToPolygon(insideLine));
        }

        insideLine.setFrom(insideLine.getFrom().add(offset));
        insideLine.setTo(insideLine.getTo().add(offset));
        assertFalse(polygon.lineBelongsToPolygon(insideLine));
    }

    @Test
    public void testLineBelongsToPolygon4()
    {
        Polygon polygon = new Polygon();
        polygon.addVertice(new Point(28684, 75012));
        polygon.addVertice(new Point(29131, 75011));
        polygon.addVertice(new Point(29130, 74164));
        polygon.addVertice(new Point(28683, 74165));

        boolean belongs = polygon.pointBelongsToPolygon(new Point(27856, 74164));
        Assert.assertFalse(belongs);
    }
}

