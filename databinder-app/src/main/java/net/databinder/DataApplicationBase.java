package net.databinder;

import java.awt.Color;
import java.net.URI;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import net.databinder.components.PageExpiredCookieless;
import net.databinder.util.ColorConverter;
import net.databinder.util.URIConverter;
import net.databinder.util.URLConverter;
import net.databinder.web.NorewriteWebResponse;

import org.apache.wicket.IConverterLocator;
import org.apache.wicket.markup.html.pages.PageExpiredErrorPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.convert.ConverterLocator;

public abstract class DataApplicationBase extends WebApplication {
	/** true if cookieless use is supported through URL rewriting(defaults to true). */
	private boolean cookielessSupported = true;

	/**
	 * Internal initialization. Client applications should not normally override
	 * or call this method.
	 */
	@Override
	protected void internalInit() {
		super.internalInit();
		dataInit();
	}
	
	abstract protected void dataInit();
	
	/**
	 * Adds converters to Wicket's base locator.
	 */
	@Override
	protected IConverterLocator newConverterLocator() {
		// register converters
		ConverterLocator converterLocator = new ConverterLocator();
		converterLocator.set(URL.class, new URLConverter());
		converterLocator.set(URI.class, new URIConverter());
		converterLocator.set(Color.class, new ColorConverter());
		return converterLocator;
	}
	/**
	 * If <code>isCookielessSupported()</code> returns false, this method returns
	 * a custom WebResponse that disables URL rewriting.
	 */
	@Override
	protected WebResponse newWebResponse(final HttpServletResponse servletResponse)
	{
		if (isCookielessSupported())
			return super.newWebResponse(servletResponse);
		return NorewriteWebResponse.getNew(this, servletResponse);
	}

	/**
	 * @return  true if cookieless use is supported through URL rewriting.
	 */
	public boolean isCookielessSupported() {
		return cookielessSupported;
	}

	/**
	 * Set to false to disable URL rewriting and consequentally hamper cookieless 
	 * browsing.  Users with cookies disabled, and more importantly search engines, 
	 * will still be able to browse the application through bookmarkable URLs. Because
	 * rewriting is disabled, these URLs will have no jsessionid appended and will 
	 * remain static.
	 * <p> The Application's "page expired" error page will be set to PageExpiredCookieless
	 * if cookielessSupported is false, unless an alternate error page has already been
	 * specified. This page will appear when cookieless users try to follow a link or 
	 * form-submit that requires a session, informing them that cookies are required.
	 * </p>
	 * @param cookielessSupported  true if cookieless use is supported through 
	 * URL rewriting
	 * @see net.databinder.components.PageExpiredCookieless
	 */
	protected void setCookielessSupported(boolean cookielessSupported) {
		Class expected = this.cookielessSupported ? 
				PageExpiredErrorPage.class : PageExpiredCookieless.class;
		
		this.cookielessSupported = cookielessSupported;
		
		if (getApplicationSettings().getPageExpiredErrorPage().equals(expected))
			getApplicationSettings().setPageExpiredErrorPage(cookielessSupported ?
					PageExpiredErrorPage.class : PageExpiredCookieless.class);
	}
	
	/**
	 * Reports if the program is running in a development environment, as determined by the
	 * "wicket.configuration" environment variable or context/init parameter. If that variable 
	 * is unset or set to "development", the app is considered to be running in development.  
	 * @return true if running in a development environment
	 */
	protected boolean isDevelopment() {
		return  getConfigurationType().equalsIgnoreCase(DEVELOPMENT);
	}
}