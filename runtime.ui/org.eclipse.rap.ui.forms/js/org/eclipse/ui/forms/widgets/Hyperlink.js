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

qx.Class.define( "org.eclipse.ui.forms.widgets.Hyperlink", {
  extend : qx.ui.basic.Atom,

  construct : function( style ) {
    this.base( arguments );
    this.setAppearance( "hyperlink" );
    // TODO [rh] workaround for weird getLabelObject behavior
    this.setLabel( "(empty)" );
    // End of workaround
    var labelObject = this.getLabelObject();
    labelObject.setAppearance( "hyperlink-label" );
    labelObject.setMode( qx.constant.Style.LABEL_MODE_HTML );
    labelObject.setWrap( qx.lang.String.contains( style, "wrap" ) );
    // TODO [rh] workaround for weird getLabelObject behavior
    this.setLabel( "" );
    // End of workaround
    this._savedBackgroundColor = null;
    this._savedTextColor = null;
    this._activeBackgroundColor = null;
    this._activeTextColor = null;
    this._underlineMode = null;
    this._hover = false;
    this.addEventListener( "mousemove", this._onMouseMove, this );
    this.addEventListener( "mouseout", this._onMouseOut, this );
  },

  destruct : function() {
    this.removeEventListener( "mousemove", this._onMouseMove, this );
    this.removeEventListener( "mouseout", this._onMouseOut, this );
  },

  statics : {
    UNDERLINE_NEVER : 1,
    UNDERLINE_HOVER : 2,
    UNDERLINE_ALWAYS : 3,
    // This event handler is added/removed by the server-side LCA
    onClick : function( evt ) {
      if( !org_eclipse_rap_rwt_EventUtil_suspend ) {
        var widgetManager = org.eclipse.swt.WidgetManager.getInstance();
        var id = widgetManager.findIdByWidget( evt.getTarget() );
        var req = org.eclipse.swt.Request.getInstance();
        req.addEvent( "org.eclipse.swt.events.widgetDefaultSelected", id );
        req.send();
      }
    }
  },
    
  members : {

    setActiveBackgroundColor : function( value ) {
      this._activeBackgroundColor = value;
    },

    setActiveTextColor : function( value ) {
      this._activeTextColor = value;      
    },
    
    setUnderlineMode : function( value ) {
      this._underlineMode = value;      
    },

    _onMouseMove : function( evt ) {
      if( !this._hover ) {
        this._savedBackgroundColor = this.getBackgroundColor();
        if( this._activeBackgroundColor != null ) {
          this.setBackgroundColor( this._activeBackgroundColor );
        }
        this._savedTextColor = this.getTextColor();
        if( this._activeTextColor != null ) {
          this.setTextColor( this._activeTextColor );
        }
        var mode = org.eclipse.ui.forms.widgets.Hyperlink.UNDERLINE_HOVER;
        if( this._underlineMode == mode ) {
          this.setStyleProperty( "textDecoration", "underline");
        }
        this._hover = true;
      }
    },

    _onMouseOut : function( evt ) {
      if( this._hover ) {
        this._hover = false;
        this.setBackgroundColor( this._savedBackgroundColor );
        this.setTextColor( this._savedTextColor );
        var mode = org.eclipse.ui.forms.widgets.Hyperlink.UNDERLINE_HOVER;
        if( this._underlineMode == mode ) {
          this.setStyleProperty( "textDecoration", "none");
        }
      }
    }
  }

} );
