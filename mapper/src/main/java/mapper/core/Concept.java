package mapper.core;

import java.util.List;

/**
 * @author Rabie Saidi
 */
public class Concept {
    private String label;
    private String uri;
    private boolean obsolete;
    private List<String> exactSynonyms;
    private List<String> narrowSynonyms;
    private List<String> broadSynonyms;

    public String getLabel() {
        return label;
    }

    public String getUri() {
        return uri;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public List<String> getExactSynonyms() {
        return exactSynonyms;
    }

    public List<String> getNarrowSynonyms() {
        return narrowSynonyms;
    }

    public List<String> getBroadSynonyms() {
        return broadSynonyms;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public void setExactSynonyms(List<String> exactSynonyms) {
        this.exactSynonyms = exactSynonyms;
    }

    public void setNarrowSynonyms(List<String> narrowSynonyms) {
        this.narrowSynonyms = narrowSynonyms;
    }

    public void setBroadSynonyms(List<String> broadSynonyms) {
        this.broadSynonyms = broadSynonyms;
    }
}
