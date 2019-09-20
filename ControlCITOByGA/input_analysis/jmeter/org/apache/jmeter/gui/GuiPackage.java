/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
 package org.apache.jmeter.gui;

import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.util.ListedHashTree;

/**
 * Title:        JMeter
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      Apache
 * @author Michael Stover
 * @version 1.0
 */

public class GuiPackage
{

	private static GuiPackage guiPack;
	private boolean dirty = false;

	private GuiPackage()
	{
	}

	private JMeterTreeModel treeModel;
	private org.apache.jmeter.gui.MainFrame mainFrame;
	private org.apache.jmeter.gui.tree.JMeterTreeListener treeListener;

	public static GuiPackage getInstance(JMeterTreeListener listener,
							JMeterTreeModel treeModel)
	{
		if(guiPack == null)
		{
			guiPack = new GuiPackage();
			guiPack.setTreeListener(listener);
			guiPack.setTreeModel(treeModel);
		}
		return guiPack;
	}

	public void setDirty(boolean d)
	{
		dirty = d;
	}

	public boolean isDirty()
	{
		return dirty;
	}

	public void addSubTree(ListedHashTree subTree) throws IllegalUserActionException
	{
		treeModel.addSubTree(subTree,treeListener.getCurrentNode());
	}

	public ListedHashTree getCurrentSubTree()
	{
		return treeModel.getCurrentSubTree(treeListener.getCurrentNode());
	}

	public static GuiPackage getInstance()
	{
		return guiPack;
	}

	public JMeterTreeModel getTreeModel()
	{
		return treeModel;
	}

	public void setTreeModel(JMeterTreeModel newTreeModel)
	{
		treeModel = newTreeModel;
	}

	public void setMainFrame(org.apache.jmeter.gui.MainFrame newMainFrame)
	{
		mainFrame = newMainFrame;
	}

	public org.apache.jmeter.gui.MainFrame getMainFrame()
	{
		return mainFrame;
	}
	public void setTreeListener(org.apache.jmeter.gui.tree.JMeterTreeListener newTreeListener)
	{
		treeListener = newTreeListener;
	}
	public org.apache.jmeter.gui.tree.JMeterTreeListener getTreeListener()
	{
		return treeListener;
	}
}
