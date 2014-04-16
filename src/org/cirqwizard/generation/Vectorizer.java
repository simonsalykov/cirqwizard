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

package org.cirqwizard.generation;

import org.cirqwizard.geom.*;
import org.cirqwizard.settings.Settings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Vectorizer
{
    private static final int INITIAL_SAMPLE_COUNT = (int)(0.2 * Settings.RESOLUTION);                    // Amount of samples to process before trying to decide which curve it is
    private static final int SAMPLE_COUNT = (int)(0.1 * Settings.RESOLUTION);                            // Amount of last processed points to hold for deviation calculation
    private static final double ANGULAR_THRESHOLD = Math.toRadians(2);      // Threshold of angular difference which results in a new segment start

    private static final double DEVIATION_MARGIN = 3;                       // Margin, by which arc's standard deviation should beat segment's
    // standard deviation in order to choose arc over segment
    private static final double SEGMENT_FOLLOWING_ARC_DEVIATION_MARGIN = 1; // As above, in case segment in question follows an arc
    private static final int    SEGMENT_FOLLOWING_ARC_DEVIATION_MARGIN_LIMIT = (int)(INITIAL_SAMPLE_COUNT * 1.5);
    // Limitation of previous constant scope (in samples from segment's start)

    private static final double LOW_UNCERTAINTY_THRESHOLD = 9.8;   // Arcs with uncertainty lower than that are processed as arcs
    private static final double HIGH_UNCERTAINTY_THRESHOLD = 10;    // Arcs with uncertainty higher than that are processed as segments


    private byte[] windowData;
    private int width;
    private int height;
    private List<Circle> knownCircles;

    private Point current;
    private Line currentSegment;
    private MatchedArc matchedArc;
    private ArrayList<Curve> result = new ArrayList<>();
    private LinkedList<Point> segmentPoints = new LinkedList<>();

    public Vectorizer(byte[] windowData, int width, int height, List<Circle> knownCircles, int x, int y)
    {
        this.windowData = windowData;
        this.width = width;
        this.height = height;
        this.knownCircles = knownCircles;
        current = new Point(x, y);
        currentSegment = new Line(current, current);
    }

    public List<Curve> trace()
    {
        int segmentCounter = 0;
        LinkedList<Point> lastPoints = new LinkedList<>();
        segmentPoints = new LinkedList<>();
        double angle = 0;
        matchedArc = null;

        do
        {
            lastPoints.addLast(current);
            int sampleCount = segmentCounter <= INITIAL_SAMPLE_COUNT ? INITIAL_SAMPLE_COUNT : SAMPLE_COUNT;
            while (lastPoints.size() > sampleCount)
                lastPoints.removeFirst();
            segmentPoints.addLast(current);

            segmentCounter++;
            currentSegment.setTo(current);
            boolean previousToolpathIsArc = result.size() > 0 && result.get(result.size() - 1) instanceof Arc;

            boolean restart = false;

            if (segmentCounter == INITIAL_SAMPLE_COUNT)
            {
                angle = calculateAngle(currentSegment.getFrom(), current);
                double segmentDeviation = calculateSegmentDeviation(segmentPoints);
                matchedArc = fitArc(segmentPoints, segmentDeviation, 0);
            }
            else if (segmentCounter > INITIAL_SAMPLE_COUNT)
            {
                if (matchedArc != null && matchedArc.getUncertainty() >= LOW_UNCERTAINTY_THRESHOLD)
                {
                    double segmentDeviation = calculateSegmentDeviation(segmentPoints);
                    double margin = DEVIATION_MARGIN;
                    if (segmentCounter < SEGMENT_FOLLOWING_ARC_DEVIATION_MARGIN_LIMIT && previousToolpathIsArc)
                        margin = SEGMENT_FOLLOWING_ARC_DEVIATION_MARGIN;
                    matchedArc = fitArc(segmentPoints, segmentDeviation, margin);
                }

                if (matchedArc != null && matchedArc.getUncertainty() < LOW_UNCERTAINTY_THRESHOLD)
                    restart = calculateSegmentDeviation(lastPoints) < calculateArcDeviation(lastPoints, matchedArc.getCircle().getCenter(), matchedArc.getCircle().getRadius());
                else if (matchedArc == null)
                    restart = Math.abs(calculateAngle(lastPoints.getFirst(), lastPoints.getLast()) - angle) > ANGULAR_THRESHOLD;
            }

            if (restart)
            {
                Curve curve = getCurve(calculateAngle(lastPoints.get(0), current), lastPoints.get(0));
                if (previousToolpathIsArc)
                    curve = attemptMerge(curve);

                result.add(curve);
                currentSegment = new Line(current, current);
                segmentPoints.clear();
                matchedArc = null;
                segmentCounter = 0;
            }

            windowData[current.getX() + current.getY() * width] = 0;
        }
        while (calculateNextPoint());
        if (segmentCounter > 10)
            result.add(getCurve(calculateAngle(lastPoints.get(0), current), lastPoints.get(0)));

        return result;
    }

    private boolean calculateNextPoint()
    {
        for (Direction d : directions)
        {
            Point p = current.add(d.getVector());
            if (p.getX() < 0 || p.getX() >= width || p.getY() < 0 || p.getY() >= height)
                continue;
            if (windowData[p.getX() + p.getY() * width] != 0)
            {
                current = p;
                return true;
            }
        }

        return false;
    }

    private Curve attemptMerge(Curve curve)
    {
        Arc prevArc = (Arc) result.get(result.size() - 1);
        boolean merge = false;

        if (curve instanceof Line)
        {
            double arcDeviation = calculateArcDeviation(segmentPoints, prevArc.getCenter(), prevArc.getRadius());
            double ratio = arcDeviation / calculateSegmentDeviation(segmentPoints);
            if (ratio < 1 || (segmentPoints.size() < 1.5 * INITIAL_SAMPLE_COUNT && ratio < 5))
                merge = true;
        }
        if (curve instanceof Arc)
        {
            Arc arc = (Arc) curve;
            int centersDistanceThreshold = (int)(0.4 * arc.getRadius());
            if (prevArc.getCenter().distanceTo(arc.getCenter()) <= centersDistanceThreshold && prevArc.getRadius() == arc.getRadius())
                merge = true;
            else
            {
                double prevArcDeviation = calculateArcDeviation(segmentPoints, prevArc.getCenter(), prevArc.getRadius());
                if (prevArcDeviation / calculateArcDeviation(segmentPoints, matchedArc.getCircle().getCenter(), matchedArc.getCircle().getRadius()) < 5)
                    merge = true;
            }
        }

        if (merge)
        {
            result.remove(result.size() - 1);
            curve = new Arc(prevArc.getFrom(), curve.getTo(), prevArc.getCenter(), prevArc.getRadius(), prevArc.isClockwise());
        }

        return curve;
    }

    private Curve getCurve(double heading, Point headingStartPoint)
    {
        if (matchedArc == null || matchedArc.getUncertainty() > HIGH_UNCERTAINTY_THRESHOLD)
            return currentSegment;

        double centerAngle = calculateAngle(headingStartPoint, matchedArc.getCircle().getCenter());
        double headingCenterAngle = heading - centerAngle;
        if (headingCenterAngle < -Math.PI)
            headingCenterAngle += Math.PI * 2;
        if (headingCenterAngle > Math.PI)
            headingCenterAngle -= Math.PI * 2;

        boolean clockwise = headingCenterAngle > 0;
        return new Arc(currentSegment.getFrom(), currentSegment.getTo(), matchedArc.getCircle().getCenter(), matchedArc.getCircle().getRadius(), clockwise);
    }

    private double calculateAngle(Point start, Point end)
    {
        return Math.atan2(end.getY() - start.getY(), end.getX() - start.getX());
    }

    private double calculateSegmentDeviation(LinkedList<Point> points)
    {
        double deviation = 0;
        Point start = points.getFirst();
        Point end = points.getLast();
        for (Point p : points)
        {
            double d = (p.getY() - start.getY()) * (end.getX() - p.getX()) - (end.getY() - start.getY()) * (p.getX() - start.getX());
            deviation += d * d;
        }
        return Math.sqrt(deviation);
    }

    private double calculateArcDeviation(LinkedList<Point> points, Point center, int radius)
    {
        double deviation = 0;
        for (Point p : points)
        {
            double d = (double)((p.getX() - center.getX()) * (p.getX() - center.getX()) + (p.getY() - center.getY()) * (p.getY() - center.getY())) - radius * radius;
            deviation += d * d;
        }
        return Math.sqrt(deviation);
    }

    private MatchedArc fitArc(LinkedList<Point> points, double segmentDeviation, double margin)
    {
        double minDeviation = Double.MAX_VALUE;
        Circle bestFit = null;

        for (Circle circle : knownCircles)
        {
            double deviation = calculateArcDeviation(points, circle.getCenter(), circle.getRadius());
            if (deviation < minDeviation)
            {
                minDeviation = deviation;
                bestFit = circle;
            }
        }

        double uncertainty = minDeviation / segmentDeviation;
        if (uncertainty > HIGH_UNCERTAINTY_THRESHOLD)
            return null;
        return new MatchedArc(bestFit, uncertainty);
    }

    private static Direction[] directions = {Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST,
            Direction.NORTH, Direction.NORTH_EAST};

    private static enum Direction
    {
        NORTH(new Point(0, 1)),
        NORTH_EAST(new Point(1, 1)),
        EAST(new Point(1, 0)),
        SOUTH_EAST(new Point(1, -1)),
        SOUTH(new Point(0, -1)),
        SOUTH_WEST(new Point(-1, -1)),
        WEST(new Point(-1, 0)),
        NORTH_WEST(new Point(-1, 1));

        private Point vector;

        Direction(Point vector)
        {
            this.vector = vector;
        }

        public Point getVector()
        {
            return vector;
        }
    }


}
