package edammapper.mapping;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import edammapper.edam.Branch;

public class Mapping {

	private Map<Branch, List<Match>> matches;

	private Map<Branch, List<Match>> remainingAnnotations;

	private final int matchesTop;

	private final List<Branch> branches;

	public Mapping(int matchesTop, List<Branch> branches) {
		if (matchesTop < 0) {
			// TODO
		} else if (branches == null || branches.isEmpty()) {
			// TODO
		}

		matches = new EnumMap<>(Branch.class);
		remainingAnnotations = new EnumMap<>(Branch.class);

		this.matchesTop = matchesTop;
		this.branches = branches;

		for (Branch branch : this.branches) {
			matches.put(branch, new ArrayList<Match>(this.matchesTop));
		}
		for (Branch branch : this.branches) {
			remainingAnnotations.put(branch, new ArrayList<Match>());
		}
	}

	public int getMatchesTop() {
		return matchesTop;
	}

	public List<Branch> getBranches() {
		return branches;
	}

	public List<Match> getMatches(Branch branch) {
		return matches.get(branch);
	}
	public boolean addMatch(Match match) {
		List<Match> matchesBranch = matches.get(match.getEdamUri().getBranch());
		if (matchesBranch.size() < matchesTop) {
			matchesBranch.add(match);
			return true;
		} else {
			return false;
		}
	}

	public List<Match> getRemainingAnnotations(Branch branch) {
		return remainingAnnotations.get(branch);
	}
	public void addRemainingAnnotation(Match match) {
		remainingAnnotations.get(match.getEdamUri().getBranch()).add(match);
	}

	public boolean isFull(Branch branch) {
		if (matches.get(branch).size() < matchesTop) {
			return false;
		} else {
			return true;
		}
	}
	public boolean isFull() {
		for (Branch branch : branches) {
			if (!isFull(branch)) return false;
		}
		return true;
	}
}
