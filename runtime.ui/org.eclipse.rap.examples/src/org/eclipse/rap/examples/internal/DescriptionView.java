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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rap.examples.internal.model.ExamplesModel;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;


/**
 * Shows the description of the currently selected example. The description is
 * loaded from a HTML file in a org.eclipse.swt.browser.Browser.
 */
public class DescriptionView extends ViewPart {

  public static final String ID
    = "org.eclipse.rap.examples.descriptionView";
  private static final String NO_DESCRIPTION = "";
  private static final String BASE_URL = ".";

  private Browser browser;

  public void createPartControl( final Composite parent ) {
    parent.setLayout( new FillLayout() );
    browser = new Browser( parent, SWT.NONE );
    browser.setData( WidgetUtil.CUSTOM_VARIANT, "descriptionView" );
    createSelectionListener();
  }

  public void setFocus() {
    browser.setFocus();
  }

  private void createSelectionListener() {
    ISelectionService selectionService
      = getSite().getWorkbenchWindow().getSelectionService();
    selectionService.addSelectionListener( new ISelectionListener() {
      public void selectionChanged( final IWorkbenchPart part,
                                    final ISelection selection )
      {
        IStructuredSelection sselection = ( IStructuredSelection )selection;
        Object firstElement = sselection.getFirstElement();
        if( firstElement != null ) {
          if( firstElement instanceof String ) {
            showPage( ( String )firstElement );
          }
        } else {
          browser.setText( NO_DESCRIPTION );
        }
      }
    } );
  }

  private void showPage( final String name ) {
    String descriptionPath
      = ExamplesModel.getInstance().getDescriptionUrl( name );
    if( descriptionPath != null ) {
      boolean loaded = browser.setUrl( BASE_URL + descriptionPath );
      if( !loaded ) {
        browser.setText( NO_DESCRIPTION );
      }
    }
  }
}