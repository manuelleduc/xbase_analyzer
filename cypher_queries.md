# Queries

Each query search for the production rules inherited from the parent grammars of a language but that can never be accessed by the language

## Xtext

BuildDSL

```
MATCH (a:Rule {grammar:'org.xtext.builddsl.BuildDSL'})-[:DEPENDS_OF*]->(b:Rule) WITH collect(distinct b) as bs MATCH (c:Rule) WHERE NOT (c in bs)
AND (c.grammar ='org.eclipse.xtext.xbase.Xbase' or c.grammar='org.eclipse.xtext.xbase.Xtype') RETURN c
```

GuiceModules

```
MATCH (a:Rule {grammar:'org.xtext.guicemodules.GuiceModules'})-[:DEPENDS_OF*]->(b:Rule) WITH collect(distinct b) as bs MATCH (c:Rule) WHERE NOT (c in bs)
AND (c.grammar ='org.eclipse.xtext.xbase.Xbase' or c.grammar='org.eclipse.xtext.xbase.Xtype' or c.grammar='org.eclipse.xtext.xbase.annotations.XbaseWithAnnotations') RETURN c
```

Route

```
MATCH (a:Rule {grammar:'org.xtext.httprouting.Route'})-[:DEPENDS_OF*]->(b:Rule) WITH collect(distinct b) as bs MATCH (c:Rule) WHERE NOT (c in bs)
AND (c.grammar ='org.eclipse.xtext.xbase.Xbase' or c.grammar='org.eclipse.xtext.xbase.Xtype' or c.grammar='org.eclipse.xtext.xbase.annotations.XbaseWithAnnotations') RETURN c
```

MongoBeans

```
MATCH (a:Rule {grammar:'org.xtext.mongobeans.MongoBeans'})-[:DEPENDS_OF*]->(b:Rule) WITH collect(distinct b) as bs MATCH (c:Rule) WHERE NOT (c in bs)
AND (c.grammar ='org.eclipse.xtext.xbase.Xbase' or c.grammar='org.eclipse.xtext.xbase.Xtype') RETURN c
```

Scripting

```
MATCH (a:Rule {grammar:'org.xtext.scripting.Scripting'})-[:DEPENDS_OF*]->(b:Rule) WITH collect(distinct b) as bs MATCH (c:Rule) WHERE NOT (c in bs)
AND (c.grammar ='org.eclipse.xtext.xbase.Xbase' or c.grammar='org.eclipse.xtext.xbase.Xtype') RETURN c
```

Template

```
MATCH (a:Rule {grammar:'org.xtext.template.Template'})-[:DEPENDS_OF*]->(b:Rule) WITH collect(distinct b) as bs MATCH (c:Rule) WHERE NOT (c in bs)
AND (c.grammar ='org.eclipse.xtext.xbase.Xbase' or c.grammar='org.eclipse.xtext.xbase.Xtype' or c.grammar='org.eclipse.xtext.xbase.annotations.XbaseWithAnnotations') RETURN c
```

TortoiseShell

```
MATCH (a:Rule {grammar:'org.xtext.tortoiseshell.TortoiseShell'})-[:DEPENDS_OF*]->(b:Rule) WITH collect(distinct b) as bs MATCH (c:Rule) WHERE NOT (c in bs)
AND (c.grammar ='org.eclipse.xtext.xbase.Xbase' or c.grammar='org.eclipse.xtext.xbase.Xtype') RETURN c
```

All the above

```
MATCH (a:Rule)-[:DEPENDS_OF*]->(b:Rule) WHERE (a.grammar = 'org.xtext.builddsl.BuildDSL' or a.grammar = 'org.xtext.guicemodules.GuiceModules' or a.grammar = 'org.xtext.httprouting.Route' or a.grammar = 'org.xtext.mongobeans.MongoBeans' or a.grammar = 'org.xtext.scripting.Scripting' or a.grammar = 'org.xtext.template.Template' or a.grammar = 'org.xtext.tortoiseshell.TortoiseShell') WITH collect(distinct b) as bs MATCH (c:Rule) WHERE NOT (c in bs)
AND (c.grammar ='org.eclipse.xtext.xbase.Xbase' or c.grammar='org.eclipse.xtext.xbase.Xtype') RETURN c
```


## ECore


generic query

```
MATCH (a:EClass {package: '$mainPackage'})
CALL apoc.path.subgraphNodes(a, {relationshipFilter:'>'}) YIELD node
WITH collect(distinct node) as nodes
MATCH (b:EClass) WHERE (b.package='$targetPackage1' or b.package='$package2' or ...) and not b in nodes
RETURN count(b)
```






# Conclusion

## Globally
StaticQualifier and JvmTypeParameter are never used for some reason

# Imports

XImportSection, QualifiedNameWithWildcard, XImportDeclaration, QualifiedNameInStaticImport can be removed when the XImportSection is not used in the grammar.
They could be extracted in a separated file in the same way as the annotations has been.
