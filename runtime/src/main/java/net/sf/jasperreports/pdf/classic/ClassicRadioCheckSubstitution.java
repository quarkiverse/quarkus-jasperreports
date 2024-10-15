package net.sf.jasperreports.pdf.classic;

import java.io.IOException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.RadioCheckField;
import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import net.sf.jasperreports.engine.JRRuntimeException;

/**
 * OpenPDF between 1.X and 2.X renamed getRadioField() to getKidField().
 */
@TargetClass(className = "net.sf.jasperreports.pdf.classic.ClassicRadioCheck")
final class ClassicRadioCheckSubstitution {

    @Alias
    ClassicPdfProducer pdfProducer;
    @Alias
    RadioCheckField radioCheckField;

    @Substitute
    public void addToGroup() throws IOException {
        PdfFormField radioGroup = this.pdfProducer.getRadioGroup(this.radioCheckField);
        try {
            radioGroup.addKid(this.radioCheckField.getKidField());
        } catch (DocumentException var3) {
            throw new JRRuntimeException(var3);
        }
    }

}
