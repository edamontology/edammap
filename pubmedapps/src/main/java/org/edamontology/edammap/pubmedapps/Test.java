/*
 * Copyright © 2018, 2019 Erik Jaaniso
 *
 * This file is part of PubMedApps.
 *
 * PubMedApps is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PubMedApps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PubMedApps.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.edamontology.edammap.pubmedapps;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.edamontology.pubfetcher.core.common.FetcherArgs;
import org.edamontology.pubfetcher.core.common.PubFetcher;
import org.edamontology.pubfetcher.core.db.Database;
import org.edamontology.pubfetcher.core.db.publication.Publication;
import org.edamontology.pubfetcher.core.db.publication.PublicationIds;
import org.edamontology.pubfetcher.core.fetching.Fetcher;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.edamontology.edammap.core.idf.Idf;
import org.edamontology.edammap.core.preprocessing.PreProcessor;

public final class Test {

	private static final Logger logger = LogManager.getLogger();

	static void beforeAfter(String queryIdf, String pubFile, String database, PreProcessor preProcessor) throws IOException {
		Idf idf = new Idf(queryIdf);

		Set<Publication> publications = new LinkedHashSet<>(PubFetcher.getPublications(database, Collections.singletonList(pubFile), PubMedApps.class.getSimpleName()));

		Map<String, Integer> before = new HashMap<>();
		Map<String, Integer> after = new HashMap<>();
		Map<String, Integer> all = new HashMap<>();
		Map<String, Double> allBeforeScores = new HashMap<>();
		int allBeforeScoresSum = 0;
		Map<String, Double> allAfterScores = new HashMap<>();
		int allAfterScoresSum = 0;

		for (Publication publication : publications) {
			String toolTitle = publication.getTitle().getContent();
			Matcher titleSeparator = Common.TITLE_SEPARATOR.matcher(toolTitle);
			if (titleSeparator.find()) {
				toolTitle = toolTitle.substring(0, titleSeparator.start()).trim();
			} else {
				continue;
			}

			List<String> toolTitleProcessedWords = preProcessor.process(toolTitle);
			if (toolTitleProcessedWords.size() != 1) continue;
			String toolTitleProcessed = toolTitleProcessedWords.get(0);

			List<String> abstractSentences = preProcessor.sentences(preProcessor.removeLinks(publication.getAbstract().getContent()));
			List<List<String>> processed = new ArrayList<>();
			for (String sentence : abstractSentences) {
				processed.add(preProcessor.process(sentence));
			}

			Map<String, Double> scores = new HashMap<>();
			for (List<String> sentence : processed) {
				for (String word : sentence) {
					scores.merge(word, Math.pow(idf.getIdf(word), Common.QUERY_IDF_SCALING), Double::sum);
				}
			}

			for (List<String> sentenceProcessed : processed) {
				for (int i = 0; i < sentenceProcessed.size(); ++i) {
					if (sentenceProcessed.get(i).equals(toolTitleProcessed)) {
						if (i - 1 >= 0) before.merge(sentenceProcessed.get(i - 1), 1, Integer::sum);
						if (i - 2 >= 0) before.merge(sentenceProcessed.get(i - 2), 1, Integer::sum);
						if (i + 1 < sentenceProcessed.size()) after.merge(sentenceProcessed.get(i +	1), 1, Integer::sum);
						if (i + 2 < sentenceProcessed.size()) after.merge(sentenceProcessed.get(i +	2), 1, Integer::sum);
					}
				}
			}

			for (List<String> sentenceProcessed : processed) {
				for (int i = 0; i < sentenceProcessed.size(); ++i) {
					String wordProcessed = sentenceProcessed.get(i);
					all.merge(wordProcessed, 1, Integer::sum);
					if (i - 1 >= 0) {
						allBeforeScores.merge(wordProcessed, scores.get(sentenceProcessed.get(i - 1)), Double::sum);
						++allBeforeScoresSum;
					}
					if (i - 2 >= 0) {
						allBeforeScores.merge(wordProcessed, scores.get(sentenceProcessed.get(i - 2)), Double::sum);
						++allBeforeScoresSum;
					}
					if (i + 1 < sentenceProcessed.size()) {
						allAfterScores.merge(wordProcessed, scores.get(sentenceProcessed.get(i + 1)), Double::sum);
						++allAfterScoresSum;
					}
					if (i + 2 < sentenceProcessed.size()) {
						allAfterScores.merge(wordProcessed, scores.get(sentenceProcessed.get(i + 2)), Double::sum);
						++allAfterScoresSum;
					}
				}
			}
		}

		Map<String, Integer> beforeSorted = before.entrySet().stream()
			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) -> { throw new AssertionError(); }, LinkedHashMap::new));
		System.out.println("BEFORE_TOOL_TITLE\tCOUNT\tTOTAL\tPRECISION\tAVERAGE_SCORE\tPRECISION/AVERAGE_SCORE");
		for (Map.Entry<String, Integer> bs : beforeSorted.entrySet()) {
			String word = bs.getKey();
			int count = bs.getValue();
			int total = all.get(word);
			double precision = count / (double) total;
			Double totalScore = allAfterScores.get(word);
			double averageScore = (totalScore != null ? totalScore / allAfterScoresSum : 0);
			System.out.printf(Locale.ROOT, "%16s\t%d\t%d\t%.6f\t%.6f\t%8.1f\n", word, count, total, precision, averageScore, precision / averageScore);
		}
		System.out.println();
		Map<String, Integer> afterSorted = after.entrySet().stream()
			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) -> { throw new AssertionError(); }, LinkedHashMap::new));
		System.out.println("AFTER_TOOL_TITLE\tCOUNT\tTOTAL\tPRECISION\tAVERAGE_SCORE\tPRECISION/AVERAGE_SCORE");
		for (Map.Entry<String, Integer> as : afterSorted.entrySet()) {
			String word = as.getKey();
			int count = as.getValue();
			int total = all.get(word);
			double precision = count / (double) total;
			Double totalScore = allBeforeScores.get(word);
			double averageScore = (totalScore != null ? totalScore / allBeforeScoresSum : 0);
			System.out.printf(Locale.ROOT, "%16s\t%d\t%d\t%.6f\t%.6f\t%8.1f\n", word, count, total, precision, averageScore, precision / averageScore);
		}
	}

	static void europepmcAbstract(int min, String database, PreProcessor preProcessor, FetcherArgs fetcherArgs) throws IOException, ParseException {
		Marker mainMarker = MarkerManager.getMarker(PubMedApps.MAIN_MARKER);

		Map<String, Integer> tokenCount = new LinkedHashMap<>();

		try (Database db = new Database(database)) {
			for (PublicationIds publicationIds : db.getPublicationIds()) {
				Set<String> tokens = new HashSet<>();
				for (String token : preProcessor.process(db.getPublication(publicationIds).getAbstract().getContent())) {
					tokens.add(token);
				}
				for (String token : tokens) {
					tokenCount.merge(token, 1, (x, y) -> x + y);
				}
			}
		}

		String email = "";
		if (fetcherArgs.getPrivateArgs().getEuropepmcEmail() != null && !fetcherArgs.getPrivateArgs().getEuropepmcEmail().isEmpty()) {
			email = "&email=" + fetcherArgs.getPrivateArgs().getEuropepmcEmail();
		}
		Fetcher fetcher = new Fetcher(fetcherArgs.getPrivateArgs());

		Map<String, Integer> tokenCountSorted = tokenCount.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) -> { throw new AssertionError(); }, LinkedHashMap<String, Integer>::new));

		int last = 0;
		for (Map.Entry<String, Integer> token : tokenCountSorted.entrySet()) {
			if (token.getValue() < min) {
				break;
			} else {
				++last;
			}
		}

		int current = 0;
		long start = System.currentTimeMillis();
		for (Map.Entry<String, Integer> token : tokenCountSorted.entrySet()) {
			if (token.getValue() < min) {
				break;
			} else {
				++current;
			}
			System.err.print(PubFetcher.progress(current, last, start) + "  \r");

			try {
				String abstractQuery = new URI("https", "www.ebi.ac.uk", "/europepmc/webservices/rest/search", "resulttype=idlist&format=xml&query=abstract:" + token.getKey() + " src:med" + email, null).toASCIIString();
				Document doc = fetcher.getDoc(abstractQuery, false, fetcherArgs);
				if (doc != null) {
					Element hitCount = doc.getElementsByTag("hitCount").first();
					if (hitCount != null) {
						try {
							int count = Integer.parseInt(hitCount.text());
							double share = token.getValue() / (double) count;
							System.out.println(share + "\t" + token.getKey() + "\t" + token.getValue() + "\t" + count);
						} catch (NumberFormatException e) {
							logger.error(mainMarker, "hitCount is not a number in Europe PMC query " + abstractQuery, e);
						}
					} else {
						logger.error(mainMarker, "Element hitCount not found in Europe PMC query {}", abstractQuery);
					}
				} else {
					logger.error(mainMarker, "No Document returned for Europe PMC query {}", abstractQuery);
				}
			} catch (URISyntaxException e) {
				logger.error(mainMarker, "Invalid Europe PMC query URI!", e);
			}
		}
	}
}
