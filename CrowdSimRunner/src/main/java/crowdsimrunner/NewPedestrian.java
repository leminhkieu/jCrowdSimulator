package crowdsimrunner;

import de.fhg.ivi.crowdsimulation.crowd.Pedestrian;
import de.fhg.ivi.crowdsimulation.crowd.forcemodel.ForceModel;
import de.fhg.ivi.crowdsimulation.crowd.forcemodel.numericintegration.NumericIntegrator;
import de.fhg.ivi.crowdsimulation.geom.Quadtree;


public class NewPedestrian extends Pedestrian {

    public NewPedestrian(double initialPositionX, double initialPositionY, float normalDesiredVelocity,
                         float maximumDesiredVelocity, ForceModel forceModel, NumericIntegrator numericIntegrator,
                         Quadtree quadtree)
    {
        super(0, initialPositionX, initialPositionY, normalDesiredVelocity, maximumDesiredVelocity,
                forceModel, numericIntegrator, quadtree);
        System.out.println("Creating new pedestrian: "+this.getId());
    }
}
