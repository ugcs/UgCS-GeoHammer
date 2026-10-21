package com.ugcs.geohammer.analytics;

import com.ugcs.geohammer.model.event.FilterAppliedEvent;
import com.ugcs.geohammer.util.Templates;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class FilterEventsAnalytics {

    private final EventSender eventSender;

    private final EventsFactory eventsFactory;

    public FilterEventsAnalytics(EventSender eventSender, EventsFactory eventsFactory) {
        this.eventSender = eventSender;
        this.eventsFactory = eventsFactory;
    }

    @EventListener
    private void onFilterApplied(FilterAppliedEvent event) {
        String template = Templates.getTemplateName(event.getFile());
        eventSender.send(eventsFactory.createFilterAppliedEvent(event.getFilterType(), template));
    }
}
