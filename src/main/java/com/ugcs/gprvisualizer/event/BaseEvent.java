package com.ugcs.gprvisualizer.event;

import org.springframework.context.ApplicationEvent;

public abstract class BaseEvent extends ApplicationEvent {
    
    protected BaseEvent(Object source) {
        super(source);
    }
}
