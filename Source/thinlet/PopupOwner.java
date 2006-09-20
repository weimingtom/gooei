package thinlet;

import de.ofahrt.utils.input.KeyboardEvent;

public interface PopupOwner
{

void closePopup();
boolean handleKeyPress(KeyboardEvent event);

}
