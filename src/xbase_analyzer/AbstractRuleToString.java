package xbase_analyzer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Grammar;

public final class AbstractRuleToString {

	private final String separator;

	public AbstractRuleToString(final String separator) {
		this.separator = separator;
	}

	public AbstractRuleToString() {
		this(".");
	}

	public String apply(final AbstractRule abstractRule) {
		final StringBuilder sb = new StringBuilder();

		EObject eContainer = abstractRule.eContainer();
		while (!(eContainer instanceof Grammar)) {
			eContainer = eContainer.eContainer();
		}

		final Grammar grammar = (Grammar) eContainer;
		sb.append(grammar.getName().replaceAll("\\.", this.separator));
		sb.append(this.separator);
		sb.append(abstractRule.getName());
		return sb.toString();
	}

}
