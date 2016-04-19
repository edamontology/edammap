package edammapper.mapping;

public class Levenshtein {
	public static int standard(String a, String b) {
		// a or b null or empty case
		// a and b equal case

		int[] colPrev = new int[a.length() + 1];
		int[] col = new int[a.length() + 1];

		for (int i = 0; i <= a.length(); ++i) colPrev[i] = i;
		for (int j = 1; j <= b.length(); ++j) {
			col[0] = j;
			for (int i = 1; i <= a.length(); ++i) {
				int match = ((a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1);
				col[i] = Math.min(Math.min(colPrev[i] + 1, col[i - 1] + 1), colPrev[i - 1] + match);
			}
			int[] swap = colPrev;
			colPrev = col;
			col = swap;
		}

		return colPrev[a.length()];
	}
}
