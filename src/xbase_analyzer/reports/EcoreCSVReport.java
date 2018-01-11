package xbase_analyzer.reports;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import xbase_analyzer.utils.ecore.EClassNameComparator;
import xbase_analyzer.utils.ecore.EClassToString;

public class EcoreCSVReport {
	public void produceEcoreCSV(final DefaultDirectedGraph<EClass, DefaultEdge> graph) {
		try {
			final CsvListWriter csv = new CsvListWriter(new FileWriter(new File("results.csv")),
					CsvPreference.EXCEL_PREFERENCE);

			final List<String> headers = buildCSVHeader(graph);
			csv.writeHeader(headers.toArray(new String[headers.size()]));

			final DijkstraShortestPath<EClass, DefaultEdge> dsp = new DijkstraShortestPath<>(graph);

			final List<EClass> sorted = graph.vertexSet().stream().sorted(new EClassNameComparator())
					.collect(Collectors.toList());

			sorted.forEach(c1 -> {

				final List<String> line = new ArrayList<>();
				line.add(new EClassToString().apply(c1));
				sorted.forEach(c2 -> {

					final GraphPath<EClass, DefaultEdge> dst = dsp.getPath(c1, c2);
					final String cell = Optional.ofNullable(dst).map(x -> String.valueOf(x.getLength())).orElse("");
					line.add(cell);
				});

				try {
					csv.write(line);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			});

			csv.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private List<String> buildCSVHeader(final DefaultDirectedGraph<EClass, DefaultEdge> graph) {
		final List<String> headers = graph.vertexSet().stream().map(new EClassToString()).sorted()
				.collect(Collectors.toList());

		final List<String> ret = new ArrayList<>();
		ret.add("");
		ret.addAll(headers);
		return ret;
	}
}
