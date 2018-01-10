package xbase_analyzer;

import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public final class EClassConsumer implements Consumer<EClassifier> {
	private final Set<EClass> visitedClasses;
	private final Set<EPackage> visitedPackages;
	private final DefaultDirectedGraph<EClass, DefaultEdge> graph;

	public EClassConsumer(final Set<EClass> visitedClasses, final Set<EPackage> visitedPackages,
			final DefaultDirectedGraph<EClass, DefaultEdge> graph) {
		this.visitedClasses = visitedClasses;
		this.visitedPackages = visitedPackages;
		this.graph = graph;
	}

	@Override
	public void accept(final EClassifier c) {
		if (c instanceof EClass) {
			if (!visitedClasses.contains(c)) {
				final EClass cls = (EClass) c;
				markVisited(cls);
				registerClass(cls);
				final EList<EClass> eSuperTypes = cls.getESuperTypes();
				eSuperTypes.forEach(pc -> {
					registerClass(pc);
					graph.addEdge(cls, pc);
				});
				eSuperTypes.forEach(new EClassConsumer(visitedClasses, visitedPackages, graph));

				cls.getEAllReferences().stream().filter(r -> r.getEType() instanceof EClass).map(r -> {
					final EClass eType = (EClass) r.getEType();
					registerClass(eType);
					graph.addEdge(cls, eType);
					return eType;
				}).forEach(new EClassConsumer(visitedClasses, visitedPackages, graph));
			}
		}
	}

	private void markVisited(final EClass cls) {
		visitedClasses.add(cls);
		registerPackage(cls.getEPackage());
	}

	private void registerPackage(final EPackage ePackage) {
		if (ePackage != null && !visitedPackages.contains(ePackage)) {
			visitedPackages.add(ePackage);
			final EList<EClassifier> eClassifiers = ePackage.getEClassifiers();
			eClassifiers.forEach(new EClassConsumer(visitedClasses, visitedPackages, graph));
		}
	}

	private void registerClass(final EClass cls) {
		if (!graph.containsVertex(cls)) {
			graph.addVertex(cls);
		}
		registerPackage(cls.getEPackage());
	}
}