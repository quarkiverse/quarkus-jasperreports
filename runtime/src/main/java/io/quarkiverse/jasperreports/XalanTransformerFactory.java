package io.quarkiverse.jasperreports;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.XMLFilter;

/**
 * A {@link TransformerFactory} delegating to a {@link TransformerFactory} created via
 * {@code TransformerFactory.newInstance("org.apache.xalan.xsltc.trax.TransformerFactoryImpl", Thread.currentThread().getContextClassLoader())}
 *
 * @see <a href=
 *      "https://github.com/apache/camel-quarkus/blob/ff092e50666465f93c0ac71f607886e397e81598/extensions-support/xalan/runtime/src/main/java/org/apache/camel/quarkus/support/xalan/XalanTransformerFactory.java">XalanTransformerFactory.java</a>
 */
public final class XalanTransformerFactory extends SAXTransformerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(XalanTransformerFactory.class);

    private final SAXTransformerFactory delegate;

    public XalanTransformerFactory() {
        final SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance(
                "org.apache.xalan.xsltc.trax.TransformerFactoryImpl",
                Thread.currentThread().getContextClassLoader());
        try {
            factory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (TransformerException e) {
            LOGGER.warn("Unsupported TransformerFactory feature " + javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING);
        }

        this.delegate = factory;
    }

    @Override
    public Transformer newTransformer(Source source) throws TransformerConfigurationException {
        return delegate.newTransformer(source);
    }

    @Override
    public Transformer newTransformer() throws TransformerConfigurationException {
        return delegate.newTransformer();
    }

    @Override
    public Templates newTemplates(Source source) throws TransformerConfigurationException {
        return delegate.newTemplates(source);
    }

    @Override
    public Source getAssociatedStylesheet(Source source, String media, String title, String charset)
            throws TransformerConfigurationException {
        return delegate.getAssociatedStylesheet(source, media, title, charset);
    }

    @Override
    public void setURIResolver(URIResolver resolver) {
        delegate.setURIResolver(resolver);
    }

    @Override
    public URIResolver getURIResolver() {
        return delegate.getURIResolver();
    }

    @Override
    public void setFeature(String name, boolean value) throws TransformerConfigurationException {
        delegate.setFeature(name, value);
    }

    @Override
    public boolean getFeature(String name) {
        return delegate.getFeature(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        delegate.setAttribute(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        return delegate.getAttribute(name);
    }

    @Override
    public void setErrorListener(ErrorListener listener) {
        delegate.setErrorListener(listener);
    }

    @Override
    public ErrorListener getErrorListener() {
        return delegate.getErrorListener();
    }

    @Override
    public TransformerHandler newTransformerHandler(Source source) throws TransformerConfigurationException {
        return delegate.newTransformerHandler(source);
    }

    @Override
    public TransformerHandler newTransformerHandler(Templates templates) throws TransformerConfigurationException {
        return delegate.newTransformerHandler(templates);
    }

    @Override
    public TransformerHandler newTransformerHandler() throws TransformerConfigurationException {
        return delegate.newTransformerHandler();
    }

    @Override
    public TemplatesHandler newTemplatesHandler() throws TransformerConfigurationException {
        return delegate.newTemplatesHandler();
    }

    @Override
    public XMLFilter newXMLFilter(Source source) throws TransformerConfigurationException {
        return delegate.newXMLFilter(source);
    }

    @Override
    public XMLFilter newXMLFilter(Templates templates) throws TransformerConfigurationException {
        return delegate.newXMLFilter(templates);
    }

}