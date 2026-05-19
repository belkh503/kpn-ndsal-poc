package com.kpn.ndsal.resourcemanager.application.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntity;

import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ContextService {

    public void setContext(RequestQueueEntity requestQueueEntity) {
        TextMapGetter<Map<String, String>> getter = new TextMapGetter<>() {
            @Override
            public Iterable<String> keys(Map<String, String> carrier) {
                return carrier.keySet();
            }

            @Override
            public String get(Map<String, String> carrier, String key) {
                return carrier.get(key);
            }
        };
        Context context = W3CTraceContextPropagator.getInstance()
            .extract(Context.current(), requestQueueEntity.getContext(), getter);
        context.makeCurrent();
    }

}
