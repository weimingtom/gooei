package gooei;

import java.awt.Rectangle;

public interface Desktop
{

SimpleClipboard getSystemClipboard();
void requestFocus();
void transferFocus();
void transferFocusBackward();
void repaint(Widget widget, Rectangle area);

}
