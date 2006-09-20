package thinlet.api;

import de.ofahrt.utils.input.KeyboardEvent;

public interface FocusableWidget extends Widget
{

boolean invokeFocusLost();
boolean invokeFocusGained();
/** Handle Keyboard input. */
boolean handleKeyPress(KeyboardEvent event);

}
