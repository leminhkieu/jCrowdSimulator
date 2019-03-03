package de.fhg.ivi.crowdsimulation.crowd;

import de.fhg.ivi.crowdsimulation.CrowdSimulator;
import de.fhg.ivi.crowdsimulation.crowd.Pedestrian;
import de.fhg.ivi.crowdsimulation.crowd.forcemodel.ForceModel;
import de.fhg.ivi.crowdsimulation.crowd.forcemodel.numericintegration.NumericIntegrator;
import de.fhg.ivi.crowdsimulation.geom.Quadtree;


public class NewPedestrian extends Pedestrian {

    private static final int XLIM = 45; // the point at which pedestrians leave the simulation
    // (should be part of CrowdSimRUnnerMain but caused a circular dependency)

    // Useful to have a link back to the crowd simulator and crowd that this pedestrian belongs to
    private CrowdSimulator crowdSimulator;
    private Crowd crowd;

    public NewPedestrian(double initialPositionX, double initialPositionY, float normalDesiredVelocity,
                         float maximumDesiredVelocity, ForceModel forceModel, NumericIntegrator numericIntegrator, Quadtree quadtree,
                         CrowdSimulator crowdSimulator, Crowd crowd)
    {
        super(0, initialPositionX, initialPositionY, normalDesiredVelocity, maximumDesiredVelocity,
                forceModel, numericIntegrator, quadtree);
        this.crowdSimulator = crowdSimulator;
        this.crowd = crowd;
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

        // See if this agent needs to be removed. Remove them by removing their
        // group (they are the only member of their group) from the constituent crowd.
        //System.out.println(this.getId()+"m "+this.getCurrentPositionVector());
        if (this.getCurrentPositionVector().getX() > XLIM) {
            System.out.println("Agent "+this.getId()+" needs to exit");
            System.out.println(this.crowd);
            Group groupToRemove = null;
            for (Group group : this.crowd.getGroups())
            {
                if (group.getPedestrians(false).contains(this)) {
                    groupToRemove = group;
                    break;
                }
            }
            if (groupToRemove==null) {
                System.err.println("ERROR");
            }
            this.crowd.getGroups().remove(groupToRemove);
        }
    }
}
