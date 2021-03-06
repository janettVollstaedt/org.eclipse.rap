/*******************************************************************************
 * Copyright (c) 2002-2006 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 ******************************************************************************/
package com.w4t.webcardlayoutkit;

import com.w4t.*;



/** <p>The renderer for {@link org.eclipse.rwt.WebCardLayout WebCardLayout} on
  * Internet Explorer 5 and higher.</p>
  */
public class WebCardLayoutRenderer_Ie5up_Noscript 
  extends WebCardLayoutRenderer_DOM 
{
  public void processAction( final WebComponent component ) {
    WebButton[] cardList = getCardList( ( WebContainer )component );
    if( cardList != null ) {
      for( int i = 0; i < cardList.length; i++ ) {
        ProcessActionUtil.processActionPerformedNoScript( cardList[ i ] );      
      }
    }
  }
}