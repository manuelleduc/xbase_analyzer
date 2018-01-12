package xbase_analyzer.reports;

import static org.neo4j.driver.v1.Values.parameters;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

import xbase_analyzer.utils.ecore.NamedEdge;

public class EcoreNeo4jReport {

	public void produce(final DefaultDirectedGraph<EClass, NamedEdge> graph) {
		this.cleanup();
		final String url = "bolt://localhost:7687";
		final String user = "xbase";
		final String password = "xbase";
		final Driver driver = GraphDatabase.driver(url, AuthTokens.basic(user, password));

		try (Session session = driver.session()) {

			for (final EClass eClass : graph.vertexSet()) {

				final EPackage ePackage = eClass.getEPackage();

				session.writeTransaction(new TransactionWork<String>() {

					@Override
					public String execute(final Transaction tx) {
						tx.run("MERGE (a:EClass " + "{class: $class, package: $package})",
								parameters("class", eClass.getName(), "package", ePackage.getName()));
						return null;
					}
				});
			}

			for (final NamedEdge edge : graph.edgeSet()) {

				final EClass eClassA = graph.getEdgeSource(edge);
				final EPackage ePackageA = eClassA.getEPackage();
				final EClass eClassB = graph.getEdgeTarget(edge);
				final EPackage ePackageB = eClassB.getEPackage();

				session.writeTransaction(new TransactionWork<String>() {

					@Override
					public String execute(final Transaction tx) {
						tx.run("MATCH (a:EClass {class: $classa, package: $packagea}), (b:EClass {class: $classb, package: $packageb}) "
								+ "MERGE (a)-[:" + edge.getName() + "]->(b)",
								parameters("classa", eClassA.getName(), "packagea", ePackageA.getName(), "classb",
										eClassB.getName(), "packageb", ePackageB.getName()));
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
					tx.run("MATCH (n:Ecore) DETACH DELETE n");
					return null;
				}
			});
		}
		driver.close();

	}

}
