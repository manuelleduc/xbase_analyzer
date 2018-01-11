package xbase_analyzer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.xtext.AbstractRule;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class XtextGraphvizReport {

	public void produceXtextGraphviz(String name, DefaultDirectedGraph<AbstractRule, DefaultEdge> graph)
			throws IOException {
		final String nl = System.lineSeparator();
		final StringBuilder sb = new StringBuilder();
		sb.append("digraph {");
		sb.append(nl);

		final AbstractRuleToString eClassToString = new AbstractRuleToString("_");

		graph.vertexSet().forEach(e -> {
			final AbstractRuleToString eClassToStringDot = new AbstractRuleToString(".");
			sb.append(eClassToString.apply(e));
			sb.append(" [label=\"" + eClassToStringDot.apply(e) + "\"]");
			sb.append(nl);
		});

		graph.edgeSet().forEach(e -> {
			final AbstractRule src = graph.getEdgeSource(e);
			final AbstractRule tgt = graph.getEdgeTarget(e);

			final String ssrc = eClassToString.apply(src);
			final String stgt = eClassToString.apply(tgt);

			sb.append(ssrc + " -> " + stgt);
			sb.append(nl);
		});
		sb.append(nl);
		sb.append("}");

		final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("result_xtext_" + name + ".dot"));

		bufferedWriter.write(sb.toString());

		bufferedWriter.close();
	}

}
