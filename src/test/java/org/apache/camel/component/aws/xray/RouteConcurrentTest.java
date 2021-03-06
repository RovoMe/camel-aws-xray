/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.aws.xray;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import java.util.concurrent.TimeUnit;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;

public class RouteConcurrentTest extends CamelAwsXRayTestSupport {

  public RouteConcurrentTest() {
    super(
        TestDataBuilder.createTrace().inRandomOrder()
            .withSegment(TestDataBuilder.createSegment("foo"))
            .withSegment(TestDataBuilder.createSegment("bar"))
    );
  }

  @Test
  public void testRoute() throws Exception {
    NotifyBuilder notify = new NotifyBuilder(context).whenDone(2).create();

    template.sendBody("seda:foo", "Hello World");

    assertThat("Not all exchanges were fully processed",
        notify.matches(10, TimeUnit.SECONDS), is(equalTo(true)));

    verify();
  }

  @Override
  protected RouteBuilder createRouteBuilder() throws Exception {
    return new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from("seda:foo?concurrentConsumers=5").routeId("foo")
            .log("routing at ${routeId}")
            .delay(simple("${random(1000,2000)}"))
            .to("seda:bar");

        from("seda:bar?concurrentConsumers=5").routeId("bar")
            .log("routing at ${routeId}")
            .delay(simple("${random(0,500)}"));
      }
    };
  }
}