package solver.solverTypes;

public class SearchParameters {
    private double varDecayRate;
    private double clauseDecayRate;
    
    public SearchParameters(double varDecay, double clauseDecay) {
        this.varDecayRate = varDecay;
        this.clauseDecayRate = clauseDecay;
    }
    
    public double getVarDecay() { return varDecayRate; }
    public double getClauseDecay() { return clauseDecayRate; }
}
