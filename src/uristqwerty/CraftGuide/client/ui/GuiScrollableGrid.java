package uristqwerty.CraftGuide.client.ui;

import uristqwerty.CraftGuide.CraftGuide;
import uristqwerty.CraftGuide.client.ui.Rendering.GridRect;
import uristqwerty.gui.components.GuiElement;

public class GuiScrollableGrid extends GuiElement
{
	protected GuiScrollBar scrollBar;
	protected int rowHeight;

	private GridRect display;
	private int rows = 0, columns = 1, cells = 0;
	private int columnWidth = 1;
	private int reducedWidth = 0;

	public int borderSize = 1;
	public boolean flexibleSize = false;

	private int lastMouseX, lastMouseY;
	private float lastScroll;

	public GuiScrollableGrid(int x, int y, int width, int height, GuiScrollBar scrollBar, int rowHeight, int columnWidth)
	{
		super(x, y, width, height);
		this.display = new GridRect(0, 0, width, height, this);
		this.rowHeight = rowHeight;
		this.scrollBar = scrollBar;
		this.columnWidth = columnWidth;

		scrollBar.setPageSize(height / rowHeight);

		recalculateColumns();
	}

	@Override
	public void draw()
	{
		if(lastScroll != scrollBar.getValue())
		{
			lastScroll = scrollBar.getValue();
			mouseMoved(lastMouseX, lastMouseY);
		}

		if(flexibleSize)
		{
			int width = columnWidth * ((bounds.width() + reducedWidth) / columnWidth);

			if(width != bounds.width())
			{
				int xOff = (reducedWidth / 2) &~1;
				reducedWidth = bounds.width() + reducedWidth - width;

				setPosition(bounds.x() - xOff + ((reducedWidth / 2) &~1), bounds.y());
				setSize(width, bounds.height());
			}
		}

		drawBackground();
		render(display);
		drawChildren();
	}

	@Override
	public void drawBackground()
	{
		if(background != null)
		{
			render(background, -borderSize, -borderSize, bounds.width() + borderSize * 2, bounds.height() + borderSize * 2);
		}
	}

	@Override
	public void mouseMoved(int x, int y)
	{
		int scrollY = (int)(scrollBar.getValue() * rowHeight) + y - bounds.y();
		int row = scrollY / rowHeight;

		lastMouseX = x;
		lastMouseY = y;
		lastScroll = scrollBar.getValue();

		mouseMovedRow(row, x - bounds.x(), scrollY % rowHeight, containsPoint(x, y));

		super.mousePressed(x, y);
	}

	@Override
	public void mousePressed(int x, int y)
	{
		int scrollY = (int)(scrollBar.getValue() * rowHeight) + y - bounds.y();
		int row = scrollY / rowHeight;

		rowClicked(row, x - bounds.x(), scrollY % rowHeight, containsPoint(x, y));
		super.mousePressed(x, y);
	}

	@Override
	public void onResize(int oldWidth, int oldHeight)
	{
		scrollBar.setPageSize(bounds.height() / rowHeight);
		display.setSize(bounds.width(), bounds.height());


		if(flexibleSize)
		{
			int width = columnWidth * ((bounds.width() + reducedWidth) / columnWidth);

			if(width != bounds.width())
			{
				int xOff = reducedWidth / 2;
				reducedWidth = bounds.width() + reducedWidth - width;

				setPosition(bounds.x() - xOff + reducedWidth / 2, bounds.y());
				setSize(width, bounds.height());
			}
		}

		recalculateColumns();
	}

	public void recalculateColumns()
	{
		setColumns(Math.max(bounds.width() / columnWidth, 1));
	}

	public void setRows(int rowCount)
	{
		rows = rowCount;

		float end = rows - bounds.height() / (float)rowHeight;

		if(end < 0)
		{
			end = 0;
		}

		scrollBar.setScale(0, end);
	}

	public void setColumns()
	{
		setColumns(bounds.width() / columnWidth);
	}

	public void setColumnWidth(int newWidth)
	{
		columnWidth = newWidth;
		recalculateColumns();
	}

	public void setRowHeight(int newHeight)
	{
		rowHeight = newHeight;
		setRows(rows);
		scrollBar.setPageSize(bounds.height() / rowHeight);
	}

	public void setColumns(int columns)
	{
		this.columns = columns;
		setRows((cells + columns - 1) / columns);
	}

	public void setCells(int cells)
	{
		this.cells = cells;
		setRows((cells + columns - 1) / columns);
	}

	public void renderGridRows(GuiRenderer renderer, int xOffset, int yOffset)
	{
		int scrollY = (int)(scrollBar.getValue() * rowHeight);
		int y = yOffset - (scrollY % rowHeight);
		int row = scrollY / rowHeight;
		int max = yOffset + bounds.height();

		while(y < max && row < rowCount())
		{
			renderGridRow(renderer, xOffset, y, row);
			y += rowHeight;
			row++;
		}
	}

	public void renderGridRow(GuiRenderer renderer, int xOffset, int yOffset, int row)
	{
		for(int i = 0; i < columns; i++)
		{
			int columnX = columnOffset(i);

			renderGridCell(renderer, xOffset + columnX, yOffset, row * columns + i);
		}
	}

	public int columnOffset(int column)
	{
		if(CraftGuide.gridPacking)
		{
			return column * columnWidth;
		}
		else
		{
			return columns < 2? 0 : (int)((bounds.width() - columnWidth) * column / (float)(columns - 1));
		}
	}

	protected int columnAtX(int x)
	{
		if(CraftGuide.gridPacking)
		{
			return Math.min(x / columnWidth, columns - 1);
		}
		else
		{
			return (x * columns) / bounds.width();
		}
	}

	public void rowClicked(int row, int x, int y, boolean inBounds)
	{
		int column = columnAtX(x);
		int columnX = columnOffset(column);

		if(inBounds && x - columnX < columnWidth && row * columns + column < cells)
		{
			cellClicked(row * columns + column, x - columnX, y);
		}
	}

	public void mouseMovedRow(int row, int x, int y, boolean inBounds)
	{
		int column = columnAtX(x);

		if(column >= 0 && row * columns + column < cells)
		{
			int columnX = columnOffset(column);

			if(x >= columnX && x - columnX < columnWidth)
			{
				mouseMovedCell(row * columns + column, x - columnX, y, inBounds);
			}
		}
	}

	public int cellAtCoords(int x, int y)
	{
		int row = ((int)(scrollBar.getValue() * rowHeight) + y) / rowHeight;
		return columnAtX(x) + row * columns;
	}

	public int rowCount()
	{
		return rows;
	}

	public int firstVisibleRow()
	{
		return (int)(scrollBar.getValue());
	}

	public int lastVisibleRow()
	{
		return Math.min((int)(scrollBar.getValue() + (bounds.height() + rowHeight - 1) / (float)rowHeight), rows);
	}

	public void mouseMovedCell(int cell, int x, int y, boolean inBounds)
	{
		/** Default implementation: Do nothing */
	}

	public void cellClicked(int cell, int x, int y)
	{
		/** Default implementation: Do nothing */
	}

	/**
	 * Called by the default implementation of {@link renderGridRow} one
	 * time for each column in the row. Override this in order to render
	 * each cell individually.
	 * <br><br>
	 * (x, y) is the absolute screen position of this cell's top left corner.
	 * @param renderer
	 * @param x
	 * @param y
	 * @param cell [previous cells in this row] + ([number of previous rows]
	 * * [cells per row])
	 */
	public void renderGridCell(GuiRenderer renderer, int x, int y, int cell)
	{
		/** Default implementation: Do nothing */
	}
}
