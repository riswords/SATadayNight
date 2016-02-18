package solver;

import collections.DoubleVec;
import collections.IVec;
import collections.IntVec;
import solver.solverTypes.LBool;
import solver.solverTypes.Literal;
import solver.IVarOrder;

public class SimpleVarOrder implements IVarOrder {

    private IVec<LBool> assignments;
    
    // variable activity
    private DoubleVec activity; // heuristic measure of the activity of a variable
    private double varActivityIncrement; // variable activity increment
    private double varActivityDecay; // decay factor for variable activity
    
    // list of unassigned variables in ascending order of activity
    private IntVec sortedUnassigned;
    private int lastVarID;
    
    public SimpleVarOrder(AssignmentManager assignments, double activityDecayFactor) {
        this.assignments = assignments.getAssignments();
        this.activity = new DoubleVec();
        varActivityIncrement = 1.0;
        varActivityDecay = 1.0 / activityDecayFactor;
        
        this.lastVarID = -1;
        this.sortedUnassigned = new IntVec();
    }

    @Override
    public void newVar() {
        lastVarID += 1;
        sortedUnassigned.push(lastVarID);
        activity.push(0);
    }

    @Override
    public void update(int var) {
        for(int i=0; i<sortedUnassigned.size(); ++i) {
            if(sortedUnassigned.get(i) == var) {
                for(int j=i+1; j < sortedUnassigned.size(); ++j) {
                    int jVar = sortedUnassigned.get(j);
                    if(activity.get(var) > activity.get(jVar)) {
                        // activity on x is larger than activity on jVar, so swap them
                        sortedUnassigned.set(j, var);
                        sortedUnassigned.set(j-1, jVar); 
                    }
                }
                // break early if we updated the variable
                return;
            }
        }
    }

    @Override
    public void updateAll() {
        // TODO: re-evaluate for assigned or not to clear out assignments that may have been inferred?
        // right now, go through all varIDs and call update for the ones that are unassigned
        for(int i=0; i<lastVarID; ++i) {
            if(assignments.get(i) != LBool.UNDEFINED)
                update(i);
        }
    }

    /**
     * Re-insert {@code var} into the sorted list of unassigned variables, based on its activity level
     */
    @Override
    public void undo(int var) {
        // insert x into sorted order, based on its activity
        // TODO this can be a binary search type thing since the list is sorted, but just doing a linear search for now
        sortedUnassigned.push(var);
        update(var);
    }

    @Override
    public int selectVariable() {
        int ret = sortedUnassigned.last();
        sortedUnassigned.pop();
        return ret;
    }

    @Override
    public Literal selectLiteral() {
        // TODO not sure how the polarity should be selected. For now, just always returning true.
        // This shouldn't cause a problem because if there's a conflict, we should have a learned conflict clause with 
        // the negation in it, but probably worth noting that there might be a situation where this could result in 
        // divergence...
        return new Literal(selectVariable(), false);
    }

    @Override
    public void setAssigned(int var) {
        sortedUnassigned.remove(var);
    }
    
    public void bumpVarActivity(Literal p) {
        int x = p.var();
        double oldActivity = activity.get(x);
        double newActivity = oldActivity + varActivityIncrement;
        activity.set(x, newActivity);
        if(newActivity > 1e100)
            rescaleVarActivity();
        update(x);
    }
    
    public void decayVarActivity() {
        varActivityIncrement *= varActivityDecay;
    }
    
    private void rescaleVarActivity() {
        for(int i=0; i<numVars(); ++i) {
            activity.set(i, activity.get(i) * 1e-100);
        }
        varActivityIncrement *= 1e-100;
    }
    
    private int numVars() {
        return lastVarID + 1;
    }
}
