package org.apache.camel.component.aws.xray.component;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;

public class TestXRayEndpoint extends DefaultEndpoint {

  private final String remaining;

  public TestXRayEndpoint(final String uri, final String remaining, final TestXRayComponent component) {
    super(uri, component);

    this.remaining = remaining;
  }

  @Override
  public TestXRayComponent getComponent() {
    return (TestXRayComponent) super.getComponent();
  }

  @Override
  public Producer createProducer() {
    return new TestXRayProducer(this, remaining);
  }

  @Override
  public Consumer createConsumer(Processor processor) {
    throw new UnsupportedOperationException("You cannot create a Consumer for message monitoring");
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
