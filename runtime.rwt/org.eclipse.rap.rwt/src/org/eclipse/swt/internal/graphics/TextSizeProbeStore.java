/*******************************************************************************
 * Copyright (c) 2007, 2010 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 *     EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.graphics;

import java.math.BigDecimal;
import java.util.*;

import org.eclipse.rwt.SessionSingletonBase;
import org.eclipse.rwt.internal.service.ContextProvider;
import org.eclipse.rwt.internal.service.IServiceStateInfo;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;


final class TextSizeProbeStore {
  
  // TODO [fappel]: improve probe text determination...
  static String DEFAULT_PROBE;
  static {
    StringBuffer result = new StringBuffer();
    for( int i = 33; i < 122; i++ ) {
      if( i != 34 && i != 39 ) {
        result.append( ( char ) i );
      }
    }
    DEFAULT_PROBE = result.toString();
  }

  private static final String PROBE_REQUESTS
    = TextSizeProbeStore.class.getName() + ".ProbeRequests";
  
  private static Map probes = new HashMap(); 
  
  private Map probeResults = new HashMap();
  
  
  public interface IProbe {
    FontData getFontData();
    String getString();
    String getJSProbeParam();
  }
  
  public interface IProbeResult {
    IProbe getProbe();
    Point getSize();
    float getAvgCharWidth();
  }
  
  private static final class ProbeImpl implements IProbe {

    private final String probeText;
    private final FontData fontData;
    private String jsProbeParam;

    private ProbeImpl( final String probeText, final FontData fontData ) {
      this.probeText = probeText;
      this.fontData = fontData;
      this.jsProbeParam = createProbeParam( this );
    }

    public FontData getFontData() {
      return fontData;
    }

    public String getString() {
      return probeText;
    }

    public String getJSProbeParam() {
      if( "".equals( jsProbeParam ) ) {
        jsProbeParam = createProbeParam( this );
      }
      return jsProbeParam;
    }
  }
  
  private TextSizeProbeStore() {
    // prevent instance creation
  }
   
  static TextSizeProbeStore getInstance() {
    Object instance
      = SessionSingletonBase.getInstance( TextSizeProbeStore.class );
    return ( TextSizeProbeStore )instance;
  }
  
  IProbeResult getProbeResult( final FontData fontData ) {
    return ( IProbeResult )probeResults.get( fontData );
  }
  
  boolean containsProbeResult( final FontData fontData ) {
    return getProbeResult( fontData ) != null;
  }
  
  IProbeResult createProbeResult( final IProbe probe, final Point size ) {
    FontData fontData = probe.getFontData();
    IProbeResult result = new IProbeResult() {
      private float avgCharWidth;
      public IProbe getProbe() {
        return probe;
      }
      public Point getSize() {
        return size;
      }
      public float getAvgCharWidth() {
        if( avgCharWidth == 0 ) {
          BigDecimal width = new BigDecimal( getSize().x );
          BigDecimal charCount 
            = new BigDecimal( getProbe().getString().length() );
          int roundingMethod = BigDecimal.ROUND_HALF_UP;
          avgCharWidth 
            = width.divide( charCount, 2, roundingMethod ).floatValue();
        }
        return avgCharWidth;
      }
    };
    probeResults.put( fontData, result );
    return result;
  }

  
  ////////////////////////////
  // application global probes
  
  public static IProbe[] getProbeList() {
    IProbe[] result;
    synchronized( probes ) {
      if( probes.isEmpty() ) {
        FontData[] fontList = TextSizeStorageRegistry.obtain().getFontList();
        for( int i = 0; i < fontList.length; i++ ) {
          createProbe( fontList[ i ], getProbeString( fontList[ i ] ) );
        }
      }
      result = new IProbe[ probes.size() ];
      probes.values().toArray( result );
    }
    return result;
  }

  private static String getProbeString( final FontData fontData ) {
    // TODO [fappel]: probe string determination different from default
    return DEFAULT_PROBE;
  }
  
  static IProbe getProbe( final FontData font ) {
    IProbe result;
    synchronized( probes ) {
      result = ( IProbe )probes.get( font );
    }
    return result;
  }
  
  static boolean containsProbe( final FontData fontData ) {
    return getProbe( fontData ) != null;
  }
  
  static IProbe createProbe( final FontData fontData, final String probeText ) {
    IProbe result = new ProbeImpl( probeText, fontData );
    synchronized( probes ) {
      probes.put( fontData, result );
    }
    TextSizeStorageRegistry.obtain().storeFont( fontData );
    return result;
  }

  static void reset() {
    synchronized( probes ) {
      ITextSizeStorage registry = TextSizeStorageRegistry.obtain();
      if( registry instanceof DefaultTextSizeStorage ) {
        ( ( DefaultTextSizeStorage )registry ).resetFontList();
      }
      probes.clear();
    }
  }

  static void addProbeRequest( final FontData font ) {
    IProbe probe = getProbe( font );
    if( probe == null ) {
      FontData fontData = font;
      probe = createProbe( fontData, getProbeString( fontData ) );
    }
    getProbeRequestsInternal().add( probe );
  }
  
  static IProbe[] getProbeRequests() {
    Set probeRequests = getProbeRequestsInternal();
    IProbe[] result = new IProbe[ probeRequests.size() ];
    probeRequests.toArray( result );
    return result;
  }

  
  //////////////////
  // helping methods
  
  private static String createProbeParam( final IProbe probe ) {
    StringBuffer result = new StringBuffer();
    final Display display = Display.getCurrent();
    if( display != null ) {
      result.append( "[ " );
      result.append( probe.getFontData().hashCode() );
      result.append( ", " );
      result.append( "\"" );
      result.append( probe.getString() );
      result.append( "\", " );
      result.append(
        TextSizeDeterminationFacade.createFontParam( probe.getFontData() ) );
      result.append( " ]" );
    }
    return result.toString();
  }

  private static Set getProbeRequestsInternal() {
    IServiceStateInfo stateInfo = ContextProvider.getStateInfo();
    Set result = ( Set )stateInfo.getAttribute( PROBE_REQUESTS );
    if( result == null ) {
      result = new HashSet();
      stateInfo.setAttribute( PROBE_REQUESTS, result );
    }
    return result;
  }
}