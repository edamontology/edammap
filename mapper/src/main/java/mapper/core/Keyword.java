package mapper.core;

/**
 * @author Rabie Saidi
 */
public class Keyword {
    private String value;
    private Keyword parent;

    public Keyword(String value) {
        this.value = value;
    }

    public Keyword(String value, String parent) {
        this.value = value;
        this.parent = new Keyword(parent);
    }

    public void setParent(Keyword parent) {
        this.parent = parent;
    }

    public String getValue() {
        return value;
    }

    public Keyword getParent() {
        return parent;
    }
}
