package org.apache.camel.component.aws.xray.bean;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.camel.Handler;
import org.apache.camel.component.aws.xray.XRayTrace;

@XRayTrace
public class ProcessingCamelBean {

  private static AtomicInteger INVOKED = new AtomicInteger(0);

  @Handler
  public void performTask() {

    INVOKED.incrementAndGet();

    try {
      // sleep 5 seconds
      Thread.sleep(3000);
    } catch (InterruptedException iEx) {
      // do nothing
    }
  }

  public static int gotInvoked() {
    return INVOKED.get();
  }
}
