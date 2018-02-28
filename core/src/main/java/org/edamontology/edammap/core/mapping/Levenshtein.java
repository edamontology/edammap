/*
 * Copyright © 2016 Erik Jaaniso
 *
 * This file is part of EDAMmap.
 *
 * EDAMmap is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EDAMmap is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EDAMmap.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.edamontology.edammap.core.mapping;

public class Levenshtein {

	public static int standard(String a, String b) {
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

	// Ukkonen E. Algorithms for approximate string matching. (http://www.sciencedirect.com/science/article/pii/S0019995885800462)
	// Berghel H, Roach D. An extension of Ukkonen's enhanced dynamic programming ASM algorithm. (http://berghel.net/publications/asm/asm.pdf)

	public Levenshtein() {
		l_max = 100;
		initFkp();
	}

	private int l_max;

	private int[][] fkp;

	private int zero_k;

	private void initFkp() {
		int max_k = 2 * (l_max + 1) + 1;
		int max_p = (l_max + 1) + 2;

		fkp = new int[max_k][max_p];

		zero_k = max_k / 2;

		for (int k = -zero_k; k < max_k - zero_k; ++k) {
			for (int p = 0; p < max_p - 1; ++p) {
				if (p == Math.abs(k)) {
					if (k < 0) {
						fkp[k + zero_k][p] = Math.abs(k) - 1;
					} else {
						fkp[k + zero_k][p] = -1;
					}
				} else if (p < Math.abs(k)) {
					fkp[k + zero_k][p] = Integer.MIN_VALUE;
				}
			}
		}
	}

	private void f(int k, int p, String a, String b) {
		int t = Math.max(fkp[k + zero_k][p] + 1,
			Math.max(fkp[k + zero_k - 1][p], fkp[k + zero_k + 1][p] + 1));
		while (t < Math.min(a.length(), b.length() - k) && a.charAt(t) == b.charAt(t + k)) ++t;
		fkp[k + zero_k][p + 1] = t;
	}

	public int improved(String a, String b, int p_max) {
		if (p_max == 0) {
			if (a.equals(b)) return 0;
			else return -1;
		}

		int m = a.length();
		int n = b.length();

		if (l_max < Math.max(m, n)) {
			l_max = Math.max(m, n);
			initFkp();
		}

		int p = Math.abs(n - m);

		do {
			if (p > p_max) return -1;

			for (int i = (p - (n - m)) / 2; i > 0; --i) {
				f(n - m + i, p - i, a, b);
			}
			for (int i = (n - m + p) / 2; i > 0; --i) {
				f(n - m - i, p - i, a, b);
			}
			f(n - m, p, a, b);

			++p;
		} while (fkp[n - m + zero_k][p] != m);

		return p - 1;
	}
}
