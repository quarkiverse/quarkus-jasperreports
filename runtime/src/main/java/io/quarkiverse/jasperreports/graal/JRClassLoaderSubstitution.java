package io.quarkiverse.jasperreports.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * Substitution class for net.sf.jasperreports.engine.util.JRClassLoader.
 * JasperReports loads classes by bytes out of .jasper files which is not supported by native images.
 * This substitution class will load the classes using the current thread's context class loader.
 */
@TargetClass(className = "net.sf.jasperreports.engine.util.JRClassLoader")
final class JRClassLoaderSubstitution {

    /**
     * Loads a class with the specified name using the current thread's context class loader.
     *
     * @param className The fully qualified name of the class to load.
     * @param bytecodes The bytecodes of the class (not used in this implementation).
     * @return The loaded Class object.
     * @throws RuntimeException if the class cannot be found.
     */
    @Substitute
    Class<?> loadClass(String className, byte[] bytecodes) {
        Class<?> clazz;

        try {
            clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return clazz;
    }
}