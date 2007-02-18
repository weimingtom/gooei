package de.ofahrt.gooei.lwjgl;

public class StateStack
{

	private static class State
	{
		int x0, y0, x1, y1, tx, ty;
		boolean enabled;
	}

private int index = 0;
private State[] data = new State[100];

public StateStack()
{
	for (int i = 0; i < data.length; i++)
		data[i] = new State();
}

public int getX0()
{ return data[index].x0; }

public int getY0()
{ return data[index].y0; }

public int getX1()
{ return data[index].x1; }

public int getY1()
{ return data[index].y1; }

public int getTX()
{ return data[index].tx; }

public int getTY()
{ return data[index].ty; }

public boolean getEnabled()
{ return data[index].enabled; }

public void init(int x0, int y0, int x1, int y1)
{
	index = 0;
	data[index].x0 = x0;
	data[index].y0 = y0;
	data[index].x1 = x1;
	data[index].y1 = y1;
	data[index].tx = 0;
	data[index].ty = 0;
	data[index].enabled = true;
}

public void clip(int x0, int y0, int x1, int y1)
{
	if (x0 > data[index].x0) data[index].x0 = x0;
	if (y0 > data[index].y0) data[index].y0 = y0;
	if (x1 < data[index].x1) data[index].x1 = x1;
	if (y1 < data[index].y1) data[index].y1 = y1;
}

public void push()
{
	data[index+1].x0 = data[index].x0;
	data[index+1].y0 = data[index].y0;
	data[index+1].x1 = data[index].x1;
	data[index+1].y1 = data[index].y1;
	data[index+1].tx = data[index].tx;
	data[index+1].ty = data[index].ty;
	data[index+1].enabled = data[index].enabled;
	index++;
}

public void pop()
{ index--; }

public void translate(int dx, int dy)
{
	data[index].x0 -= dx;
	data[index].y0 -= dy;
	data[index].x1 -= dx;
	data[index].y1 -= dy;
	data[index].tx += dx;
	data[index].ty += dy;
}

public void updateEnabled(boolean enabled)
{ data[index].enabled &= enabled; }

}
