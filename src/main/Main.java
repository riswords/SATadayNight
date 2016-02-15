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
            DIMACSParser.parseDIMACS("test/problemSpecs/satrace15_708_2664_aes_32_3_keyfind_1.cnf", testSolver);
            
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
