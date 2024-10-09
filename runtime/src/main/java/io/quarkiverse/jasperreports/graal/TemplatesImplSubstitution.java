package io.quarkiverse.jasperreports.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * Substitution class for org.apache.xalan.xsltc.trax.TemplatesImpl.
 * This class is used to load classes by bytes out of .jasper files which is not supported by native images.
 * This substitution class will throw an UnsupportedOperationException when trying to define a class.
 *
 * @see <a href=
 *      "https://github.com/apache/camel-quarkus/blob/ff092e50666465f93c0ac71f607886e397e81598/extensions-support/xalan/runtime/src/main/java/org/apache/camel/quarkus/support/xalan/XalanTransformerFactory.java">XalanTransformerFactory.java</a>
 */
@TargetClass(className = "org.apache.xalan.xsltc.trax.TemplatesImpl")
final class TemplatesImplSubstitution {

    @TargetClass(className = "org.apache.xalan.xsltc.trax.TemplatesImpl", innerClass = "TransletClassLoader")
    static final class TransletClassLoader {
        @Substitute
        Class defineClass(final byte[] b) {
            throw new UnsupportedOperationException();
        }
    }
}