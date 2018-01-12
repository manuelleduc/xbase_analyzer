package xbase_analyzer.reports.executable;

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
public class XtextRulesUsageReport {
	public static void main(final String[] args) throws SQLException, IOException {
		new XtextRulesUsageReport().execute();
	}

	private void execute() throws SQLException, IOException {
		extracted("org.xtext.builddsl.BuildDSL");
		extracted("org.xtext.guicemodules.GuiceModules");
		extracted("org.xtext.httprouting.Route");
		extracted("org.xtext.mongobeans.MongoBeans");
		extracted("org.xtext.scripting.Scripting");
		extracted("org.xtext.template.Template");
		extracted("org.xtext.tortoiseshell.TortoiseShell");

	}

	private void extracted(String string) throws SQLException {
		final String nl = System.lineSeparator();
		final Connection connection = DriverManager.getConnection("jdbc:sqlite:result.db");
		final Statement statement = connection.createStatement();

		final ResultSet rs = statement.executeQuery(
				"SELECT grammar, rule FROM xtext WHERE grammar IN ('org.eclipse.xtext.xbase.Xbase', 'org.eclipse.xtext.xbase.Xtype', 'org.eclipse.xtext.xbase.annotations.XbaseWithAnnotations')");

		while (rs.next()) {
			final String grammar = rs.getString(1);
			final String rule = rs.getString(2);

			final Statement statement2 = connection.createStatement();
			final ResultSet executeQuery = statement2
					.executeQuery("SELECT count(*) FROM xtext_dependencies WHERE grammarSrc = '" + string
							+ "' and grammarDst = " + "\"" + grammar + "\" AND  ruleDst = \"" + rule + "\"");

			executeQuery.next();
			final int count = executeQuery.getInt(1);

			if (count == 0) {
				System.out.println(grammar + "." + rule + " is never used in " + string);
			}
		}
	}
}
