# xbase-study

- source code: https://github.com/manuelleduc/xtext-extras


# Observations

## Xtext

### Production rules dependencies
- `StaticQualifier` and `JvmTypeParameter` are never used for some reason.
- `XImportSection`, `QualifiedNameWithWildcard`, `XImportDeclaration`, `QualifiedNameInStaticImport` can be removed when the `XImportSection` is not used in the grammar.
They could be extracted in a separated file in the same way as the annotations has been.


### Import declarations.

Most of the import are here just to be explicitly added to the scope.
This is used for the ecore generation from the grammar.

For instance, Xtext will set an inheritance relationship between two EClasses only if:
1. They are imported
2. The fields are compatible

Example:

TurtoiseShell has a `Body` production Rule which returns a `XBlockExpression`. `XBlockExpression` is defined in `Xbase.ecore`.

With an import to `Xbase`, no `XBlockExpression` EClass is created in `TurtoiseShell.ecore`. With a qualified import to `Xbase`, the same behavior is observed.

Without an import to `Xbase`, a `XBlockExpression` EClass is created in `TurtoiseShell.ecore`. At this point the semantics does not compile because a `xbase.XExpression` is expected, but a `turtoiseShell.XBlockExpression` is returned, which does not inherit from `xbase.XExpression`.

### Repos github with xtext + xbase

Search query: https://github.com/search?p=3&q=XbaseWithAnnotations+extension%3Axtext+stars%3A10&type=Code&utf8=%E2%9C%93

- https://github.com/wikthewiz/gs
- https://github.com/cdietrich/mql (Xbase) - analysé - pas de surprise
- https://github.com/lunifera/lunifera-dsl + https://github.com/lunifera/lunifera-xtext-runtimebuilder-example (XbaseWithAnnotations) - Xtext 2.7.3 - seems too old to be used now...
- https://github.com/serhatGezgin/snow (XbaseWithAnnotations) - dead: yazgel-utils submodule missing from github
- https://github.com/JGen-Notes/JGenNotesDataModel (XbaseWithAnnotations) - analysé - pas de surprise
- https://github.com/logicfish/XActiv (XbaseWithAnnotations) - pas de .project...
- https://github.com/josros/boby (XbaseWithAnnotations) - does not compile for some reason
- https://github.com/rostigerloeffel/gdx4e (XbaseWithAnnotations)
  - 83 rules are inherited but not used. It's inherited from xbasewithannotation but does not need xbase nor xbasewithannotations.
  - 91 classes sont inutilisées dans les éléments imports (normal, le ecore de base n'a aucune crossref...)
- https://github.com/Tutti91/Xtext (XbaseWithAnnotations), following rules are unused, XImportDeclaration and XImportSection are unused in the metamodel.
  - org.eclipse.xtext.xbase.annotations.XbaseWithAnnotations.XAnnotation
  - org.eclipse.xtext.xbase.annotations.XbaseWithAnnotations.XAnnotationElementValuePair
  - org.eclipse.xtext.xbase.annotations.XbaseWithAnnotations.XAnnotationElementValueOrCommaList
  - org.eclipse.xtext.xbase.annotations.XbaseWithAnnotations.XAnnotationElementValue
  - org.eclipse.xtext.xbase.annotations.XbaseWithAnnotations.XAnnotationOrExpression
  - org.eclipse.xtext.xbase.Xbase.StaticQualifier
  - org.eclipse.xtext.xbase.Xtype.JvmTypeParameter
  - org.eclipse.xtext.xbase.Xtype.QualifiedNameWithWildcard
  - org.eclipse.xtext.xbase.Xtype.XImportSection
  - org.eclipse.xtext.xbase.Xtype.XImportDeclaration
  - org.eclipse.xtext.xbase.Xtype.QualifiedNameInStaticImport
- https://github.com/UBC-Stat-ML/blangDSL (XbaseWithAnnotations) - missing dependencies
- https://github.com/LorenzoBettini/edelta (XbaseWithAnnotations  - by LorenzoBettini)
- https://github.com/svenefftinge/Xtext-LWC-contribution--Instances- (xtext 2.0.0)
- https://github.com/XtextHaskell/language.common (no xbase)
- https://github.com/loradd/benji
- https://github.com/templarad/Argyle (no xbase)


## Ecore

- Starting from the EPackage of a given language, all the classes of the inherited EPackages can be reached.
