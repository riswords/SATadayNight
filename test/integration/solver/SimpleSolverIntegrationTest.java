package integration.solver;

import org.junit.Test;
import static org.junit.Assert.*;

import collections.BoolVec;
import collections.SimpleVec;
import solver.SimpleSolver;
import solver.solverTypes.Literal;

public class SimpleSolverIntegrationTest {

    /**
     * Test Case 1:
     * clause1: 0 1 2
     * clause2: -0 -1
     * clause3: -2
     * 
     * Acceptable models:
     * -0, 1, -2
     * 0, -1, -2
     * 
     * Rough trace at time of writing:
     *      Clauses 1, 2 added
     *      Clause 3 results in -2 being enqueued
     *      -2 is propagated
     *      No further propagations, so 1 is assumed
     *      Based on watches, clause2 is examined:
     *          No other literals remain to watch
     *          -0 is enqueued, which leads to a satisfying assignment
     */
    @Test
    public void testCase1() {
        SimpleSolver testSolver = new SimpleSolver();
        initVariables(testSolver, 3);

        // clause1
        SimpleVec<Literal> clause1 = new SimpleVec<Literal>();
        addLiteral(clause1, 0, true);
        addLiteral(clause1, 1, true);
        addLiteral(clause1, 2, true);
        testSolver.addClause(clause1);

        // clause2
        SimpleVec<Literal> clause2 = new SimpleVec<Literal>();
        addLiteral(clause2, 0, false);
        addLiteral(clause2, 1, false);
        testSolver.addClause(clause2);

        // clause3
        SimpleVec<Literal> clause3 = new SimpleVec<Literal>();
        addLiteral(clause3, 2, false);
        testSolver.addClause(clause3);

        BoolVec permittedAssign1 = new BoolVec(3);
        permittedAssign1.set(0, false);
        permittedAssign1.set(1, true);
        permittedAssign1.set(2, false);
        
        BoolVec permittedAssign2 = new BoolVec(3);
        permittedAssign2.set(0, true);
        permittedAssign2.set(1, false);
        permittedAssign2.set(2, false);
        
        if(testSolver.solve()) {
            BoolVec model = testSolver.getModel();
            assertTrue(verifyModel(model, permittedAssign1) ||
                    verifyModel(model, permittedAssign2));
        }
    }
    
    /**
     * Test Case 2:
     * clause1: 0 1 2
     * clause2: 0 1 -2
     * clause3: 0 -1 2
     * clause4: 0 -1 -2
     * clause5: -0 1 2
     * clause6: -0 1 -2
     * clause7: -0 -1 2
     * clause8: -0 -1 -2
     * 
     * UNSAT
     * 
     * Rough trace at time of writing:
     *      Clauses 1-8 added
     *      
     */
    @Test
    public void testCase2() {
        SimpleSolver testSolver = new SimpleSolver();
        initVariables(testSolver, 3);

        // clause1
        SimpleVec<Literal> clause1 = new SimpleVec<Literal>();
        addLiteral(clause1, 0, true);
        addLiteral(clause1, 1, true);
        addLiteral(clause1, 2, true);
        testSolver.addClause(clause1);

        // clause2
        SimpleVec<Literal> clause2 = new SimpleVec<Literal>();
        addLiteral(clause2, 0, true);
        addLiteral(clause2, 1, true);
        addLiteral(clause2, 2, false);
        testSolver.addClause(clause2);

        // clause3
        SimpleVec<Literal> clause3 = new SimpleVec<Literal>();
        addLiteral(clause3, 0, true);
        addLiteral(clause3, 1, false);
        addLiteral(clause3, 2, true);
        testSolver.addClause(clause3);
        
        // clause4
        SimpleVec<Literal> clause4 = new SimpleVec<Literal>();
        addLiteral(clause4, 0, true);
        addLiteral(clause4, 1, false);
        addLiteral(clause4, 2, false);
        testSolver.addClause(clause4);
        
        // clause5
        SimpleVec<Literal> clause5 = new SimpleVec<Literal>();
        addLiteral(clause5, 0, false);
        addLiteral(clause5, 1, true);
        addLiteral(clause5, 2, true);
        testSolver.addClause(clause5);
        
        // clause6
        SimpleVec<Literal> clause6 = new SimpleVec<Literal>();
        addLiteral(clause6, 0, false);
        addLiteral(clause6, 1, true);
        addLiteral(clause6, 2, false);
        testSolver.addClause(clause6);
        
        // clause7
        SimpleVec<Literal> clause7 = new SimpleVec<Literal>();
        addLiteral(clause7, 0, false);
        addLiteral(clause7, 1, false);
        addLiteral(clause7, 2, true);
        testSolver.addClause(clause7);
        
        // clause8
        SimpleVec<Literal> clause8 = new SimpleVec<Literal>();
        addLiteral(clause8, 0, false);
        addLiteral(clause8, 1, false);
        addLiteral(clause8, 2, false);
        testSolver.addClause(clause8);

        // UNSAT
        assertFalse(testSolver.solve());
    }
    
    private static void initVariables(SimpleSolver solver, int numVars) {
        for(int i=0; i<numVars; ++i)
            solver.newVariable();
    }
    
    private static void addLiteral(SimpleVec<Literal> clause, int varID, boolean isPositive) {
        clause.push(new Literal(varID, !isPositive));
    }
    
    private static boolean verifyModel(BoolVec model, BoolVec expectedModel) {
        for(int i=0; i<expectedModel.size(); ++i) {
            boolean expectedSign = expectedModel.get(i);
            if(model.get(i) != expectedSign)
                return false;
        }
        return true;
    }

}
