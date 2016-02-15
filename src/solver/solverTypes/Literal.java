package solver.solverTypes;

/**
 * A Literal is represented as an integer where the least significant bit is the sign (1 if the Literal is negated), 
 * and the most significant bits represent a unique ID (doubled). 
 */
public class Literal {

    public static final Literal UNDEFINED_LITERAL = new Literal(Variable.VAR_UNDEF, false);

    private int var;

    public Literal(int varID, boolean isNegated) {
        this.var = (2 * varID) + (isNegated ? 1 : 0);
    }

    /**
     * Return a new Literal with the same variable ID but the opposite sign.
     */
    public Literal negated() {
        return new Literal(var(), !sign());
    }

    /**
     * Returns whether the Literal has a negation sign, i.e., true if the Literal is negated, false if it is positive.
     */
    public boolean sign() {
        return var % 2 == 1;
    }

    /**
     * Returns the integer representation of the underlying variable for this Literal.
     */
    public int var() {
        return var / 2;
    }

    /**
     * Convert a Literal to an integer suitable for indexing into an array. In this implementation, the positive and 
     * negative versions of a Literal are consecutive integers, with positive being one smaller than negative.
     * E.g., if variable x has ID 1, it uses indices 2 and 3 to represent x and !x, respectively.  
     */
    public int index() {
        return var;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof Literal)
            return this.var == ((Literal) other).var;
        else
            return false;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(var);
    }
    
    @Override
    public String toString() {
        return (sign() ? "-" : "") + var();
    }
}
