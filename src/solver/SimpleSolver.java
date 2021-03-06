package solver;

import java.util.Optional;

import collections.BoolVec;
import collections.DoubleVec;
import collections.Queue;
import collections.Vec;
import collections.IntVec;
import collections.Pair;
import collections.SimpleQueue;
import collections.SimpleVec;
import solver.solverTypes.Constraint;
import solver.solverTypes.LBool;
import solver.solverTypes.Literal;
import solver.solverTypes.SearchParameters;
import solver.solverTypes.SimpleClause;
import solver.solverTypes.SimpleVarOrder;
import exception.IllegalStateException;

public class SimpleSolver implements Solver {

    public SimpleSolver() {
        // constraint management
        constraints = new SimpleVec<Constraint<SimpleSolver>>();
        learnts = new SimpleVec<SimpleClause>();
        clauseActivityIncrement = 1.0;
        clauseActivityDecay = 0.999;

        // propagation
        watches = new SimpleVec<Vec<Constraint<SimpleSolver>>>();
        undos = new SimpleVec<Vec<Constraint<SimpleSolver>>>();
        propagationQueue = new SimpleQueue<Literal>();

        // assignments
        assigns = new SimpleVec<LBool>();
        trail = new SimpleVec<Literal>();
        trailLim = new IntVec();
        reason = new SimpleVec<Constraint<SimpleSolver>>();
        level = new IntVec();
        rootLevel = -1;
        model = new BoolVec();

        // variable order (after assigns is initialized)
        activity = new DoubleVec();
        varActivityIncrement = 1.0;
        varActivityDecay = 0.95;
        variableOrder = new SimpleVarOrder(assigns, activity);
    }

    @Override
    public int newVariable() {
        return newVariable(1);
    }
    
    @Override
    public int newVariable(int newVars) {
        int newIndex = numVars() + newVars;
        for(int i=0; i<newVars; ++i) {
            watches.push(new SimpleVec<Constraint<SimpleSolver>>());
            watches.push(new SimpleVec<Constraint<SimpleSolver>>());
            undos.push(new SimpleVec<Constraint<SimpleSolver>>());
            variableOrder.newVar();
        }
        reason.growTo(newIndex, null);
        assigns.growTo(newIndex, LBool.UNDEFINED);
        level.growTo(newIndex, -1);
        activity.growTo(newIndex, 0);
        return newIndex;
    }

    /**
     * Used for adding a new problem clause to the solver.
     */
    @Override
    public boolean addClause(Vec<Literal> literals) {
        Pair<Boolean, SimpleClause> newClauseResult = SimpleClause.clauseNew(this, literals, false);
        if(!newClauseResult.getFirst()) {
            // clause creation failed: either this is an empty clause, or it's a unit clause that conflicts 
            // with a pre-existing top-level assignment
            return false;
        }
        else {
            SimpleClause clause = newClauseResult.getSecond();
            if(clause == null)
                // clause is either unit (and enqueued), already satisfied, or must be satisfied regardless of the 
                // assignment, so don't bother adding it to the problem set
                return true;
            else {
                constraints.push(clause);
                // TODO: anything else to do when adding a user-defined/top-level clause?
                return true;
            }
        }
    }

    /**
     * Top-level simplification of constraint database. Remove any satisfied constraints and simplify the remaining 
     * constraints under the current (partial) assignment. Returns false if a top-level conflict is found.
     * Pre-condition: decision level is 0
     * Post-condition: propagation queue is empty
     */
    @Override
    public boolean simplifyDB() {
        if(decisionLevel() != 0)
            throw new IllegalStateException("Decision level must be 0 before simplifying the constraint database.");
        if(propagate().isPresent()) {
            return false;
        }
        
        simplifyConstraints(constraints);
        simplifyLearntClauses(learnts);
        return true;
    }

    /**
     * Simplify the set of problem constraints by removing the ones that are satisfied. Works identically to 
     * simplifyLearntClauses (in violation of DRY principle :( ) but has to be duplicated because of Java's lack of 
     * covariance (right now learnts have type IVec<SimpleClause> while problem constraints are 
     * IVec<IConstraint<SimpleSolver>>). 
     */
    private void simplifyConstraints(Vec<Constraint<SimpleSolver>> constraints) {
        int j = 0;
        for(int i=0; i<constraints.size(); ++i) {
            if(constraints.get(i).simplify(this))
                constraints.get(i).remove(this);
            else {
                constraints.set(j, constraints.get(i));
                j += 1;
            }
        }
        constraints.shrinkBy(constraints.size() - j);
    }

    /**
     * Simplify the set of learnt clauses by removing the ones that are satisfied. Works identically to 
     * simplifyConstraints (in violation of DRY principle :( ) but has to be duplicated because of Java's lack of 
     * covariance (right now learnts have type IVec<SimpleClause> while problem constraints are 
     * IVec<IConstraint<SimpleSolver>>). 
     */
    private void simplifyLearntClauses(Vec<SimpleClause> learntClauses) {
        int j = 0;
        for(int i=0; i<learntClauses.size(); ++i) {
            if(learntClauses.get(i).simplify(this))
                learntClauses.get(i).remove(this);
            else {
                learntClauses.set(j, learntClauses.get(i));
                j += 1;
            }
        }
        learntClauses.shrinkBy(learntClauses.size() - j);
    }
    
    @Override
    public boolean solve() {
        return solve(new SimpleVec<Literal>());
    }

    @Override
    public boolean solve(Vec<Literal> assumptions) {
        SearchParameters params = new SearchParameters(0.95, 0.999);
        double numConflicts = 100;
        double numLearnts = numConstraints() / 3.0;
        LBool status = LBool.UNDEFINED;

        // push incremental assumptions
        for(int i=0; i<assumptions.size(); ++i) {
            if(!assume(assumptions.get(i)) || propagate().isPresent()) {
                cancelUntil(0);
                return false;
            }
        }

        rootLevel = decisionLevel();

        // solve
        while(status.equals(LBool.UNDEFINED)) {
            status = search((int)numConflicts, (int)numLearnts, params);
            numConflicts *= 1.5;
            numLearnts *= 1.1;
        }

        cancelUntil(0);
        return status.equals(LBool.TRUE);
    }

    private boolean assume(Literal p) {
        trailLim.push(trail.size());
        return enqueue(p);
    }

    public boolean enqueue(Literal p) { return enqueue(p, null); }

    public boolean enqueue(Literal p, Constraint<SimpleSolver> from) {
        switch(value(p)) {
            case FALSE:
                // enqueued a conflicting assignment
                return false;
            case TRUE:
                // enqueued an existing, consistent assignment
                return true;
            case UNDEFINED:
                // enqueued a new fact, so store it
                int varP = p.var();
                assigns.set(varP, LBool.fromBoolean(!p.sign()));
                variableOrder.setAssigned(varP);
                level.set(varP, decisionLevel());
                reason.set(varP, from);
                trail.push(p);
                propagationQueue.insert(p);
                return true;
            default:
                // should not get here, since LBools can only be T/F/UNDEF...
                throw new IllegalStateException("Somehow you have an LBool that isn't TRUE, FALSE, or UNDEFINED. " +
                        "Update the switch statement in SimpleSolver.enqueue().");
        }
    }

    private Optional<Constraint<SimpleSolver>> propagate() {
        while(propagationQueue.size() > 0) {
            Literal p = propagationQueue.dequeue();
            Vec<Constraint<SimpleSolver>> temp = new SimpleVec<Constraint<SimpleSolver>>();
            watches.get(p.index()).moveTo(temp);

            for(int i=0; i<temp.size(); ++i) {
                if(!temp.get(i).propagate(this, p)) {
                    // constraint is conflicting: copy remaining watches to watches[p] and return the constraint
                    for(int j=i+1; j<temp.size(); ++j) {
                        watches.get(p.index()).push(temp.get(j));
                    }
                    propagationQueue.clear();
                    return Optional.of(temp.get(i));
                }
            }
        }
        return Optional.empty();
    }

    private void cancelUntil(int level) {
        while(decisionLevel() > level)
            cancel();
    }

    private void cancel() {
        int c = trail.size() - trailLim.last();
        for(; c > 0; c--)
            undoOne();
        trailLim.pop();
    }

    private void undoOne() {
        Literal p = trail.last();
        int x = p.var();
        assigns.set(x, LBool.UNDEFINED);
        reason.set(x, null);
        level.set(x, -1);
        variableOrder.undo(x);
        trail.pop();
        while(undos.get(x).size() > 0) {
            undos.get(x).last().undo(this, p);
            undos.get(x).pop();
        }
    }

    private LBool search(int numConflicts, int numLearnts, SearchParameters params) {
        int conflictCount = 0;
        varActivityDecay = 1.0 / params.getVarDecay();
        clauseActivityDecay = 1.0 / params.getClauseDecay();
        model.clear();

        while(true) {
            Optional<Constraint<SimpleSolver>> conflict = propagate();
            if(conflict.isPresent()) {
                // conflict
                conflictCount += 1;
                Vec<Literal> learntClause = new SimpleVec<Literal>();
                int backtrackLevel = -1;
                if(decisionLevel() == rootLevel)
                    return LBool.FALSE;
                analyze(conflict.get(), learntClause);
                cancelUntil(Math.max(backtrackLevel, rootLevel));
                record(learntClause);
                decayActivities();
            }
            else {
                // no conflict
                if(decisionLevel() == 0) {
                    // simplify the set of problem clauses
                    boolean result = simplifyDB();
                    if(!result)
                        throw new IllegalStateException("simplifyDB() should not have been able to return false. " + 
                                "See SimpleSolver.search().");
                }
                if(learnts.size() - numAssigns() >= numLearnts)
                    // reduce the set of learnt clauses
                    reduceDB();

                int numVars = numVars();
                if(numAssigns() == numVars) {
                    // model found
                    model.growTo(numVars);
                    for(int i=0; i<numVars; ++i)
                        model.set(i, value(i) == LBool.TRUE);
                    cancelUntil(rootLevel);
                    return LBool.TRUE;
                }
                else if(conflictCount >= numConflicts) {
                    // reached bound on number of conflicts, so force a restart
                    cancelUntil(rootLevel);
                    return LBool.UNDEFINED;
                }
                else {
                    // new variable decision
                    Literal p = variableOrder.selectLiteral();
                    boolean result = assume(p);
                    if(!result)
                        throw new IllegalStateException("assume(p) should not have been able to return false.");
                }
            }
        }
    }
    
    /**
     * Analyze the input conflict and produce a reason clause.
     * Pre-conditions:
     *      outLearnt is assumed to be cleared
     *      the current decision level must be greater than root level
     * Post-condition:
     *      outLearnt[0] is the asserting literal at level outBacktrackLevel (the returned int)
     * Side effect:
     *      will undo part of the trail, but not beyond the last decision level
     */
    private int analyze(Constraint<SimpleSolver> conflict, Vec<Literal> outLearnt) {
        assert (outLearnt.size() == 0) :
            "Pre-condition failure in analyze: outLearnt input should be cleared.";
        assert (decisionLevel() > rootLevel) :
            "Pre-condition failure in analyze: current decision level must be greater than root level.";
        
        BoolVec seen = new BoolVec(numVars(), false);
        int counter = 0;
        Literal p = Literal.UNDEFINED_LITERAL;

        Vec<Literal> reasonForP = new SimpleVec<Literal>();
        outLearnt.push(null);
        int outBacktrackLevel = 0;
        do {
            reasonForP.clear();
            assert (conflict != null) :
                "Invariant failure: conflict should not be null. See Solver.analyze().";
            conflict.calcReason(this, p, reasonForP);
            
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

    private void record(Vec<Literal> clauseVec) {
        Literal p = clauseVec.get(0);   // saving so we have this after clauseVec gets cleared
        Pair<Boolean, SimpleClause> newClauseResult = SimpleClause.clauseNew(this, clauseVec, true);
        assert (newClauseResult.getFirst()) :
            "Constructing clause should not fail here.";
        SimpleClause clause = newClauseResult.getSecond();
        boolean enqueueResult = enqueue(p, clause);
        assert(enqueueResult) :
            "Enqueuing clause should not fail here.";
        if(clause != null)
            learnts.push(clause);
    }

    private void decayActivities() {
        decayVarActivity();
        decayClauseActivity();
    }

    private void decayVarActivity() {
        varActivityIncrement *= varActivityDecay;
    }

    private void decayClauseActivity() {
        clauseActivityIncrement *= clauseActivityDecay;
    }

    /**
     * Remove half of the learnt clauses, minus some locked clauses. (A locked clause is a clause that is the reason 
     * for a current assignment). Clauses below a certain level of activity can also be removed.
     */
    private void reduceDB() {
        int i, j;
        double limit = clauseActivityIncrement / learnts.size();
        sortByActivity(learnts);
        for(i = 0, j = 0; i < learnts.size() / 2; ++i) {
            if(!learnts.get(i).isLocked(this))
                learnts.get(i).remove(this);
            else {
                learnts.set(j, learnts.get(i));
                j += 1;
            }
        }
        for(; i<learnts.size(); ++i) {
            if(!learnts.get(i).isLocked(this)
                    && learnts.get(i).getActivity() < limit)
                learnts.get(i).remove(this);
            else {
                learnts.set(j, learnts.get(i));
                j += 1;
            }
        }
        learnts.shrinkBy(i - j);
    }

    @Override
    public BoolVec getModel() { return model; }
    
    private int decisionLevel() { return trailLim.size(); }
    
    private int numVars() { return assigns.size(); }

    private int numAssigns() { return trail.size(); }

    private int numConstraints() { return constraints.size(); }
    
    private LBool value(int varID) {
        return assigns.get(varID);
    }
    
    public LBool value(Literal p) {
        return p.sign()
                ? assigns.get(p.var()).negate()
                : assigns.get(p.var());
    }

    public int getLiteralDecisionLevel(Literal p) {
        return level.get(p.var());
    }

    /**
     * Return the constraint which implied the value for variable {@code index}.
     */
    public Constraint<SimpleSolver> getReason(int index) {
        return reason.get(index);
    }

    public Vec<Constraint<SimpleSolver>> getWatches(int index) {
        return watches.get(index);
    }

    public void bumpVarActivity(Literal p) {
        int x = p.var();
        double oldActivity = activity.get(x);
        double newActivity = oldActivity + varActivityIncrement;
        activity.set(x, newActivity);
        if(newActivity > 1e100)
            rescaleVarActivity();
        variableOrder.update(x);
    }

    private void rescaleVarActivity() {
        for(int i=0; i<numVars(); ++i) {
            activity.set(i, activity.get(i) * 1e-100);
        }
        varActivityIncrement *= 1e-100;
    }

    public void bumpClauseActivity(SimpleClause clause) {
        double newActivity = clause.getActivity() + clauseActivityIncrement;
        clause.setActivity(newActivity);
        if(newActivity > 1e100)
            rescaleClauseActivity();
        // TODO: might not need to sort here
        sortByActivity(learnts);
    }

    private void rescaleClauseActivity() {
        for(int i=0; i<learnts.size(); ++i) {
            double activity = learnts.get(i).getActivity();
            learnts.get(i).setActivity(activity * 1e-100);
        }
        clauseActivityIncrement *= 1e-100;
    }

    private void sortByActivity(Vec<SimpleClause> clauses) {
        clauses.sort((SimpleClause c1, SimpleClause c2) -> {
            return Double.compare(c2.getActivity(), c1.getActivity());
        });
    }

    /* Constraint management */
    private Vec<Constraint<SimpleSolver>> constraints; // list of constraints
    private Vec<SimpleClause> learnts; // learnt clauses
    private double clauseActivityIncrement; // clause activity increment
    private double clauseActivityDecay; // decay factor for clause activity

    /* Variable Order */
    private DoubleVec activity; // heuristic measure of the activity of a variable
    private double varActivityIncrement; // variable activity increment
    private double varActivityDecay; // decay factor for variable activity
    private VariableOrder variableOrder; // keep track of dynamic variable order

    /* Propagation */
    // For each literal p, a list of constraints watching p. A constraint will be inspected when p becomes true.
    private Vec<Vec<Constraint<SimpleSolver>>> watches;

    // For each variable x, a list of constraints that need to update when x becomes unbound by backtracking
    private Vec<Vec<Constraint<SimpleSolver>>> undos;
    private Queue<Literal> propagationQueue; // propagation queue

    /* Assignments */
    private Vec<LBool> assigns; // current assignment indexed on variables
    private Vec<Literal> trail; // list of assignments in chronological order
    private IntVec trailLim; // separator indices for different decision levels in a trail
    private Vec<Constraint<SimpleSolver>> reason; // for each variable, the constraint that implied its value
    private IntVec level; // for each variable, the decision level at which it was assigned
    private int rootLevel; // separates incremental and search assumptions

    private BoolVec model; // store the final model
}
