/*******************************************************************************
 * Copyright (c) 2002, 2008 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 ******************************************************************************/

package org.eclipse.swt.internal.widgets.groupkit;

import java.io.IOException;

import org.eclipse.rwt.lifecycle.*;
import org.eclipse.swt.widgets.*;

public class GroupLCA extends AbstractWidgetLCA {

  private static final String QX_TYPE = "org.eclipse.swt.widgets.Group";
  private static final String PROP_TEXT = "text";

  public void preserveValues( final Widget widget ) {
    Group group = ( Group )widget;
    ControlLCAUtil.preserveValues( group );
    IWidgetAdapter adapter = WidgetUtil.getAdapter( group );
    adapter.preserve( PROP_TEXT, group.getText() );
    WidgetLCAUtil.preserveCustomVariant( group );
  }

  public void readData( final Widget widget ) {
    ControlLCAUtil.processMouseEvents( ( Group )widget );
    ControlLCAUtil.processKeyEvents( ( Group )widget );
    ControlLCAUtil.processMenuDetect( ( Group )widget );
    WidgetLCAUtil.processHelp( widget );
  }

  public void renderInitialization( final Widget widget ) throws IOException {
    Group group = ( Group )widget;
    JSWriter writer = JSWriter.getWriterFor( group );
    writer.newWidget( QX_TYPE );    
    ControlLCAUtil.writeStyleFlags( group );
  }

  public void renderChanges( final Widget widget ) throws IOException {
    Group group = ( Group )widget;
    ControlLCAUtil.writeChanges( group );
    JSWriter writer = JSWriter.getWriterFor( group );
    String text = group.getText();
    if( WidgetLCAUtil.hasChanged( widget, PROP_TEXT, text, "" ) ) {
      text = WidgetLCAUtil.escapeText( text, true );
      writer.set( "legend", text );
    }
    WidgetLCAUtil.writeCustomVariant( group );
  }

  public void renderDispose( final Widget widget ) throws IOException {
    JSWriter writer = JSWriter.getWriterFor( widget );
    writer.dispose();
  }
}
