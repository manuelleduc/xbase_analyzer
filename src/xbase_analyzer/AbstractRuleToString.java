package xbase_analyzer;

import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Grammar;

import xbase_analyzer.utils.xtext.XtextUtil;

public final class AbstractRuleToString {

	private final String separator;
	
	private final XtextUtil xtextUtil = new XtextUtil();

	public AbstractRuleToString(final String separator) {
		this.separator = separator;
	}

	public AbstractRuleToString() {
		this(".");
	}

	public String apply(final AbstractRule abstractRule) {
		final StringBuilder sb = new StringBuilder();

		final Grammar grammar = xtextUtil.lookupGrammar(abstractRule);
		sb.append(grammar.getName().replaceAll("\\.", this.separator));
		sb.append(this.separator);
		sb.append(abstractRule.getName());
		return sb.toString();
	}
}
