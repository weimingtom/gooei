package de.ofahrt.utils.fonts.ttf;

class BezierSpline
{

int x0, y0, x1, y1, x2, y2;

int a, b, c, d, e, f;

public void update()
{
	a = x0-2*x1+x2;
	b = x1-x0;
	c = x0;
	
	d = y0-2*y1+y2;
	e = y1-y0;
	f = y0;
}

public int getMinY()
{
	if (y0 < y1) return y0 < y2 ? y0 : y2;
	else         return y1 < y2 ? y1 : y2;
}

public int getMaxY()
{
	if (y0 > y1) return y0 > y2 ? y0 : y2;
	else         return y1 > y2 ? y1 : y2;
}

}
