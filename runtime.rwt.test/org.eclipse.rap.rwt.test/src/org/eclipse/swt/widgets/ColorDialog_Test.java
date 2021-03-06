/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.widgets;

import junit.framework.TestCase;

import org.eclipse.rwt.Fixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

public class ColorDialog_Test extends TestCase {

  protected void setUp() throws Exception {
    Fixture.setUp();
  }

  protected void tearDown() throws Exception {
    Fixture.tearDown();
  }

  public void testRGB() {
    Display display = new Display();
    Shell shell = new Shell( display, SWT.NONE );
    RGB rgb = new RGB( 255, 0, 0 );
    ColorDialog dialog = new ColorDialog( shell );
    assertNull( dialog.getRGB() );
    dialog.setRGB( rgb );
    assertEquals( rgb, dialog.getRGB() );
  }

}
