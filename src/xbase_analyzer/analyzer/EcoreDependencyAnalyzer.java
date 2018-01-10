package xbase_analyzer.analyzer;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import xbase_analyzer.utils.ecore.EClassConsumer;

public class EcoreDependencyAnalyzer {
	public DefaultDirectedGraph<EClass, DefaultEdge> ecoreDependencyAnalysis(final EPackage epackage) {
		final Set<EClass> visitedClasses = new HashSet<>();
		final Set<EPackage> visitedPackages = new HashSet<>();
		visitedPackages.add(epackage);
		final DefaultDirectedGraph<EClass, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		// Produce a dependency graph of the targeted EPackage
		epackage.getEClassifiers().forEach(new EClassConsumer(visitedClasses, visitedPackages, graph));
		return graph;
	}
}
