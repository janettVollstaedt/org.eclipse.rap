/*******************************************************************************
 * Copyright (c) 2010 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.canvaskit;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.rwt.Fixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.internal.graphics.GCAdapter;
import org.eclipse.swt.internal.graphics.IGCAdapter;
import org.eclipse.swt.internal.graphics.GCOperation.DrawLine;
import org.eclipse.swt.widgets.*;


public class CanvasLCA_Test extends TestCase {

  protected void setUp() throws Exception {
    Fixture.setUp();
  }

  protected void tearDown() throws Exception {
    Fixture.tearDown();
  }

  public void testWriteSingleGCOperation() throws IOException {
    Fixture.fakeResponseWriter();
    Display display = new Display();
    Composite shell = new Shell( display, SWT.NONE );
    Canvas canvas = new Canvas( shell, SWT.NONE );
    canvas.setSize( 50, 50 );
    canvas.setFont( new Font( display, "Arial", 11, SWT.NORMAL ) );
    Fixture.markInitialized( display );
    Fixture.markInitialized( canvas );
    Fixture.preserveWidgets();
    GCAdapter adapter = ( GCAdapter )canvas.getAdapter( IGCAdapter.class );
    adapter.addGCOperation( new DrawLine( 1, 2, 3, 4 ) );
    new CanvasLCA().renderChanges( canvas );
    String expected
      = "var w = wm.findWidgetById( \"w2\" );"
      + "var gc = w.getGC();"
      + "gc.init( 50, 50, \"11px Arial\", \"#f8f8ff\", \"#000000\" );"
      + "gc.drawLine( 1, 2, 3, 4 );";
    assertEquals( expected, Fixture.getAllMarkup() );
  }

  public void testWriteMultipleGCOperations() throws IOException {
    Fixture.fakeResponseWriter();
    Display display = new Display();
    Composite shell = new Shell( display, SWT.NONE );
    Canvas canvas = new Canvas( shell, SWT.NONE );
    canvas.setSize( 50, 50 );
    canvas.setFont( new Font( display, "Arial", 11, SWT.NORMAL ) );
    Fixture.markInitialized( display );
    Fixture.markInitialized( canvas );
    Fixture.preserveWidgets();
    GCAdapter adapter = ( GCAdapter )canvas.getAdapter( IGCAdapter.class );
    adapter.addGCOperation( new DrawLine( 1, 2, 3, 4 ) );
    adapter.addGCOperation( new DrawLine( 5, 6, 7, 8 ) );
    new CanvasLCA().renderChanges( canvas );
    String expected
      = "var w = wm.findWidgetById( \"w2\" );"
      + "var gc = w.getGC();"
      + "gc.init( 50, 50, \"11px Arial\", \"#f8f8ff\", \"#000000\" );"
      + "gc.drawLine( 1, 2, 3, 4 );"
      + "gc.drawLine( 5, 6, 7, 8 );";
    assertEquals( expected, Fixture.getAllMarkup() );
  }
}
