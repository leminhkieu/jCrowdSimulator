package de.fhg.ivi.crowdsimulation.crowd;

import de.fhg.ivi.crowdsimulation.crowd.Pedestrian;
import de.fhg.ivi.crowdsimulation.crowd.forcemodel.ForceModel;
import de.fhg.ivi.crowdsimulation.crowd.forcemodel.numericintegration.NumericIntegrator;
import de.fhg.ivi.crowdsimulation.geom.Quadtree;


public class NewPedestrian extends Pedestrian {

    private static final int XLIM = 40; // the point at which pedestrians leave the simulation
    // (should be part of CrowdSimRUnnerMain but caused a circular dependency)

    public NewPedestrian(double initialPositionX, double initialPositionY, float normalDesiredVelocity,
                         float maximumDesiredVelocity, ForceModel forceModel, NumericIntegrator numericIntegrator,
                         Quadtree quadtree)
    {
        super(0, initialPositionX, initialPositionY, normalDesiredVelocity, maximumDesiredVelocity,
                forceModel, numericIntegrator, quadtree);
        //System.out.println("Creating new pedestrian: "+this.getId());
    }

    /**
     * This moves the agent. Overridden from {@link Pedestrian) parent class so
     * that bespoke code can be called at every iteration.
     *
     * @param time the current time (simulated) time, given in milliseconds
     * @param simulationInterval the time between this method invocation and the last one, given in
     */
    @Override
    public void move(long time, double simulationInterval) {
        super.move(time,simulationInterval);
        //System.out.println(this.getId()+"m "+this.getCurrentPositionVector());
        if (this.getCurrentPositionVector().getX() > XLIM) {
            System.out.println("Agent "+this.getId()+" needs to exit");

        }
    }
}
