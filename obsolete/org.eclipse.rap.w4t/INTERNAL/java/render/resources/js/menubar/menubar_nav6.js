//<!--
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

  /* the menubar handler prototype */
  function MenuBarHandler() {
    
    /* ******************************************************* *
     * declare member variables of the menubar handler object. *
     * ******************************************************* */

    /* for tracking the currently active button */
    this.activeButton = null;

    /* ******************************************************* *
     * declare member functions of the menubar handler object. *
     * ******************************************************* */

    /* resets all menus on the page, if there was a mousedown
     * which not belonged to the menu  */
    this.pageMousedown = mnu_pageMousedown;

    /** activates the menu that belongs to the hovered button */
    this.buttonMouseover = mnu_buttonMouseover;

    /* toggles the clicked button and resets the currently 
     * active button */
    this.buttonClick = mnu_buttonClick;

    /* configures the passed button as depressed, positions 
     * and shows it */
    this.depressButton = mnu_depressButton;

    /* changes the active button back to initial state */
    this.resetActiveButton = mnu_resetActiveButton;
    
    /* changes an active button back to initial state */
    this.resetButton = mnu_resetButton;
 
    /* helping function: returns the true x coordinate of 
     * an element relative to the page. */
    this.getPageOffsetLeft = mnu_getPageOffsetLeft;

    /* helping function: returns the true y coordinate of 
     * an element relative to the page. */
    this.getPageOffsetTop = mnu_getPageOffsetTop;


    /* ******************************************************* *
     *                       inits.                            *
     * ******************************************************* */
    
    document.addEventListener( "mousedown", mnu_pageMousedown, true );

  } // constructor


  /** activates the menu that belongs to the hovered button */
  function mnu_buttonMouseover( button, menuName, menuBar ) {
    // if any other button menu is active, deactivate it and activate 
    // this one.
    // Note: if this button has no menu, leave the active menu alone.
    if(    this.activeButton 
        && this.activeButton != button ) 
    {
      this.resetButton( this.activeButton );
      if( menuName ) {
        this.buttonClick( button, menuName, menuBar );
      }
    }
  }

  /* toggles the clicked button and resets the currently 
   * active button */
  function mnu_buttonClick( button, menuName, menuBar ) {
    // blur focus from the link to remove the outline
    button.blur();
    // associate the named menu to this button
    // (if not already done)
    if( !button.menu ) {
      button.menu = document.getElementById( menuName );
    }	

    // reset the currently active button, if any
    if(    this.activeButton 
        && this.activeButton != button )
    {
      this.resetButton( this.activeButton );
    }
		
    // toggle the button's state
    if( button.isDepressed ) {
      this.resetButton( button );
    } else {
      this.depressButton( button, menuBar );
    }
    return false;
  }

  /* changes the active button back to initial state */
  function mnu_resetActiveButton() {
    if( this.activeButton != null ) {
      this.resetButton( this.activeButton );
    }
  }
  
  /* changes an active button back to initial state */
  function mnu_resetButton( button ) {
    // restore the button's style class.
    button.className = "menuButton";

    // hide the button's menu.
    if( button.menu ) {
      button.menu.style.visibility = "hidden";
    }

    // set button state and clear active menu variable
    button.isDepressed = false;
    this.activeButton = null;
  }

  /* helping function: returns the true x coordinate of 
   * an element relative to the page. */
  function mnu_getPageOffsetLeft( el ) {
    return     el.offsetLeft 
           + ( el.offsetParent ? this.getPageOffsetLeft( el.offsetParent ) 
                               : 0 );
  }

  /* helping function: returns the true y coordinate of 
   * an element relative to the page. */
  function mnu_getPageOffsetTop( el ) {
    return     el.offsetTop 
           + ( el.offsetParent ? this.getPageOffsetTop( el.offsetParent ) 
                               : 0 );
  }

  /* resets all menus on the page, if there was a mousedown
   * which not belonged to the menu  */
  function mnu_pageMousedown( event ) {
    var el;

    //  exit, if there is no active menu
    if( !menuBarHandler.activeButton ) {
      return;
    }

    // find the element that was clicked on
    el = ( event.target.className ? event.target : event.target.parentNode );

    // exit, if the active button was clicked on
    if( el == menuBarHandler.activeButton ) {
      return;
    }

    // if the element clicked on was not a menu button or item,
    //  close the active menu
    if(    el.className != "menuButton"  
        && el.className != "menuItem" 
        && el.className != "menuItemSep" 
        && el.className != "menu" )
    {
      menuBarHandler.resetButton( menuBarHandler.activeButton );
    }
  }

  /* configures the passed button as depressed, positions and shows it */
  function mnu_depressButton( button, menuBar ) {
    var w, dw, x, y;

    // change the button's style class to make it look 
    // like it's depressed
    button.className = "menuButtonActive";

    // position the associated drop down menu under the button and
    // show it
    x = this.getPageOffsetLeft( button );
    y = this.getPageOffsetTop( button ) + button.offsetHeight;
    button.menu.style.left = x + "px";
    button.menu.style.top  = y + "px";
    button.menu.style.visibility = "visible";
   
    // set button state and set the class member which stores the 
    // active button
    button.isDepressed = true;
    this.activeButton = button;
  }
  
  /*****************************************************************/ 
  /*              create the menubar handler instance              */
  /*****************************************************************/ 
  var menuBarHandler = new MenuBarHandler();
  
//--> end hide JavaScript