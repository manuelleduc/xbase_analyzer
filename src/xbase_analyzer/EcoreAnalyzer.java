package xbase_analyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.junit.Test;

import xbase_analyzer.analyzer.EcoreDependencyAnalyzer;
import xbase_analyzer.reports.EcoreGraphvizReport;
import xbase_analyzer.utils.ecore.NamedEdge;

public class EcoreAnalyzer {
	@Test
	public void test() throws Exception {
		new EcoreAnalyzer().exec();
	}

	private void exec() throws IOException {

		/*
		 * 
		 */

		this.ecoreAnalysis("guiceModules", Arrays.asList("xtype", "xbase", "xAnnotations", "types"),
				"/org.xtext.guicemodules/model/generated/GuiceModules.ecore",
				"/org.eclipse.xtext.xbase/model/Xtype.ecore", "/org.eclipse.xtext.xbase/model/Xbase.ecore",
				"/org.eclipse.xtext.xbase/model/XAnnotations.ecore",
				"/org.eclipse.xtext.common.types/model/JavaVMTypes.ecore");

		this.ecoreAnalysis("build", Arrays.asList("xtype", "xbase", "types"),
				"/org.xtext.builddsl/model/generated/BuildDSL.ecore", "/org.eclipse.xtext.xbase/model/Xtype.ecore",
				"/org.eclipse.xtext.xbase/model/Xbase.ecore",
				"/org.eclipse.xtext.common.types/model/JavaVMTypes.ecore");

		this.ecoreAnalysis("route", Arrays.asList("xtype", "xbase", "xAnnotations", "types"),
				"/org.xtext.httprouting/model/generated/Route.ecore", "/org.eclipse.xtext.xbase/model/Xtype.ecore",
				"/org.eclipse.xtext.xbase/model/Xbase.ecore", "/org.eclipse.xtext.xbase/model/XAnnotations.ecore",
				"/org.eclipse.xtext.common.types/model/JavaVMTypes.ecore");

		this.ecoreAnalysis("mongoBeans", Arrays.asList("xtype", "xbase", "types"),
				"/org.xtext.mongobeans/model/generated/MongoBeans.ecore", "/org.eclipse.xtext.xbase/model/Xtype.ecore",
				"/org.eclipse.xtext.xbase/model/Xbase.ecore",
				"/org.eclipse.xtext.common.types/model/JavaVMTypes.ecore");

		this.ecoreAnalysis("scripting", Arrays.asList("xbase", "types"),
				"/org.xtext.scripting/model/generated/Scripting.ecore", "/org.eclipse.xtext.xbase/model/Xbase.ecore",
				"/org.eclipse.xtext.common.types/model/JavaVMTypes.ecore");

		this.ecoreAnalysis("template", Arrays.asList("xtype", "xbase", "xAnnotations", "types"),
				"/org.xtext.template/model/generated/Template.ecore", "/org.eclipse.xtext.xbase/model/Xtype.ecore",
				"/org.eclipse.xtext.xbase/model/Xbase.ecore", "/org.eclipse.xtext.xbase/model/XAnnotations.ecore",
				"/org.eclipse.xtext.common.types/model/JavaVMTypes.ecore");

		this.ecoreAnalysis("tortoiseShell", Arrays.asList("xbase", "types"),
				"/org.xtext.tortoiseshell/model/generated/TortoiseShell.ecore",
				"/org.eclipse.xtext.xbase/model/Xbase.ecore",
				"/org.eclipse.xtext.common.types/model/JavaVMTypes.ecore");

	}

	private void ecoreAnalysis(final String mainPackage, final List<String> packagesLibs, final String... paths)
			throws IOException {
		final ResourceSet set = initResourceSet();
		final DefaultDirectedGraph<EClass, NamedEdge> graph = new DefaultDirectedGraph<>(NamedEdge.class);
		final Set<EClass> visitedClasses = new HashSet<>();
		final Set<EPackage> visitedPackages = new HashSet<>();
		for (final String path : paths) {
			final Resource resource = set.getResource(URI.createPlatformPluginURI(path, false), true);
			final EPackage epackage = (EPackage) resource.getContents().get(0);
			new EcoreDependencyAnalyzer().ecoreDependencyAnalysis(epackage, graph, visitedClasses, visitedPackages);

		}
		new EcoreGraphvizReport().produceEcoreGraphviz(graph, mainPackage);

		final DijkstraShortestPath<EClass, NamedEdge> dsp = new DijkstraShortestPath<>(graph);
		final List<EClass> guiceModulesClasses = graph.vertexSet().stream().filter(v -> {
			final String name = v.getEPackage().getName();
			return name.equals(mainPackage);
		}).collect(Collectors.toList());

		final List<EClass> packagesLibsClasses = graph.vertexSet().stream().filter(v -> {
			final String name = v.getEPackage().getName();
			return packagesLibs.contains(name);
		}).collect(Collectors.toList());

		final List<EClass> res = new ArrayList<EClass>(packagesLibsClasses);

		System.out.println(mainPackage + " analysis");
		for (final EClass guiceClass : guiceModulesClasses) {
			for (final EClass libClass : packagesLibsClasses) {
				if (res.contains(libClass) && dsp.getPath(guiceClass, libClass) != null) {
					res.remove(libClass);
				}
			}
		}
		res.stream().map(libClass -> libClass.getEPackage().getName() + "." + libClass.getName()).sorted()
				.forEach(libClass -> System.out.println("- " + libClass + " unused"));

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
}
