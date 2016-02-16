package solver.solverComponents;

import java.util.Optional;

public interface Propagator {

    public Optional<Clause> propagate();
    
}
