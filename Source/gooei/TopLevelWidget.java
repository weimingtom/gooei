package gooei;

import java.awt.Rectangle;

public interface TopLevelWidget extends Widget
{

	public enum LayoutPolicy
	{
		NORMAL, CENTERED, FIXED;
	}

LayoutPolicy getPolicy();
Rectangle getPreferredLayout();

}
