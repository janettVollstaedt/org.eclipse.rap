/*******************************************************************************
 * Copyright (c) 2002, 2009 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 *     EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rwt.lifecycle;

import java.util.*;


public final class JSListenerType implements Comparable {

  public static final JSListenerType ACTION = new JSListenerType( "ACTION" );
  public static final JSListenerType STATE_AND_ACTION
    = new JSListenerType( "STATE_AND_ACTION" );

  private static int nextOrdinal;
  private final static JSListenerType[] values = new JSListenerType[] {
    ACTION,
    STATE_AND_ACTION
  };
  
  public static final List VALUES 
    = Collections.unmodifiableList( Arrays.asList( values ) );
  
  private final String name;
  private final int ordinal;

  
  public JSListenerType( final String name ) {
    this.name = name;
    this.ordinal = nextOrdinal++;
  }
  
  public String toString() {
    return name;
  }

  public int compareTo( final Object toCompare ) {
    return this.ordinal - ( ( JSListenerType )toCompare ).ordinal;
  }

  public int getOrdinal() {
    return ordinal;
  }
}