/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/

qx.Class.define("org.eclipse.rwt.test.Presenter", {
  extend : qx.core.Target,
  type : "singleton",
  
  construct : function() {
    this.base( arguments );
    
    var content = document.createElement( "div" );
    content.style.zIndex = 1000000;
    content.style.position = "absolute";
    content.style.left = "50%";
    content.style.top = "50%";
    content.style.marginLeft = "-35%";
    content.style.marginTop = "-250px";
    content.style.width = "70%";
    content.style.height = "500px";
    content.style.border = "2px solid black";    
    content.style.backgroundColor = "white";
    document.body.appendChild( content );
    this._content = content;
    
    var text = document.createElement( "div" );
    text.style.overflow = "scroll";
    text.style.top = 0;
    text.style.left = 0;
    text.style.width = "100%";
    text.style.height = "446px";
    text.style.fontFamily = "monospace";
    this._content.appendChild( text );
    this._text = text;
    
    var bar = document.createElement( "div" );    
    bar.style.position = "absolute";
    bar.style.left = 0;
    bar.style.top = 446;
    bar.style.width = "100%";
    bar.style.height = "50px";
    bar.style.borderTopWidth = "2px";
    bar.style.borderTopColor = "black";
    bar.style.borderTopStyle = "solid";        
    this._progressBar = bar;
    content.appendChild( bar );

    var indicator = document.createElement( "div" );
    indicator.style.position = "absolute";
    indicator.style.left = "0px";
    indicator.style.top = "0px";
    indicator.style.width = "0";
    indicator.style.height = "100%";
    indicator.style.border = "0px";    
    bar.appendChild( indicator );
    this._progressIndicator = indicator;
    this.setFailed( false );    
  },

  members : {
    
    setNumberTestsFinished : function( finished, total ) {      
      var pos = Math.round( ( 100 / total ) * finished );
      this._progressIndicator.style.width = pos + "%";
    },
    
    setFailed : function( failed ) {
      this._progressIndicator.style.backgroundColor = 
        failed ? "#FA0000" : "#00F700";
    },
    
    log : function( text ) {
      var msg = document.createTextNode( text );
      var br = document.createElement( "br" );
      this._text.appendChild( msg );
      this._text.appendChild( br );
      br.scrollIntoView();
    }
  }
  
} );