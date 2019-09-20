package groovy.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:http://jira.codehaus.org/browse/GROOVY-6456
 * <p>
 * This is a race bug. 
 * Reproduce environment: groovy 1.7.9, JDK 1.6.0_33
 * 
 * Options: 
 * --threadnum, -tn : thread number, default value is 50.
 * 
 * 
 * Two exceptions may be thrown out
 * <p>
 * 1) Exception in thread "Thread-13" java.lang.StringIndexOutOfBoundsException:
 * String index out of range: -13 at
 * java.lang.String.substring(String.java:1937) at
 * java.lang.String.subSequence(String.java:1972) at
 * java.util.regex.Matcher.getSubSequence(Matcher.java:1151) at
 * java.util.regex.Matcher.appendReplacement(Matcher.java:746) at
 * java.util.regex.Matcher.replaceAll(Matcher.java:813) at
 * groovy.servlet.AbstractHttpServlet
 * .applyResourceNameMatcher(AbstractHttpServlet.java:283) at
 * groovy.servlet.AbstractHttpServlet.getScriptUri(AbstractHttpServlet.java:251)
 * at groovy.servlet.Groovy6456$TestThread.run(Groovy6456.java:65)
 * 
 * 2) Exception in thread "Thread-1" java.lang.IllegalStateException: No match
 * available at java.util.regex.Matcher.appendReplacement(Matcher.java:692) at
 * java.util.regex.Matcher.replaceAll(Matcher.java:813) at
 * groovy.servlet.AbstractHttpServlet
 * .applyResourceNameMatcher(AbstractHttpServlet.java:283) at
 * groovy.servlet.AbstractHttpServlet.getScriptUri(AbstractHttpServlet.java:251)
 * at groovy.servlet.Groovy6456$TestThread.run(Groovy6456.java:90)
 * 
 * @author Ziyi Lin
 *
 */
public class Groovy6456 {

    private GroovyServlet groovyServlet;
    private static final int DEFAULT_THREAD_NUM = 10;
    private volatile boolean buggy;

    public Groovy6456() throws ServletException {
        groovyServlet = new GroovyServlet();
        ServletConfig config = new TestConfig();
        groovyServlet.init(config);
        buggy = false;
    }

    public void run(int threadNum) {
        Thread[] ts = new Thread[threadNum];
        for (int i = 0; i < threadNum; i++) {
            ts[i] = new TestThread();
            ts[i].start();
        }
        for (int i = 0; i < threadNum; i++) {
            try {
                ts[i].join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws ServletException {
        Reporter.reportStart("groovy6456", 0, "race");
        if (!OptionHelper.optionParse(args)) {
            return;
        }
        Groovy6456 test = new Groovy6456();
        test.run(OptionHelper.getThreadNumValue(DEFAULT_THREAD_NUM));
        Reporter.reportEnd(test.buggy);
    }

    private class TestThread extends Thread {

        public void run() {
            if (!buggy) {
                try {
                    HttpServletRequest request = new TestRequest();
                    groovyServlet.getScriptUri(request);
                } catch (RuntimeException e) {
                    if (!buggy) {
                        buggy = true;
                        throw e;
                    }
                }
            }
        }
    }

}

// Mock class
class TestConfig implements ServletConfig {

    private Map<String, String> map = new HashMap<String, String>();

    public TestConfig() {
        map.put("verbose", "false");
        map.put("encoding", "utf-8");
        map.put("resource.name.regex", "\\d+");
        map.put("resource.name.replacement", "");
    }

    @Override
    public String getInitParameter(String arg0) {
        return map.get(arg0);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServletContext getServletContext() {
        // TODO Auto-generated method stub
        return new TestServletContext();
    }

    @Override
    public String getServletName() {
        // TODO Auto-generated method stub
        return null;
    }

}

class TestServletContext implements ServletContext {

    @Override
    public Object getAttribute(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Enumeration getAttributeNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServletContext getContext(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getContextPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getInitParameter(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Enumeration getInitParameterNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMajorVersion() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getMimeType(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMinorVersion() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRealPath(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URL getResource(String arg0) throws MalformedURLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set getResourcePaths(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServerInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Servlet getServlet(String arg0) throws ServletException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServletContextName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Enumeration getServletNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Enumeration getServlets() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void log(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void log(Exception arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void log(String arg0, Throwable arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeAttribute(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAttribute(String arg0, Object arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public Dynamic addFilter(String arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Dynamic addFilter(String arg0, Filter arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Dynamic addFilter(String arg0, Class<? extends Filter> arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addListener(String arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public <T extends EventListener> void addListener(T arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addListener(Class<? extends EventListener> arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0, Servlet arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0, Class<? extends Servlet> arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> arg0) throws ServletException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> arg0) throws ServletException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> arg0) throws ServletException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void declareRoles(String... arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ClassLoader getClassLoader() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getEffectiveMajorVersion() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getEffectiveMinorVersion() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilterRegistration getFilterRegistration(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServletRegistration getServletRegistration(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getVirtualServerName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean setInitParameter(String arg0, String arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> arg0) {
        // TODO Auto-generated method stub
        
    }

}

class TestRequest implements HttpServletRequest {

    private Map<String, String> map;

    public TestRequest() {
        map = new HashMap<String, String>();
        map.put(AbstractHttpServlet.INC_SERVLET_PATH,
                "http://127.0.0.1/123Test.groovy");
    }

    @Override
    public Object getAttribute(String arg0) {

        return map.get(arg0);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getContentLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getContentType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLocalAddr() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLocalName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getLocalPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Locale getLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getParameter(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getParameterValues(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProtocol() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRealPath(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRemoteAddr() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRemoteHost() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRemotePort() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getScheme() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServerName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getServerPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isSecure() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removeAttribute(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAttribute(String arg0, Object arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setCharacterEncoding(String arg0)
            throws UnsupportedEncodingException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getAuthType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getContextPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getDateHeader(String arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getHeader(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Enumeration<String> getHeaders(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getIntHeader(String arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getMethod() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPathInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPathTranslated() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getQueryString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRemoteUser() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRequestURI() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StringBuffer getRequestURL() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServletPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HttpSession getSession() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HttpSession getSession(boolean arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Principal getUserPrincipal() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isUserInRole(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getContentLengthLong() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public DispatcherType getDispatcherType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServletContext getServletContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String changeSessionId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Part getPart(String arg0) throws IOException, ServletException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void login(String arg0, String arg1) throws ServletException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void logout() throws ServletException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0) throws IOException, ServletException {
        // TODO Auto-generated method stub
        return null;
    }
}
