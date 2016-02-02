package mapper.core;

/**
 * @author Rabie Saidi
 */
public enum MatchType{
    LABEL("Label"),
    EXACT_SYNONYM("Exact_synonym"),
    NARROW_SYNONYM("Narrow_synonym"),
    BROAD_SYNONYM("Broad_synonym"),
    NONE("None");


    MatchType(String type) {

    }
}
