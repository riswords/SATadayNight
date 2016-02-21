package solver;

import collections.BoolVec;
import collections.Vec;
import solver.solverTypes.Literal;

public interface Solver {

    /**
     * Introduce a new variable. Returns the ID of the new variable.
     */
    public int newVariable();
    
    /**
     * Introduce {@code numVars} new variables. Returns the ID of the last variable introduced.
     */
    public int newVariable(int numVars);

    /**
     * Introduce new clauses. May detect some conflicts and return false. If this happens, the 
     * solver may be left in an undefined state and shouldn't be used further.
     */
    public boolean addClause(Vec<Literal> literals);

    /**
     * May be called before solve() to simplify the problem constraints
     */
    public boolean simplifyDB();

    /**
     * Under the specified assumptions, returns true if the problem is satisfiable and false if 
     * it is unsatisfiable under the assumptions. If satisfiable, the model may be accessed using 
     * getModel().
     * 
     * After determining truth or contradiction, the assumptions are undone and the solver is 
     * returned to a usable state.
     * 
     * Note: if not overriding the definition of {@code solve{}} with no arguments, then the implementation of solve 
     * must handle the case when {@code assumptions} is null.
     * 
     * An alternative interface might distinguish between unsatisfiable and unsatisfiable under 
     * the assumptions.
     */
    public boolean solve(Vec<Literal> assumptions);

    /**
     * Convenience method to call solve() with no assumptions.
     */
    default boolean solve() {
        return solve(null);
    }
    
    /**
     * If the problem has been solved and is satisfiable, returns the model.
     */
    public BoolVec getModel();
}
