package org.apache.camel.component.aws.xray.component;

import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

public class TestXRayComponent extends DefaultComponent {

  public TestXRayComponent() {
    super();
  }

  public TestXRayComponent(final CamelContext context) {
    super(context);
  }

  @Override
  protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) {
    return new TestXRayEndpoint(uri, remaining, this);
  }
}
