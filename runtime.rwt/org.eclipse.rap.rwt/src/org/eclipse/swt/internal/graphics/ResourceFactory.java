/*******************************************************************************
 * Copyright (c) 2002, 2010 Innoopract Informationssysteme GmbH.
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

import java.io.*;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rwt.internal.resources.ResourceManager;
import org.eclipse.rwt.internal.service.ServletLog;
import org.eclipse.rwt.resources.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.*;


public final class ResourceFactory {

  private static final Map colors = new HashMap();
  private static final Map fonts = new HashMap();
  private static final Map images = new HashMap();
  private static final Map cursors = new HashMap();
  private static final ImageDataCache imageDataCache = new ImageDataCache();

  /////////
  // Colors

  public static Color getColor( final RGB rgb ) {
    if( rgb == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    return getColor( rgb.red, rgb.green, rgb.blue );
  }

  public static Color getColor( final int red,
                                final int green,
                                final int blue )
  {
    int colorNr = computeColorNr( red, green, blue );
    return getColor( colorNr );
  }

  public static int computeColorNr( final int red,
                                    final int green,
                                    final int blue )
  {
    if(    red > 255
        || red < 0
        || green > 255
        || green < 0
        || blue > 255
        || blue < 0 )
    {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    int colorNr = red | green << 8 | blue << 16;
    return colorNr;
  }

  private static Color getColor( final int value ) {
    Color result;
    Integer key = new Integer( value );
    synchronized( colors ) {
      if( colors.containsKey( key ) ) {
        result = ( Color )colors.get( key );
      } else {
        result = createColorInstance( value );
        colors.put( key, result );
      }
    }
    return result;
  }


  ////////
  // Fonts

  public static Font getFont( final String name,
                              final int height,
                              final int style )
  {
    int checkedStyle = checkFontStyle( style );
    Font result;
    Integer key = new Integer( fontHashCode( name, height, checkedStyle ) );
    synchronized( fonts ) {
      result = ( Font )fonts.get( key );
      if( result == null ) {
        FontData fontData = new FontData( name, height, checkedStyle );
        result = createFontInstance( fontData );
        fonts.put( key, result );
      }
    }
    return result;
  }

  public static int fontHashCode( final String name,
                                  final int height,
                                  final int style )
  {
    int nameHashCode = name == null ? 0 : name.hashCode();
    return nameHashCode ^ height << 2 ^ style;
  }

  public static int checkFontStyle( final int style ) {
    int result = SWT.NORMAL;
    if( ( style & SWT.BOLD ) != 0 ) {
      result |= SWT.BOLD;
    }
    if( ( style & SWT.ITALIC ) != 0 ) {
      result |= SWT.ITALIC;
    }
    return result;
  }

  /////////
  // Images

  public static synchronized Image findImage( final String path ) {
    IResourceManager manager = ResourceManager.getInstance();
    return findImage( path, manager.getContextLoader() );
  }

  public static synchronized Image findImage( final String path,
                                              final ClassLoader imageLoader )
  {
    if( path == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( "".equals( path ) ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    Image result;
    if( images.containsKey( path ) ) {
      result = ( Image )images.get( path );
    } else {
      result = createImage( path, imageLoader );
    }
    return result;
  }

  public static synchronized Image findImage( final String path,
                                              final InputStream inputStream )
  {
    if( path == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( "".equals( path ) ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    Image result;
    if( images.containsKey( path ) ) {
      result = ( Image )images.get( path );
    } else {
      result = createImage( path, inputStream );
    }
    return result;
  }

  public static synchronized Image findImage( final ImageData imageData ) {
    if( imageData == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    Image result;
    ImageLoader loader = new ImageLoader();
    loader.data = new ImageData[] { imageData };
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    int type
      = imageData.type != SWT.IMAGE_UNDEFINED ? imageData.type : SWT.IMAGE_PNG;
    loader.save( outputStream, type );
    byte[] bytes = outputStream.toByteArray();
    InputStream inputStream = new ByteArrayInputStream( bytes );
    String path
      = "generated/"
      + hashCode( bytes )
      + getImageFileExtension( type );
    if( images.containsKey( path ) ) {
      result = ( Image )images.get( path );
    } else {
      result = createImage( path, inputStream );
    }
    return result;
  }

  public static String getImagePath( final Image image ) {
    String result = null;
    if( image != null ) {
      result = ResourceManager.getInstance().getLocation( image.resourceName );
    }
    return result;
  }

  public static ImageData getImageData( final Image image ) {
    ImageData result = imageDataCache.getImageData( image );
    if( result == null ) {
      if( image != null ) {
        String imagePath = image.resourceName;
        try {
          IResourceManager manager = ResourceManager.getInstance();
          InputStream inputStream = manager.getRegisteredContent( imagePath );
          if( inputStream != null ) {
            try {
              result = new ImageData( inputStream );
            } finally {
              inputStream.close();
            }
          }
        } catch( IOException e ) {
          // failed to close input stream - should not happen
          throw new RuntimeException( e );
        }
      }
      if( result != null ) {
        imageDataCache.putImageData( image, result );
      }
    }
    return result;
  }

  ///////////
  // Cursors

  public static Cursor getCursor( final int style ) {
    Cursor result;
    Integer key = new Integer( style );
    synchronized( Cursor.class ) {
      result = ( Cursor )cursors.get( key );
      if( result == null ) {
        result = createCursorInstance( style );
        cursors.put( key, result );
      }
    }
    return result;
  }

  ///////////////
  // Test helpers

  public static void clear() {
    colors.clear();
    fonts.clear();
    images.clear();
    cursors.clear();
  }

  static int colorsCount() {
    return colors.size();
  }

  static int fontsCount() {
    return fonts.size();
  }

  static int imagesCount() {
    return images.size();
  }

  static int cursorsCount() {
    return cursors.size();
  }


  //////////////////
  // Helping methods

  private static Image createImage( final String path,
                                    final ClassLoader imageLoader )
  {
    Image result;
    IResourceManager manager = ResourceManager.getInstance();
    ClassLoader loaderBuffer = manager.getContextLoader();
    if( imageLoader != null ) {
      manager.setContextLoader( imageLoader );
    }
    try {
      InputStream inputStream = manager.getResourceAsStream( path );
      result = createImage( path, inputStream );
    } finally {
      manager.setContextLoader( loaderBuffer );
    }
    return result;
  }

  private static Image createImage( final String path,
                                    final InputStream inputStream )
  {
    if( inputStream == null ) {
      String txt = "Image ''{0}'' cannot be found.";
      String msg = MessageFormat.format( txt, new Object[] { path } );
      SWT.error( SWT.ERROR_INVALID_ARGUMENT,
                 new IllegalArgumentException( msg ),
                 msg );
    }
    Point size = registerImage( path, inputStream );
    Image result;
    if( size != null ) {
      result = createImageInstance( path, size.x, size.y );
    } else {
      result = createImageInstance( path, -1, -1 );
    }
    images.put( path, result );
    return result;
  }

  public static Point registerImage( final String name,
                                      final InputStream inputStream )
  {
    ////////////////////////////////////////////////////////////////////////////
    // TODO: [fappel] Image size calculation and resource registration both
    //                read the input stream. Because of this I use a workaround
    //                with a BufferedInputStream. Resetting it after reading the
    //                image size enables the ResourceManager to reuse it for
    //                registration. Note that the order is crucial here, since
    //                the ResourceManager seems to close the stream (shrug).
    //                It would be nice to find a solution without reading the
    //                stream twice.

    BufferedInputStream bis = new BufferedInputStream( inputStream );
    bis.mark( Integer.MAX_VALUE );
    Point result = null;
    try {
      ImageData data = new ImageData( bis );
      result = new Point( data.width, data.height );
    } catch( SWTException e ) {
      ServletLog.log( "Failed to determine size for image: " + name, e );
    }
    try {
      bis.reset();
    } catch( final IOException shouldNotHappen ) {
      String txt = "Could not reset input stream while reading image ''{0}''.";
      String msg = MessageFormat.format( txt, new Object[] { name } );
      throw new RuntimeException( msg, shouldNotHappen );
    }
    IResourceManager manager = ResourceManager.getInstance();
    manager.register( name, bis );
    return result;
  }

  //////////////////
  // Helping methods

  private static String getImageFileExtension( final int type ) {
    String result;
    switch( type ) {
      case SWT.IMAGE_BMP:
      case SWT.IMAGE_BMP_RLE:
      case SWT.IMAGE_OS2_BMP:
        result = ".bmp";
      break;
      case SWT.IMAGE_GIF:
        result = ".gif";
      break;
      case SWT.IMAGE_ICO:
        result = ".ico";
      break;
      case SWT.IMAGE_JPEG:
        result = ".jpg";
      break;
      case SWT.IMAGE_PNG:
        result = ".png";
      break;
      default:
        result = "";
      break;
    }
    return result;
  }

  private static int hashCode( final byte bytes[] ) {
    int result;
    if( bytes == null ) {
      result = 0;
    } else {
      result = 1;
      for( int i = 0; i < bytes.length; i++ ) {
        result = 31 * result + bytes[ i ];
      }
    }
    return result;
  }

  ////////////////////
  // Instance creation

  private static Color createColorInstance( final int colorNr ) {
    Color result = null;
    try {
      Class[] paramList = new Class[] { int.class };
      Constructor constr = Color.class.getDeclaredConstructor( paramList );
      constr.setAccessible( true );
      Object[] args = new Object[] { new Integer( colorNr ) };
      result = ( Color )constr.newInstance( args );
    } catch( final Exception e ) {
      throw new RuntimeException( "Failed to instantiate Color", e );
    }
    return result;
  }

  private static Font createFontInstance( final FontData fontData ) {
    Font result = null;
    try {
      Class[] paramList = new Class[] { FontData.class };
      Constructor constr = Font.class.getDeclaredConstructor( paramList );
      constr.setAccessible( true );
      result = ( Font )constr.newInstance( new Object[] { fontData } );
    } catch( final Exception e ) {
      throw new RuntimeException( "Failed to instantiate Font", e );
    }
    return result;
  }

  private static Image createImageInstance( final String resourceName,
                                            final int width, 
                                            final int height )
  {
    Image result = null;
    try {
      Class imageClass = Image.class;
      Class[] paramList = new Class[] { String.class, int.class, int.class };
      Constructor constructor = imageClass.getDeclaredConstructor( paramList );
      constructor.setAccessible( true );
      Object[] args = new Object[] {
        resourceName,
        new Integer( width ), 
        new Integer( height )
      };
      result = ( Image )constructor.newInstance( args );
    } catch( final Exception e ) {
      throw new RuntimeException( "Failed to instantiate Image", e );
    }
    return result;
  }

  private static Cursor createCursorInstance( final int style ) {
    Cursor result = null;
    try {
      Class cursorClass = Cursor.class;
      Class[] paramList = new Class[] { int.class };
      Constructor constr = cursorClass.getDeclaredConstructor( paramList );
      constr.setAccessible( true );
      result = ( Cursor )constr.newInstance( new Object[] {
        new Integer( style )
      } );
    } catch( final Exception e ) {
      throw new RuntimeException( "Failed to instantiate Cursor", e );
    }
    return result;
  }

  private ResourceFactory() {
    // prevent instantiation
  }
}
