package xbase_analyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
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
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Test;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class Analyzer {
	private final class EClassNameComparator implements Comparator<EClass> {
		final EClassToString e2s = new EClassToString();

		@Override
		public int compare(final EClass o1, final EClass o2) {
			return e2s.apply(o1).compareTo(e2s.apply(o2));
		}
	}

	private final class EClassToString implements Function<EClass, String> {

		private final String separator;

		EClassToString() {
			this(".");
		}

		EClassToString(final String sep) {
			this.separator = sep;
		}

		@Override
		public String apply(final EClass eClass) {
			final EPackage ePackage = eClass.getEPackage();
			final String ret;
			if (ePackage != null) {
				ret = ePackage.getName() + separator + eClass.getName();
			} else {
				ret = eClass.getName();
			}

			return ret;
		}
	}

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
			}
			// else {
			// System.out.println(c + " is not an EClass");
			// }
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

		produceEcoreCSV(graph);
		produceEcoreGraphviz(graph);
		produceEcoreSqlite(graph);

	}

	private void produceEcoreSqlite(final DefaultDirectedGraph<EClass, DefaultEdge> graph)
			throws SQLException, IOException {
		cleanupEcoreSqlite();
		final Connection connection = DriverManager.getConnection("jdbc:sqlite:result.db");
		final Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE dependencies (pkgSrc TEXT, src TEXT, pkdDst TEXT, dst TEXT, lgt NUMERIC)");
		statement.execute("CREATE TABLE eclass (name TEXT, epackage TEXT)");

		final List<EClass> sorted = graph.vertexSet().stream().sorted(new EClassNameComparator())
				.collect(Collectors.toList());

		final DijkstraShortestPath<EClass, DefaultEdge> dsp = new DijkstraShortestPath<>(graph);

		sorted.forEach(c1 -> {

			final List<String> line = new ArrayList<>();
			line.add(new EClassToString().apply(c1));

			final String c1EPackageName = c1.getEPackage().getName();
			final String c1Name = c1.getName();
			try {
				final String sql = "INSERT INTO eclass(epackage, name) SELECT \"" + c1EPackageName + "\", \"" + c1Name
						+ "\" WHERE NOT EXISTS (SELECT 1 FROM eclass WHERE epackage = \"" + c1EPackageName
						+ "\" and name = \"" + c1Name + "\")";
				System.out.println(sql);
				statement.execute(sql);
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
			sorted.forEach(c2 -> {
				try {

					final GraphPath<EClass, DefaultEdge> dst = dsp.getPath(c1, c2);
					final Integer cell = Optional.ofNullable(dst).map(x -> x.getLength()).orElse(null);
					if (cell != null && cell > 0) {
						statement.execute(
								"INSERT INTO dependencies VALUES (\"" + c1EPackageName + "\", \"" + c1Name + "\", \""
										+ c2.getEPackage().getName() + "\", \"" + c2.getName() + "\", " + cell + ")");
					}
				} catch (final SQLException e) {
					e.printStackTrace();
				}
				// final GraphPath<EClass, DefaultEdge> dst = dsp.getPath(c1, c2);
				// final String cell = Optional.ofNullable(dst).map(x ->
				// String.valueOf(x.getLength())).orElse("");
				// line.add(cell);

			});

		});

		connection.close();

	}

	private void cleanupEcoreSqlite() throws IOException {
		final File file = new File("result.db");
		if (file.exists()) {
			Files.delete(file.toPath());
		}
	}

	private void produceEcoreGraphviz(final DefaultDirectedGraph<EClass, DefaultEdge> graph) throws IOException {
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

	private void produceEcoreCSV(final DefaultDirectedGraph<EClass, DefaultEdge> graph) {
		try {
			final CsvListWriter csv = new CsvListWriter(new FileWriter(new File("results.csv")),
					CsvPreference.STANDARD_PREFERENCE);

			final List<String> headers = buildCSVHeader(graph);
			csv.writeHeader(headers.toArray(new String[headers.size()]));

			final DijkstraShortestPath<EClass, DefaultEdge> dsp = new DijkstraShortestPath<>(graph);

			final List<EClass> sorted = graph.vertexSet().stream().sorted(new EClassNameComparator())
					.collect(Collectors.toList());

			sorted.forEach(c1 -> {

				final List<String> line = new ArrayList<>();
				line.add(new EClassToString().apply(c1));
				sorted.forEach(c2 -> {

					final GraphPath<EClass, DefaultEdge> dst = dsp.getPath(c1, c2);
					final String cell = Optional.ofNullable(dst).map(x -> String.valueOf(x.getLength())).orElse("");
					line.add(cell);
				});

				try {
					csv.write(line);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			});

			csv.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
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

	private List<String> buildCSVHeader(final DefaultDirectedGraph<EClass, DefaultEdge> graph) {
		final List<String> headers = graph.vertexSet().stream().map(new EClassToString()).sorted()
				.collect(Collectors.toList());

		final List<String> ret = new ArrayList<>();
		ret.add("");
		ret.addAll(headers);
		return ret;
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
