package xbase_analyzer.reports;

import java.net.URI;

import org.eclipse.xtext.Grammar;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.TransactionWork;

public class XtextNeo4jReport {

	public void produce(final Grammar grammar) {

		final URI uri = URI.create("http://127.0.0.1:7474");
		final String user = "admin";
		final String password = "admin";
		final Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));

		try (Session session = driver.session()) {
session.writeTransaction(new TransactionWork<T>() {
})				
		}
		driver.close();
	}

}
