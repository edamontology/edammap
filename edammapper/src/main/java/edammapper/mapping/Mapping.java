package edammapper.mapping;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import edammapper.edam.Branch;

public class Mapping {

	private Map<Branch, List<Match>> matches;

	private final int matchesTop;

	private final List<Branch> branches;

	public Mapping(int matchesTop, List<Branch> branches) {
		if (matchesTop < 0) {
			//
		} else if (branches == null || branches.isEmpty()) {
			//
		}

		matches = new EnumMap<>(Branch.class);

		this.matchesTop = matchesTop;
		this.branches = branches;

		for (Branch branch : this.branches) {
			matches.put(branch, new ArrayList<Match>(this.matchesTop));
		}
	}

	public int getMatchesTop() {
		return matchesTop;
	}

	public List<Branch> getBranches() {
		return branches;
	}

	public int getMatchesSize(Branch branch) {
		if (!branches.contains(branch)) {
			//
		}
		return matches.get(branch).size();
	}

	public Match getMatch(Branch branch, int index) {
		if (!branches.contains(branch)) {
			//
		} else if (index < 0 || index >= matches.get(branch).size()) {
			//
		}
		return matches.get(branch).get(index);
	}

	public double getLastMatchScore(Branch branch) {
		if (!branches.contains(branch)) {
			//
		}
		int size = matches.get(branch).size();
		if (size == 0) {
			return 0;
		} else {
			return matches.get(branch).get(size - 1).getScore();
		}
	}

	public boolean addMatch(Branch branch, Match match) {
		if (!branches.contains(branch)) {
			//
		} else if (match == null) {
			//
		}
		List<Match> matchesBranch = matches.get(branch);
		if (matchesBranch.size() == matchesTop && matchesTop > 0) {
			Match lastMatch = matchesBranch.get(matchesBranch.size() - 1);
			if (match.compareTo(lastMatch) <= 0) {
				return false;
			} else {
				matchesBranch.remove(lastMatch);
			}
		}
		int i = 0;
		int s = matchesBranch.size();
		while (i < s) {
			if (match.compareTo(matchesBranch.get(i)) > 0) {
				matchesBranch.add(i, match);
				break;
			}
			++i;
		}
		if (i == s && matchesTop > 0) {
			matchesBranch.add(i, match);
		}
		return true;
	}
}
