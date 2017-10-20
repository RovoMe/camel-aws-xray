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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.component.aws.xray.TestDataBuilder.TestSegment;
import org.apache.camel.component.aws.xray.TestDataBuilder.TestSubsegment;
import org.apache.camel.component.aws.xray.TestDataBuilder.TestTrace;
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Rule;


public class CamelAwsXRayTestSupport extends CamelTestSupport {

  private List<TestTrace> testData;

  @Rule
  public MockAWSDaemon socketListener = new MockAWSDaemon();

  public CamelAwsXRayTestSupport(TestTrace... testData) {
    this.testData = Arrays.asList(testData);
  }

  @Override
  protected void doPostSetup() throws Exception {

  }

  @Override
  protected CamelContext createCamelContext() throws Exception {
    CamelContext context = super.createCamelContext();

    context.setTracing(true);
    final Tracer tracer = new Tracer();
    tracer.getDefaultTraceFormatter().setShowBody(false);
    tracer.setLogLevel(LoggingLevel.INFO);
    context.getInterceptStrategies().add(tracer);

    XRayTracer xRayTracer = new XRayTracer();
    xRayTracer.setCamelContext(context);
    xRayTracer.setExcludePatterns(getExcludePatterns());

    xRayTracer.init(context);

    return context;
  }

  protected Set<String> getExcludePatterns() {
    return new HashSet<>();
  }

  protected void verify() {
    Map<String, TestTrace> receivedData = socketListener.getReceivedData();
    assertThat("Incorrect number of traces",
        receivedData.size(), is(equalTo(testData.size())));
    int i = 0;
    for (String key : receivedData.keySet()) {
      TestTrace trace = receivedData.get(key);
      verifyTraces(testData.get(i++), trace);
    }
  }

  private void verifyTraces(TestTrace expected, TestTrace actual) {
    assertThat("Incorrect number of segment for trace",
        actual.getSegments().size(), is(equalTo(expected.getSegments().size())));
    List<TestSegment> expectedSegments = new ArrayList<>(expected.getSegments());
    List<TestSegment> actualSegments = new ArrayList<>(actual.getSegments());
    for (int i = 0; i < expected.getSegments().size(); i++) {
      verifySegments(expectedSegments.get(i), actualSegments.get(i));
    }
  }

  private void verifySegments(TestSegment expected, TestSegment actual) {
    assertThat("Incorrect name of segment",
        actual.getName(), is(equalTo(expected.getName())));

    if (!expected.getSubsegments().isEmpty()) {
      for (int i = 0; i < expected.getSubsegments().size(); i++) {
        verifySubsegments(expected.getSubsegments().get(i), actual.getSubsegments().get(i));
      }
    }
    if (!expected.getAnnotations().isEmpty()) {
      verifyAnnotations(expected.getAnnotations(), actual.getAnnotations());
    }
    if (!expected.getMetadata().isEmpty()) {
      verifyMetadata(expected.getMetadata(), actual.getMetadata());
    }
  }

  private void verifySubsegments(TestSubsegment expected, TestSubsegment actual) {
    assertThat("Incorrect name of subsegment",
        actual.getName(), is(equalTo(expected.getName())));

    if (!expected.getSubsegments().isEmpty()) {
      for (int i = 0; i < expected.getSubsegments().size(); i++) {
        verifySubsegments(expected.getSubsegments().get(i), actual.getSubsegments().get(i));
      }
    }
    if (!expected.getAnnotations().isEmpty()) {
      verifyAnnotations(expected.getAnnotations(), actual.getAnnotations());
    }
    if (!expected.getMetadata().isEmpty()) {
      verifyMetadata(expected.getMetadata(), actual.getMetadata());
    }
  }

  private void verifyAnnotations(Map<String, Object> expected, Map<String, Object> actual) {
    assertThat(actual.size(), is(equalTo(expected.size())));
    for (String key : expected.keySet()) {
      assertTrue("Annotation " + key + " is missing", actual.containsKey(key));
      assertThat("Annotation value of " + key + " is different",
          actual.get(key), is(equalTo(expected.get(key))));
    }
  }

  private void verifyMetadata(Map<String, Map<String, Object>> expected,
                              Map<String, Map<String, Object>> actual) {

    assertThat("Insufficient number of metadata found",
        actual.size(), is(greaterThanOrEqualTo(expected.size())));
    for (String namespace : expected.keySet()) {
      assertTrue("Namespace " + namespace + " not found in metadata",
          actual.containsKey(namespace));
      for (String key : expected.get(namespace).keySet()) {
        assertTrue("Key " + key + " of namespace + " + namespace + " not found",
            actual.get(namespace).containsKey(key));
        assertThat("Incorrect value of key " + key + " in namespace " + namespace,
            actual.get(namespace).get(key), is(equalTo(expected.get(namespace).get(key))));
      }
    }
  }
}