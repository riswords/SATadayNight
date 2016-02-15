package solver.solverTypes;

import collections.IVec;
import collections.Pair;
import collections.SimpleVec;
import exception.UncheckedInvariantException;
import solver.SimpleSolver;

public class SimpleClause implements IConstraint<SimpleSolver>, Comparable<SimpleClause> {

    private boolean isLearnt;
    private double activity;
    private IVec<Literal> literals;

    /**
     * Use clauseNew to construct clauses
     */
    private SimpleClause(IVec<Literal> lits, boolean learnt) {
        this.literals = lits;
        this.isLearnt = learnt;
        this.activity = 0.0;
    }

    public boolean isLocked(SimpleSolver solver) {
        return solver.getReason(literals.get(0).var()).equals(this);
    }

    @Override
    public void remove(SimpleSolver solver) {
        solver.getWatches(literals.get(0).negated().index()).remove(this);
        solver.getWatches(literals.get(1).negated().index()).remove(this);
    }

    @Override
    public boolean propagate(SimpleSolver solver, Literal p) {
        // Make sure the false literal is literals[1]
        Literal notP = p.negated();
        if(literals.get(0).equals(notP)) {
            literals.set(0, literals.get(1));
            literals.set(1, notP);
        }

        // if 0th watch is true, then the clause is already satisfied
        if(solver.value(literals.get(0)) == LBool.TRUE) {
            // reinsert clause into watcher list
            solver.getWatches(p.index()).push(this);
            return true;
        }

        // look for a new literal to watch
        for(int i = 2; i < literals.size(); ++i) {
            if(solver.value(literals.get(i)) != LBool.FALSE) {
                literals.set(1, literals.get(i));
                literals.set(i, notP);
                // insert clause into watcher list
                solver.getWatches(literals.get(1).negated().index()).push(this);
                return true;
            }
        }

        // clause is unit under assignment
        solver.getWatches(p.index()).push(this);
        // enqueue for propagation
        return solver.enqueue(literals.get(0), this);
    }

    @Override
    public boolean simplify(SimpleSolver solver) {
        int j = 0;
        for(int i = 0; i < literals.size(); ++i) {
            LBool iSolverValue = solver.value(literals.get(i));
            if(iSolverValue == LBool.TRUE)
                return true;
            else if(iSolverValue == LBool.UNDEFINED) {
                // false literals aren't copied (only occur for i >= 2)
                literals.set(j, literals.get(i));
                j += 1;
            }
        }
        literals.shrinkBy(literals.size() - j);
        return false;
    }

    @Override
    public void undo(SimpleSolver s, Literal p) {
        // in this simple implementation, does nothing
    }

    @Override
    public void calcReason(SimpleSolver solver, Literal p, IVec<Literal> outReason) {
        // invariant: p == LIT_UNDEFINED or p == literals[0]
        if(! (p.equals(Literal.UNDEFINED_LITERAL) || p.equals(literals.get(0))))
            throw new UncheckedInvariantException("Invariant failure: p should either be undefined or the first " +
                    "literal in the clause. ");
        
        int startIndex = p.equals(Literal.UNDEFINED_LITERAL) ? 0 : 1;
        for(int i = startIndex; i < literals.size(); ++i) {
            // invariant: solver.value(lits[i]) == FALSE
            if(solver.value(literals.get(i)) != LBool.FALSE)
                throw new UncheckedInvariantException("Invariant failure: value assigned to literals[i] should be "
                        + "false.");
            outReason.push(literals.get(i).negated());
            if(isLearnt)
                solver.bumpClauseActivity(this);
        }
    }

    public double getActivity() {
        return activity;
    }

    public void setActivity(double newActivity) {
        this.activity = newActivity;
    }

    /**
     * Based on implementation described in original MiniSat paper, but since we can't really do 
     * the out parameter thing well, return a pair of a Boolean (indicating the original return 
     * value) and a SimpleClause (the newly constructed clause).
     *      
     * Additional notes:
     * Post-condition: ps is cleared
     * For learnt clauses, all literal will be false except lits[0] (due to design of analyze())
     * For propagation to work, the second watch in a learnt clause should be put on the first literal to be unbound 
     *      during backtracking (i.e., the one with the highest decision level).
     * For top-level/user-defined constraints, just pick the first two literals to watch
     */
    public static Pair<Boolean, SimpleClause> clauseNew(SimpleSolver solver, IVec<Literal> ps, boolean learnt) {
        // normalize clause
        if(!learnt) {
            if(clauseAlreadySatisfied(solver, ps)
                    || clauseHasNegAndPosLiteralOccurrence(ps))
                return new Pair<Boolean, SimpleClause>(true, null);
            removeAllFalseLiterals(solver, ps);
            removeDuplicateLiterals(ps);
        }

        // empty clause
        if(ps.size() == 0)
            return new Pair<Boolean, SimpleClause>(false, null);
        // unit clause
        else if(ps.size() == 1) {
            return (solver.enqueue(ps.get(0)))
                    ? new Pair<Boolean, SimpleClause>(true, null)
                    : new Pair<Boolean, SimpleClause>(false, null);
        }
        else {
            IVec<Literal> copyPs = new SimpleVec<Literal>();
            ps.moveTo(copyPs);
            SimpleClause newClause = new SimpleClause(copyPs, learnt);

            if(learnt) {
                // pick a second literal to watch
                int indexOfMaxDL = findLiteralWithMaxDecisionLevel(solver, copyPs); // use copyPs because ps is cleared
                Literal tmp = copyPs.get(1);
                copyPs.set(1, copyPs.get(indexOfMaxDL));
                copyPs.set(indexOfMaxDL, tmp);

                // bump clause activity
                solver.bumpClauseActivity(newClause);   // newly learnt clauses are active
                for(int i=0; i<ps.size(); ++i)
                    solver.bumpVarActivity(ps.get(i));  // vars in a conflict clause are active
            }
            
            // add clause to watcher lists
            solver.getWatches(copyPs.get(0).negated().index()).push(newClause);
            solver.getWatches(copyPs.get(1).negated().index()).push(newClause);
            return new Pair<Boolean, SimpleClause>(true, newClause);
        }
    }

    /**
     * Check whether the solver already has an assignment that satisfies this clause.
     */
    private static boolean clauseAlreadySatisfied(SimpleSolver solver, IVec<Literal> ps) {
        for(int i=0; i<ps.size(); ++i) {
            Literal p = ps.get(i);
            if(solver.value(p) == LBool.TRUE)
                return true;
        }
        return false;
    }

    /**
     * Check a vector of Literals for positive and negative occurrences of the same literal.
     */
    private static boolean clauseHasNegAndPosLiteralOccurrence(IVec<Literal> ps) {
        for(int i=0; i<ps.size(); ++i) {
            Literal iLitNegated = ps.get(i).negated();
            for(int j=i+1; j<ps.size(); ++j) {
                Literal jLiteral = ps.get(j);
                if(iLitNegated.equals(jLiteral))
                    return true;
            }
        }
        return false;
    }

    /**
     * Remove from the vector all Literals which are already assigned a value of false in the solver.
     */
    private static void removeAllFalseLiterals(SimpleSolver solver, IVec<Literal> ps) {
        for(int i=0; i<ps.size(); ++i) {
            Literal p = ps.get(i);
            if(solver.value(p) == LBool.FALSE) {
                ps.remove(p);
                i -= 1;
            }
        }
    }

    /**
     * Remove Literals that occur more than once (with the same sign) from the vector.
     */
    private static void removeDuplicateLiterals(IVec<Literal> ps) {
        for(int i=0; i<ps.size(); ++i) {
            Literal iLiteral = ps.get(i);
            for(int j=i+1; j<ps.size(); ++j) {
                Literal jLiteral = ps.get(j);
                if(iLiteral.equals(jLiteral)) {
                    ps.remove(jLiteral);
                    j -= 1;
                }
            }
        }
    }

    private static int findLiteralWithMaxDecisionLevel(SimpleSolver solver, IVec<Literal> ps) {
        int indexOfMaxLevel = 0;
        int maxDecisionLevel = -1;
        for(int i=0; i<ps.size(); ++i) {
            int decisionLevel = solver.getLiteralDecisionLevel(ps.get(i));
            if(decisionLevel > maxDecisionLevel) {
                maxDecisionLevel = decisionLevel;
                indexOfMaxLevel = i;
            }
        }
        return indexOfMaxLevel;
    }

    /**
     * Sort clauses by activity, in descending order
     */
    @Override
    public int compareTo(SimpleClause clause) {
        return Double.compare(clause.activity, activity);
    }
}
