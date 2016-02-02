package mapper.core;

/**
 * @author Rabie Saidi
 * Date: 09/10/2014
 * Time: 11:36
 */
public class ComparisonResult implements Comparable {
    public void setQuery(String query) {
        this.query = query;
    }

    private String query;
    private final String reference;
    private double editScore;
    private double substringScore;

    public double getGlobalScore() {
        return (2 * editScore + substringScore) / 3;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    private String uri;
    private String obselete;
    private MatchType matchType;
    private MatchConfidence matchConfidence;
    private BranchType branch;

    public void setObselete(String obselete) {
        this.obselete = obselete;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public void setMatchConfidence(MatchConfidence matchConfidence) {
        this.matchConfidence = matchConfidence;
    }

    public void setBranch(BranchType branch) {
        this.branch = branch;
    }

    public ComparisonResult(String query, String reference) {
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
        double editScore1 = ((ComparisonResult)o1).getEditScore();
        double editSubstring1 = ((ComparisonResult)o1).getSubstringScore();
        double editScore2 = ((ComparisonResult)o1).getEditScore();
        double editSubstring2 = ((ComparisonResult)o1).getSubstringScore();

        if(editSubstring1 > editSubstring2) return 1;
        else if(editSubstring2 > editSubstring1) return -1;
        else if (editScore1 > editScore2) return 1;
        else if (editScore2 > editScore1) return -1;
        return 0;
    }

    @Override
    public int compareTo(Object o) {

        double editScore2 = ((ComparisonResult)o).getEditScore();
        double substringScore2 = ((ComparisonResult)o).getSubstringScore();
        double globalScore = getGlobalScore();
        double globalScore2 = ((ComparisonResult)o).getGlobalScore();
        MatchType matchType2 = ((ComparisonResult) o).getMatchType();

        if( globalScore > globalScore2) return -1;
        else if(globalScore2 > globalScore) return 1;

        else if(substringScore > substringScore2) return -1;
        else if(substringScore2 > substringScore) return 1;
        else if (editScore > editScore2) return -1;
        else if (editScore2 > editScore) return 1;
        else
        switch (matchType){
            case LABEL: switch (matchType2){
                case LABEL: return 0;
                case EXACT_SYNONYM: return -1;
                case NARROW_SYNONYM: return -1;
                case BROAD_SYNONYM: return -1;
            }
            case EXACT_SYNONYM: switch (matchType2){
                case LABEL: return 1;
                case EXACT_SYNONYM: return 0;
                case NARROW_SYNONYM: return -1;
                case BROAD_SYNONYM: return -1;
            }
            case NARROW_SYNONYM: switch (matchType2){
                case LABEL: return 1;
                case EXACT_SYNONYM: return 1;
                case NARROW_SYNONYM: return 0;
                case BROAD_SYNONYM: return -1;
            }
            case BROAD_SYNONYM: switch (matchType2){
                case LABEL: return 1;
                case EXACT_SYNONYM: return 1;
                case NARROW_SYNONYM: return 1;
                case BROAD_SYNONYM: return 0;
            }
        }

        return 0;
    }

    //keyword_or_phrase | label_or_synonym | URI | obsolete | match_type | match_conf | branch
    @Override
    public String toString() {
        return  query + " | " +
                reference + " | " +
                uri + " | " +
                obselete + " | " +
                matchType + " | " +
                matchConfidence + " | " +
                branch + " || " +
                editScore + " | " +
                substringScore + " | " +
                getGlobalScore();
    }

    public BranchType getBranch() {
        return branch;
    }

    public MatchConfidence getMatchConfidence() {
        return matchConfidence;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public String getObselete() {
        return obselete;
    }

    public String getUri() {
        return uri;
    }
}
