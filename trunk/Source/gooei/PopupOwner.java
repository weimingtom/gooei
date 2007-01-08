package gooei;

import gooei.input.KeyboardEvent;

public interface PopupOwner
{

void closePopup();
boolean handleKeyPress(KeyboardEvent event);

}
