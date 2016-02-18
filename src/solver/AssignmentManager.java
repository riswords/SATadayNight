package solver;

import collections.BoolVec;
import collections.IVec;
import collections.IntVec;
import collections.SimpleVec;
import solver.solverTypes.IConstraint;
import solver.solverTypes.LBool;
import solver.solverTypes.Literal;

public class AssignmentManager {

    private IVec<LBool> assignments; // current assignment indexed on variables
    private IVec<Literal> trail; // list of assignments in chronological order
    private IntVec trailLimits; // separator indices for different decision levels in a trail
    private IVec<IConstraint<SimpleSolver>> reason; // for each variable, the constraint that implied its value
    private IntVec level; // for each variable, the decision level at which it was assigned
    
    private SimpleSolver solver;
    
    public AssignmentManager(SimpleSolver solver) {
        this.solver = solver;
        assignments = new SimpleVec<LBool>();
        trail = new SimpleVec<Literal>();
        trailLimits = new IntVec();
        reason = new SimpleVec<IConstraint<SimpleSolver>>();
        level = new IntVec();
    }
    
    public void newVariables(int numVars) {
        int newNumVars = numVars() + numVars;
        assignments.growTo(newNumVars, LBool.UNDEFINED);
        reason.growTo(newNumVars);
        level.growTo(numVars, -1);
    }
    
    public IVec<LBool> getAssignments() {
        return assignments;
    }
    
    public int decisionLevel() { return trailLimits.size(); }
    
    public void assume(Literal p) {
        trailLimits.push(trail.size());
    }
    
    public void assign(Literal p, IConstraint<SimpleSolver> from) {
        int varP = p.var();
        assignments.set(varP, LBool.fromBoolean(!p.sign()));
        level.set(varP, decisionLevel());
        reason.set(varP, from);
        trail.push(p);
    }
    
    public void cancelUntil(int level) {
        while(decisionLevel() > level)
            cancel();
    }
    
    private void cancel() {
        int c = trail.size() - trailLimits.last();
        for(; c > 0; c--)
            undoOne();
        trailLimits.pop();
    }
    
    private void undoOne() {
        Literal p = trail.last();
        int x = p.var();
        assignments.set(x, LBool.UNDEFINED);
        reason.set(x, null);
        level.set(x, -1);
        trail.pop();
        solver.undoAssignment(p);
    }
    
    public int analyze(IConstraint<SimpleSolver> conflict, IVec<Literal> outLearnt, int rootLevel) {
        assert (outLearnt.size() == 0) :
            "Pre-condition failure in analyze: outLearnt input should be cleared.";
        assert (decisionLevel() > rootLevel) :
            "Pre-condition failure in analyze: current decision level must be greater than root level.";
        
        BoolVec seen = new BoolVec(numVars(), false);
        int counter = 0;
        Literal p = Literal.UNDEFINED_LITERAL;

        IVec<Literal> reasonForP = new SimpleVec<Literal>();
        outLearnt.push(null);
        int outBacktrackLevel = 0;
        do {
            reasonForP.clear();
            assert (conflict != null) :
                "Invariant failure: conflict should not be null. See Solver.analyze().";
            conflict.calcReason(solver, p, reasonForP);
            
            // trace reason for p
            for(int i = 0; i<reasonForP.size(); ++i) {
                Literal q = reasonForP.get(i);
                int qVar = q.var();
                if(!seen.get(qVar)) {
                    seen.set(qVar, true);
                    if(level.get(qVar) == decisionLevel())
                        counter += 1;
                    else if(level.get(qVar) > 0) {
                        outLearnt.push(q.negated());
                        outBacktrackLevel = Math.max(outBacktrackLevel, level.get(qVar));
                    }
                }

                // select next literal to look at
                do {
                    p = trail.last();
                    conflict = reason.get(p.var());
                    undoOne();
                } while(!seen.get(p.var()) && trail.size() > 0);
                counter -= 1;
            }
        } while(counter > 0);
        outLearnt.set(0, p.negated());
        return outBacktrackLevel;
    }
    
    /**
     * Return the constraint which implied the value for variable {@code index}.
     */
    public IConstraint<SimpleSolver> getReason(int index) {
        return reason.get(index);
    }

    public LBool value(int varID) {
        return assignments.get(varID);
    }
    
    public LBool value(Literal p) {
        return p.sign()
                ? assignments.get(p.var()).negate()
                : assignments.get(p.var());
    }
    
    public int getDecisionLevel(Literal p) {
        return level.get(p.var());
    }
    
    public int numVars() { return assignments.size(); }
    
    public int numAssigns() { return trail.size(); }
}
