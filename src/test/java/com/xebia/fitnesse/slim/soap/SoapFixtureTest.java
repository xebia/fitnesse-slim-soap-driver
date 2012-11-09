package com.xebia.fitnesse.slim.soap;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class SoapFixtureTest {

    @Mock
    private SOAPConnection soapConnection;

    private SoapFixture soapFixture;

    @Before
    public void init() throws Throwable {
        soapFixture = new SoapFixture();
        soapFixture.addPrefixNamespace("ns", "a");
        ReflectionTestUtils.setField(soapFixture, "soapConnection", soapConnection);
    }
    @Test
    public void shouldCreateNewContent() throws Throwable {
        soapFixture.setXPathValue("ns:child", "value");
        assertXml("<ns:child xmlns:ns=\"a\">value</ns:child>");
    }
    @Test
    public void setHeadersShouldOverwriteValue() {
        soapFixture.setHeaderValue("a", "waarde");
        soapFixture.setHeaderValue("a", "waarde2");
        assertThat(soapFixture.headers(), is("[Accept] = [text/xml, text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2]\n[a] = [waarde2]"));
    }
    @Test
    public void shouldShowHeaders() throws Throwable {
        soapFixture.setHeaderValue("a", "waarde");
        soapFixture.setHeaderValue("b", "ookwaarde");
        assertThat(soapFixture.headers(), is("[Accept] = [text/xml, text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2]\n[a] = [waarde]\n[b] = [ookwaarde]"));
    }
    @Test
    public void headersShouldPersistBetweenCalls() throws Exception {
        soapFixture.setHeaderValue("b", "test");
        assertThat(soapFixture.headers(), is("[Accept] = [text/xml, text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2]\n[b] = [test]"));
        soapFixture.sendTo("http://example.com");
        assertThat(soapFixture.headers(), is("[Accept] = [text/xml, text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2]\n[b] = [test]"));
    }

    @Test
    public void shouldShowHeadersWhenThereAreNone() throws Throwable {
        assertThat(soapFixture.headers(), is("[Accept] = [text/xml, text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2]"));
    }
    @Test
    public void shouldAddSubFieldsToSameContainer() throws Throwable {
        soapFixture.setXPathValue("child/field1", "value1");
        soapFixture.setXPathValue("child/field2", "value2");
        assertXml("<child><field1>value1</field1><field2>value2</field2></child>");
    }

    @Test
    public void shouldHandleArray() throws Throwable {
        soapFixture.setXPathValue("child[1]/field", "value");
        assertXml("<child><field>value</field></child>");
        soapFixture.setXPathValue("child[2]/field", "value");
        assertXml("<child><field>value</field></child><child><field>value</field></child>");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldPointOutZeroIndex() throws Exception {
	soapFixture.setXPathValue("child[0]/field", "value");
    
    }
    @Test
    public void shouldAcceptIndexJumps() throws Exception {
	soapFixture.setXPathValue("child[9]/field", "value");
        assertXml("<child/><child/><child/><child/><child/><child/><child/><child/><child><field>value</field></child>");
    }
    
    @Test
    public void shouldParseIndexCorrectly() throws Exception {
	soapFixture.setXPathValue("child[10]/field", "value");
        assertXml("<child/><child/><child/><child/><child/><child/><child/><child/><child/><child><field>value</field></child>");
    }
    
    @Test
    public void shouldHandleAttribute() throws Throwable {
        soapFixture.setXPathValue("child/@attributename", "value");
        assertXml("<child attributename=\"value\"/>");
    }

    @Test
    public void shouldHandleAttributeWithNamespace() throws Throwable {
        soapFixture.addPrefixNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        soapFixture.setXPathValue("child/@xsi:attributename", "value");
        assertXml("<child xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:attributename=\"value\"/>");
    }

    @Test
    public void shouldHandleAttributeWithNamespaceInElementWithNamespace() throws Throwable {
        soapFixture.addPrefixNamespace("req", "http://example.com/namespace");
        soapFixture.addPrefixNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        soapFixture.setXPathValue("req:child/@xsi:attributename", "value");
        soapFixture.setXPathValue("req:child/@xsi:other", "someother");
        assertXml("<req:child xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:attributename=\"value\" xsi:other=\"someother\" xmlns:req=\"http://example.com/namespace\"/>");
    }

    private void assertXml(String xml) throws TransformerException, SOAPException {
        assertThat(soapFixture.request().toString(), is(xml));
    }

    @Test
    public void exceptionInSendToShouldEmptyResponse() throws Exception {
        when(soapConnection.call(any(SOAPMessage.class), any(Object.class))).thenThrow(new NullPointerException("Simulated runtime exception in soap call"));
        ReflectionTestUtils.setField(soapFixture, "responseMessage", MessageFactory.newInstance().createMessage());
        try {
            soapFixture.sendTo("http://example.com");
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("Simulated runtime exception in soap call"));
        }
        assertNull(ReflectionTestUtils.getField(soapFixture, "responseMessage"));
    }

    @Test
    public void shouldShowResponseMessage() throws Throwable {
        final ArgumentCaptor<SOAPMessage> argumentCaptor = ArgumentCaptor.forClass(SOAPMessage.class);
        String url = "http://someurl";
        when(soapConnection.call(argumentCaptor.capture(), eq(url))).thenAnswer(new Answer<SOAPMessage>() {
            public SOAPMessage answer(InvocationOnMock invocation) throws Throwable {
                return argumentCaptor.getValue();
            }
        });

        soapFixture.setXPathValue("ns:some/ns:path", "data");
        soapFixture.sendTo(url);
        assertThat(soapFixture.response(), is(notNullValue()));
        assertThat(soapFixture.getXPath("ns:some/ns:path"), is("data"));
    }
}
