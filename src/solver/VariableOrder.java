package solver;

import solver.solverTypes.Literal;

public interface VariableOrder {

    /**
     * Called when a new variable is created.
     */
    public void newVar();

    /**
     * Called when a variable has increased in activity
     */
    public void update(int var);

    /**
     * Called when all variables have been assigned new activities.
     */
    public void updateAll();

    /**
     * Called when a variable is unbound (i.e., may be selected again).
     */
    public void undo(int var);

    /**
     * Called to select a new, unassigned variable.
     * In VSIDS, this would be the variable with the highest activity.
     */
    public int selectVariable();
    
    /**
     * Called to select a new, unassigned Literal for assignment (variable with polarity).
     * In VSIDS, this would be the variable with the highest activity.
     */
    public Literal selectLiteral();
    
    /**
     * Called when a variable is assigned.
     */
    public void setAssigned(int var);
}
