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

package org.eclipse.rwt.internal.theme;

import junit.framework.TestCase;


public class JsonArray_Test extends TestCase {

  public void testAppend() {
    JsonArray array = new JsonArray();
    assertEquals( "[]", array.toString() );
    array.append( "a" );
    assertEquals( "[ \"a\" ]", array.toString() );
    array.append( 23 );
    assertEquals( "[ \"a\", 23 ]", array.toString() );
    array.append( false );
    assertEquals( "[ \"a\", 23, false ]", array.toString() );
    array.append( ( String )null );
    assertEquals( "[ \"a\", 23, false, null ]", array.toString() );
    array.append( 10f );
    assertEquals( "[ \"a\", 23, false, null, 10.0 ]", array.toString() );
  }

  public void testAppendArray() {
    JsonArray array = new JsonArray();
    array.append( 1 );
    array.append( new JsonArray() );
    assertEquals( "[ 1, [] ]", array.toString() );
    array.append( ( JsonArray )null );
    assertEquals( "[ 1, [], null ]", array.toString() );
  }

  public void testAppendObject() {
    JsonArray array = new JsonArray();
    array.append( 1 );
    array.append( new JsonObject() );
    assertEquals( "[ 1, {} ]", array.toString() );
    array.append( ( JsonObject )null );
    assertEquals( "[ 1, {}, null ]", array.toString() );
  }

  public void testValueOf() {
    assertEquals( "[]", JsonArray.valueOf( new String[ 0 ] ).toString() );
    JsonArray expected = new JsonArray();
    expected.append( "A" );
    expected.append( "B" );
    String[] array = new String[] { "A", "B" };
    assertEquals( expected.toString(), JsonArray.valueOf( array ).toString() );
    expected = new JsonArray();
    expected.append( 1f );
    expected.append( 2f );
    expected.append( 3f );
    float[] floatArray = new float[] { 1f, 2f, 3f };
    assertEquals( expected.toString(),
                  JsonArray.valueOf( floatArray ).toString() );
  }
}
