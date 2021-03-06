/*******************************************************************************
 * Copyright (c) 2008 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.ui.internal.servlet;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.http.registry.HttpContextExtensionService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

public class HttpServiceTracker extends ServiceTracker {
  public static final String DEFAULT_SERVLET = "rap";
  public static final String ID_HTTP_CONTEXT = "org.eclipse.rap.httpcontext";

  private HttpService httpService;
  private final List servletAliases = new ArrayList();

  public HttpServiceTracker( final BundleContext context ) {
    super( context, HttpService.class.getName(), null );
  }

  public Object addingService( final ServiceReference reference ) {
    httpService = ( HttpService )context.getService( reference );
    HttpContext rapContext = getRAPHttpContext( reference );

    if( servletAliases.size() == 0 ) {
    	// register default servlet
      servletAliases.add( DEFAULT_SERVLET );
    }
    for( Iterator it = servletAliases.iterator(); it.hasNext(); ) {
      String name = ( String )it.next();
      try {
        RequestHandler handler = new RequestHandler();
        httpService.registerServlet( "/" + name, handler, null, rapContext ); //$NON-NLS-1$
      } catch( Exception e ) {
        String text = "Could not register servlet mapping ''{0}''.";
        Object[] param = new Object[] { name };
        String msg = MessageFormat.format( text, param );
        Status status = new Status( IStatus.ERROR,
                                    PlatformUI.PLUGIN_ID,
                                    IStatus.OK,
                                    msg,
                                    e );
        WorkbenchPlugin.getDefault().getLog().log( status );
      }
    }
    return httpService;
  }

  private HttpContext getRAPHttpContext( final ServiceReference reference ) {
    String name = HttpContextExtensionService.class.getName();
    ServiceReference serviceRef = context.getServiceReference( name );
    HttpContextExtensionService service
      = ( HttpContextExtensionService )context.getService( serviceRef );
    return service.getHttpContext( reference, ID_HTTP_CONTEXT );
  }

  public void removedService( final ServiceReference reference,
                              final Object service )
  {
    for( Iterator iterator = servletAliases.iterator(); iterator.hasNext(); ) {
      String name = ( String )iterator.next();
      httpService.unregister( "/" + name ); //$NON-NLS-1$
    }
    super.removedService( reference, service );
  }

  public void addServletAlias(final String name) {
    servletAliases.add( name );
  }
}
