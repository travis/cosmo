package org.osaf.cosmo.log;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A servlet filter to log basic information about HTTP requests.
 * 
 * @author travis
 *
 */
public class HttpLoggingFilter implements Filter {
	private static final Log log = LogFactory.getLog(HttpLoggingFilter.class);

	public void destroy() {
		// Nothing to destroy

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		String logMessage = request.getProtocol() + " ";
		
		try {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;
		
			logMessage += httpRequest.getMethod() + " " +
				httpRequest.getRequestURI() + " " +
				httpRequest.getQueryString() + " " +
				httpRequest.getContentLength() + " " +
				httpRequest.getRequestedSessionId() + " ";
		}
		catch (ClassCastException e) {
			/* 
			 * This is no big deal, we log only the protocol.
			 */
		} 
		
		log.info(logMessage);
		chain.doFilter(request, response);

	}

	public void init(FilterConfig arg0) throws ServletException {
		// Nothing to create

	}

}
