/** 
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, 
 * Heinrich Heine Universitaet Duesseldorf
 * This software is licenced under EPL 1.0 (http://www.eclipse.org/org/documents/epl-v10.html) 
 * */

package de.bmotionstudio.gef.editor.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import de.bmotionstudio.gef.editor.AttributeConstants;
import de.bmotionstudio.gef.editor.model.BControl;
import de.bmotionstudio.gef.editor.model.Visualization;

public class PasteCommand extends Command {

	private CopyPasteHelper cHelper;

	private HashMap<BControl, BControl> list = new HashMap<BControl, BControl>();

	private List<BControl> parentControls = new ArrayList<BControl>();

	@Override
	public boolean canExecute() {
		cHelper = (CopyPasteHelper) Clipboard.getDefault().getContents();
		if (cHelper == null)
			return false;
		ArrayList<BControl> myList = cHelper.getList();
		if (myList.isEmpty())
			return false;
		Iterator<?> it = myList.iterator();
		while (it.hasNext()) {
			BControl node = (BControl) it.next();
			if (isPastableControl(node)) {
				list.put(node, null);
			}
		}
		return true;
	}

	public boolean addElement(BControl control) {
		if (!parentControls.contains(control)) {
			return parentControls.add(control);
		}
		return false;
	}

	public boolean isContainer(BControl control) {
		return control.canHaveChildren();
	}

	@Override
	public void execute() {

		if (!canExecute())
			return;

		for (BControl parent : parentControls) {

			Iterator<BControl> it = list.keySet().iterator();
			while (it.hasNext()) {
				BControl control = (BControl) it.next();
				control.setParent(parent);
				try {
					BControl clone = (BControl) control.clone();
					clone.setParent(parent);
					int x = Integer.valueOf(Integer.valueOf(clone
							.getAttributeValue(AttributeConstants.ATTRIBUTE_X)
							.toString()));
					int y = Integer.valueOf(Integer.valueOf(clone
							.getAttributeValue(AttributeConstants.ATTRIBUTE_Y)
							.toString()));
					clone.setAttributeValue(AttributeConstants.ATTRIBUTE_X, x
							+ cHelper.getDistance());
					clone.setAttributeValue(AttributeConstants.ATTRIBUTE_Y, y
							+ cHelper.getDistance());
					list.put(control, clone);
					cHelper.setDistance(cHelper.getDistance() + 10);
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
			redo();

		}

	}

	@Override
	public void redo() {
		Iterator<BControl> it = list.values().iterator();
		while (it.hasNext()) {
			BControl control = it.next();
			if (isPastableControl(control)) {
				control.getParent().addChild(control);
			}
		}
	}

	@Override
	public boolean canUndo() {
		return !(list.isEmpty());
	}

	@Override
	public void undo() {
		Iterator<BControl> it = list.values().iterator();
		while (it.hasNext()) {
			BControl bcontrol = it.next();
			if (isPastableControl(bcontrol)) {
				bcontrol.getParent().removeChild(bcontrol);
			}
		}
	}

	public boolean isPastableControl(BControl control) {
		if (control instanceof Visualization)
			return false;
		return true;
	}

	public HashMap<BControl, BControl> getList() {
		return this.list;
	}

}
