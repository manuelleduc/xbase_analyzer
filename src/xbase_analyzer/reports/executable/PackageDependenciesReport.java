package xbase_analyzer.reports.executable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Reads the sqlite database produced by @see xbase_analyzer.Analyzer and build
 * a dot file of the graph dependencies between packages.
 * 
 * @author mleduc
 *
 */
public class PackageDependenciesReport {
	public static void main(final String[] args) throws SQLException, IOException {
		new PackageDependenciesReport().execute();
	}

	private void execute() throws SQLException, IOException {
		final String nl = System.lineSeparator();
		final Connection connection = DriverManager.getConnection("jdbc:sqlite:result.db");
		final Statement statement = connection.createStatement();

		final ResultSet rs = statement.executeQuery("SELECT DISTINCT pkgSrc, pkgDst, count(*) " + "FROM dependencies "
				+ "where pkgDst <> dependencies.pkgSrc and lgt = 1 " + "GROUP BY pkgSrc, pkgDst");

		final StringBuilder sb = new StringBuilder();

		sb.append("digraph {");
		sb.append(nl);
		while (rs.next()) {
			
			final String column1 = rs.getString(1);
			final String column2 = rs.getString(2);
			final int column3 = rs.getInt(3);
			sb.append(column1);
			sb.append(" -> ");
			sb.append(column2);
			sb.append("\t[penwidth=" + column3 / 10.0 + ", label=" + column3 + ", weight=" + column3 + "]");
			sb.append(nl);
		}
		sb.append("}");

		final FileWriter fw = new FileWriter(new File("packages_dependencies.dot"));

		fw.write(sb.toString());

		fw.close();

	}
}
