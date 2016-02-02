package mapper.core;

/**
 * @author Rabie Saidi
 * Date: 09/10/2014
 * Time: 13:46
 */
public class Matcher {

    public double editScore(String query, String reference){
        double score = 0;
        String queryModified = query.toLowerCase().trim().replaceAll("\\s+","");
        String referenceModified = reference.toLowerCase().trim().replaceAll("\\s+","");
        // Calculating the similarity score
        // score = 1 - distance / length of largest string
        if(queryModified != null && referenceModified != null) {
            score = 1.0 - (double) Score.LevenshteinDistance(queryModified, referenceModified)
                    / (double) (Math.max(queryModified.length(), referenceModified.length()));
        }
        return score;
    }

    public double substringScore(String query, String reference){
        double score = 0;
        String queryModified = query.toLowerCase().trim().replaceAll("\\s+","");
        String referenceModified = reference.toLowerCase().trim().replaceAll("\\s+","");
        if(queryModified != null && referenceModified != null) {
            // Strings are similar if one of them contains the other
            if (referenceModified.contains(queryModified)
                    || queryModified.contains(referenceModified)) {

                // Calculating the similarity score
                // score = 1 - length difference / biggest length
                score = 1.0 - (double) Math.abs(referenceModified.length() - queryModified.length())
                        / (double) (Math.max(referenceModified.length(), queryModified.length()));
            }
        }
        return score;
    }
}
