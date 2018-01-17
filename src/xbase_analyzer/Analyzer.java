package xbase_analyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.GrammarToDot;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.XtextStandaloneSetup;
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
import com.google.inject.Injector;
import com.google.inject.Provider;

import xbase_analyzer.analyzer.EcoreDependencyAnalyzer;
import xbase_analyzer.reports.EcoreCSVReport;
import xbase_analyzer.reports.EcoreGraphvizReport;
import xbase_analyzer.reports.EcoreNeo4jReport;
import xbase_analyzer.reports.EcoreSqliteReport;
import xbase_analyzer.reports.XtextNeo4jReport;
import xbase_analyzer.reports.XtextSqliteReport;
import xbase_analyzer.utils.ecore.NamedEdge;

public class Analyzer {

	@Inject
	private Provider<ResourceSet> resourceSetProvider;

	public static void main(final String[] args) throws Exception {
		new Analyzer().exec();
	}

	@Test
	public void test() throws Exception {
		new Analyzer().exec();
	}

	public void exec() throws IOException, SQLException {

		// this.ecoreAnalysis("/org.eclipse.xtext.xbase/model/XAnnotations.ecore",
		// "/org.eclipse.xtext.xbase/model/Xtype.ecore",
		// "/org.xtext.builddsl/model/generated/BuildDSL.ecore",
		// "/org.xtext.guicemodules/model/generated/GuiceModules.ecore",
		// "/org.xtext.httprouting/model/generated/Route.ecore",
		// "/org.xtext.mongobeans/model/generated/MongoBeans.ecore",
		// "/org.xtext.scripting/model/generated/Scripting.ecore",
		// "/org.xtext.template/model/generated/Template.ecore",
		// "/org.xtext.tortoiseshell/model/generated/TortoiseShell.ecore",
		// "/org.eclipse.xtext.xbase/model/Xbase.ecore",
		// "/org.eclipse.xtext.common.types/model/JavaVMTypes.ecore");

		// this.ecoreAnalysis("/org.eclipse.xtext.mql/src-gen/org/eclipse/xtext/mqrepl/ModelQueryLanguage.ecore",
		// "/org.eclipse.xtext.xbase/model/Xtype.ecore",
		// "/org.eclipse.xtext.xbase/model/Xbase.ecore",
		// "/org.eclipse.xtext.common.types/model/JavaVMTypes.ecore");

		// this.ecoreAnalysis("/eu.jgen.notes.dm/model/generated/DataModel.ecore",
		// "/org.eclipse.xtext.xbase/model/XAnnotations.ecore",
		// "/org.eclipse.xtext.xbase/model/Xtype.ecore",
		// "/org.eclipse.xtext.common.types/model/JavaVMTypes.ecore",
		// "/org.eclipse.xtext.xbase/model/Xbase.ecore");

		// this.ecoreAnalysis("/com.nukulargames.gdx4e.actors.model/model/Actors.ecore",
		// "/org.eclipse.xtext.xbase/model/Xtype.ecore",
		// "/org.eclipse.xtext.common.types/model/JavaVMTypes.ecore",
		// "/org.eclipse.xtext.xbase/model/XAnnotations.ecore");

		this.ecoreAnalysis("/org.xtext.language/model/generated/Language.ecore",
				"/org.eclipse.xtext.xbase/model/XAnnotations.ecore", "/org.eclipse.xtext.xbase/model/Xtype.ecore",
				"/org.eclipse.xtext.xbase/model/Xbase.ecore",
				"/org.eclipse.xtext.common.types/model/JavaVMTypes.ecore");

		// BuildDSL
		xtextAnalysis();

	}

	private void xtextAnalysis() throws IOException, SQLException {

		
		new XtextSqliteReport().init();
		new XtextNeo4jReport().cleanup();

		// this.xtextAnalysis("/org.xtext.builddsl/src/org/xtext/builddsl/BuildDSL.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xbase.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xtype.xtext");
		//
		// // GuiceModules
		// this.xtextAnalysis("/org.xtext.guicemodules/src/org/xtext/guicemodules/GuiceModules.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/annotations/XbaseWithAnnotations.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xbase.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xtype.xtext");
		//
		// // Route
		// this.xtextAnalysis("/org.xtext.httprouting/src/org/xtext/httprouting/Route.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/annotations/XbaseWithAnnotations.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xbase.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xtype.xtext");
		//
		// // MongoBeans
		// this.xtextAnalysis("/org.xtext.mongobeans/src/org/xtext/mongobeans/MongoBeans.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xbase.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xtype.xtext");
		//
		// // Scripting
		// this.xtextAnalysis("/org.xtext.scripting/src/org/xtext/scripting/Scripting.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xbase.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xtype.xtext");
		//
		// // Template
		// this.xtextAnalysis("/org.xtext.template/src/org/xtext/template/Template.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/annotations/XbaseWithAnnotations.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xbase.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xtype.xtext");
		//
		// // TortoiseShell
		// this.xtextAnalysis("/org.xtext.tortoiseshell/src/org/xtext/tortoiseshell/TortoiseShell.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xbase.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xtype.xtext");

		// this.xtextAnalysis("/org.eclipse.xtext.mql/src/org/eclipse/xtext/mqrepl/ModelQueryLanguage.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xbase.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xtype.xtext");

		// this.xtextAnalysis("/eu.jgen.notes.dm/src/eu/jgen/notes/dm/DataModel.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/annotations/XbaseWithAnnotations.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xbase.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xtype.xtext");

		// this.xtextAnalysis("/com.nukulargames.gdx4e.actors.dsl/src/com/nukulargames/gdx4e/actors/dsl/Dsl.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/annotations/XbaseWithAnnotations.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xbase.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xtype.xtext");

		// this.xtextAnalysis("/org.xtext.language/src/org/xtext/language/Language.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/annotations/XbaseWithAnnotations.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xbase.xtext",
		// "/org.eclipse.xtext.xbase/src/org/eclipse/xtext/xbase/Xtype.xtext");
	}

	private void ecoreAnalysis(final String... paths) throws IOException, SQLException {
		final ResourceSet set = initResourceSet();
		final DefaultDirectedGraph<EClass, NamedEdge> graph = new DefaultDirectedGraph<>(NamedEdge.class);
		final Set<EClass> visitedClasses = new HashSet<>();
		final Set<EPackage> visitedPackages = new HashSet<>();
		for (final String path : paths) {
			final Resource resource = set.getResource(URI.createPlatformPluginURI(path, false), true);
			final EPackage epackage = (EPackage) resource.getContents().get(0);
			new EcoreDependencyAnalyzer().ecoreDependencyAnalysis(epackage, graph, visitedClasses, visitedPackages);

		}
		new EcoreCSVReport().produceEcoreCSV(graph);
		new EcoreGraphvizReport().produceEcoreGraphviz(graph, "global");
		new EcoreSqliteReport().produceEcoreSqlite(graph);
		new EcoreNeo4jReport().produce(graph);

	}

	private ResourceSet initResourceSet() {
		final ResourceSet set = new XtextResourceSet();
		attachHandler(set);
		return set;
	}

	private void attachHandler(final ResourceSet set) {
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

	/**
	 * Execute the analysis of xtext specifications.
	 * 
	 * @param path
	 *            the path to the main Xtext file.
	 * @param paths
	 *            The path to the references Xtext files (needed because automatic
	 *            resolutions of referenced Xtext file does not work for some
	 *            reason).
	 * @throws IOException
	 * @throws SQLException
	 */
	private void xtextAnalysis(final String path, final String... paths) throws IOException, SQLException {

		/*
		 * TODO : overloading of production rules (by inheritence). relation between
		 * xtext and ecore.
		 */

		final Grammar grammar = loadXtextGrammar(path, paths);

		xtextGrammarToDotFile(grammar);

		final DefaultDirectedGraph<AbstractRule, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

		final List<AbstractRule> rules = GrammarUtil.allRules(grammar);
		for (final AbstractRule rule : rules) {
			final AbstractElement alternatives = rule.getAlternatives();
			visitAbstractElement(alternatives, rule, graph);
		}

		// new XtextGraphvizReport().produceXtextGraphviz(grammar.getName(), graph);

		new XtextSqliteReport().xtextDependencyDB(grammar, graph, rules);
		new XtextNeo4jReport().produce(graph);

	}

	private void xtextGrammarToDotFile(final Grammar grammar) throws IOException {
		final GrammarToDot g2t = new GrammarToDot();

		final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(grammar.getName() + ".dot")));

		final String dot = g2t.draw(grammar);
		bufferedWriter.write(dot);

		bufferedWriter.close();
	}

	private Grammar loadXtextGrammar(final String path, final String... paths) {
		final Injector injector = new XtextStandaloneSetup().createInjectorAndDoEMFRegistration();
		injector.injectMembers(this);

		final ResourceSet set = resourceSetProvider.get();
		attachHandler(set);
		final Resource resource = set.getResource(URI.createPlatformPluginURI(path, false), true);

		for (final String tmp : paths) {
			set.getResource(URI.createPlatformPluginURI(tmp, false), true);
		}

		final Grammar grammar = (Grammar) resource.getContents().get(0);
		return grammar;
	}

	private void visitAbstractElement(final AbstractElement abstractElement, final AbstractRule root,
			final DefaultDirectedGraph<AbstractRule, DefaultEdge> graph) {
		if (abstractElement instanceof RuleCall) {
			final RuleCall rc = (RuleCall) abstractElement;
			final AbstractRule rule = rc.getRule();

			if (!graph.containsVertex(root))
				graph.addVertex(root);
			if (!graph.containsVertex(rule))
				graph.addVertex(rule);
			graph.addEdge(root, rule);
		}
		final List<AbstractElement> collect = abstractElement.eContents().stream()
				.filter(x -> x instanceof AbstractElement).map(x -> (AbstractElement) x).collect(Collectors.toList());
		for (final AbstractElement ae : collect) {
			visitAbstractElement(ae, root, graph);
		}
	}

	private void xtextCSV(final Grammar grammar) throws IOException {
		final CsvListWriter csvWriter = new CsvListWriter(new OutputStreamWriter(System.out),
				CsvPreference.STANDARD_PREFERENCE);

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
}
