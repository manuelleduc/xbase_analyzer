package xbase_analyzer.reports;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import xbase_analyzer.utils.ecore.EClassNameComparator;
import xbase_analyzer.utils.ecore.EClassToString;

public class EcoreSqliteReport {

	private void cleanupEcoreSqlite() throws IOException {
		final File file = new File("result.db");
		if (file.exists()) {
			Files.delete(file.toPath());
		}
	}

	public void produceEcoreSqlite(final DefaultDirectedGraph<EClass, DefaultEdge> graph)
			throws SQLException, IOException {
		cleanupEcoreSqlite();
		final Connection connection = DriverManager.getConnection("jdbc:sqlite:result.db");
		final Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE dependencies (pkgSrc TEXT, src TEXT, pkdDst TEXT, dst TEXT, lgt NUMERIC)");
		statement.execute("CREATE TABLE eclass (name TEXT, epackage TEXT)");

		final List<EClass> sorted = graph.vertexSet().stream().sorted(new EClassNameComparator())
				.collect(Collectors.toList());

		final DijkstraShortestPath<EClass, DefaultEdge> dsp = new DijkstraShortestPath<>(graph);

		sorted.forEach(c1 -> {

			final List<String> line = new ArrayList<>();
			line.add(new EClassToString().apply(c1));

			final String c1EPackageName = c1.getEPackage().getName();
			final String c1Name = c1.getName();
			try {
				final String sql = "INSERT INTO eclass(epackage, name) SELECT \"" + c1EPackageName + "\", \"" + c1Name
						+ "\" WHERE NOT EXISTS (SELECT 1 FROM eclass WHERE epackage = \"" + c1EPackageName
						+ "\" and name = \"" + c1Name + "\")";
				System.out.println(sql);
				statement.execute(sql);
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
			sorted.forEach(c2 -> {
				try {

					final GraphPath<EClass, DefaultEdge> dst = dsp.getPath(c1, c2);
					final Integer cell = Optional.ofNullable(dst).map(x -> x.getLength()).orElse(null);
					if (cell != null && cell > 0) {
						statement.execute(
								"INSERT INTO dependencies VALUES (\"" + c1EPackageName + "\", \"" + c1Name + "\", \""
										+ c2.getEPackage().getName() + "\", \"" + c2.getName() + "\", " + cell + ")");
					}
				} catch (final SQLException e) {
					e.printStackTrace();
				}
				// final GraphPath<EClass, DefaultEdge> dst = dsp.getPath(c1, c2);
				// final String cell = Optional.ofNullable(dst).map(x ->
				// String.valueOf(x.getLength())).orElse("");
				// line.add(cell);

			});

		});

		connection.close();

	}

}
