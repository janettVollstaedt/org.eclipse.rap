/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.defaultpresentation;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.Geometry;
import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.presentations.PaneFolder;
import org.eclipse.ui.internal.presentations.PaneFolderButtonListener;
import org.eclipse.ui.internal.presentations.util.*;
import org.eclipse.ui.internal.util.Util;

/**
 * @since 1.0
 */
public class DefaultTabFolder extends AbstractTabFolder {

    private PaneFolder paneFolder;
    private Control viewToolBar;
    private Label titleLabel;

    private PaneFolderButtonListener buttonListener = new PaneFolderButtonListener() {
        public void stateButtonPressed(int buttonId) {
            fireEvent(TabFolderEvent.stackStateToEventId(buttonId));
        }

        /**
         * Called when a close button is pressed.
         *
         * @param item the tab whose close button was pressed
         */
        public void closeButtonPressed(CTabItem item) {
            fireEvent(TabFolderEvent.EVENT_CLOSE, getTab(item));
        }
        /**
         *
         * @since 1.0
         */
        public void showList(CTabFolderEvent event) {
        	// TODO hack to prevent opening the default part list
//            event.doit = false;
//            fireEvent(TabFolderEvent.EVENT_SHOW_LIST);
        }
    };

    // Workaround cause SWT.Selection does not carry item
//    private Listener selectionListener = new Listener() {
//        public void handleEvent(Event e) {
    private SelectionListener selectionListener = new SelectionAdapter() {
    	public void widgetSelected(SelectionEvent e) {
            AbstractTabItem item = getTab((CTabItem) e.item);

            if (item != null) {
                fireEvent(TabFolderEvent.EVENT_TAB_SELECTED, item);
            }
        }
    };

    // TODO: [fappel] initialization via theme listener
    private static Color activeForeground = null;
    private static Color inactiveForeground = null;
    private static Color[] activeSelectedBackground = new Color[] { null, null };
    private static Color[] unselectedBackground = new Color[] { null, null };
    private static Color[] inactiveSelectedBackground = new Color[] { null, null };
    private static DefaultTabFolderColors colorsInactiveSelected
      = new DefaultTabFolderColors( inactiveForeground,
                                    inactiveSelectedBackground,
                                    null,
                                    false );
    private static DefaultTabFolderColors colorsActiveSelected
      = new DefaultTabFolderColors( activeForeground,
    	                            activeSelectedBackground,
                                    null,
                                    false );
    private static DefaultTabFolderColors colorsInactive
      = new DefaultTabFolderColors( inactiveForeground,
                                    inactiveSelectedBackground,
                                    null,
                                    false );

    private DefaultTabFolderColors[] activeShellColors = {
    		colorsInactiveSelected, colorsActiveSelected, colorsInactive
    };
    private DefaultTabFolderColors[] inactiveShellColors = {
    		colorsInactiveSelected, colorsActiveSelected, colorsInactive
    };
//    private static DefaultTabFolderColors defaultColors = new DefaultTabFolderColors();
//
//    private DefaultTabFolderColors[] activeShellColors = {defaultColors, defaultColors, defaultColors};
//    private DefaultTabFolderColors[] inactiveShellColors = {defaultColors, defaultColors, defaultColors};
    private boolean shellActive = false;

    /**
     * Create a new instance of the receiver
     *
     * @param parent
     * @param flags
     * @param allowMin
     * @param allowMax
     */
    public DefaultTabFolder(Composite parent, int flags, boolean allowMin, boolean allowMax) {
//        paneFolder = new PaneFolder(parent, flags | SWT.NO_BACKGROUND);
    	paneFolder = new PaneFolder(parent, flags );
        paneFolder.addButtonListener(buttonListener);
        paneFolder.setMinimizeVisible(allowMin);
        paneFolder.setMaximizeVisible(allowMax);
//        paneFolder.getControl().addListener(SWT.Selection, selectionListener);
        ((CTabFolder)paneFolder.getControl()).addSelectionListener(selectionListener);
        paneFolder.setTopRight(null);

        // Initialize view menu dropdown
        {
//            ToolBar actualToolBar = new ToolBar(paneFolder.getControl(), SWT.FLAT | SWT.NO_BACKGROUND);
        	ToolBar actualToolBar = new ToolBar(paneFolder.getControl(), SWT.FLAT);
            viewToolBar = actualToolBar;

	        ToolItem pullDownButton = new ToolItem(actualToolBar, SWT.PUSH);
	        Image hoverImage = WorkbenchImages
	                .getImage(IWorkbenchGraphicConstants.IMG_LCL_RENDERED_VIEW_MENU);
//	        pullDownButton.setDisabledImage(hoverImage);
	        pullDownButton.setImage(hoverImage);
	        pullDownButton.setToolTipText(WorkbenchMessages.get().Menu);
//            actualToolBar.addMouseListener(new MouseAdapter() {
//                public void mouseDown(MouseEvent e) {
//                    fireEvent(TabFolderEvent.EVENT_PANE_MENU, getSelection(), getPaneMenuLocation());
//                }
//            });
            pullDownButton.addSelectionListener(new SelectionAdapter() {
            	public void widgetSelected(SelectionEvent e) {
                    fireEvent(TabFolderEvent.EVENT_PANE_MENU, getSelection(), getPaneMenuLocation());

            		super.widgetSelected(e);
            	}
            });
        }

        // TODO: remove hack
        ActivateListener activateListener = new ActivateAdapter() {
            public void activated( ActivateEvent event ) {
              fireEvent( TabFolderEvent.EVENT_GIVE_FOCUS_TO_PART );
            }
        };

        // Initialize content description label
        {
	        titleLabel = new Label(paneFolder.getControl(), SWT.NONE);
	        titleLabel.moveAbove(null);
	        titleLabel.setVisible(false);
            attachListeners(titleLabel, false);
            ActivateEvent.addListener( titleLabel , activateListener );
        }

        ActivateEvent.addListener( paneFolder.getControl(), activateListener );
        ActivateEvent.addListener( paneFolder.getViewForm(), activateListener );

        attachListeners(paneFolder.getControl(), false);
        attachListeners(paneFolder.getViewForm(), false);

        paneFolder.setTabHeight(computeTabHeight());

        viewToolBar.moveAbove(null);

        // TODO: hack around workbench themes
        Display display = paneFolder.getControl().getDisplay();
        activeShellColors[ 1 ].background[ 0 ]
          = display.getSystemColor( SWT.COLOR_LIST_SELECTION );
        activeShellColors[ 1 ].foreground
          = display.getSystemColor( SWT.COLOR_LIST_SELECTION_TEXT );
//      inactiveShellColors[1].background[ 0 ]
//        = paneFolder.getControl().getDisplay().getSystemColor( SWT.COLOR_LIST_SELECTION );
    }

    /**
     * Changes the minimum number of characters to display in the pane folder
     * tab. This control how much information will be displayed to the user.
     *
     * @param count
     *            The number of characters to display in the tab folder; this
     *            value should be a positive integer.
     * @see org.eclipse.swt.custom.CTabFolder#setMinimumCharacters(int)
     * @since 1.0
     */
    public void setMinimumCharacters(int count) {
        paneFolder.setMinimumCharacters(count);
    }

//    public void setSimpleTabs(boolean simple) {
//        paneFolder.setSimpleTab(simple);
//    }

    /**
     * @param item
     * @return
     * @since 1.0
     */
    protected DefaultTabItem getTab(CTabItem item) {
        return (DefaultTabItem)item.getData();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#computeSize(int, int)
     */
    public Point computeSize(int widthHint, int heightHint) {
        return paneFolder.computeMinimumSize();
    }

    /* package */ PaneFolder getFolder() {
        return paneFolder;
    }

    public AbstractTabItem getSelection() {
        return getTab(paneFolder.getSelection());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#add(int)
     */
    public AbstractTabItem add(int index, int flags) {
        DefaultTabItem result = new DefaultTabItem((CTabFolder)getFolder().getControl(), index, flags);

        result.getWidget().setData(result);

        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getContentParent()
     */
    public Composite getContentParent() {
        return paneFolder.getContentParent();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#setContent(org.eclipse.swt.widgets.Control)
     */
    public void setContent(Control newContent) {
        paneFolder.setContent(newContent);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getItems()
     */
    public AbstractTabItem[] getItems() {
        CTabItem[] items = paneFolder.getItems();

        AbstractTabItem[] result = new AbstractTabItem[items.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = getTab(items[i]);
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getItemCount()
     */
    public int getItemCount() {
        // Override retrieving all the items when we just want the count.
        return paneFolder.getItemCount();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#setSelection(org.eclipse.ui.internal.presentations.util.AbstractTabItem)
     */
    public void setSelection(AbstractTabItem toSelect) {
        paneFolder.setSelection(indexOf(toSelect));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getToolbarParent()
     */
    public Composite getToolbarParent() {
        return paneFolder.getControl();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getControl()
     */
    public Control getControl() {
        return paneFolder.getControl();
    }

    public void setUnselectedCloseVisible(boolean visible) {
        paneFolder.setUnselectedCloseVisible(visible);
    }

//    public void setUnselectedImageVisible(boolean visible) {
//        paneFolder.setUnselectedImageVisible(visible);
//    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getTabArea()
     */
    public Rectangle getTabArea() {
        return Geometry.toDisplay(paneFolder.getControl(), paneFolder.getTitleArea());
    }

    /**
     * @param enabled
     * @since 1.0
     */
    public void enablePaneMenu(boolean enabled) {
        if (enabled) {
            paneFolder.setTopRight(viewToolBar);
            viewToolBar.setVisible(true);
        } else {
            paneFolder.setTopRight(null);
            viewToolBar.setVisible(false);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#setSelectedInfo(org.eclipse.ui.internal.presentations.util.PartInfo)
     */
    public void setSelectedInfo(PartInfo info) {
        String newTitle = DefaultTabItem.escapeAmpersands(info.contentDescription);

        if (!Util.equals(titleLabel.getText(), newTitle)) {
            titleLabel.setText(newTitle);
        }

        if (!info.contentDescription.equals(Util.ZERO_LENGTH_STRING)) {
            paneFolder.setTopLeft(titleLabel);
            titleLabel.setVisible(true);
        } else {
            paneFolder.setTopLeft(null);
            titleLabel.setVisible(false);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getPaneMenuLocation()
     */
    public Point getPaneMenuLocation() {
        Point toolbarSize = viewToolBar.getSize();

        return viewToolBar.toDisplay(0,toolbarSize.y);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getPartListLocation()
     */
    public Point getPartListLocation() {
        return paneFolder.getControl().toDisplay(paneFolder.getChevronLocation());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getSystemMenuLocation()
     */
    public Point getSystemMenuLocation() {
        Rectangle bounds = DragUtil.getDisplayBounds(paneFolder.getControl());

		int idx = paneFolder.getSelectionIndex();
		if (idx > -1) {
		    CTabItem item = paneFolder.getItem(idx);
		    Rectangle itemBounds = item.getBounds();

		    bounds.x += itemBounds.x;
		    bounds.y += itemBounds.y;
		}

		Point location = new Point(bounds.x, bounds.y
		        + paneFolder.getTabHeight());

		return location;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#isOnBorder(org.eclipse.swt.graphics.Point)
     */
    public boolean isOnBorder(Point toTest) {
        Control content = paneFolder.getContent();
        if (content != null) {
            Rectangle displayBounds = DragUtil.getDisplayBounds(content);

            if (paneFolder.getTabPosition() == SWT.TOP) {
                return toTest.y >= displayBounds.y;
            }

            if (toTest.y >= displayBounds.y && toTest.y < displayBounds.y + displayBounds.height) {
                return true;
            }
        }

        return super.isOnBorder(toTest);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#layout(boolean)
     */
    public void layout(boolean flushCache) {
        paneFolder.layout(flushCache);
        super.layout(flushCache);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#setState(int)
     */
    public void setState(int state) {
        paneFolder.setState(state);
        super.setState(state);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#setActive(int)
     */
    public void setActive(int activeState) {
        super.setActive(activeState);
        updateColors();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#setTabPosition(int)
     */
    public void setTabPosition(int tabPosition) {
        paneFolder.setTabPosition(tabPosition);
        super.setTabPosition(tabPosition);
        layout(true);
    }

    public void flushToolbarSize() {
        paneFolder.flushTopCenterSize();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#setToolbar(org.eclipse.swt.widgets.Control)
     */
    public void setToolbar(Control toolbarControl) {
        paneFolder.setTopCenter(toolbarControl);
        super.setToolbar(toolbarControl);
    }

    public void setColors(DefaultTabFolderColors colors, int activationState, boolean shellActivationState) {
        Assert.isTrue(activationState < activeShellColors.length);

        if (shellActivationState) {
            activeShellColors[activationState] = colors;
        } else {
            inactiveShellColors[activationState] = colors;
        }

        if (activationState == getActive() && shellActive == shellActivationState) {
            updateColors();
        }
    }

    /**
     *
     * @since 1.0
     */
    private void updateColors() {
        DefaultTabFolderColors currentColors = shellActive ?
                activeShellColors[getActive()]
                : inactiveShellColors[getActive()];
        paneFolder.setSelectionForeground(currentColors.foreground);
        paneFolder.setSelectionBackground(currentColors.background, currentColors.percentages, currentColors.vertical);
    }

    public void setColors(DefaultTabFolderColors colors, int activationState) {
        setColors(colors, activationState, true);
        setColors(colors, activationState, false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#shellActive(boolean)
     */
    public void shellActive(boolean isActive) {
        this.shellActive = isActive;
        super.shellActive(isActive);

        updateColors();
    }

    /**
     * @param font
     * @since 1.0
     */
//    public void setFont(Font font) {
//        if (font != paneFolder.getControl().getFont()) {
//            paneFolder.getControl().setFont(font);
//            layout(true);
//            paneFolder.setTabHeight(computeTabHeight());
//        }
//    }

    /**
     * @return the required tab height for this folder.
     */
    protected int computeTabHeight() {
//        GC gc = new GC(getControl());
//
//        // Compute the tab height
//        int tabHeight = Math.max(viewToolBar.computeSize(SWT.DEFAULT,
//                SWT.DEFAULT).y, gc.getFontMetrics().getHeight());
//
//        gc.dispose();
//
//        return tabHeight;

        // Compute the tab height
        int toolBarHeight = viewToolBar.computeSize(SWT.DEFAULT,SWT.DEFAULT).y;
        int fontHeight = Graphics.getCharHeight( getControl().getFont() );
        int tabHeight = Math.max(toolBarHeight, fontHeight );

        return tabHeight;
    }

    /**
     * @param b
     * @since 1.0
     */
    public void setSingleTab(boolean b) {
        paneFolder.setSingleTab(b);
        AbstractTabItem[] items = getItems();

        for (int i = 0; i < items.length; i++) {
            DefaultTabItem item = (DefaultTabItem)items[i];

            item.updateTabText();
        }

        layout(true);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		getFolder().setVisible(visible);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#showMinMax(boolean)
	 */
	public void showMinMax(boolean show) {
        paneFolder.showMinMax(show);
	}
}
