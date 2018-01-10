package xbase_analyzer.utils.ecore;

import java.util.Comparator;

import org.eclipse.emf.ecore.EClass;

public final class EClassNameComparator implements Comparator<EClass> {
	final EClassToString e2s = new EClassToString();

	@Override
	public int compare(final EClass o1, final EClass o2) {
		return e2s.apply(o1).compareTo(e2s.apply(o2));
	}
}