/*******************************************************************************
 * Copyright (c) 2008 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 ******************************************************************************/

package org.eclipse.rwt.internal.theme.css;

import java.io.*;

import junit.framework.TestCase;

import org.w3c.css.sac.CSSException;


public class CssFileReader_Test extends TestCase {

  private static final String PACKAGE = "org/eclipse/rwt/internal/theme/css/";
  
  private static final String TEST_SYNTAX_CSS = "TestSyntax.css";

  private static final String TEST_SELECTORS_CSS = "TestSelectors.css";

  private static final int ALL_RULE = 0;
  private static final int ELEMENT_RULE = 1;
  private static final int CLASS_RULE = 2;
  private static final int PSEUDO_CLASS_RULE = 3;
  private static final int ATTRIBUTE_RULE = 4;
  private static final int ATTRIBUTE_VALUE_RULE = 5;
  private static final int ONE_OF_ATTRIBUTE_RULE = 6;
  private static final int COMBINED_ATTRIBUTE_RULE = 7;
  private static final int SELECTOR_LIST_RULE = 8;
  
  public void testParseSac() throws Exception {
    ClassLoader classLoader = CssFileReader_Test.class.getClassLoader();
    InputStream inStream = classLoader.getResourceAsStream( PACKAGE
                                                            + TEST_SYNTAX_CSS );
    assertNotNull( inStream );
    CssFileReader reader = new CssFileReader();
    ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    System.setErr( new PrintStream( stderr ) );
    StyleSheet styleSheet = reader.parse( inStream, TEST_SYNTAX_CSS );
    assertTrue( stderr.size() > 0 );
    StyleRule[] rules = styleSheet.getStyleRules();
    CSSException[] problems = reader.getProblems();
    assertNotNull( rules );
    assertTrue( rules.length > 0 );
    assertNotNull( problems );
    assertTrue( problems.length > 0 );
    assertTrue( containsProblem( problems, "import rules not supported" ) );
    assertTrue( containsProblem( problems, "page rules not supported" ) );
    inStream.close();
  }

  public void testCascadeMatchAll() throws Exception {
    StyleSheet styleSheet = getStyleSheet( TEST_SELECTORS_CSS );
    StyleRule[] styleRules = styleSheet.getStyleRules();
    StylableElement text = new StylableElement( "Text" );
    text.setAttribute( "style", "SIMPLE" );
    StyleRule[] rules = styleSheet.getMatchingStyleRules( text );
    assertNotNull( rules );
    assertEquals( 2, rules.length );
    assertEquals( styleRules[ ALL_RULE ], rules[ 0 ] );
    assertEquals( styleRules[ ATTRIBUTE_VALUE_RULE ], rules[ 1 ] );
  }

  public void testMatchAll() throws Exception {
    StyleRule[] rules = getStyleSheet( TEST_SELECTORS_CSS ).getStyleRules();
    ElementMatcher matchingRule = rules[ ALL_RULE ];
    StylableElement label = new StylableElement( "Label" );
    assertTrue( matchingRule.matches( label ) );
    StylableElement tree = new StylableElement( "Tree" );
    assertTrue( matchingRule.matches( tree ) );
    StylableElement list = new StylableElement( "NONE" );
    assertTrue( matchingRule.matches( list ) );
  }

  public void testMatchElement() throws Exception {
    StyleRule[] rules = getStyleSheet( TEST_SELECTORS_CSS ).getStyleRules();
    ElementMatcher matchingRule = rules[ ELEMENT_RULE ];
    StylableElement label = new StylableElement( "Label" );
    assertTrue( matchingRule.matches( label ) );
    StylableElement button = new StylableElement( "Button" );
    assertFalse( matchingRule.matches( button ) );
  }

  public void testMatchClass() throws Exception {
    StyleRule[] rules = getStyleSheet( TEST_SELECTORS_CSS ).getStyleRules();
    ElementMatcher matchingRule = rules[ CLASS_RULE ];
    StylableElement button = new StylableElement( "Button" );
    assertFalse( matchingRule.matches( button ) );
    button.setClass( "special" );
    assertTrue( matchingRule.matches( button ) );
    StylableElement label = new StylableElement( "Label" );
    assertFalse( matchingRule.matches( label ) );
    label.setClass( "special" );
    assertFalse( matchingRule.matches( label ) );
  }

  public void testMatchPseudoClass() throws Exception {
    StyleRule[] rules = getStyleSheet( TEST_SELECTORS_CSS ).getStyleRules();
    ElementMatcher matchingRule = rules[ PSEUDO_CLASS_RULE ];
    StylableElement list = new StylableElement( "List" );
    assertFalse( matchingRule.matches( list ) );
    list.setPseudoClass( "selected" );
    assertTrue( matchingRule.matches( list ) );
    StylableElement button = new StylableElement( "Button" );
    assertFalse( matchingRule.matches( button ) );
    button.setPseudoClass( "selected" );
    assertFalse( matchingRule.matches( button ) );
  }

  public void testMatchAttribute() throws Exception {
    StyleRule[] rules = getStyleSheet( TEST_SELECTORS_CSS ).getStyleRules();
    ElementMatcher matchingRule = rules[ ATTRIBUTE_RULE ];
    StylableElement text = new StylableElement( "Text" );
    assertFalse( matchingRule.matches( text ) );
    text.setAttribute( "SIMPLE", "" );
    assertFalse( matchingRule.matches( text ) );
    text.setAttribute( "SIMPLE", "x" );
    assertTrue( matchingRule.matches( text ) );
  }

  public void testMatchAttributeValue() throws Exception {
    StyleRule[] rules = getStyleSheet( TEST_SELECTORS_CSS ).getStyleRules();
    ElementMatcher matchingRule = rules[ ATTRIBUTE_VALUE_RULE ];
    StylableElement text = new StylableElement( "Text" );
    assertFalse( matchingRule.matches( text ) );
    text.setAttribute( "style", "" );
    assertFalse( matchingRule.matches( text ) );
    text.setAttribute( "style", "SIMPLE" );
    assertTrue( matchingRule.matches( text ) );
    text.setAttribute( "style", "SIMPLE BORDER" );
    assertFalse( matchingRule.matches( text ) );
  }

  public void testMatchOneOfAttribute() throws Exception {
    StyleRule[] rules = getStyleSheet( TEST_SELECTORS_CSS ).getStyleRules();
    ElementMatcher matchingRule = rules[ ONE_OF_ATTRIBUTE_RULE ];
    StylableElement text = new StylableElement( "Text" );
    assertFalse( matchingRule.matches( text ) );
    text.setAttribute( "style", "" );
    assertFalse( matchingRule.matches( text ) );
    text.setAttribute( "style", "x" );
    assertFalse( matchingRule.matches( text ) );
    text.setAttribute( "BORDER", "x" );
    assertFalse( matchingRule.matches( text ) );
    text.setAttribute( "style", "BORDER" );
    assertTrue( matchingRule.matches( text ) );
    text.setAttribute( "style", "SIMPLE BORDER" );
    assertTrue( matchingRule.matches( text ) );
  }

  public void testCombinedAttributes() throws Exception {
    StyleRule[] rules = getStyleSheet( TEST_SELECTORS_CSS ).getStyleRules();
    ElementMatcher matchingRule = rules[ COMBINED_ATTRIBUTE_RULE ];
    StylableElement text = new StylableElement( "Text" );
    text.setClass( "special" );
    text.setAttribute( "style", "SIMPLE" );
    assertFalse( matchingRule.matches( text ) );
    text.setAttribute( "style", "BORDER" );
    assertFalse( matchingRule.matches( text ) );
    text.setAttribute( "style", "SIMPLE BORDER" );
    assertTrue( matchingRule.matches( text ) );
    text.setAttribute( "style", "BORDER SIMPLE" );
    assertTrue( matchingRule.matches( text ) );
    text.resetClass( "special" );
    assertFalse( matchingRule.matches( text ) );
  }

  public void testSelectorList() throws Exception {
    StyleRule[] rules = getStyleSheet( TEST_SELECTORS_CSS ).getStyleRules();
    ElementMatcher matchingRule = rules[ SELECTOR_LIST_RULE ];
    StylableElement tree = new StylableElement( "Tree" );
    assertFalse( matchingRule.matches( tree ) );
    StylableElement label = new StylableElement( "Label" );
    assertTrue( matchingRule.matches( label ) );
    StylableElement button = new StylableElement( "Button" );
    assertFalse( matchingRule.matches( button ) );
    button.setClass( "special" );
    assertTrue( matchingRule.matches( button ) );
    StylableElement list = new StylableElement( "List" );
    assertFalse( matchingRule.matches( list ) );
    list.setPseudoClass( "selected" );
    assertTrue( matchingRule.matches( list ) );
  }

  private StyleSheet getStyleSheet( final String fileName )
    throws CSSException, IOException
  {
    StyleSheet result;
    ClassLoader classLoader = CssFileReader_Test.class.getClassLoader();
    InputStream inStream = classLoader.getResourceAsStream( PACKAGE + fileName );
    try {
      CssFileReader reader = new CssFileReader();
      result = reader.parse( inStream, TEST_SELECTORS_CSS );
    } finally {
      inStream.close();
    }
    return result;
  }
  
  private boolean containsProblem( final Object[] array, final String part ) {
    boolean result = false;
    for( int i = 0; i < array.length && !result; i++ ) {
      result |= array[ i ].toString().indexOf( part ) != -1;
    }
    return result ;
  }
}