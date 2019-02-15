package crowdsimrunner;

import de.fhg.ivi.crowdsimulation.CrowdSimulator;
import de.fhg.ivi.crowdsimulation.crowd.Crowd;
import de.fhg.ivi.crowdsimulation.crowd.Pedestrian;
import de.fhg.ivi.crowdsimulation.crowd.forcemodel.ForceModel;
import de.fhg.ivi.crowdsimulation.crowd.forcemodel.numericintegration.NumericIntegrator;
import de.fhg.ivi.crowdsimulation.geom.Quadtree;

public class NewPedestrian extends Pedestrian {

    public NewPedestrian(double initialPositionX, double initialPositionY, float normalDesiredVelocity,
                         float maximumDesiredVelocity, CrowdSimulator cs, Quadtree quadtree)
    {
        super(0, initialPositionX, initialPositionY, normalDesiredVelocity, maximumDesiredVelocity,
                cs.getForceModel(), cs.getNumericIntegrator(), quadtree);
    }

    @Override
    public void move(long time, double simulationInterval) {
        super.move(time, simulationInterval);
        // TODO Add stuff here that can be called each time this pedestrian moves
    }
}
