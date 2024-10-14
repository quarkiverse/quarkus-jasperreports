package io.quarkiverse.jasperreports.graal;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.lowagie.text.pdf.BaseFont;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * FOP is used for one minor feature of OpenPDF, and we don't want to bring in the FOP bloat for
 * one small feature. I could be convinced otherwise in the future!
 */
@TargetClass(className = "com.lowagie.text.pdf.FopGlyphProcessor")
final class FopGlyphProcessorSubstitution {

    @Substitute
    public static boolean isFopSupported() {
        return false;
    }

    @Substitute
    public static byte[] convertToBytesWithGlyphs(BaseFont font, String text, String fileName,
            Map<Integer, int[]> longTag, String language) throws UnsupportedEncodingException {
        throw new UnsupportedEncodingException(
                "Quarkus OpenPDF does not support FOP.  Report this to the extension if you do need this functionality.");
    }
}