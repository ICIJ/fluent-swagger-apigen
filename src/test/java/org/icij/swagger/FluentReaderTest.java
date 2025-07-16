package org.icij.swagger;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class FluentReaderTest {
    @Test
    public void test_regexp_curly_brace() throws Exception {
        FluentReader.RouteCollection routeCollection = new FluentReader.RouteCollection();
        Method method = DummyResource.class.getMethod("getFoo");

        routeCollection.addResource("GET", method,"/api/:project/:id?p1=:p1&p2=:p2");

        assertThat(routeCollection.routes.size()).isEqualTo(1);
        assertThat(routeCollection.routes.iterator().next().uriPattern()).isEqualTo("/api/{project}/{id}?p1={p1}&p2={p2}");
    }

    static class DummyResource {
        public String getFoo() {
            return "foo";
        }
    }
}
