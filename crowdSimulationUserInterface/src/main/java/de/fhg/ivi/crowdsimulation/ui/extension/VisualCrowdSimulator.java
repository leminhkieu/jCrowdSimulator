package de.fhg.ivi.crowdsimulation.ui.extension;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import de.fhg.ivi.crowdsimulation.CrowdSimulator;
import de.fhg.ivi.crowdsimulation.CrowdSimulatorNotValidException;
import de.fhg.ivi.crowdsimulation.crowd.Crowd;
import de.fhg.ivi.crowdsimulation.crowd.Pedestrian;

/**
 * This class extends the class {@link CrowdSimulator}.
 * <p>
 * Added functionality is creating {@link VisualCrowd} objects with {@link Color}.
 *
 * @author hahmann
 */
public class VisualCrowdSimulator extends CrowdSimulator<VisualCrowd>
{
    /**
     * Constructor.
     * <p>
     * Calls super constructor of {@link CrowdSimulator#CrowdSimulator(ThreadPoolExecutor)}.
     */
    public VisualCrowdSimulator(ThreadPoolExecutor threadPool)
    {
        super(threadPool);
    }

    /**
     * Creates a new {@link VisualCrowd} object including the {@link Pedestrian}s, which are
     * contained by the {@link VisualCrowd}.
     * <p>
     *
     * @param pedestrians pedestrianPositions a {@link HashMap} of {@link Integer} (used as
     *            Pedestrian Ids) and {@link Coordinate}s which describe the starting positions
     *            (x,y) of the {@link Pedestrian}s
     *
     * @param ignoreInvalid if {@code true} in case Validation checks yield validation errors, only
     *            a warning is printed into the log output, otherwise
     *            {@link CrowdSimulatorNotValidException} is thrown
     * @param color the {@link Color} to be associated with this {@link VisualCrowd}
     *
     * @throws CrowdSimulatorNotValidException
     * @return the created Crowd object
     */
    public VisualCrowd createVisualCrowd(List<Geometry> pedestrians, boolean ignoreInvalid,  Color color) throws CrowdSimulatorNotValidException
    {
        Crowd crowd = getCrowdFactory().createCrowdFromGeometries(pedestrians, ignoreInvalid, this);
        return new VisualCrowd(crowd, color);
    }
}
