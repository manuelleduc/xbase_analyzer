package xbase_analyzer.reports;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Grammar;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import xbase_analyzer.utils.xtext.XtextUtil;

public class XtextSqliteReport {

	private final XtextUtil xtextUtil = new XtextUtil();

	public void xtextDependencyDB(final Grammar grammar, final DefaultDirectedGraph<AbstractRule, DefaultEdge> graph,
			final List<AbstractRule> rules) throws SQLException {

		final Connection connection = DriverManager.getConnection("jdbc:sqlite:result.db");
		final Statement statement = connection.createStatement();
		initializeDatabase(statement);
		cleanupEcoreSqlite(statement);

		final DijkstraShortestPath<AbstractRule, DefaultEdge> dsp = new DijkstraShortestPath<>(graph);

		rules.forEach(ar -> {
			if (graph.containsVertex(ar)) {
				final Grammar g = xtextUtil.lookupGrammar(ar);

				try {
					statement.execute("INSERT INTO xtext (grammar, rule) SELECT \"" + g.getName() + "\", \""
							+ ar.getName() + "\" WHERE NOT EXISTS (SELECT 1 FROM xtext WHERE grammar = \"" + g.getName()
							+ "\" and rule = \"" + ar.getName() + "\");");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});

		rules.stream().filter(r -> {
			final Grammar g = xtextUtil.lookupGrammar(r);
			return g == grammar;
		}).forEach(ar1 -> {
			rules.forEach(ar2 -> {
				if (graph.containsVertex(ar1) && graph.containsVertex(ar2)) {
					final GraphPath<AbstractRule, DefaultEdge> graphPath = dsp.getPath(ar1, ar2);
					final Integer dst = Optional.ofNullable(graphPath).map(x -> x.getLength()).orElse(null);
					if (dst != null && dst > 0) {
						try {

							final Grammar g1 = xtextUtil.lookupGrammar(ar1);
							final Grammar g2 = xtextUtil.lookupGrammar(ar2);

							statement.execute(
									"INSERT INTO xtext_dependencies (grammarSrc, ruleSrc, grammarDst, ruleDst, dst) SELECT \""
											+ g1.getName() + "\", \"" + ar1.getName() + "\", \"" + g2.getName()
											+ "\", \"" + ar2.getName() + "\", " + dst + "\n"
											+ "WHERE NOT EXISTS (SELECT 1 FROM xtext_dependencies WHERE grammarDst = \""
											+ g1.getName() + "\" and ruleSrc = \"" + ar1.getName()
											+ "\" and grammarDst = \"" + g2.getName() + "\" and ruleDst = \""
											+ ar2.getName() + "\" and dst = " + dst + ");");
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else {
					// we do not deal with mandatory elements, defined in the core of xtext such as
					// ML_COMMENT or WS or ANY_OTHER.
					// System.out.println("ERR");
					// System.out.println(ar1 + " " + graph.containsVertex(ar1));
					// System.out.println(ar2 + " " + graph.containsVertex(ar2));
				}
			});
		});
	}

	private void cleanupEcoreSqlite(Statement statement) throws SQLException {
		statement.execute("DELETE FROM xtext");
		statement.execute("DELETE FROM xtext_dependencies");

	}

	private void initializeDatabase(Statement statement) throws SQLException {
		statement.execute("CREATE TABLE  IF NOT EXISTS xtext (grammar TEXT, rule  TEXT)");
		statement.execute(
				"CREATE TABLE IF NOT EXISTS  xtext_dependencies ( grammarSrc TEXT, ruleSrc TEXT, grammarDst TEXT, ruleDst TEXT, dst NUMERIC )");

	}
}
