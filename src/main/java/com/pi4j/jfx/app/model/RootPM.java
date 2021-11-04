package com.pi4j.jfx.app.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RootPM {
    private final StringProperty systemInfo = new SimpleStringProperty("JavaFX " + System.getProperty("javafx.version") + ", running on Java " + System.getProperty("java.version") + ".");
    private final IntegerProperty counter   = new SimpleIntegerProperty(73);
    private final BooleanProperty ledGlows  = new SimpleBooleanProperty(false);

    public void increaseCounter(){
        setCounter(getCounter() + 1);
    }

    public void decreaseCounter(){
        setCounter(getCounter() - 1);
    }

    //all necessary getter- and setter-methods (typically generated via Code -> Generate... -> Getter and Setter)
    private int getCounter() {
        return counter.get();
    }

    public IntegerProperty counterProperty() {
        return counter;
    }

    private void setCounter(int counter) {
        this.counter.set(counter);
    }

    public String getSystemInfo() {
        return systemInfo.get();
    }

    public StringProperty systemInfoProperty() {
        return systemInfo;
    }

    public void setSystemInfo(String systemInfo) {
        this.systemInfo.set(systemInfo);
    }

    public boolean isLedGlows() {
        return ledGlows.get();
    }

    public BooleanProperty ledGlowsProperty() {
        return ledGlows;
    }

    public void setLedGlows(boolean ledGlows) {
        this.ledGlows.set(ledGlows);
    }
}
