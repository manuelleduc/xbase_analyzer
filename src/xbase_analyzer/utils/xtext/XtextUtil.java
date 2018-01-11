package xbase_analyzer.utils.xtext;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Grammar;

public class XtextUtil {
	public Grammar lookupGrammar(final AbstractRule abstractRule) {
		EObject eContainer = abstractRule.eContainer();
		while (!(eContainer instanceof Grammar)) {
			eContainer = eContainer.eContainer();
		}

		final Grammar grammar = (Grammar) eContainer;
		return grammar;
	}
}
