/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.dnd.dragsourcekit;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.dnd.*;


public final class DNDLCAUtil {
  
  private DNDLCAUtil() {
    // prevent instantiation
  }
  
  public static String[] convertTransferTypes( final Transfer[] transfer ) {
    List allTypes = new ArrayList();
    for( int i = 0; i < transfer.length; i++ ) {
      TransferData[] supported = transfer[ i ].getSupportedTypes();
      for( int j = 0; j < supported.length; j++ ) {
        allTypes.add( supported[ j ] );
      }
    }
    String[] result = new String[ allTypes.size() ];
    for( int i = 0; i < allTypes.size(); i++ ) {
      TransferData dataType = ( TransferData )allTypes.get( i );
      result[ i ] = String.valueOf( dataType.type );
    }
    return result;
  }
  
  public static String[] convertOperations( final int operations ) {
    String[] result = new String[ 3 ];
    result[ 0 ] = ( operations & DND.DROP_COPY  ) != 0 ? "copy" : null;  
    result[ 1 ] = ( operations & DND.DROP_MOVE  ) != 0 ? "move" : null;  
    result[ 2 ] = ( operations & DND.DROP_LINK  ) != 0 ? "link" : null;  
    return result;
  }

}
