package solver.solverComponents;

import solver.solverTypes.Literal;

public interface VariableOrder {

    public int selectVariable();
    
    public Literal selectLiteral();
}
