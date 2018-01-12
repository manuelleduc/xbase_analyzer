package xbase_analyzer.reports;

import static org.neo4j.driver.v1.Values.parameters;

import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Grammar;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

import xbase_analyzer.utils.xtext.XtextUtil;

public class XtextNeo4jReport {

	private final XtextUtil xtextUtil = new XtextUtil();

	public void produce(final DefaultDirectedGraph<AbstractRule, DefaultEdge> graph) {

		final String url = "bolt://localhost:7687";
		final String user = "xbase";
		final String password = "xbase";
		final Driver driver = GraphDatabase.driver(url, AuthTokens.basic(user, password));

		try (Session session = driver.session()) {

			for (final AbstractRule abstractRule : graph.vertexSet()) {

				final Grammar grammar = xtextUtil.lookupGrammar(abstractRule);

				session.writeTransaction(new TransactionWork<String>() {

					@Override
					public String execute(final Transaction tx) {
						tx.run("MERGE (a:Rule " + "{rule: $rule, grammar: $grammar})",
								parameters("rule", abstractRule.getName(), "grammar", grammar.getName()));
						return null;
					}
				});
			}

			for (final DefaultEdge edge : graph.edgeSet()) {

				final AbstractRule rulea = graph.getEdgeSource(edge);
				final Grammar grammara = xtextUtil.lookupGrammar(rulea);
				final AbstractRule ruleb = graph.getEdgeTarget(edge);
				final Grammar grammarb = xtextUtil.lookupGrammar(ruleb);

				session.writeTransaction(new TransactionWork<String>() {

					@Override
					public String execute(final Transaction tx) {
						tx.run("MATCH (a:Rule {rule: $rulea, grammar: $grammara}), (b:Rule {rule: $ruleb, grammar: $grammarb}) "
								+ "MERGE (a)-[:DEPENDS_OF]->(b)",
								parameters("rulea", rulea.getName(), "grammara", grammara.getName(), "ruleb",
										ruleb.getName(), "grammarb", grammarb.getName()));
						return null;
					}
				});
			}
		}
		driver.close();
	}

	public void cleanup() {
		final String url = "bolt://localhost:7687";
		final String user = "xbase";
		final String password = "xbase";
		final Driver driver = GraphDatabase.driver(url, AuthTokens.basic(user, password));

		try (Session session = driver.session()) {
			session.writeTransaction(new TransactionWork<String>() {

				@Override
				public String execute(final Transaction tx) {
					tx.run("MATCH (n) DETACH DELETE n");
					return null;
				}
			});
		}
		driver.close();

	}

}
