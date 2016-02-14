package mapper.core;

/**
 * Contains a number of methods implementing some known distance between strings
 * @author Rabie Saidi
 * @version 1.0
 */
public class Score {
    public static int LevenshteinDistance(String s, String t){
        // degenerate cases
        if (s.equals(t)) return 0;
        if (s.length() == 0) return t.length();
        if (t.length() == 0) return s.length();

        // create two work vectors of integer distances
        int[] v0 = new int[t.length() + 1];
        int[] v1 = new int[t.length() + 1];

        // initialize v0 (the previous row of distances)
        // this row is A[0][i]: edit distance for an empty s
        // the distance is just the number of characters to delete from t
        for (int i = 0; i < v0.length; i++)
            v0[i] = i;

        for (int i = 0; i < s.length(); i++)
        {
            // calculate v1 (current row distances) from the previous row v0
            // first element of v1 is A[i+1][0]
            // edit distance is delete (i+1) chars from s to match empty t
            v1[0] = i + 1;
            // use formula to fill in the rest of the row
            for (int j = 0; j < t.length(); j++)
            {
                int cost = (s.charAt(i) == t.charAt(j)) ? 0 : 1;
                v1[j + 1] = Math.min(Math.min(v1[j] + 1, v0[j + 1] + 1), v0[j] + cost);
            }
            // copy v1 (current row) to v0 (previous row) for next iteration
            System.arraycopy(v1, 0, v0, 0, v0.length);
        }

        return v1[t.length()];
    }

    public static int HammingDistance(String s, String t){
        // check preconditions
        if (s == null || t == null || s.length() != t.length()) {
            throw new IllegalArgumentException();
        }

        // compute hamming distance
        int distance = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != t.charAt(i)) {
                distance++;
            }
        }
        return distance;
    }

    public static int editDistance(int costInsert, int costDel, int costSusbt, String s, String t){
        return 0;
    }
}
