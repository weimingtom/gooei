package de.ofahrt.gooei.impl;

import gooei.Desktop;
import gooei.IconAndText;
import gooei.ScrollableWidget;
import gooei.Widget;
import gooei.utils.Alignment;
import gooei.utils.HAlignment;
import gooei.utils.Icon;
import gooei.utils.VAlignment;

import java.awt.Dimension;
import java.awt.Rectangle;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public class PanelWidget extends AbstractContainerWidget<Widget> implements IconAndText, ScrollableWidget
{

	private static class TableConstraint
	{
		private int colspan = 1;
		private int rowspan = 1;
		private int weightx = 0;
		private int weighty = 0;
		
		private HAlignment halign = HAlignment.FILL;
		private VAlignment valign = VAlignment.FILL;
		
		public int getColspan()
		{ return colspan; }
		public void setColspan(int colspan)
		{ this.colspan = colspan; }
		
		public int getRowspan()
		{ return rowspan; }
		public void setRowspan(int rowspan)
		{ this.rowspan = rowspan; }
		
		public int getWeightX()
		{ return weightx; }
		public void setWeightX(int weightx)
		{ this.weightx = weightx; }
		
		public int getWeightY()
		{ return weighty; }
		public void setWeightY(int weighty)
		{ this.weighty = weighty; }
		
		public HAlignment getHalign()
		{ return halign; }
		public void setHalign(HAlignment halign)
		{
			if (halign == null) throw new NullPointerException();
			this.halign = halign;
		}
		
		public VAlignment getValign()
		{ return valign; }
		public void setValign(VAlignment valign)
		{
			if (valign == null) throw new NullPointerException();
			this.valign = valign;
		}
	}

private int columns = 0;
private int gap = 0;
private int top = 0, left = 0, bottom = 0, right = 0;

private String text = null;
private Icon icon = null;
private Alignment alignment = Alignment.LEFT;

private int titleheight;

private boolean border = false;
private boolean scrollable = false;

public PanelWidget(Desktop desktop)
{ super(desktop); }

public int getColumns()
{ return columns; }

public void setColumns(int columns)
{ this.columns = columns; }

public int getGap()
{ return gap; }

public void setGap(int gap)
{ this.gap = gap; }

public int getTop()
{ return top; }

public void setTop(int top)
{ this.top = top; }

public int getLeft()
{ return left; }

public void setLeft(int left)
{ this.left = left; }

public int getBottom()
{ return bottom; }

public void setBottom(int bottom)
{ this.bottom = bottom; }

public int getRight()
{ return right; }

public void setRight(int right)
{ this.right = right; }

public boolean hasBorder()
{ return border; }

public void setBorder(boolean border)
{ this.border = border; }

public boolean isScrollable()
{ return scrollable; }

public void setScrollable(boolean scrollable)
{ this.scrollable = scrollable; }

public String getText()
{ return text; }

public void setText(String text)
{ this.text = text; }

public Icon getIcon()
{ return icon; }

public void setIcon(Icon icon)
{ this.icon = icon; }

public Alignment getAlignment()
{ return alignment; }

public void setAlignment(Alignment alignment)
{
	if (alignment == null) throw new NullPointerException();
	this.alignment = alignment;
}

@Override
protected TableConstraint getConstraintFor(Widget child)
{ return (TableConstraint) super.getConstraintFor(child); }

public void setColspan(Widget child, int colspan)
{ getConstraintFor(child).setColspan(colspan); }

public void setRowspan(Widget child, int rowspan)
{ getConstraintFor(child).setRowspan(rowspan); }

public void setWeightX(Widget child, int weightx)
{ getConstraintFor(child).setWeightX(weightx); }

public void setWeightY(Widget child, int weighty)
{ getConstraintFor(child).setWeightY(weighty); }

public void setHalign(Widget child, HAlignment halign)
{ getConstraintFor(child).setHalign(halign); }

public void setValign(Widget child, VAlignment valign)
{ getConstraintFor(child).setValign(valign); }

public int getTitleHeight()
{ return titleheight; }

@Override
public boolean acceptChild(Widget child)
{ return !(child instanceof DialogWidget); }

@Override
protected Object createConstraints(Widget child)
{ return new TableConstraint(); }

/**
 * @param component a container
 * @return null for zero visible subcomponent, otherwise an array contains the following lists:
 * <ul><li>columnwidths, preferred width of grid columns</li>
 * <li>rowheights, preferred heights of grid rows</li>
 * <li>columnweights, grid column-width weights</li>
 * <li>rowweights, grid row-height weights</li>
 * <li>gridx, horizontal location of the subcomponents</li>
 * <li>gridy, vertical locations</li>
 * <li>gridwidth, column spans</li>
 * <li>gridheight, row spans</li></ul>
 */
private int[][] getGrid()
{
	int count = 0; // count of the visible subcomponents
	for (final Widget comp : this)
		if (comp.isVisible()) count++;
	if (count == 0) return null; // zero visible subcomponents
	
	int icols = (columns != 0) ? columns : count;
	int irows = (columns != 0) ? ((count + columns - 1) / columns) : 1;
	int[][] grid = {
		new int[icols], new int[irows], // columnwidths, rowheights
		new int[icols], new int[irows], // columnweights, rowweights
		new int[count], new int[count], // gridx, gridy
		new int[count], new int[count] }; // gridwidth, gridheight
	int[] columnheight = new int[icols];
	int[][] cache = null; // preferredwidth, height, columnweight, rowweight
	
	int i = 0;
	int x = 0, y = 0;
	int nextsize = 0;
	int index = 0;
	for (final Widget comp : this)
	{
		TableConstraint constraint = (TableConstraint) getConstraint(index);
		index++;
		if (!comp.isVisible()) continue;
		int colspan = ((columns != 0) && (columns < count)) ?
			Math.min(constraint.getColspan(), columns) : 1;
		int rowspan = (columns != 1) ? constraint.getRowspan() : 1;
		
		for (int j = 0; j < colspan; j++)
		{
			if ((columns != 0) && (x + colspan > columns))
			{
				x = 0;
				y++;
				j = -1;
			}
			else if (columnheight[x + j] > y)
			{
				x += (j + 1);
				j = -1;
			}
		}
		if (y + rowspan > grid[1].length)
		{
			int[] rowheights = new int[y + rowspan];
			System.arraycopy(grid[1], 0, rowheights, 0, grid[1].length);
			grid[1] = rowheights;
			int[] rowweights = new int[y + rowspan];
			System.arraycopy(grid[3], 0, rowweights, 0, grid[3].length);
			grid[3] = rowweights;
		}
		for (int j = 0; j < colspan; j++)
			columnheight[x + j] = y + rowspan;
		
		int weightx = constraint.getWeightX();
		int weighty = constraint.getWeightY();
		Dimension d = comp.getPreferredSize();
		
		if (colspan == 1)
		{
			grid[0][x] = Math.max(grid[0][x], d.width); // columnwidths
			grid[2][x] = Math.max(grid[2][x], weightx); // columnweights
		}
		else
		{
			if (cache == null) cache = new int[4][count];
			cache[0][i] = d.width;
			cache[2][i] = weightx;
			if ((nextsize == 0) || (colspan < nextsize)) nextsize = colspan;
		}
		if (rowspan == 1)
		{
			grid[1][y] = Math.max(grid[1][y], d.height); // rowheights 
			grid[3][y] = Math.max(grid[3][y], weighty); // rowweights
		}
		else
		{
			if (cache == null) cache = new int[4][count];
			cache[1][i] = d.height;
			cache[3][i] = weighty;
			if ((nextsize == 0) || (rowspan < nextsize)) nextsize = rowspan;
		}
		grid[4][i] = x; //gridx
		grid[5][i] = y; //gridy
		grid[6][i] = colspan; //gridwidth
		grid[7][i] = rowspan; //gridheight
		
		x += colspan;
		i++;
	}

	while (nextsize != 0)
	{
		int size = nextsize; nextsize = 0;
		for (int j = 0; j < 2; j++) // horizontal, vertical
		{
			for (int k = 0; k < count; k++)
			{
				if (grid[6 + j][k] == size) // gridwidth, gridheight
				{
					int gridpoint = grid[4 + j][k]; // gridx, gridy
					int weightdiff = cache[2 + j][k];
					for (int m = 0; (weightdiff > 0) && (m < size); m++)
						weightdiff -= grid[2 + j][gridpoint + m];
					if (weightdiff > 0)
					{
						int weightsum = cache[2 + j][k] - weightdiff;
						for (int m = 0; (weightsum > 0) && (m < size); m++)
						{
							int weight = grid[2 + j][gridpoint + m];
							if (weight > 0)
							{
								int weightinc = weight * weightdiff / weightsum;
								grid[2 + j][gridpoint + m] += weightinc;
								weightdiff -= weightinc;
								weightsum -= weightinc;
							}
						}
						grid[2 + j][gridpoint + size - 1] += weightdiff;
					}
					
					int sizediff = cache[j][k];
					int weightsum = 0;
					for (int m = 0; (sizediff > 0) && (m < size); m++)
					{
						sizediff -= grid[j][gridpoint + m];
						weightsum += grid[2 + j][gridpoint + m];
					}
					if (sizediff > 0)
					{
						for (int m = 0; (weightsum > 0) && (m < size); m++)
						{
							int weight = grid[2 + j][gridpoint + m];
							if (weight > 0)
							{
								int sizeinc = weight * sizediff / weightsum;
								grid[j][gridpoint + m] += sizeinc;
								sizediff -= sizeinc;
								weightsum -= weight;
							}
						}
						grid[j][gridpoint + size - 1] += sizediff;
					}
				}
				else if ((grid[6 + j][k] > size) &&
						((nextsize == 0) || (grid[6 + j][k] < nextsize)))
				{
					nextsize = grid[6 + j][k];
				}
			}
		}
	}
	return grid;
}

private int getSum(int[] values, int from, int length, int adjust, boolean last)
{
	if (length <= 0) return 0;
	int value = 0;
	for (int i = 0; i < length; i++)
		value += values[from + i];
	return value + (length - (last ? 0 : 1)) * adjust;
}

@Override
public Dimension getPreferredSize()
{
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	
	// title text and icon height
	Dimension result = desktop.getSize(this, 0, 0);
	// add border size
	if (this instanceof DialogWidget)
	{
		result.width = 8;
		result.height += 8; // title width neglected
	}
	else if (hasBorder())
	{ // bordered panel
		result.width = 2;
		result.height += (result.height > 0) ? 1 : 2; // title includes line
	}
	else
		result.width = 0; // title width is clipped
	// add paddings
	result.width += getLeft() + getRight();
	result.height += getTop() + getBottom();
	// add content preferred size
	int[][] grid = getGrid();
	if (grid != null)
	{ // has components
		result.width += getSum(grid[0], 0, grid[0].length, gap, false);
		result.height += getSum(grid[1], 0, grid[1].length, gap, false);
	}
	
	if (getWidth() > 0) result.width = getWidth();
	if (getHeight() > 0) result.height = getHeight();
	return result;
}

@Override
public void doLayout()
{
	int[][] grid = getGrid();
	int contentwidth = 0;
	int contentheight = 0;
	if (grid != null)
	{ // has subcomponents
		// sums the preferred size of cell widths and heights, gaps
		contentwidth = left + getSum(grid[0], 0, grid[0].length, gap, false) + right;
		contentheight = top + getSum(grid[1], 0, grid[1].length, gap, false) + bottom;
	}
	
	titleheight = desktop.getSize(this, 0, 0).height; // title text and icon
//	boolean scrollable = isScrollable();
//	boolean border = ("panel" == getClassName()) && hasBorder();
	int iborder = (border ? 1 : 0);
	if (scrollable)
	{ // set scrollpane areas
		if (!(this instanceof DialogWidget))
		{
			int head = titleheight / 2;
			int headgap = (titleheight > 0) ? (titleheight - head - iborder) : 0;
			scrollable = layoutScroll(contentwidth, contentheight,
				head, 0, 0, 0, border, headgap);
		}
		else
		{ // dialog
			scrollable = layoutScroll(contentwidth, contentheight,
				3 + titleheight, 3, 3, 3, true, 0);
		}
	}
	if (!scrollable)
	{ // clear scrollpane bounds //+
		setView(null);
		setPort(null);
	}
	
	if (grid != null)
	{
		int areax = 0; int areay = 0; int areawidth = 0; int areaheight = 0;
		if (scrollable)
		{
			// components are relative to the viewport
			Rectangle view = getView();
			areawidth = view.width; areaheight = view.height;
		}
		else
		{ // scrollpane isn't required
			// components are relative to top/left corner
			Rectangle bounds = getBounds();
			areawidth = bounds.width;
			areaheight = bounds.height;
			if (!(this instanceof DialogWidget))
			{
				areax = iborder; areay = Math.max(iborder, titleheight);
				areawidth -= 2 * iborder; areaheight -= areay + iborder;
			}
			else
			{ // dialog
				areax = 4; areay = 4 + titleheight;
				areawidth -= 8; areaheight -= areay + 4;
			}
		}
	
		for (int i = 0; i < 2; i++)
		{ // i=0: horizontal, i=1: vertical
			// remaining space
			int d = ((i == 0) ? (areawidth - contentwidth) : (areaheight - contentheight));
			if (d != 0)
			{ //+ > 0
				int w = getSum(grid[2 + i], 0, grid[2 + i].length, 0, false);
				if (w > 0)
				{
					for (int j = 0; j < grid[i].length; j++)
					{
						if (grid[2 + i][j] != 0)
						{
							grid[i][j] += d * grid[2 + i][j] / w;
						}
					}
				}
			}
		}
		
		int i = 0;
		int index = 0;
		for (final Widget comp : this)
		{
			TableConstraint constraint = (TableConstraint) getConstraint(index);
			index++;
			if (!comp.isVisible()) continue;
			
			int ix = areax + left + getSum(grid[0], 0, grid[4][i], gap, true);
			int iy = areay + top + getSum(grid[1], 0, grid[5][i], gap, true);
			int iwidth = getSum(grid[0], grid[4][i], grid[6][i], gap, false);
			int iheight = getSum(grid[1], grid[5][i], grid[7][i], gap, false);
			HAlignment halign = constraint.getHalign();
			VAlignment valign = constraint.getValign();
			if ((halign != HAlignment.FILL) || (valign != VAlignment.FILL))
			{
				Dimension d = comp.getPreferredSize();
				if (halign != HAlignment.FILL)
				{
					int dw = Math.max(0, iwidth - d.width);
					if (halign == HAlignment.CENTER) { ix += dw / 2; }
					else if (halign == HAlignment.RIGHT) { ix += dw; }
					iwidth -= dw;
				}
				if (valign != VAlignment.FILL)
				{
					int dh = Math.max(0, iheight - d.height);
					if (valign == VAlignment.CENTER) { iy += dh / 2; }
					else if (valign == VAlignment.BOTTOM) { iy += dh; }
					iheight -= dh;
				}
			}
			comp.setBounds(ix, iy, iwidth, iheight);
			i++;
		}
	}
	
	needsLayout = false;
}

@Override
public void paint(LwjglRenderer renderer)
{
	if (needsLayout()) doLayout();
	Rectangle bounds = getBounds();
	final boolean enabled = isEnabled() && renderer.isEnabled();
//	int titleheight = getTitleHeight();
//	boolean border = hasBorder();
	renderer.paintBorderAndBackground(this, 0, titleheight / 2, bounds.width, bounds.height - (titleheight / 2),
		border, border, border, border, enabled ? 'e' : 'd');
	renderer.paintIconAndText(this, 0, 0, bounds.width, titleheight, // panel title
		false, false, false, false, 0, 3, 0, 3, false, enabled ? 'x' : 'd', false);
	
	if (getPort() != null)
		paintScroll(renderer, false);
	else
		paintAll(renderer);
}

@Override
public void paintScrollableContent(LwjglRenderer renderer)
{ paintAll(renderer); }

}
