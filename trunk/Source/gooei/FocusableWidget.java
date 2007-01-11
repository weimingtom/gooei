package gooei;

import gooei.input.KeyboardEvent;

/**
 * This interface allows a widget to receive keyboard events.
 */
public interface FocusableWidget extends Widget
{

void handleFocusLost();
void handleFocusGained();

/**
 * Handle Keyboard event.
 */
boolean handleKeyPress(KeyboardEvent event);

}
