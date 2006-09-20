package thinlet;

import java.awt.Rectangle;

import thinlet.api.ModalWidget;
import thinlet.api.ScrollableWidget;
import thinlet.help.MouseInteraction;
import thinlet.lwjgl.LwjglWidgetRenderer;
import de.ofahrt.utils.input.InputEventType;
import de.ofahrt.utils.input.MouseEvent;

public class ComboListWidget extends AbstractWidget implements ModalWidget, ScrollableWidget
{

private boolean modal = false;
private ComboBoxItem leadWidget = null;
private ComboBoxWidget comboboxWidget = null;

ComboListWidget(ThinletDesktop desktop)
{ super(desktop); }

public boolean isModal()
{ return modal; }

public void setModal(boolean modal)
{ this.modal = modal; }

public ComboBoxItem getLeadWidget()
{ return leadWidget; }

public void setLeadWidget(ComboBoxItem leadWidget)
{ this.leadWidget = leadWidget; }

public ComboBoxWidget getComboBoxWidget()
{ return comboboxWidget; }

public void setComboBoxWidget(ComboBoxWidget comboboxWidget)
{ this.comboboxWidget = comboboxWidget; }

/**
 * @param part the current hotspot item
 * @param scroll scroll to the part if true
 */
void setInside(ComboBoxItem part, boolean scroll)
{
	ComboBoxItem previous = getLeadWidget();
	if (previous != null)
		repaintScrollablePart(previous);
	setLeadWidget(part);
	if (part != null)
	{
		repaintScrollablePart(part);
		if (scroll)
		{
			Rectangle r = part.getBounds();
			scrollToVisible(r.x, r.y, 0, r.height);
		}
	}
}

@Override
public void findSubComponent(MouseInteraction mouseInteraction, int x, int y)
{
	if (!findScroll(mouseInteraction, x, y))
	{
		y += getView().y;
		for (final ComboBoxItem choice : getComboBoxWidget())
		{
			Rectangle r = choice.getBounds();
			if ((y >= r.y) && (y < r.y + r.height))
			{
				mouseInteraction.insidepart = choice;
				break;
			}
		}
	}
}

@Override
public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	InputEventType id = event.getType();
	if (!processScroll(mouseInteraction, event, part))
	{
		if ((id == InputEventType.MOUSE_ENTERED) || (id == InputEventType.DRAG_ENTERED))
		{
			if (part != null)
			{ //+ scroll if dragged
				setInside((ComboBoxItem) part, false);
			}
		}
		else if (id == InputEventType.MOUSE_RELEASED)
			getComboBoxWidget().closeCombo(this, (ComboBoxItem) part);
	}
}

@Override
public void paint(LwjglWidgetRenderer renderer, boolean enabled)
{ paintScroll(renderer, false, enabled); }

public void paintScrollableContent(LwjglWidgetRenderer renderer, boolean enabled)
{
	// clip is used for rendering acceleration
	final int clipy = renderer.getClipY();
	final int clipheight = renderer.getClipHeight();
	
	int portwidth = getPort().width;
	ComboBoxItem lead = getLeadWidget();
	for (final ComboBoxItem choice : getComboBoxWidget())
	{
		Rectangle r = choice.getBounds();
		if (clipy + clipheight <= r.y) break;
		if (clipy >= r.y + r.height) continue;
		renderer.paintIconAndText(choice, r.x, r.y, portwidth, r.height,
			false, false, false, false, 2, 4, 2, 4, false,
			choice.isEnabled() ? ((lead == choice) ? 's' : 't') : 'd', false);
	}
}

}
