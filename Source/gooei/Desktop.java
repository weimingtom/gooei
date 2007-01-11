package gooei;

import gooei.font.Font;
import gooei.font.FontMetrics;
import gooei.utils.Icon;
import gooei.utils.PreparedIcon;
import gooei.utils.TimerEventType;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;

public interface Desktop
{

Rectangle getBounds();

SimpleClipboard getSystemClipboard();
void repaint(Widget widget, Rectangle area);
void repaint(Widget child);

void addChild(Widget widget, int index);
void removeChild(Widget widget);
void moveToFront(Widget widget);

/** Posts a synthetic mouse move event. Call this if the layout has changed. */
void checkLocation();

void insertItem(Widget widget, int i);
void setPopupOwner(PopupOwner owner);
void setPopup(Widget widget, PopupOwner owner);
void closePopup();

MouseInteraction getMouseInteraction();

// Focus Management
boolean hasFocus(Widget widget);
boolean setFocus(Widget widget);
boolean setNextFocusable(Widget widget);
boolean setPreviousFocusable(Widget widget);
/** If the focus owner is a child of the removed widget, the focus needs to be updated. */
void updateFocusForRemove(Widget child);

// Timer
void setTimer(TimerEventType type, long time);

// getPreferredSize calculations
Font getDefaultFont();
FontMetrics getFontMetrics(Font font);
int getBlockSize();
Dimension getSize(IconAndText item, int dx, int dy);

// Cursor
void setCursor(Cursor cursor);

// Prepared Items
PreparedIcon prepareIcon(Icon icon);

}
