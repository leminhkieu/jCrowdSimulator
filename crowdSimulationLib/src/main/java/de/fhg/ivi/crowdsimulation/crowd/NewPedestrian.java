package de.fhg.ivi.crowdsimulation.crowd;

import com.vividsolutions.jts.math.Vector2D;
import de.fhg.ivi.crowdsimulation.CrowdSimulator;
import de.fhg.ivi.crowdsimulation.crowd.Pedestrian;
import de.fhg.ivi.crowdsimulation.crowd.forcemodel.ForceModel;
import de.fhg.ivi.crowdsimulation.crowd.forcemodel.numericintegration.NumericIntegrator;
import de.fhg.ivi.crowdsimulation.geom.Quadtree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class NewPedestrian extends Pedestrian {

    // We need to when the first agent reaches the end of the corridor so that CrowdSimRunnerMain
    // can stop creating agents
    public static boolean reachedEndOfCorridor = false;

    private static final int XLIM = 95; // the point at which pedestrians leave the simulation
    // (should be part of CrowdSimRUnnerMain but caused a circular dependency)

    public static BufferedWriter bw = null;

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

        // Initialise the file writer
        if (NewPedestrian.bw == null) {
            try {
                NewPedestrian.bw = new BufferedWriter(new FileWriter(
                        "./results/r-individual-"+System.currentTimeMillis()+".csv"));

                bw.write("Time,Agent,Xpos,Ypos,Velocity,Xforce,Yforce\n"); // Any others
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
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

        // Add this pedestrian's data to the output file
        try {
            NewPedestrian.bw.write(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                    this.crowdSimulator.getSimulatedTimeSpan(),
                    this.getId(),
                    this.getCurrentPositionVector().getX(),
                    this.getCurrentPositionVector().getY(),
                    this.getCurrentVelocity(),
                    this.getTotalForce().getX(),
                    this.getTotalForce().getY()
                    ));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // See if the agent has reached the end of the corridor; if so then move them back to the beginning
        if (this.getCurrentPositionVector().getX() > XLIM) {
            this.setCurrentPositionVector(new Vector2D(0, this.getCurrentPositionCoordinate().y));
            NewPedestrian.reachedEndOfCorridor = true;
        }

        /*
        // See if this agent needs to be removed. Remove them by removing their
        // group (they are the only member of their group) from the constituent crowd.
        if (this.getCurrentPositionVector().getX() > XLIM) {
            //System.out.println("Agent "+this.getId()+" needs to exit");
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
        */
    }
}
