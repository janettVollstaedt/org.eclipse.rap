/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.dialogs.WorkingSetConfigurationBlock;
import org.eclipse.ui.tests.harness.util.ArrayUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;

public class IWorkingSetManagerTest extends UITestCase {
    final static String WORKING_SET_NAME_1 = "ws1";

    final static String WORKING_SET_NAME_2 = "ws2";

    final static String WORKING_SET_NAME_3 = "ws3";

    IWorkingSetManager fWorkingSetManager;

    IWorkspace fWorkspace;

    IWorkingSet fWorkingSet;

    String fChangeProperty;

    Object fChangeNewValue;

    Object fChangeOldValue;

    class TestPropertyChangeListener implements IPropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            fChangeProperty = event.getProperty();
            fChangeNewValue = event.getNewValue();
            fChangeOldValue = event.getOldValue();
        }
    }

    public IWorkingSetManagerTest(String testName) {
        super(testName);
    }

    protected void doSetUp() throws Exception {
        super.doSetUp();
        fWorkingSetManager = fWorkbench.getWorkingSetManager();
        fWorkspace = ResourcesPlugin.getWorkspace();
        fWorkingSet = fWorkingSetManager.createWorkingSet(WORKING_SET_NAME_1,
                new IAdaptable[] { fWorkspace.getRoot() });

        IWorkingSet[] workingSets = fWorkingSetManager.getWorkingSets();
        for (int i = 0; i < workingSets.length; i++) {
            fWorkingSetManager.removeWorkingSet(workingSets[i]);
        }
    }

    void resetChangeData() {
        fChangeProperty = "";
        fChangeNewValue = null;
        fChangeOldValue = null;
    }

    /**
     * Tests the utility method found on the WorkingSetConfigurationBlock.
     */
    public void testConfigBlockFilter() {
    	final String [] setIds = new String[] {"5", "2", "4", "1", "3" };

    	IWorkingSet [] sets = new IWorkingSet[setIds.length * 3];
    	for (int i = 0; i < setIds.length; i++) {
			sets[i * 3] = createSet(setIds, i);
			sets[i * 3 + 1] = createSet(setIds, i);
			sets[i * 3 + 2] = createSet(setIds, i);
		}
    	IWorkingSet [] newSets = WorkingSetConfigurationBlock.filter(sets, setIds);
    	assertEquals(sets.length, newSets.length);

    	for (int j = 0; j < setIds.length; j++) {
    		newSets = WorkingSetConfigurationBlock.filter(sets, new String [] {setIds[j]});
    		assertEquals(3, newSets.length);
    		assertEquals(setIds[j], newSets[0].getId());
    		assertEquals(setIds[j], newSets[1].getId());
    		assertEquals(setIds[j], newSets[2].getId());
		}

    }

    public void testAddPropertyChangeListener() throws Throwable {
        IPropertyChangeListener listener = new TestPropertyChangeListener();
        fWorkingSetManager.addPropertyChangeListener(listener);

        resetChangeData();
        fWorkingSetManager.removeWorkingSet(fWorkingSet);
        assertEquals("", fChangeProperty);

        resetChangeData();
        fWorkingSetManager.addWorkingSet(fWorkingSet);
        assertEquals(IWorkingSetManager.CHANGE_WORKING_SET_ADD, fChangeProperty);
        assertEquals(null, fChangeOldValue);
        assertEquals(fWorkingSet, fChangeNewValue);

        resetChangeData();
        fWorkingSetManager.removeWorkingSet(fWorkingSet);
        assertEquals(IWorkingSetManager.CHANGE_WORKING_SET_REMOVE,
                fChangeProperty);
        assertEquals(fWorkingSet, fChangeOldValue);
        assertEquals(null, fChangeNewValue);

        resetChangeData();
		fWorkingSet.setLabel(WORKING_SET_NAME_3); // set the label first to
													// something other than the
													// new name. This will allow
													// us to test for the name
													// property apart from the
													// label property
		assertEquals(IWorkingSetManager.CHANGE_WORKING_SET_LABEL_CHANGE,
				fChangeProperty);
		assertEquals(null, fChangeOldValue);
		assertEquals(fWorkingSet, fChangeNewValue);
		fWorkingSet.setName(WORKING_SET_NAME_2);
		assertEquals(IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE,
				fChangeProperty);
		assertEquals(null, fChangeOldValue);
		assertEquals(fWorkingSet, fChangeNewValue);

        resetChangeData();
        fWorkingSet.setElements(new IAdaptable[] {});
        assertEquals(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE,
                fChangeProperty);
        assertEquals(null, fChangeOldValue);
        assertEquals(fWorkingSet, fChangeNewValue);
    }

    public void testAddRecentWorkingSet() throws Throwable {
        fWorkingSetManager.addRecentWorkingSet(fWorkingSet);
        fWorkingSetManager.addWorkingSet(fWorkingSet);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] { fWorkingSet },
                fWorkingSetManager.getRecentWorkingSets()));

        IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(
                WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
        fWorkingSetManager.addRecentWorkingSet(workingSet2);
        fWorkingSetManager.addWorkingSet(workingSet2);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] { workingSet2,
                fWorkingSet }, fWorkingSetManager.getRecentWorkingSets()));
    }

    public void testAddWorkingSet() throws Throwable {
        fWorkingSetManager.addWorkingSet(fWorkingSet);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] { fWorkingSet },
                fWorkingSetManager.getWorkingSets()));

        boolean exceptionThrown = false;
        try {
            fWorkingSetManager.addWorkingSet(fWorkingSet);
        } catch (RuntimeException exception) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] { fWorkingSet },
                fWorkingSetManager.getWorkingSets()));
    }

    public void testCreateWorkingSet() throws Throwable {
        IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(
                WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
        assertEquals(WORKING_SET_NAME_2, workingSet2.getName());
        assertTrue(ArrayUtil.equals(new IAdaptable[] { fWorkspace.getRoot() },
                workingSet2.getElements()));

        workingSet2 = fWorkingSetManager.createWorkingSet("",
                new IAdaptable[] {});
        assertEquals("", workingSet2.getName());
        assertTrue(ArrayUtil.equals(new IAdaptable[] {}, workingSet2
                .getElements()));
    }

// RAP [if] Commented as it fails with RAP X
// RAP [hs] fails because working sets come form the IDE    
//    public void testCreateWorkingSetFromMemento() throws Throwable {
//        IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(
//                WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
//        IMemento memento = XMLMemento.createWriteRoot("savedState"); //$NON-NLS-1$
//        workingSet2.saveState(memento);
//        IWorkingSet restoredWorkingSet2 = fWorkingSetManager
//                .createWorkingSet(memento);
//        assertEquals(WORKING_SET_NAME_2, restoredWorkingSet2.getName());
//        assertTrue(ArrayUtil.equals(new IAdaptable[] { fWorkspace.getRoot() },
//                restoredWorkingSet2.getElements()));
//    }

    public void testCreateWorkingSetSelectionDialog() throws Throwable {
        IWorkbenchWindow window = openTestWindow();
        IWorkingSetSelectionDialog dialog = fWorkingSetManager
                .createWorkingSetSelectionDialog(window.getShell(), true);

        assertNotNull(dialog);
    }

    public void testGetRecentWorkingSets() throws Throwable {
        assertEquals(0, fWorkingSetManager.getRecentWorkingSets().length);

        fWorkingSetManager.addRecentWorkingSet(fWorkingSet);
        fWorkingSetManager.addWorkingSet(fWorkingSet);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] { fWorkingSet },
                fWorkingSetManager.getRecentWorkingSets()));

        IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(
                WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
        fWorkingSetManager.addRecentWorkingSet(workingSet2);
        fWorkingSetManager.addWorkingSet(workingSet2);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] { workingSet2,
                fWorkingSet }, fWorkingSetManager.getRecentWorkingSets()));

        fWorkingSetManager.removeWorkingSet(workingSet2);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] { fWorkingSet },
                fWorkingSetManager.getRecentWorkingSets()));
    }

    public void testGetWorkingSet() throws Throwable {
        assertNull(fWorkingSetManager.getWorkingSet(WORKING_SET_NAME_1));

        fWorkingSetManager.addWorkingSet(fWorkingSet);
        assertNotNull(fWorkingSetManager.getWorkingSet(fWorkingSet.getName()));

        assertNull(fWorkingSetManager.getWorkingSet(""));

        assertNull(fWorkingSetManager.getWorkingSet(null));
    }

    public void testGetWorkingSets() throws Throwable {
		assertTrue(ArrayUtil.equals(new IWorkingSet[] {}, fWorkingSetManager
				.getWorkingSets()));

		fWorkingSetManager.addWorkingSet(fWorkingSet);
		assertTrue(ArrayUtil.equals(new IWorkingSet[] { fWorkingSet },
				fWorkingSetManager.getWorkingSets()));

		try {
			fWorkingSetManager.addWorkingSet(fWorkingSet);
			fail("Added the same set twice");
		} catch (RuntimeException exception) {
		}
		assertTrue(ArrayUtil.equals(new IWorkingSet[] { fWorkingSet },
				fWorkingSetManager.getWorkingSets()));

		IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(
				WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
		fWorkingSetManager.addWorkingSet(workingSet2);
		assertTrue(ArrayUtil.contains(fWorkingSetManager.getWorkingSets(),
				workingSet2));
		assertTrue(ArrayUtil.contains(fWorkingSetManager.getWorkingSets(),
				fWorkingSet));

		IWorkingSet workingSet3 = fWorkingSetManager.createWorkingSet(
				WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
		workingSet3.setName("ws0");
		workingSet3.setLabel(WORKING_SET_NAME_2); // reset the label - it
													// would be set to ws0 by
													// the above call.
		fWorkingSetManager.addWorkingSet(workingSet3);

		// asserts the order is correct - the name of set three should push it
		// above set two even though their labels are the same
		IWorkingSet[] sets = fWorkingSetManager.getWorkingSets();
		assertEquals(fWorkingSet, sets[0]);
		assertEquals(workingSet2, sets[2]);
		assertEquals(workingSet3, sets[1]);
    }

    public void testRemovePropertyChangeListener() throws Throwable {
        IPropertyChangeListener listener = new TestPropertyChangeListener();

        fWorkingSetManager.removePropertyChangeListener(listener);

        fWorkingSetManager.addPropertyChangeListener(listener);
        fWorkingSetManager.removePropertyChangeListener(listener);

        resetChangeData();
        fWorkingSet.setName(WORKING_SET_NAME_1);
        assertEquals("", fChangeProperty);
    }

    public void testRemoveWorkingSet() throws Throwable {
        fWorkingSetManager.removeWorkingSet(fWorkingSet);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] {}, fWorkingSetManager
                .getWorkingSets()));

        fWorkingSetManager.addWorkingSet(fWorkingSet);
        IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(
                WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
        fWorkingSetManager.addWorkingSet(workingSet2);
        fWorkingSetManager.removeWorkingSet(fWorkingSet);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] { workingSet2 },
                fWorkingSetManager.getWorkingSets()));
    }

    /**
     * Tests to ensure that a misbehaving listener does not bring down the manager.
     *
     * @throws Throwable
     */
    public void testListenerSafety() throws Throwable {
		final boolean[] result = new boolean[1];
		// add a bogus listener that dies unexpectedly
		IPropertyChangeListener badListener = new IPropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent event) {
				throw new RuntimeException();

			}
		};
		IPropertyChangeListener goodListener = new IPropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent event) {
				result[0] = true;

			}
		};
		fWorkingSetManager.addPropertyChangeListener(badListener);
		fWorkingSetManager.addPropertyChangeListener(goodListener);
		try {
			IWorkingSet set = fWorkingSetManager.createWorkingSet("foo",
					new IAdaptable[0]);
			fWorkingSetManager.addWorkingSet(set);

			assertTrue("Good listener wasn't invoked", result[0]);
		} finally {
			fWorkingSetManager.removePropertyChangeListener(badListener);
			fWorkingSetManager.removePropertyChangeListener(goodListener);
		}
	}

	/**
	 * @param setIds
	 * @param i
	 * @return
	 */
	private IWorkingSet createSet(final String[] setIds, final int i) {
		return new IWorkingSet() {

			public IAdaptable[] adaptElements(IAdaptable[] objects) {
				return null;
			}

			public IAdaptable[] getElements() {
				return null;
			}

			public String getId() {
				return setIds[i] + "";
			}

			public ImageDescriptor getImage() {
				return null;
			}

			public ImageDescriptor getImageDescriptor() {
				return null;
			}

			public String getLabel() {
				return null;
			}

			public String getName() {
				return null;
			}

			public boolean isAggregateWorkingSet() {
				return false;
			}

			public boolean isEditable() {
				return true;
			}

			public boolean isEmpty() {
				return false;
			}

			public boolean isSelfUpdating() {
				return false;
			}

			public boolean isVisible() {
				return true;
			}

			public void setElements(IAdaptable[] elements) {
			}

			public void setId(String id) {
			}

			public void setLabel(String label) {
			}

			public void setName(String name) {
			}

			public String getFactoryId() {
				return null;
			}

			public void saveState(IMemento memento) {
			}

			public Object getAdapter(Class adapter) {
				return null;
			}

			public String toString() {
				return getId();
			}
		};

	}
}
