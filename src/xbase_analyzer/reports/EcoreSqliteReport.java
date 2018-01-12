package xbase_analyzer.reports;

import java.io.IOException;
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

	private void cleanupEcoreSqlite(Statement statement) throws IOException, SQLException {
		statement.execute("DELETE FROM dependencies");
		statement.execute("DELETE FROM eclass");
	}

	public void produceEcoreSqlite(final DefaultDirectedGraph<EClass, ? extends DefaultEdge> graph)
			throws SQLException, IOException {
		final Connection connection = DriverManager.getConnection("jdbc:sqlite:result.db");
		final Statement statement = connection.createStatement();
		initializeDatabase(statement);
		cleanupEcoreSqlite(statement);

		final List<EClass> sorted = graph.vertexSet().stream().sorted(new EClassNameComparator())
				.collect(Collectors.toList());

		final DijkstraShortestPath<EClass, ? extends DefaultEdge> dsp = new DijkstraShortestPath<>(graph);

		sorted.forEach(c1 -> {

			final List<String> line = new ArrayList<>();
			line.add(new EClassToString().apply(c1));

			final String c1EPackageName = c1.getEPackage().getName();
			final String c1Name = c1.getName();
			try {
				statement.execute("INSERT INTO eclass(epackage, name) SELECT \"" + c1EPackageName + "\", \"" + c1Name
						+ "\" WHERE NOT EXISTS (SELECT 1 FROM eclass WHERE epackage = \"" + c1EPackageName
						+ "\" and name = \"" + c1Name + "\")");
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
			sorted.forEach(c2 -> {
				try {

					final GraphPath<EClass, ? extends DefaultEdge> dst = dsp.getPath(c1, c2);
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

	private void initializeDatabase(final Statement statement) throws SQLException {
		statement.execute(
				"CREATE TABLE IF NOT EXISTS dependencies (pkgSrc TEXT, src TEXT, pkgDst TEXT, dst TEXT, distance NUMERIC)");
		statement.execute("CREATE TABLE IF NOT EXISTS eclass (name TEXT, epackage TEXT)");
	}

}
