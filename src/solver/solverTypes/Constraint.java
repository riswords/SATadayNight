package solver.solverTypes;

import collections.Vec;
import solver.Solver;

public interface Constraint<S extends Solver> {

    /**
     * Remove this from the watcher lists and ensure that it can be disposed.
     */
    public void remove(S solver);

    /**
     * This method is called if the constraint is found in a watcher list during propagation of 
     * unit information for p. The constraint is removed from the list and is required to insert 
     * itself into a new or the same watcher list. Any unit information derivable as a consequence 
     * of p should be enqueued. If successful, "true" is returned; if a conflict is detected, 
     * "false" is returned. This constraint may add itself to the undo list of var (p) if it needs
     * to be updated when p becomes unbound.
     * 
     * Most likely, this will be the primary target for improvements in efficiency, as the MiniSAT 
     * solver spends about 80% of the time propagating.
     */
    public boolean propagate(S solver, Literal p);

    /**
     * At the top-level, a constraint may be given the opportunity to simplify its representation 
     * (this returns "false") or to state that the constraint is satisfied under the current 
     * assignment and can be removed (returns "true"). A constraint must not be simplifiable to 
     * produce unit information or to be conflicting; in that case, the propagation has not been 
     * correctly defined.
     */
    public boolean simplify(S solver);

    /**
     * During backtracking, this method is called if the constraint added itself to the undo list 
     * of var(p) in propagate(). The current variable assignments are guaranteed to be identical 
     * to that of the moment before propagate() was called.
     */
    public void undo(S solver, Literal p);

    /**
     * This constraint is the reason for p being true. That is, during propagation, the current
     * constraint enqueued p. The input vector (outReason) is extended to include a set of 
     * assignments (represented as literals) implying p. The current variable assignments are 
     * guaranteed to be identical to that of the moment before the constraint propagated p. The 
     * literal p is also allowed to be the special UNDEFINED_LIT in which case the reason for the 
     * clause being conflicting should be returned through the vector.
     */
    public void calcReason(S solver, Literal p, Vec<Literal> outReason);
}
