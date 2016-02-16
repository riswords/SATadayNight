package solver.solverComponents;

import collections.IVec;
import solver.solverTypes.Literal;

public class Clause {

    private IVec<Literal> clause;
    
    public IVec<Literal> toLiteralVector() {
        return clause;
    }
}
