package xbase_analyzer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XForLoopExpression;
import org.eclipse.xtext.xbase.XIfExpression;
import org.eclipse.xtext.xbase.XStringLiteral;
import org.eclipse.xtext.xbase.XbasePackage;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotation;
import org.eclipse.xtext.xtype.XtypePackage;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Test;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class Analyzer {
	private final class EClassConsumer implements Consumer<EClassifier> {
		private final Set<EClass> visitedClasses;
		private final Set<EPackage> visitedPackages;
		private final DefaultDirectedGraph<EClass, DefaultEdge> graph;

		private EClassConsumer(final Set<EClass> visitedClasses, final Set<EPackage> visitedPackages,
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
			} else {
				System.out.println(c + " is not an EClass");
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

	public static void main(final String[] args) throws IOException {
		new Analyzer().exec();
	}

	@Test
	public void testLOL() throws IOException {

		new Analyzer().exec();
		// new Analyzer().checkClassesHierarchy();

		// new Analyzer().isParentOf(, QualifiedName.class);

	}

	private void checkClassesHierarchy() {
		final Class<?>[] classes = { XExpression.class, XBlockExpression.class, JvmTypeReference.class,
				// XImportSection.class,
				XAnnotation.class, JvmFormalParameter.class, XForLoopExpression.class, XStringLiteral.class,
				XIfExpression.class };

		for (int i = 0; i < classes.length; i++) {
			for (int j = i + 1; j < classes.length; j++) {
				new Analyzer().isParentOf(classes[i], classes[j]);
			}
		}
	}

	private void isParentOf(final Class<?> a, final Class<?> b) {
		final boolean ab = a.isAssignableFrom(b);
		final boolean ba = b.isAssignableFrom(a);

		if (ab && ba) {
			System.out.println(a.getName() + " and " + b.getName() + " are probably the same");
		} else if (!ab && ba) {
			System.out.println(a.getName() + " is a child of  " + b.getName());
		} else if (ab && !ba) {
			System.out.println(a.getName() + " is a parent of  " + b.getName());
		} else {
			// System.out.println(a.getName() + "and " + b.getName() + "are not of the same
			// hierarchy");
		}

	}

	@Inject
	private Provider<ResourceSet> resourceSetProvider;

	public void exec() throws IOException {
		// final Injector injector = new
		// XtextStandaloneSetup().createInjectorAndDoEMFRegistration();
		// final Analyzer main = injector.getInstance(Analyzer.class);

		// main.xtextAnalysis(
		// "/home/mleduc/git/xtext-extras/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xtype.xtext");

		// main.xtextAnalysis(
		// "/home/mleduc/git/xtext-extras/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xbase.xtext");
		//
		//
		// System.out.println("-----------------------");

		// this.ecoreAnalysis("/home/mleduc/git/xtext-extras/org.eclipse.xtext.xbase/model/XAnnotations.ecore");
		this.ecoreAnalysis("/org.eclipse.xtext.xbase/model/XAnnotations.ecore");

		// main.xtextAnalysis(
		// "/home/mleduc/git/xtext-extras/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/annotations/XbaseWithAnnotations.xtext");

		// ecoreAnalysis();
	}

	private void ecoreAnalysis(final String file) {
		// final ResourceSet set = new ResourceSetImpl();

		final ResourceSet set = new XtextResourceSet();

		// set.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
		// "ecore", new EcoreResourceFactoryImpl());
		//
		// final XtextPlatformResourceURIHandler handler = new
		// XtextPlatformResourceURIHandler();

		final URIHandlerImpl handler = new org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl() {

			@Override
			public URI resolve(final URI uri) {
				if (uri.isPlatformResource()) {
					return URI.createURI(uri.toString().replaceAll("resource", "plugin"), false);
				}
				return super.resolve(uri);
			}

		};
		// tru.setResourceSet(set);
		set.getLoadOptions().put(XMLResource.OPTION_URI_HANDLER, handler);
		final Resource resource = set.getResource(URI.createPlatformPluginURI(file, false), true);

		// EcoreUtil2.resolveAll(resource.getContents().get(0));
		// System.out.println(EcorePlugin.getPlatformResourceMap());
		// System.out.println(
		// EcorePlugin.resolvePlatformResourcePath("/org.eclipse.xtext.common.types/model/JavaVMTypes.ecore"));
		//
		// System.out.println(set.getResource(
		// URI.createPlatformPluginURI("/org.eclipse.xtext.common.types/model/JavaVMTypes.ecore",
		// false), true));

		final EPackage epackage = (EPackage) resource.getContents().get(0);

		final Set<EClass> visitedClasses = new HashSet<>();
		final Set<EPackage> visitedPackages = new HashSet<>();
		visitedPackages.add(epackage);

		final DefaultDirectedGraph<EClass, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

		// Produce a dependency graph of the targeted EPackage
		epackage.getEClassifiers().forEach(new EClassConsumer(visitedClasses, visitedPackages, graph));

		System.out.println(graph);
		System.out.println(graph.vertexSet().size());
		System.out.println(graph.edgeSet().size());
		System.out.println(
				visitedPackages.stream().map(x -> x.toString()).collect(Collectors.joining(System.lineSeparator())));

		try {
			final CsvListWriter csv = new CsvListWriter(new FileWriter(new File("results.csv")),
					CsvPreference.STANDARD_PREFERENCE);

			final List<String> headers = graph.vertexSet().stream().filter(x -> x.getName() != null).map(x -> {
				final EPackage ePackage2 = x.getEPackage();
				final String ret;
				if (ePackage2 != null) {
					ret = ePackage2.getName() + "." + x.getName();
				} else {
					ret = x.getName();
				}

				return ret;
			}).sorted().collect(Collectors.toList());

			final List<String> headers2 = new ArrayList<>();
			headers2.add("");
			headers2.addAll(headers);

			csv.writeHeader(headers2.toArray(new String[headers2.size()]));

			csv.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}

	// xtextAnalysis(file);

	private void xtextAnalysis(final String file) throws IOException {
		final ResourceSet set = resourceSetProvider.get();
		final Resource resource = set.getResource(URI.createFileURI(file), true);

		final Grammar grammar = (Grammar) resource.getContents().get(0);

		final CsvListWriter csvWriter = new CsvListWriter(new OutputStreamWriter(System.out),
				CsvPreference.EXCEL_PREFERENCE);

		csvWriter.writeHeader("grammar", "Rule", "Type");

		final Stream<AbstractRule> concat = allRules(grammar);

		concat.forEach(r -> {
			try {
				csvWriter.write(Arrays.asList(((Grammar) r.eContainer()).getName(), r.getName(),
						Optional.ofNullable(r.getType().getClassifier()).map(x -> x.getName()).orElse("")));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		});

		csvWriter.close();
	}

	private Stream<AbstractRule> allRules(final Grammar grammar) {
		return Stream.concat(grammar.getUsedGrammars().stream().flatMap(this::allRules), grammar.getRules().stream());
	}

	private static void ecoreAnalysis() {
		final EList<EClassifier> xBaseClassifiers = XbasePackage.eINSTANCE.getEClassifiers();
		final EList<EClassifier> xtypeClassifiers = XtypePackage.eINSTANCE.getEClassifiers();

		final Stream<String> xBaseDependencies = extractDependencies(xBaseClassifiers).distinct().sorted();
		final Stream<String> xtypeDependencies = extractDependencies(xtypeClassifiers).distinct().sorted();
		// final Stream<String> dependencies = Stream.concat(xBaseDependencies,
		// xtypeDependencies).distinct().sorted();
		final String a = xBaseDependencies.collect(Collectors.joining(System.lineSeparator()));
		final String b = xtypeDependencies.collect(Collectors.joining(System.lineSeparator()));

		// subgraph clusterX
		System.out.println("digraph xbase_ecorex {");
		System.out.println("subgraph cluster0 {");
		System.out.println(a);
		System.out.println("}");

		System.out.println("subgraph cluster1 {");
		System.out.println(b);
		System.out.println("}");
		System.out.println("}");
	}

	private static Stream<String> extractDependencies(final EList<EClassifier> eClassifiers) {
		final Stream<String> hierarchy = eClassifiers.stream().filter(x -> x instanceof EClass).map(x -> ((EClass) x))
				.flatMap(c -> c.getESuperTypes().stream()
						.map(sc -> generateEcoreName(c) + " -> " + generateEcoreName(sc)));

		final Stream<String> references = eClassifiers.stream().filter(x -> x instanceof EClass).map(x -> ((EClass) x))
				.flatMap(c -> c.getEReferences().stream().map(x -> x.getEType()).map(sc -> c.getEPackage().getName()
						+ "_" + c.getName() + " -> " + sc.getEPackage().getName() + "_" + sc.getName()));

		return Stream.concat(hierarchy, references);
	}

	private static String generateEcoreName(final EClass c) {
		return "ecore_" + c.getEPackage().getName() + "_" + c.getName();
	}
}
