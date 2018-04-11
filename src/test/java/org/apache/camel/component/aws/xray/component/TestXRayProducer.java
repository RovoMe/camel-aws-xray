package org.apache.camel.component.aws.xray.component;

import org.apache.camel.Exchange;
import org.apache.camel.component.aws.xray.XRayTracer;
import org.apache.camel.component.aws.xray.bean.SomeBackingService;
import org.apache.camel.impl.DefaultProducer;

public class TestXRayProducer extends DefaultProducer {

  private final SomeBackingService backingService;
  private final String state;

  public TestXRayProducer(final TestXRayEndpoint endpoint, String state) {
    super(endpoint);

    this.state = state;
    backingService = new SomeBackingService(endpoint.getCamelContext());
  }

  @Override
  public void process(Exchange exchange) {

    byte[] body = exchange.getIn().getBody(byte[].class);

    if (trim(CommonEndpoints.RECEIVED).equals(this.state)
        || trim(CommonEndpoints.READY).equals(this.state)) {

      String traceId = exchange.getIn().getHeader(XRayTracer.XRAY_TRACE_ID, String.class);
      String key = backingService.performMethod(body, state, traceId);
      System.out.println(key);
    }
  }

  private static String trim(String endpoint) {
    return endpoint.substring(endpoint.indexOf(":") + 1);
  }
}
