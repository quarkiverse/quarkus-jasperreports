package net.sf.jasperreports.pdf.classic;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(className = "net.sf.jasperreports.pdf.classic.ClassicPdfStructureTreeRoot")
final class Target_ClassicPdfStructureTreeRoot {

    @Substitute
    public static void install(Object writer) {
        // no-op
    }
}