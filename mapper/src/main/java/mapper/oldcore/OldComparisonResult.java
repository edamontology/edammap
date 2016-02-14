package mapper.oldcore;

import mapper.core.MatchConfidence;
import mapper.core.MatchType;

/**
 * @author Rabie Saidi
 * Date: 09/10/2014
 * Time: 11:36
 */
public class OldComparisonResult implements Comparable {
    private final String query;
    private final String reference;
    private double editScore;
    private double substringScore;

    private String uri;
    private boolean obselete;
    private MatchType matchType;
    private MatchConfidence matchConfidence;

    public OldComparisonResult(String query, String reference) {
        this.query = query;
        this.reference = reference;
        this.editScore = 0;
        this.substringScore = 0;
    }

    public void setEditScore(double editScore) {
        this.editScore = editScore;
    }

    public void setSubstringScore(double substringScore) {
        this.substringScore = substringScore;
    }

    public String getQuery() {
        return query;
    }

    public String getReference() {
        return reference;
    }

    public double getEditScore() {
        return editScore;
    }

    public double getSubstringScore() {
        return substringScore;
    }


    public int compare(Object o1, Object o2) {
        double editScore1 = ((OldComparisonResult)o1).getEditScore();
        double editSubstring1 = ((OldComparisonResult)o1).getSubstringScore();
        double editScore2 = ((OldComparisonResult)o1).getEditScore();
        double editSubstring2 = ((OldComparisonResult)o1).getSubstringScore();

        if(editSubstring1 > editSubstring2) return 1;
        else if(editSubstring2 > editSubstring1) return -1;
        else if (editScore1 > editScore2) return 1;
        else if (editScore2 > editScore1) return -1;
        return 0;
    }

    @Override
    public int compareTo(Object o) {

        double editScore2 = ((OldComparisonResult)o).getEditScore();
        double substringScore2 = ((OldComparisonResult)o).getSubstringScore();

        if(substringScore > substringScore2) return -1;
        else if(substringScore2 > substringScore) return 1;
        else if (editScore > editScore2) return -1;
        else if (editScore2 > editScore) return 1;
        return 0;
    }

    @Override
    public String toString() {
        return  "{reference='" + reference + '\'' +
                ", " + substringScore +
                ", " + editScore +
                '}';
    }
}
