package solver.solverTypes;

public enum LBool {
    TRUE, FALSE, UNDEFINED;

    public static LBool fromBoolean(boolean b) {
        return b
                ? TRUE
                : FALSE;
    }

    public LBool negate() {
        switch(this) {
            case TRUE:
                return FALSE;
            case FALSE:
                return TRUE;
            default:
                return UNDEFINED;
        }
    }
}
