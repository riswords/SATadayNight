package main.dimacs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import collections.IVec;
import collections.SimpleVec;
import solver.ISolver;
import solver.solverTypes.Literal;

public class DIMACSParser {

    public static void parseDIMACS(String fileName, ISolver solver) throws DIMACSException {
        try {
            Scanner scanner = new Scanner(new BufferedReader(new FileReader(fileName)));
            scanner.useDelimiter("\\s+");
            int expectedClauses = 0;
            int actualClauses = 0;
            while(scanner.hasNext()) {
                // problem line
                if(scanner.hasNext("p"))
                    expectedClauses = parseProblemLine(scanner.nextLine(), solver);
                
                // comment line
                if(scanner.hasNext("c"))
                    scanner.nextLine();
                
                // clause line
                if(scanner.hasNextInt()) {
                    parseClauseLine(scanner.nextLine(), solver);
                    actualClauses += 1;
                }
            }
            scanner.close();
            assert (expectedClauses == actualClauses);
        }
        catch(FileNotFoundException e) {
            throw new DIMACSException("Unable to find file: " + fileName, e);
        }
    }
    
    private static int parseProblemLine(String line, ISolver solver) throws DIMACSException {
        String[] splitLine = line.split("\\s+");
        try {
            int numVars = Integer.parseInt(splitLine[2]);
            int expectedNumClauses = Integer.parseInt(splitLine[3]);
            solver.newVariable(numVars);
            return expectedNumClauses;
        }
        catch(ArrayIndexOutOfBoundsException e) {
            throw new DIMACSException("Invalid problem specification: " + line, null);
        }
        catch(NumberFormatException e) {
            throw new DIMACSException("Invalid number of variables or clauses in problem specification: " + line, null);
        }
    }
    
    private static void parseClauseLine(String line, ISolver solver) throws DIMACSException {
        String[] literals = line.split("\\s+");
        IVec<Literal> clause = new SimpleVec<Literal>();
        for(String litString : literals) {
            try {
                int litInt = Integer.parseInt(litString);
                if(litInt != 0)
                    clause.push(new Literal(Math.abs(litInt) - 1, litInt < 0));
            }
            catch(NumberFormatException e) {
                throw new DIMACSException("Invalid variable name: " + litString, e);
            }
        }
        solver.addClause(clause);
    }
}
