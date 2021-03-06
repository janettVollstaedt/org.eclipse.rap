/*******************************************************************************
 * Copyright (c) 2008, 2009 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.examples.internal;

import java.util.Locale;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.lifecycle.IEntryPoint;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;


public class Application implements IEntryPoint {

  public int createUI() {
    // Set locale to english (currently the only localization available) to
    // avoid mixed language content (see bug 288697)
    RWT.setLocale( Locale.ENGLISH );
    Display display = PlatformUI.createDisplay();
    WorkbenchAdvisor advisor = new ExamplesWorkbenchAdvisor();
    return PlatformUI.createAndRunWorkbench( display, advisor );
  }
}
