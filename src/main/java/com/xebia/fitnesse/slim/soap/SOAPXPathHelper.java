package com.xebia.fitnesse.slim.soap;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

public class SOAPXPathHelper {

    private static final Pattern NAME_PATTERN = Pattern.compile("(?:([\\S]*):)?([\\S]*?)(?:\\[([0-9]+)\\])?");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("@(?:([\\S]*):)?([\\S]*?)");

    private final NamespaceContext nsContext;

    private final XPath xPath;

    public SOAPXPathHelper(NamespaceContext nsContext) {
        this.nsContext = nsContext;
        XPathFactory factory = XPathFactory.newInstance();
        xPath = factory.newXPath();
        xPath.setNamespaceContext(nsContext);
    }

    public void setXPathValue(String path, String data, SOAPElement soapBody)
            throws SOAPException, XPathExpressionException {
        ensurePath(path, soapBody);
        Node node = (Node) xPath.evaluate(path, soapBody, XPathConstants.NODE);
        if (node == null) {
            throw new IllegalArgumentException(
                    "Could not find node matching xpath '" + path
                            + "' to set value.");
        }
        node.setTextContent(data);
    }

    private void ensurePath(String path, SOAPElement soapElement)
            throws SOAPException {
        SOAPElement current = soapElement;
        for (String part : path.split("/")) {
            if (part.startsWith("@")) {
                // Handle attribute
                current = ensureAttribute(current, part);
            } else {
                current = ensureChild(current, part);
            }
        }
    }

    private SOAPElement ensureAttribute(SOAPElement current, String part) throws SOAPException {
        Matcher matcher = ATTRIBUTE_PATTERN.matcher(part);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("No valid xml attribute name: " + part);
        }
        String prefix = matcher.group(1);
        String localName = matcher.group(2);
        QName qname = createQName(prefix, localName);
        return current.addAttribute(qname, "");
    }

    private SOAPElement ensureChild(SOAPElement current, String part)
            throws SOAPException {
        Matcher matcher = NAME_PATTERN.matcher(part);
        int index = 1;
        if (!matcher.matches()) {
            throw new IllegalArgumentException("No valid xml name: " + part);
        }
        String prefix = matcher.group(1);
        String localName = matcher.group(2);
        String indexPart = matcher.group(3);
        if (indexPart != null) {
            index = Integer.parseInt(indexPart);
            if(index <= 0) {
                throw new IllegalArgumentException("Invalid index in xPath, index must be larger then 0, but was: " + index);
            }
        }
        QName qname = createQName(prefix, localName);
        @SuppressWarnings("unchecked")
        Iterator<SOAPElement> iterator = current.getChildElements(qname);
        SOAPElement currentChild = null;
        for (int i = 0; i < index; i++) {
            if (iterator.hasNext()) {
                currentChild = iterator.next();
            } else {
                currentChild = current.addChildElement(qname);
            }
        }
        return currentChild;
    }

    private QName createQName(String prefix, String localName) {
        if (prefix != null) {
            return new QName(nsContext.getNamespaceURI(prefix), localName, prefix);
        } else {
            return new QName(localName);
        }
    }

    public String getXPathValue(String path, SOAPElement node)
            throws XPathExpressionException {
        return xPath.evaluate(path, node);
    }

}
