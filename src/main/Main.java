package main;

import collections.SimpleVec;
import solver.SimpleSolver;
import solver.solverTypes.Literal;

public class Main {

    public static void main(String[] args) {
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
        boolean result = testSolver.solve();
        System.out.println(result);
    }
    
    private static void initVariables(SimpleSolver solver, int numVars) {
        for(int i=0; i<numVars; ++i)
            solver.newVariable();
    }
    
    private static void addLiteral(SimpleVec<Literal> clause, int varID, boolean isPositive) {
        clause.push(new Literal(varID, !isPositive));
    }
}
