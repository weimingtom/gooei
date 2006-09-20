package thinlet.awt;

import java.awt.image.BufferedImage;

import thinlet.help.Icon;

final class AWTIcon implements Icon
{

private final BufferedImage image;

public AWTIcon(BufferedImage image)
{
	if (image == null) throw new NullPointerException();
	this.image = image;
}

public BufferedImage getImage()
{ return image; }

public int getHeight()
{ return image.getHeight(); }

public int getWidth()
{ return image.getWidth(); } 

}
