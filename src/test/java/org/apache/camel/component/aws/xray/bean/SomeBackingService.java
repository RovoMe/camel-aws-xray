package org.apache.camel.component.aws.xray.bean;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Entity;
import java.util.UUID;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.aws.xray.XRayTracer;

public class SomeBackingService {

  private final Endpoint targetEndpoint;
  private final ProducerTemplate template;

  public SomeBackingService(CamelContext context) {
    targetEndpoint = context.getEndpoint("seda:backingTask");
    template = context.createProducerTemplate();
  }

  public String performMethod(byte[] body, String state, String traceId) {

    String key = UUID.randomUUID().toString();

    Entity traceEntity = AWSXRay.getGlobalRecorder().getTraceEntity();
    traceEntity.putMetadata("state", state);

    Exchange newExchange = targetEndpoint.createExchange(ExchangePattern.InOnly);
    newExchange.getIn().setBody(body);
    newExchange.getIn().setHeader("KEY", key);
    newExchange.getIn().setHeader(XRayTracer.XRAY_TRACE_ID, traceId);
    newExchange.getIn().setHeader(XRayTracer.XRAY_TRACE_ENTITY, traceEntity);
    template.asyncSend(targetEndpoint, newExchange);

    return key;
  }
}
