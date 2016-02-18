package main;

import collections.BoolVec;
import main.dimacs.DIMACSException;
import main.dimacs.DIMACSParser;
import solver.SimpleSolver;

public class Main {

    public static void main(String[] args) {
        try {
            SimpleSolver testSolver = new SimpleSolver();
            //DIMACSParser.parseDIMACS("test/problemSpecs/simpleunsat_3_8.txt", testSolver);
            //DIMACSParser.parseDIMACS("test/problemSpecs/simplesat_5_3.txt", testSolver);
            DIMACSParser.parseDIMACS("test/problemSpecs/satrace15_596_2780_aes_64_1_keyfind_1.cnf", testSolver);
            
            boolean result = testSolver.solve();
            if(result) {
                outputCertificate(testSolver.getModel());
                System.exit(10);
            }
            else {
                outputUNSATResult();
                System.exit(20);
            }
        }
        catch(DIMACSException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    /**
     * Important note: for the solver, it's easier to index variables from 0, however, problem descriptions typically
     * start with variable 1 and use 0 as an end of line delimiter. To compensate for this discrepancy, the parser
     * subtracts 1 from the literal read in from the file so that the solver can use 0-indexed variables.
     *
     * This discrepancy is also accounted for when outputting certificates of satisfiability.
     */
    private static void outputCertificate(BoolVec model) {
        System.out.println("s SATISFIABLE");
        for(int i=0; i<model.size(); ++i) {
            String sign = !model.get(i) ? "-" : "";
            System.out.println("v " + sign + (i + 1));
        }
    }
    
    private static void outputUNSATResult() {
        System.out.println("s UNSATISFIABLE");
    }
}
