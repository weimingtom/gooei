package de.ofahrt.gooei.lwjgl;

// FIXME: allow complete state saving
public class ClipStack
{

private int index = 0;
private int[] data = new int[100*6];

public ClipStack()
{/*OK*/}

public int getX0()
{ return data[index+0]; }

public int getY0()
{ return data[index+1]; }

public int getX1()
{ return data[index+2]; }

public int getY1()
{ return data[index+3]; }

public int getTX()
{ return data[index+4]; }

public int getTY()
{ return data[index+5]; }

public void init(int x0, int y0, int x1, int y1)
{
	index = 0;
	data[0] = x0;
	data[1] = y0;
	data[2] = x1;
	data[3] = y1;
	data[4] = 0;
	data[5] = 0;
}

public void clip(int x0, int y0, int x1, int y1)
{
	if (x0 > data[index+0]) data[index+0] = x0;
	if (y0 > data[index+1]) data[index+1] = y0;
	if (x1 < data[index+2]) data[index+2] = x1;
	if (y1 < data[index+3]) data[index+3] = y1;
}

public void push()
{
	data[index+ 6] = data[index+0];
	data[index+ 7] = data[index+1];
	data[index+ 8] = data[index+2];
	data[index+ 9] = data[index+3];
	data[index+10] = data[index+4];
	data[index+11] = data[index+5];
	index += 6;
}

public void pop()
{ index -= 6; }

public void translate(int dx, int dy)
{
	data[index+0] -= dx;
	data[index+1] -= dy;
	data[index+2] -= dx;
	data[index+3] -= dy;
	data[index+4] += dx;
	data[index+5] += dy;
}

}
