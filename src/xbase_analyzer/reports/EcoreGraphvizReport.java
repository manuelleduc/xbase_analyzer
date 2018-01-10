package xbase_analyzer.reports;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.emf.ecore.EClass;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import xbase_analyzer.EClassToString;

public class EcoreGraphvizReport {

	public void produceEcoreGraphviz(final DefaultDirectedGraph<EClass, DefaultEdge> graph) throws IOException {
		final String nl = System.lineSeparator();
		final StringBuilder sb = new StringBuilder();
		sb.append("digraph {");
		sb.append(nl);
		graph.edgeSet().forEach(e -> {
			final EClass src = graph.getEdgeSource(e);
			final EClass tgt = graph.getEdgeTarget(e);

			final EClassToString eClassToString = new EClassToString("_");
			final String ssrc = eClassToString.apply(src);
			final String stgt = eClassToString.apply(tgt);

			sb.append(ssrc + " -> " + stgt);
			sb.append(nl);
		});
		sb.append(nl);
		sb.append("}");

		final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("result.dot"));

		bufferedWriter.write(sb.toString());

		bufferedWriter.close();

	}

}
