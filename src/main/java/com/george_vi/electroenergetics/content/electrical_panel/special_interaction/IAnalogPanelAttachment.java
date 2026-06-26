package com.george_vi.electroenergetics.content.electrical_panel.special_interaction;

public interface IAnalogPanelAttachment {
    int getAnalogState();
    void setAnalogState(int state);

    default int getAnalogBound() {
        return 15;
    }

    default int getAnalogMin() {
        return 0;
    }
}
