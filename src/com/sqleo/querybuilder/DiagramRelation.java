/*
 *
 * Modified by SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2012 anudeepgade@users.sourceforge.net
 * 
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

package com.sqleo.querybuilder;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.util.I18n;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.io.CSVRelationDefinition;
import com.sqleo.environment.io.ManualDBMetaData;
import com.sqleo.environment.mdi.DialogPreferences;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.QueryTokens.Column;


public class DiagramRelation extends JPanel
{
	public static Color highlightColor = Color.black;
	public static Color normalColor = Color.lightGray;

	private static Stroke highlightStroke = new BasicStroke((float) (2f));
	private static Stroke normalStroke = new BasicStroke((float) (2f));

	QueryTokens.Join querytoken;

	DiagramAbstractEntity primaryEntity;
	DiagramField primaryField;
	DiagramAbstractEntity foreignEntity;
	DiagramField foreignField;

	Anchor anchor;
	ViewDiagram owner;

	private boolean highlight = false;

	DiagramRelation(ViewDiagram owner)
	{
		this.owner = owner;
		setLayout(null);
		setOpaque(false);

		anchor = new Anchor();
	}

	public void setName(String name)
	{
		super.setName(name);
		onPropertyChanged();
	}

	public boolean isHighlight()
	{
		return highlight;
	}
	
	public void setHighlight(boolean b)
	{
		// A black border is better visible.
		// A selected relation is not black drawn with a 2 points stroke
		// setForeground(b ? Color.lightGray : Color.black);
		// anchor.setBorder(new LineBorder(getForeground(),1));

		this.highlight = b;
		this.doResize();
		this.repaint();
	}

	QueryTokens.Join getQueryToken()
	{
		return querytoken;
	}
	
	void setQueryToken(QueryTokens.Join token)
	{
		querytoken = token;
		onPropertyChanged();
	}

	void setValues(int jointype, String operator)
	{
		querytoken.setType(jointype);
		querytoken.getCondition().setOperator(operator);
		onPropertyChanged();
	}

	private void onPropertyChanged()
	{
		String tip = querytoken.getCondition().toString();
		if (this.getName() != null)
			tip = "[ " + this.getName() + " ] " + tip;

		anchor.setToolTipText(tip);

		switch (querytoken.getType())
		{
		case QueryTokens.Join.LEFT_OUTER:
		case QueryTokens.Join.RIGHT_OUTER:
			anchor.setBackground(Color.yellow);
			break;
		case QueryTokens.Join.FULL_OUTER:
			anchor.setBackground(Color.green);
			break;
		default:
			anchor.setBackground(Color.red);
		}
		
		this.doResize();
		this.repaint();
	}

	void onCreate(QueryBuilder builder,QueryTokens.Join join)
	{
		//setQueryToken(new QueryTokens.Join(primaryField.querytoken, "=", foreignField.querytoken));
		setQueryToken(join);
		builder.browser.addFromClause(querytoken);
	}

	void onDestroy(QueryBuilder builder)
	{
		primaryField.unjoined();
		foreignField.unjoined();

		primaryEntity.doFlush();
		foreignEntity.doFlush();

		builder.browser.removeFromClause(querytoken);
	}
	
	
	void doResize()
	{
		if(isArcRendering()){
			doResizeArc();
		}else {
			doResizeLinear();
		}
	}
	
	private boolean isArcRendering(){
		return Preferences.getBoolean(DialogPreferences.QB_RELATION_RENDER_ARC_KEY,true) ;
	}
	
	/**
	 * array of points to draw the connection line. It is updated by the method
	 * doResize()
	 * 
	 */
	private Point[] serie = isArcRendering() ? 
			new Point[] { new Point(0, 0), new Point(0, 0), new Point(0, 0) } : 
			new Point[4]	;
	private Color plusColor;
	private Color minusColor;

	/**
	 * Updates the serie array accordely to the fields positions.
	 * 
	 */
	private void doResizeArc()
	{
		try
		{
			int yFieldP = (int) primaryField.getLocationOnScreen().getY() - (int) primaryEntity.getLocationOnScreen().getY() + primaryEntity.getLocation().y;
			int yFieldF = (int) foreignField.getLocationOnScreen().getY() - (int) foreignEntity.getLocationOnScreen().getY() + foreignEntity.getLocation().y;

			int py = yFieldP + (primaryField.getSize().height / 2);
			int fy = yFieldF + (foreignField.getSize().height / 2);
	
			// 1. Check for space between the two fields...
			//        ____ ____
			//        | P |__ ____ ____ __| P |
			//        |____| \__ | F | | F |__/ |____|
			//             |____| |____|
			// px1 px2 fx1 fx2 fx1 fx2 px1 px2
	
			int px1 = primaryEntity.getLocation().x;
			int px2 = px1 + primaryEntity.getSize().width;
	
			int fx1 = foreignEntity.getLocation().x;
			int fx2 = fx1 + foreignEntity.getSize().width;
	
			int xMin = 0, yMin = 0, xMax = 0, yMax = 0;
	
			if (px2 < fx1)
			{
				plusColor = this.isRight() || this.isFull() ? isHighlight() ? Color.green.darker() : Color.green  : isHighlight() ? Color.black: Color.lightGray;
				minusColor = this.isLeft() || this.isFull() ? isHighlight() ? Color.green.darker() : Color.green  : isHighlight() ? Color.black: Color.lightGray;

				serie[0].x = px2;
				serie[0].y = py;
				serie[2].x = fx1;
				serie[2].y = fy;
				serie[1].x = (px2 + fx1) / 2;
				serie[1].y = (py + fy) / 2;
	
				xMin = px1;
				xMax = fx2;
			}
			else if (px1 > fx2)
			{
				plusColor = this.isLeft() || this.isFull() ? isHighlight() ? Color.green.darker() : Color.green  : isHighlight()?Color.black:Color.lightGray;
				minusColor = this.isRight() || this.isFull() ? isHighlight() ? Color.green.darker() : Color.green  : isHighlight()?Color.black:Color.lightGray;

				serie[0].x = fx2;
				serie[0].y = fy;
				serie[2].x = px1;
				serie[2].y = py;
				serie[1].x = (fx2 + px1) / 2;
				serie[1].y = (py + fy) / 2;
	
				xMin = fx1;
				xMax = px2;
			}
			else
			{
				plusColor = this.isRight() || this.isFull() ? isHighlight() ? Color.green.darker() : Color.green  : isHighlight()?Color.black:Color.lightGray;
				minusColor = this.isLeft() || this.isFull() ? isHighlight() ? Color.green.darker() : Color.green  : isHighlight()?Color.black:Color.lightGray;

				serie[0].x = px2;
				serie[0].y = py;
				serie[2].x = fx2;
				serie[2].y = fy;
				serie[1].x = Math.max(px2, fx2) + 30;
				serie[1].y = (py + fy) / 2;
	
				xMin = Math.min(px2, fx2);
				xMax = serie[1].x;
			}
	
			yMin = Math.min(py, fy);
			yMax = Math.max(py, fy);
	
			Rectangle area = new Rectangle(xMin, yMin, xMax - xMin + 1, yMax - yMin + 1);
	
			for (int i = 0; i < serie.length; ++i)
			{
				serie[i].x -= area.x;
				serie[i].y -= area.y;
			}
			setBounds(area);
			anchor.setLocation(serie[1].x + area.x - (anchor.getSize().width / 2), serie[1].y + area.y - (anchor.getSize().height / 2));
        }
        catch(Exception e)
        {
        	// BUG: 1929659
        }
	}
	
	private void doResizeLinear(){
	try{	
 		int yFieldP = (int)primaryField.getLocationOnScreen().getY() - (int)primaryEntity.getLocationOnScreen().getY() + primaryEntity.getLocation().y;
 		int yFieldF = (int)foreignField.getLocationOnScreen().getY() - (int)foreignEntity.getLocationOnScreen().getY() + foreignEntity.getLocation().y;
 
		int yStart = yFieldP + (primaryField.getSize().height/2);
		int yEnd = yFieldF + (foreignField.getSize().height/2);

		int xMin = primaryEntity.getLocation().x;
		int xEnd = foreignEntity.getLocation().x;
		int xMax = foreignEntity.getLocation().x + foreignEntity.getSize().width;
		int xStart = primaryEntity.getLocation().x + primaryEntity.getSize().width;

		if(xStart < xEnd){
			plusColor = this.isRight() || this.isFull() ? isHighlight() ? Color.green.darker() : Color.green  : isHighlight() ? Color.black: Color.lightGray;
			minusColor = this.isLeft() || this.isFull() ? isHighlight() ? Color.green.darker() : Color.green  : isHighlight() ? Color.black: Color.lightGray;
		}else if (xStart > xMax){
			plusColor = this.isLeft() || this.isFull() ? isHighlight() ? Color.green.darker() : Color.green  : isHighlight()?Color.black:Color.lightGray;
			minusColor = this.isRight() || this.isFull() ? isHighlight() ? Color.green.darker() : Color.green  : isHighlight()?Color.black:Color.lightGray;
		}else {
			plusColor = this.isRight() || this.isFull() ? isHighlight() ? Color.green.darker() : Color.green  : isHighlight() ? Color.black: Color.lightGray;
			minusColor = this.isLeft() || this.isFull() ? isHighlight() ? Color.green.darker() : Color.green  : isHighlight() ? Color.black: Color.lightGray;
		}

		if(xEnd < xMin)
		{
			int x = xEnd;
			xEnd = xMin;
			xMin = x;
			
			yStart = yFieldF + (foreignField.getSize().height/2);
			yEnd = yFieldP + (primaryField.getSize().height/2);
		}
	
		if(xStart > xMax)
		{
			int x = xStart;
			xStart = xMax;
			xMax = x;
		}

		int yMin = primaryEntity.getLocation().y;
		int yMinF = foreignEntity.getLocation().y;
		if(yMinF < yMin) { 
			yMin = yMinF;
		}
		
		int yMax = primaryEntity.getLocation().y + primaryEntity.getSize().height;
		int yMaxF = foreignEntity.getLocation().y + foreignEntity.getSize().height;
		if(yMaxF > yMax) {
			yMax = yMaxF;
		}
		
		Rectangle area = new Rectangle(xMin, yMin, xMax-xMin, yMax-yMin);

		yStart-= area.y;
		yEnd-= area.y;
		
		int y = yStart > yEnd ? yEnd + ((yStart-yEnd)/2) : yStart + ((yEnd-yStart)/2);
		int x = xEnd - xStart;
		if( x > (anchor.getSize().width*2))
			x = xStart - area.x + (x/2);
		else
			x = (area.width=area.width+30)- 15;

		serie[0] = new Point(xStart-area.x,yStart);
		serie[1] = new Point(x,yStart);
		serie[2] = new Point(x,yEnd);
		serie[3] = new Point(xEnd-area.x,yEnd);
		
 		setBounds(area);
 		anchor.setLocation(x + area.x - (anchor.getSize().width/2), y + area.y - (anchor.getSize().height/2));
 		
	   }catch(Exception e)
	   { 
	    // BUG: 1929659
	   }

	}
	
	protected void paintChildren(Graphics g)
	{
		if(isArcRendering()){
			paintArc(g);
		}else {
			paintLinear(g);
		}

	}
	
	protected void paintArc(Graphics g)
	{
		((Graphics2D) g).setStroke(isHighlight() ? highlightStroke : normalStroke);

		int arc_w = serie[2].x - serie[0].x;
		int arc_h = serie[2].y - serie[0].y;

		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// -\_ and _/-
		if (serie[0].x > serie[1].x || serie[2].x > serie[1].x)
		{
			if (arc_h == 0)
			{
				int midx = (serie[0].x+serie[2].x)/2;
				int midy = (serie[0].y+serie[2].y)/2;
				g.setColor(plusColor);
				g.drawLine(serie[0].x, serie[0].y, midx, midy) ;
				
				g.setColor(minusColor);
				g.drawLine(midx, midy, serie[2].x, serie[2].y);
			}
			else if (arc_h < 0) // __/--
			{
				g.setColor(plusColor);
				g.drawArc(serie[0].x - arc_w / 2, serie[0].y + arc_h, arc_w, -arc_h, 270, 90);
				
				g.setColor(minusColor);
				g.drawArc(serie[0].x + arc_w / 2, serie[0].y + arc_h, arc_w, -arc_h, 180, -90);
			}
			else if (arc_h > 0) // --\_
			{
				g.setColor(plusColor);
				g.drawArc(serie[0].x - arc_w / 2, serie[0].y, arc_w, arc_h, 0, 90);
				
				g.setColor(minusColor);
				g.drawArc(serie[0].x + arc_w / 2, serie[0].y, arc_w, arc_h, 180, 90);
			}
		}
		else
		{

			if (arc_h == 0)
			{
				int midx = (serie[0].x+serie[2].x)/2;
				int midy = (serie[0].y+serie[2].y)/2;
				g.setColor(plusColor);
				g.drawLine(serie[0].x, serie[0].y, midx, midy) ;
				
				g.setColor(minusColor);
				g.drawLine(midx, midy, serie[2].x, serie[2].y);
				
			}
			else if (arc_h < 0) // ]
			{
				arc_w = serie[1].x - serie[0].x;
				arc_h = serie[0].y - serie[1].y;

				g.setColor(plusColor);
				g.drawArc(serie[0].x - arc_w, serie[1].y - arc_h, arc_w * 2, arc_h * 2, 270, 90);

				arc_w = serie[1].x - serie[2].x;
				arc_h = serie[1].y - serie[2].y;

				g.setColor(minusColor);
				g.drawArc(serie[2].x - arc_w, serie[2].y, arc_w * 2, arc_h * 2, 90, -90);

			}
			else if (arc_h > 0) // ]
			{
				arc_w = serie[1].x - serie[0].x;
				arc_h = serie[1].y - serie[0].y;
				
				g.setColor(plusColor);
				g.drawArc(serie[0].x - arc_w, serie[0].y, arc_w * 2, arc_h * 2, 90, -90);

				arc_w = serie[1].x - serie[2].x;
				arc_h = serie[2].y - serie[1].y;

				g.setColor(minusColor);
				g.drawArc(serie[2].x - arc_w, serie[1].y - arc_h, arc_w * 2, arc_h * 2, 270, 90);

			}
		}

//		 g.drawLine(serie[0].x,serie[0].y,serie[1].x,serie[1].y);
//		 g.drawLine(serie[1].x,serie[1].y,serie[2].x,serie[2].y);
//		 g.drawLine(serie[2].x,serie[2].y,serie[3].x,serie[3].y);

		super.paintChildren(g);
	}
	
	protected void paintLinear(Graphics g)
	{
		((Graphics2D) g).setStroke(isHighlight() ? highlightStroke : normalStroke);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		 
		 g.setColor(plusColor);
		 g.drawLine(serie[0].x,serie[0].y,serie[1].x,serie[1].y);
		 g.drawLine(serie[1].x,serie[1].y,(serie[1].x+serie[2].x)/2,(serie[1].y+serie[2].y)/2);

		 g.setColor(minusColor);
		 g.drawLine((serie[1].x+serie[2].x)/2,(serie[1].y+serie[2].y)/2,serie[2].x,serie[2].y);
		 g.drawLine(serie[2].x,serie[2].y,serie[3].x,serie[3].y);

		super.paintChildren(g);
	}

	private boolean isLeft(){
		return QueryTokens.Join.LEFT_OUTER == querytoken.getType();
	}
	
	private boolean isRight(){
		return QueryTokens.Join.RIGHT_OUTER == querytoken.getType();
	}
	
	private boolean isFull(){
		return QueryTokens.Join.FULL_OUTER == querytoken.getType();
	}	
	
	

	private class Anchor extends JPanel implements MouseListener
	{
		Anchor()
		{
			addMouseListener(this);
			setBorder(LineBorder.createBlackLineBorder());
			setBackground(Color.red);
			setOpaque(true);
			setSize(10, 10);
		}

		public void mouseClicked(MouseEvent me)
		{
			if (SwingUtilities.isRightMouseButton(me))
			{
				JPopupMenu popup = new JPopupMenu();
				popup.add(new ActionEdit());
				popup.addSeparator();
				popup.add(new ActionRemove());
				popup.add(new ActionSaveToDefinitionFile());

				popup.show(this, me.getX(), me.getY());
			}
                        else if (me.getClickCount() == 2)
                        {
                            new  ActionEdit().actionPerformed(new ActionEvent(this,0,""));
                        }
			// else // We want the highlight always...
			DiagramRelation.this.owner.setHighlight(DiagramRelation.this);
		}

		public void mouseEntered(MouseEvent e)
		{
		}

		public void mouseExited(MouseEvent e)
		{
		}

		public void mousePressed(MouseEvent e)
		{
		}

		public void mouseReleased(MouseEvent e)
		{
		}
	}
	private class ActionSaveToDefinitionFile extends AbstractAction
	{
		private String fkDefFileName = null;
		ActionSaveToDefinitionFile()
		{
			super(I18n.getString("querybuilder.menu.savejoin", "save to definition file..."));
			ManualDBMetaData md = ConnectionAssistant.getManualDBMetaData(owner.getBuilder().getConnectionHandlerKey());
			setEnabled(md!=null);
			if(md!=null){
				fkDefFileName = md.getFKDefFileName();
			}
		}

		public void actionPerformed(ActionEvent e)
		{
			int option = JOptionPane.showConfirmDialog(Application.window,"Do you want to add join to definition file ?",Application.PROGRAM,JOptionPane.YES_NO_OPTION);
			if(option == JOptionPane.YES_OPTION){
				if(fkDefFileName!=null){
					String joinName = getName();
					Column fk = foreignField.querytoken;
					Column pk = primaryField.querytoken;
					if(null == joinName) {
						String tabColName = fk.getTable().getName()+"."+fk.getName();
						int option2 = JOptionPane.showConfirmDialog(Application.window,"Does "+tabColName+" belongs to Primary Table ?",Application.PROGRAM,JOptionPane.YES_NO_OPTION);
						if(option2 == JOptionPane.YES_OPTION){
							pk = foreignField.querytoken;
							fk = primaryField.querytoken;
						}
					}
					
					CSVRelationDefinition rdef = new CSVRelationDefinition();
					rdef.setJoinType(querytoken.getTypeName());
					// ticket # 134 
					if (owner.getBuilder().objects.jComboBoxSchemas.getItemCount() >0 ) {
						rdef.setFktSchema(fk.getTable().getSchema() != null ? fk.getTable().getSchema() : owner.getBuilder().objects.jComboBoxSchemas.getSelectedItem().toString());
						rdef.setPktSchema(pk.getTable().getSchema() != null ? pk.getTable().getSchema() : owner.getBuilder().objects.jComboBoxSchemas.getSelectedItem().toString());
					}else{
						rdef.setFktSchema(null);
						rdef.setPktSchema(null);
					}
					rdef.setFktName(fk.getTable().getName());
					rdef.setFktColumnName(fk.getName());
					rdef.setPktName(pk.getTable().getName());
					rdef.setPktColumnName(pk.getName());
					// Alias is too much difficult to use
					// rdef.setPktAlias(pk.getTable().getAlias());
					String relName= rdef.getFktName().toUpperCase()+"_"+rdef.getPktName().toUpperCase();
					if(joinName!=null){
						relName="SQLeo_"+joinName;
					}else{
						relName="SQLeo_"+relName;
					}
					rdef.setFkName(relName);
					rdef.setPkName(relName);
					
					ManualDBMetaData.saveDefinitionToFile(rdef, fkDefFileName);
				}
			}
		}
	}
	private class ActionEdit extends AbstractAction
	{
		ActionEdit()
		{
			super(I18n.getString("querybuilder.menu.edit", "edit..."));
		}

		public void actionPerformed(ActionEvent e)
		{
			new MaskJoin(DiagramRelation.this,DiagramRelation.this.owner.getBuilder()).showDialog();
		}
	}

	private class ActionRemove extends AbstractAction
	{
		ActionRemove()
		{
			super(I18n.getString("querybuilder.menu.remove", "remove"));
		}

		public void actionPerformed(ActionEvent e)
		{
			DiagramRelation.this.owner.removeRelation(DiagramRelation.this);
		}
	}
}