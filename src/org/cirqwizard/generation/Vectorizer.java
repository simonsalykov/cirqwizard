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
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.ApplicationConstants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class Vectorizer
{
    private static final int INITIAL_SAMPLE_COUNT = (int)(0.15 * ApplicationConstants.RESOLUTION);                    // Amount of samples to process before trying to decide which curve it is
    private static final int SAMPLE_COUNT = (int)(0.1 * ApplicationConstants.RESOLUTION);                            // Amount of last processed points to hold for deviation calculation
    private static final double ANGULAR_THRESHOLD = Math.toRadians(3);      // Threshold of angular difference which results in a new segment start
    private static final int MAX_ARC_DEVIATION = 10; // Tolerated deviation of the distance from arc's center to its points from the radius

    private static final double LOW_UNCERTAINTY_THRESHOLD = 0.6;   // Arcs with uncertainty lower than that are processed as arcs
    private static final double HIGH_UNCERTAINTY_THRESHOLD = 10.0;    // Arcs with uncertainty higher than that are processed as segments


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
            int sampleCount = segmentCounter <= INITIAL_SAMPLE_COUNT ? INITIAL_SAMPLE_COUNT : (matchedArc == null ? SAMPLE_COUNT : (int)((double) matchedArc.getCircle().getRadius() * 2 * (Math.PI / 15)));
            while (lastPoints.size() > sampleCount)
                lastPoints.removeFirst();
            segmentPoints.addLast(current);

            segmentCounter++;
            currentSegment.setTo(current);

            boolean restart = false;

            if (segmentCounter == INITIAL_SAMPLE_COUNT)
            {
                angle = calculateAngle(currentSegment.getFrom(), current);
                matchedArc = fitArc(segmentPoints, calculateSegmentDeviation(segmentPoints));
            }
            else if (segmentCounter > INITIAL_SAMPLE_COUNT)
            {
                if (matchedArc != null && matchedArc.getUncertainty() >= LOW_UNCERTAINTY_THRESHOLD)
                    matchedArc = fitArc(segmentPoints, calculateSegmentDeviation(segmentPoints));

                if (matchedArc != null && matchedArc.getUncertainty() < LOW_UNCERTAINTY_THRESHOLD)
                    restart = Math.abs(matchedArc.getCircle().getRadius() - matchedArc.getCircle().getCenter().distanceTo(current)) >= MAX_ARC_DEVIATION;
                else if (matchedArc == null || segmentCounter >  (double)matchedArc.getCircle().getRadius() * 2 * (Math.PI / 15))
                    restart = Math.abs(calculateAngle(lastPoints.getFirst(), lastPoints.getLast()) - angle) > ANGULAR_THRESHOLD;
            }

            if (restart)
            {

                Curve curve = getCurve(calculateAngle(lastPoints.get(0), current), lastPoints.get(0));
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
        int r1 = (int)matchedArc.getCircle().getCenter().distanceTo(currentSegment.getFrom());
        int r2 = (int)matchedArc.getCircle().getCenter().distanceTo(currentSegment.getTo());
        if (Math.abs(r1 - matchedArc.getCircle().getRadius()) > MAX_ARC_DEVIATION + 1|| Math.abs(r2 - matchedArc.getCircle().getRadius()) > MAX_ARC_DEVIATION + 1)
            LoggerFactory.getApplicationLogger().log(Level.WARNING, "Arc geometry violation: " + matchedArc + " / " + currentSegment + " / " + r1 + " | " + r2);
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

    private MatchedArc fitArc(LinkedList<Point> points, double segmentDeviation)
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

        if (bestFit == null)
            return null;
        if (Math.abs(bestFit.getRadius() - bestFit.getCenter().distanceTo(points.getFirst())) >= MAX_ARC_DEVIATION ||
                Math.abs(bestFit.getRadius() - bestFit.getCenter().distanceTo(points.getLast())) >= MAX_ARC_DEVIATION)
            return null;
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
