package xbase_analyzer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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

import xbase_analyzer.reports.EcoreCSVReport;
import xbase_analyzer.reports.EcoreGraphvizReport;
import xbase_analyzer.reports.EcoreSqliteReport;

public class Analyzer {
	public static void main(final String[] args) throws Exception {
		new Analyzer().exec();
	}

	@Test
	public void test() throws Exception {
		new Analyzer().exec();
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

	public void exec() throws IOException, SQLException {
		this.ecoreAnalysis("/org.eclipse.xtext.xbase/model/XAnnotations.ecore");
	}

	private void ecoreAnalysis(final String path) throws IOException, SQLException {
		final ResourceSet set = initResourceSet();
		final Resource resource = set.getResource(URI.createPlatformPluginURI(path, false), true);

		final EPackage epackage = (EPackage) resource.getContents().get(0);

		final Set<EClass> visitedClasses = new HashSet<>();
		final Set<EPackage> visitedPackages = new HashSet<>();
		visitedPackages.add(epackage);
		final DefaultDirectedGraph<EClass, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

		// Produce a dependency graph of the targeted EPackage
		epackage.getEClassifiers().forEach(new EClassConsumer(visitedClasses, visitedPackages, graph));

		new EcoreCSVReport().produceEcoreCSV(graph);
		new EcoreGraphvizReport().produceEcoreGraphviz(graph);
		new EcoreSqliteReport().produceEcoreSqlite(graph);

	}

	private ResourceSet initResourceSet() {
		final ResourceSet set = new XtextResourceSet();

		final URIHandlerImpl handler = new org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl() {

			@Override
			public URI resolve(final URI uri) {
				if (uri.isPlatformResource()) {
					return URI.createURI(uri.toString().replaceAll("resource", "plugin"), false);
				}
				return super.resolve(uri);
			}

		};
		set.getLoadOptions().put(XMLResource.OPTION_URI_HANDLER, handler);
		return set;
	}

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
