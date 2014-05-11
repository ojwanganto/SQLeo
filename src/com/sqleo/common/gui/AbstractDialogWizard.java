/*
 * SQLeonardo :: java database frontend
 * Copyright (C) 2004 nickyb@users.sourceforge.net
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.sqleo.common.gui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;

import com.sqleo.common.util.I18n;

public abstract class AbstractDialogWizard extends AbstractDialogModal
{
	private int step = 0;
	
	private JPanel cardPanel;
	private CardLayout cardLayout;

	protected CommandButton btnBack;
	protected CommandButton btnNext;
	
	protected AbstractDialogWizard(Component owner, String title)
	{
		this(owner, title, INITIAL_WIDTH, INITIAL_HEIGHT);
	}
	
	protected AbstractDialogWizard(Component owner, String title, int width, int height)
	{
		super(owner, title, width, height);
		
		cardPanel = new JPanel();
		cardPanel.setLayout(cardLayout = new CardLayout());
		
		getContentPane().add(cardPanel);
		
		btnNext = insertButton(1,I18n.getString("application.next","Next >"));
		btnBack = insertButton(1,I18n.getString("application.back","< Back"));
	}
	
	protected void setBarEnabled(boolean b)
	{
		super.setBarEnabled(b);
		btnBack.setEnabled(step!=0);
	}
	
	protected boolean onBack()
	{
		setStep(0);
		return false;
	}

	protected abstract boolean onNext();
	
	protected void addStep(Component c)
	{
		cardPanel.add("step"+cardPanel.getComponentCount(),c);
	}
	
	protected int getStep()
	{
		return step;
	}
	
	protected void setStep(int idx)
	{
		setBarEnabled(false);
		
		step = idx;
		cardLayout.show(cardPanel,"step"+step);		
		
		if(step==cardPanel.getComponentCount()-1)
			btnNext.setText(I18n.getString("application.ok","Ok"));
		else
			btnNext.setText(I18n.getString("application.next","Next >"));

		setBarEnabled(true);
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == btnBack)
		{
			if(onBack())
				setStep(step-1);
			return;
		}
		else if(ae.getSource() == btnNext)
		{
			if(!onNext()) return;
			
			if(!btnNext.getText().equals(I18n.getString("application.ok","Ok")))
			{
				setStep(step+1);
				return;
			}
		}

		super.actionPerformed(ae);
	}
}