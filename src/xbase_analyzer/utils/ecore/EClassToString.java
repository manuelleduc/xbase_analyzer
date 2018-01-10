package xbase_analyzer.utils.ecore;

import java.util.function.Function;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

public final class EClassToString implements Function<EClass, String> {

	private final String separator;

	public EClassToString() {
		this(".");
	}

	public EClassToString(final String sep) {
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