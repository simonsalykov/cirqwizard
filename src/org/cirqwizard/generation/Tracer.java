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

import org.cirqwizard.appertures.CircularAperture;
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Line;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.toolpath.CircularToolpath;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Tracer
{
    private static final int INITIAL_SAMPLE_COUNT = 200;                    // Amount of samples to process before trying to decide which curve it is
    private static final int SAMPLE_COUNT = 100;                            // Amount of last processed points to hold for deviation calculation
    private static final double ANGULAR_THRESHOLD = Math.toRadians(2);      // Threshold of angular difference which results in a new segment start
    private static final int FUSE_COUNTER = 2000000;                        // Safety fuse for breaking out of loop, should it become infinite

    private static final double DEVIATION_MARGIN = 3;                       // Margin, by which arc's standard deviation should beat segment's
                                                                            // standard deviation in order to choose arc over segment
    private static final double SEGMENT_FOLLOWING_ARC_DEVIATION_MARGIN = 1; // As above, in case segment in question follows an arc
    private static final int    SEGMENT_FOLLOWING_ARC_DEVIATION_MARGIN_LIMIT = (int)(INITIAL_SAMPLE_COUNT * 1.5);
                                                                            // Limitiation of previous constant scope (in samples from segment's start)

    private static final double LOW_UNCERTAINTY_THRESHOLD = 0.3;   // Arcs with uncertainty lower than that are processed as arcs
    private static final double HIGH_UNCERTAINTY_THRESHOLD = 2;     // Arcs with uncertainty higher than that are processed as segments

    private byte[] windowData;

    private int width;
    private int height;
    private int inflation;
    private int toolDiameter;

    private Point current;

    private Line currentSegment;

    private ArrayList<Integer> radii;
    private ArrayList<Flash> circularFlashes;

    public Tracer(byte[] windowData, int width, int height, int inflation, int toolDiameter, ArrayList<Integer> radii, ArrayList<Flash> circularFlashes)
    {
        this.windowData = windowData;
        this.width = width;
        this.height = height;
        this.inflation = inflation;
        this.toolDiameter = toolDiameter;
        this.radii = radii;
        this.circularFlashes = circularFlashes;
    }

    public List<Toolpath> process()
    {
        ArrayList<Toolpath> result = new ArrayList<Toolpath>();

        for (int y = height - 1; y >= 0; y--)
        {
            for (int x = width - 1; x >= 0; x--)
            {
                int index = x + y * width;
                if (windowData[index] != 0)
                    result.addAll(trace(x, y));
            }
        }

        return result;
    }

    private List<Toolpath> trace(int x, int y)
    {
        current = new Point(x, y);
        currentSegment = new Line(current, current);
        Direction direction = Direction.EAST;

        int segmentCounter = 0;
        LinkedList<Point> lastPoints = new LinkedList<>();
        LinkedList<Point> segmentPoints = new LinkedList<>();
        double angle = 0;
        MatchedArc matchedArc = null;

        ArrayList<Toolpath> result = new ArrayList<Toolpath>();

        Direction lastDirection = null;
        int fuse = 0;

        Logger logger = LoggerFactory.getApplicationLogger();

        do
        {
            if (fuse++ > FUSE_COUNTER)
            {
                StringBuffer str = new StringBuffer("Tracing algorithm failed. Exiting with emergency break. Last points were:\n");
                for (Point p : lastPoints)
                    str.append(p + "\n");
                logger.log(Level.SEVERE, str.toString());
                break;
            }

            if (direction != lastDirection)
                lastDirection = direction;

            lastPoints.addLast(current);
            int sampleCount = segmentCounter <= INITIAL_SAMPLE_COUNT ? INITIAL_SAMPLE_COUNT : SAMPLE_COUNT;
            while (lastPoints.size() > sampleCount)
                lastPoints.removeFirst();
            segmentPoints.addLast(current);

            segmentCounter++;
            currentSegment.setTo(current);
            boolean previousToolpathIsArc = result.size() > 0 && result.get(result.size() - 1) instanceof CircularToolpath;

            boolean restart = false;

            if (segmentCounter == INITIAL_SAMPLE_COUNT)
                angle = calculateAngle(currentSegment.getFrom(), current);
            else if (segmentCounter >= INITIAL_SAMPLE_COUNT)
            {
                if (matchedArc == null || (matchedArc.getUncertainty() > LOW_UNCERTAINTY_THRESHOLD &&
                        matchedArc.getUncertainty() < HIGH_UNCERTAINTY_THRESHOLD))
                {
                    double segmentDeviation = calculateSegmentDeviation(segmentPoints);
                    double margin = DEVIATION_MARGIN;
                    if (segmentCounter < SEGMENT_FOLLOWING_ARC_DEVIATION_MARGIN_LIMIT && previousToolpathIsArc)
                        margin = SEGMENT_FOLLOWING_ARC_DEVIATION_MARGIN;
                    matchedArc = fitArc(segmentPoints, segmentDeviation, margin);
                }

                if (matchedArc.getUncertainty() < LOW_UNCERTAINTY_THRESHOLD)
                    restart = calculateSegmentDeviation(lastPoints) < calculateArcDeviation(lastPoints, matchedArc.getCenter(), matchedArc.getRadius());
                else if (matchedArc.getUncertainty() > HIGH_UNCERTAINTY_THRESHOLD)
                    restart = Math.abs(calculateAngle(lastPoints.getFirst(), lastPoints.getLast()) - angle) > ANGULAR_THRESHOLD;
            }

            if (restart)
            {
                Toolpath toolpath = getToolpath(toolDiameter, matchedArc, calculateAngle(lastPoints.get(0), current), lastPoints.get(0));
                if (previousToolpathIsArc)
                {
                    CircularToolpath prev = (CircularToolpath) result.get(result.size() - 1);
                    Arc prevArc = (Arc) prev.getCurve();
                    boolean merge = false;

                    if (toolpath instanceof LinearToolpath)
                    {
                        double arcDeviation = calculateArcDeviation(segmentPoints, prevArc.getCenter(), prevArc.getRadius());
                        double ratio = arcDeviation / calculateSegmentDeviation(segmentPoints);
                        if (ratio < 1 || (segmentPoints.size() < 1.5 * INITIAL_SAMPLE_COUNT && ratio < 5))
                            merge = true;
                    }
                    if (toolpath instanceof CircularToolpath)
                    {
                        Arc arc = (Arc)((CircularToolpath)toolpath).getCurve();
                        int centersDistanceThreshold = (int)(0.4 * arc.getRadius());
                        if (prevArc.getCenter().distanceTo(arc.getCenter()) <= centersDistanceThreshold && prevArc.getRadius() == arc.getRadius())
                            merge = true;
                        else
                        {
                            double prevArcDeviation = calculateArcDeviation(segmentPoints, prevArc.getCenter(), prevArc.getRadius());
                            if (prevArcDeviation / calculateArcDeviation(segmentPoints, matchedArc.getCenter(), matchedArc.getRadius()) < 5)
                                merge = true;
                        }
                    }

                    if (merge)
                    {
                        result.remove(result.size() - 1);
                        Point[] newCenters = calculateArcCenters(prevArc.getCenter(), current, prevArc.getRadius());
                        double e0 = newCenters[0].distanceTo(prevArc.getCenter());
                        double e1 = newCenters[1].distanceTo(prevArc.getCenter());
                        int centersDistanceThreshold = (int)(0.4 * prevArc.getRadius());
                        matchedArc.setCenter(prevArc.getCenter());
                        matchedArc.setUncertainty(1);
                        if (Math.min(e0, e1) < centersDistanceThreshold)
                            matchedArc.setCenter(e0 < e1 ? newCenters[0] : newCenters[1]);
                        toolpath = new CircularToolpath(toolDiameter, prev.getCurve().getFrom(), ((CuttingToolpath)toolpath).getCurve().getTo(), matchedArc.getCenter(),
                                prevArc.getRadius(), prevArc.isClockwise());
                    }
                }

                result.add(toolpath);
                currentSegment = new Line(current, current);
                segmentPoints.clear();
                matchedArc = null;
                segmentCounter = 0;
            }

            windowData[current.getX() + current.getY() * width] = 0;

            boolean hasContinuation = false;
            for (Direction d : directions)
            {
                Point p = current.add(d.getVector());
                if (p.getX() < 0 || p.getX() >= width || p.getY() < 0 || p.getY() >= height)
                    continue;
                if (windowData[p.getX() + p.getY() * width] != 0)
                {
                    direction = d;
                    current = p;
                    hasContinuation = true;
                    break;
                }
            }
            if (!hasContinuation)
                break;
        }
        while (current.getX() >= 0 && current.getX() < width && current.getY() >= 0 && current.getY() < height);
        if (segmentCounter > 10)
            result.add(getToolpath(toolDiameter, matchedArc, calculateAngle(lastPoints.get(0), current), lastPoints.get(0)));

        return result;
    }

    private Toolpath getToolpath(int toolDiameter, MatchedArc matchedArc, double heading, Point headingStartPoint)
    {
        if (matchedArc == null || matchedArc.getUncertainty() > HIGH_UNCERTAINTY_THRESHOLD)
            return new LinearToolpath(toolDiameter, currentSegment.getFrom(), currentSegment.getTo());

        double centerAngle = calculateAngle(headingStartPoint, matchedArc.getCenter());
        double headingCenterAngle = heading - centerAngle;
        if (headingCenterAngle < -Math.PI)
            headingCenterAngle += Math.PI * 2;
        if (headingCenterAngle > Math.PI)
            headingCenterAngle -= Math.PI * 2;

        boolean clockwise = headingCenterAngle > 0;
        return new CircularToolpath(toolDiameter, currentSegment.getFrom(), currentSegment.getTo(), matchedArc.getCenter(), matchedArc.getRadius(), clockwise);
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

    private MatchedArc fitArc(LinkedList<Point> points, double segmentDeviation, double margin)
    {
        Point start = points.getFirst();
        Point end = points.getLast();

        double minDeviation = Double.MAX_VALUE;
        Point bestFit = null;
        int bestRadius = 0;

        // Go through known apertures first
        for (Flash flash : circularFlashes)
        {
            int radius = ((CircularAperture)flash.getAperture()).getDiameter() + inflation;
            double deviation = calculateArcDeviation(points, flash.getPoint(), radius);
            if (deviation < minDeviation)
            {
                minDeviation = deviation;
                bestFit = flash.getPoint();
                bestRadius = radius;
            }
        }

        double deviationsRatio = minDeviation / segmentDeviation;

        if (deviationsRatio < HIGH_UNCERTAINTY_THRESHOLD)
            return new MatchedArc(bestFit, bestRadius, deviationsRatio);

        double segmentAngle = Math.atan2(points.getLast().getY() - points.getFirst().getY(), points.getLast().getX() - points.getFirst().getX());

        for (int r : radii)
        {
            int radius = r + toolDiameter / 2;
            Point[] centers = calculateArcCenters(start, end, radius);
            double minAngle = Double.MAX_VALUE;
            Point center = null;

            for (Point c : centers)
            {

                double angle = calculateArcCenterAngle(c, segmentAngle);
                if (angle < minAngle)
                {
                    minAngle = angle;
                    center = c;
                }
            }

            double d = calculateArcDeviation(points, center, radius);
            if (d * margin < segmentDeviation && d < minDeviation)
            {
                minDeviation = d;
                bestFit = center;
                bestRadius = radius;
            }
        }

        return new MatchedArc(bestFit, bestRadius, minDeviation / segmentDeviation);
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

    private double calculateArcCenterAngle(Point center, double segmentAngle)
    {
        double angle = Math.atan2(center.getY() - current.getY(), center.getX() - current.getX()) - segmentAngle;
        if (angle > Math.PI)
            angle -= Math.PI;
        if (angle < -Math.PI)
            angle += Math.PI;
        return angle;
    }

    private Point[] calculateArcCenters(Point p1, Point p2, double radius)
    {
        double p1x = p1.getX();
        double p1y = p1.getY();
        double p2x = p2.getX();
        double p2y = p2.getY();

        double vx = p1x - p2x;
        double vy = p1y - p2y;
        double vsq = vx * vx + vy * vy;

        double cx1 = ((p1x + p2x) * vsq - Math.sqrt(-vsq * (-4 * radius * radius + vsq) * vy * vy)) / (2 * vsq);
        double cy1 = 1.0d / (2 * vsq * vy) * (Math.pow(p1y, 4) - 2 * Math.pow(p1y, 3) * p2y + 2 * p1y * Math.pow(p2y, 3) + p1y * p1y * vx * vx - p2y * p2y * (p2y * p2y + vx * vx) +
                p1x * Math.sqrt(-vsq * (-4 * radius * radius + vsq) * vy * vy) - p2x * Math.sqrt(-vsq * (-4 * radius * radius + vsq) * vy * vy));
        double cx2 = ((p1x + p2x) * vsq + Math.sqrt(-vsq * (-4 * radius * radius + vsq) * vy * vy)) / (2 * vsq);
        double cy2 = 1.0d / (2 * vsq * vy) * (Math.pow(p1y, 4) - 2 * Math.pow(p1y, 3) * p2y + 2 * p1y * Math.pow(p2y, 3) + p1y * p1y * vx * vx - p2y * p2y * (p2y * p2y + vx * vx) -
                p1x * Math.sqrt(-vsq * (-4 * radius * radius + vsq) * vy * vy) + p2x * Math.sqrt(-vsq * (-4 * radius * radius + vsq) * vy * vy));
        return new Point[] {new Point((int)cx1, (int)cy1), new Point((int)cx2, (int)cy2)};
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


