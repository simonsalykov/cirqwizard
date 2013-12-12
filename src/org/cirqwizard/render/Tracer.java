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

package org.cirqwizard.render;

import org.cirqwizard.appertures.CircularAperture;
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.math.RealNumber;
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

    private Raster raster;

    private byte[] windowData;

    private int width;
    private int height;
    private RealNumber toolDiameter;

    private PointI current;

    private Segment currentSegment;

    private ArrayList<Flash> circularFlashes;

    public Tracer(Raster raster, byte[] windowData, int width, int height, RealNumber toolDiameter, ArrayList<Flash> circularFlashes)
    {
        this.raster = raster;
        this.windowData = windowData;
        this.width = width;
        this.height = height;
        this.toolDiameter = toolDiameter;
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
        current = new PointI(x, y);
        currentSegment = new Segment(current, current);
        Direction direction = Direction.EAST;

        int segmentCounter = 0;
        LinkedList<PointI> lastPoints = new LinkedList<PointI>();
        LinkedList<PointI> segmentPoints = new LinkedList<PointI>();
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
                for (PointI p : lastPoints)
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
            currentSegment.setEnd(current);
            boolean previousToolpathIsArc = result.size() > 0 && result.get(result.size() - 1) instanceof CircularToolpath;

            boolean restart = false;

            if (segmentCounter == INITIAL_SAMPLE_COUNT)
                angle = calculateAngle(currentSegment.getStart(), current);
            else if (segmentCounter >= INITIAL_SAMPLE_COUNT)
            {
                if (matchedArc == null || (matchedArc.getUncertainty() > LOW_UNCERTAINTY_THRESHOLD &&
                        matchedArc.getUncertainty() < HIGH_UNCERTAINTY_THRESHOLD))
                {
                    double segmentDeviation = calculateSegmentDeviation(segmentPoints);
                    double margin = DEVIATION_MARGIN;
                    if (segmentCounter < SEGMENT_FOLLOWING_ARC_DEVIATION_MARGIN_LIMIT && previousToolpathIsArc)
                        margin = SEGMENT_FOLLOWING_ARC_DEVIATION_MARGIN;
                    matchedArc = fitArc(toolDiameter, segmentPoints, segmentDeviation, margin);
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
                        double arcDeviation = calculateArcDeviation(segmentPoints, new PointI(prevArc.getCenter().getX().getValue().intValue(), prevArc.getCenter().getY().getValue().intValue()),
                                prevArc.getRadius().doubleValue());
                        double ratio = arcDeviation / calculateSegmentDeviation(segmentPoints);
                        if (ratio < 1 || (segmentPoints.size() < 1.5 * INITIAL_SAMPLE_COUNT && ratio < 5))
                            merge = true;
                    }
                    if (toolpath instanceof CircularToolpath)
                    {
                        Arc arc = (Arc)((CircularToolpath)toolpath).getCurve();
                        RealNumber centersDistanceThreshold = arc.getRadius().multiply(new RealNumber("0.4"));
                        if (prevArc.getCenter().distanceTo(arc.getCenter()).lessOrEqualTo(centersDistanceThreshold) && prevArc.getRadius().equals(arc.getRadius()))
                            merge = true;
                        else
                        {
                            double prevArcDeviation = calculateArcDeviation(segmentPoints, new PointI(prevArc.getCenter().getX().getValue().intValue(), prevArc.getCenter().getY().getValue().intValue()),
                                    prevArc.getRadius().doubleValue());
                            if (prevArcDeviation / calculateArcDeviation(segmentPoints, matchedArc.getCenter(), matchedArc.getRadius()) < 5)
                                merge = true;
                        }
                    }

                    if (merge)
                    {
                        result.remove(result.size() - 1);
                        PointI[] newCenters = calculateArcCenters(new PointI(prevArc.getFrom().getX().getValue().intValue(), prevArc.getFrom().getY().getValue().intValue()),
                                current,
                                prevArc.getRadius().doubleValue());
                        PointI ac = new PointI(prevArc.getCenter().getX().getValue().intValue(), prevArc.getCenter().getY().getValue().intValue());
                        double e0 = Math.sqrt((newCenters[0].x - ac.x) * (newCenters[0].x - ac.x) + (newCenters[0].y - ac.y) * (newCenters[0].y - ac.y));
                        double e1 = Math.sqrt((newCenters[1].x - ac.x) * (newCenters[1].x - ac.x) + (newCenters[1].y - ac.y) * (newCenters[1].y - ac.y));
                        RealNumber centersDistanceThreshold = prevArc.getRadius().multiply(new RealNumber("0.4"));
                        matchedArc.setCenter(new PointI(prevArc.getCenter().getX().getValue().intValue(), prevArc.getCenter().getY().getValue().intValue()));
                        matchedArc.setUncertainty(1);
                        if (Math.min(e0, e1) < centersDistanceThreshold.doubleValue())
                            matchedArc.setCenter(e0 < e1 ? newCenters[0] : newCenters[1]);
                        Point newCenter = new Point(matchedArc.getCenter().x, matchedArc.getCenter().y);
                        toolpath = new CircularToolpath(toolDiameter, prev.getCurve().getFrom(), ((CuttingToolpath)toolpath).getCurve().getTo(), newCenter, prevArc.getRadius(), prevArc.isClockwise());
                    }
                }

                result.add(toolpath);
                currentSegment = new Segment(current, current);
                segmentPoints.clear();
                matchedArc = null;
                segmentCounter = 0;
            }

            windowData[current.x + current.y * width] = 0;

            boolean hasContinuation = false;
            for (Direction d : directions)
            {
                PointI p = getNextPoint(current, d);
                if (p.x < 0 || p.x >= width || p.y < 0 || p.y >= height)
                    continue;
                if (windowData[p.x + p.y * width] != 0)
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
        while (current.x >= 0 && current.x < width && current.y >= 0 && current.y < height);
        if (segmentCounter > 10)
            result.add(getToolpath(toolDiameter, matchedArc, calculateAngle(lastPoints.get(0), current), lastPoints.get(0)));

        return result;
    }

    private Toolpath getToolpath(RealNumber toolDiameter, MatchedArc matchedArc, double heading, PointI headingStartPoint)
    {
        Point start = new Point(new RealNumber(currentSegment.getStart().x), new RealNumber(currentSegment.getStart().y));
        Point end = new Point(new RealNumber(currentSegment.getEnd().x), new RealNumber(currentSegment.getEnd().y));

        if (matchedArc == null || matchedArc.getUncertainty() > HIGH_UNCERTAINTY_THRESHOLD)
            return new LinearToolpath(toolDiameter, start, end);

        Point center = new Point(new RealNumber(matchedArc.getCenter().x), new RealNumber(matchedArc.getCenter().y));

        double centerAngle = calculateAngle(headingStartPoint, matchedArc.getCenter());
        double headingCenterAngle = heading - centerAngle;
        if (headingCenterAngle < -Math.PI)
            headingCenterAngle += Math.PI * 2;
        if (headingCenterAngle > Math.PI)
            headingCenterAngle -= Math.PI * 2;

        boolean clockwise = headingCenterAngle > 0;
        return new CircularToolpath(toolDiameter, start, end, center, new RealNumber(matchedArc.getRadius()), clockwise);
    }

    private double calculateAngle(PointI start, PointI end)
    {
        return Math.atan2(end.y - start.y, end.x - start.x);
    }

    private double calculateSegmentDeviation(LinkedList<PointI> points)
    {
        double deviation = 0;
        PointI start = points.getFirst();
        PointI end = points.getLast();
        for (PointI p : points)
        {
            double d = (p.y - start.y) * (end.x - p.x) - (end.y - start.y) * (p.x - start.x);
            deviation += d * d;
        }
        return Math.sqrt(deviation);
    }

    private MatchedArc fitArc(RealNumber toolDiameter, LinkedList<PointI> points, double segmentDeviation, double margin)
    {
        PointI start = points.getFirst();
        PointI end = points.getLast();

        double minDeviation = Double.MAX_VALUE;
        PointI bestFit = null;
        double bestRadius = 0;

        // Go through known apertures first
        for (Flash flash : circularFlashes)
        {
            PointI center = new PointI(flash.getX().getValue().intValue(), flash.getY().getValue().intValue());
            double radius = ((CircularAperture)flash.getAperture()).getDiameter().doubleValue() + toolDiameter.multiply(raster.getResolution()).divide(2).doubleValue();
            double deviation = calculateArcDeviation(points, center, radius);
            if (deviation < minDeviation)
            {
                minDeviation = deviation;
                bestFit = center;
                bestRadius = radius;
            }
        }

        double deviationsRatio = minDeviation / segmentDeviation;

        if (deviationsRatio < HIGH_UNCERTAINTY_THRESHOLD)
            return new MatchedArc(bestFit, bestRadius, deviationsRatio);

        double segmentAngle = Math.atan2(points.getLast().y - points.getFirst().y, points.getLast().x - points.getFirst().x);

        for (RealNumber r : raster.getRadii())
        {
            double radius = r.doubleValue() + toolDiameter.multiply(raster.getResolution()).divide(2).doubleValue();
            PointI[] centers = calculateArcCenters(start, end, radius);
            double minAngle = Double.MAX_VALUE;
            PointI center = null;

            for (PointI c : centers)
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

    private double calculateArcDeviation(LinkedList<PointI> points, PointI center, double radius)
    {
        double deviation = 0;
        for (PointI p : points)
        {
            double d = (double)((p.x - center.x) * (p.x - center.x) + (p.y - center.y) * (p.y - center.y)) - radius * radius;
            deviation += d * d;
        }
        return Math.sqrt(deviation);
    }

    private double calculateArcCenterAngle(PointI center, double segmentAngle)
    {
        double angle = Math.atan2(center.y - current.y, center.x - current.x) - segmentAngle;
        if (angle > Math.PI)
            angle -= Math.PI;
        if (angle < -Math.PI)
            angle += Math.PI;
        return angle;
    }

    private PointI[] calculateArcCenters(PointI p1, PointI p2, double radius)
    {
        double p1x = p1.x;
        double p1y = p1.y;
        double p2x = p2.x;
        double p2y = p2.y;

        double vx = p1x - p2x;
        double vy = p1y - p2y;
        double vsq = vx * vx + vy * vy;

        double cx1 = ((p1x + p2x) * vsq - Math.sqrt(-vsq * (-4 * radius * radius + vsq) * vy * vy)) / (2 * vsq);
        double cy1 = 1.0d / (2 * vsq * vy) * (Math.pow(p1y, 4) - 2 * Math.pow(p1y, 3) * p2y + 2 * p1y * Math.pow(p2y, 3) + p1y * p1y * vx * vx - p2y * p2y * (p2y * p2y + vx * vx) +
                p1x * Math.sqrt(-vsq * (-4 * radius * radius + vsq) * vy * vy) - p2x * Math.sqrt(-vsq * (-4 * radius * radius + vsq) * vy * vy));
        double cx2 = ((p1x + p2x) * vsq + Math.sqrt(-vsq * (-4 * radius * radius + vsq) * vy * vy)) / (2 * vsq);
        double cy2 = 1.0d / (2 * vsq * vy) * (Math.pow(p1y, 4) - 2 * Math.pow(p1y, 3) * p2y + 2 * p1y * Math.pow(p2y, 3) + p1y * p1y * vx * vx - p2y * p2y * (p2y * p2y + vx * vx) -
                p1x * Math.sqrt(-vsq * (-4 * radius * radius + vsq) * vy * vy) + p2x * Math.sqrt(-vsq * (-4 * radius * radius + vsq) * vy * vy));
        return new PointI[] {new PointI((int)cx1, (int)cy1), new PointI((int)cx2, (int)cy2)};
    }

    private PointI getNextPoint(PointI p, Direction direction)
    {
        switch (direction)
        {
            case NORTH: return new PointI(p.x, p.y + 1);
            case NORTH_EAST: return new PointI(p.x + 1, p.y + 1);
            case EAST: return new PointI(p.x + 1, p.y);
            case SOUTH_EAST: return new PointI(p.x + 1, p.y - 1);
            case SOUTH: return new PointI(p.x, p.y - 1);
            case SOUTH_WEST: return new PointI(p.x - 1, p.y - 1);
            case WEST: return new PointI(p.x - 1, p.y);
            case NORTH_WEST: return new PointI(p.x - 1, p.y + 1);
        }
        throw new IllegalArgumentException("Illegal direction: " + direction);
    }

    private static Direction[] directions = {Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST,
        Direction.NORTH, Direction.NORTH_EAST};

    private static enum Direction
    {
        NORTH,
        NORTH_EAST,
        EAST,
        SOUTH_EAST,
        SOUTH,
        SOUTH_WEST,
        WEST,
        NORTH_WEST;
    }
}


