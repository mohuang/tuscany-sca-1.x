/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tuscany.tomcat.integration;

import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.startup.ContextConfig;
import org.apache.tuscany.tomcat.TuscanyHost;
import org.apache.tuscany.tomcat.TuscanyValve;

import java.io.File;

/**
 * @version $Rev$ $Date$
 */
public class TomcatIntegrationTestCase extends AbstractTomcatTest {
    protected File app1;

    public void testRuntimeIntegration() throws Exception {
        StandardContext ctx = new StandardContext();

        // caution: this sets the parent of the webapp loader to the test classloader so it can find TestServlet
        // anything that relies on the TCCL may not work correctly
        ClassLoader cl = TestServlet.class.getClassLoader();
        ctx.setParentClassLoader(cl);

        ctx.addLifecycleListener(new ContextConfig());
        ctx.setName("testContext");
        ctx.setDocBase(app1.getAbsolutePath());
        StandardWrapper wrapper = new StandardWrapper();
        wrapper.setServletClass(TestServlet.class.getName());
        ctx.addChild(wrapper);
        host.addChild(ctx);
        boolean found = false;
        for (Valve valve: ctx.getPipeline().getValves()) {
            if (valve instanceof TuscanyValve) {
                found = true;
                break;
            }
        }
        assertTrue("TuscanyValve not in pipeline", found);

        request.setContext(ctx);
        request.setWrapper(wrapper);
        host.invoke(request, response);

        host.removeChild(ctx);
    }

    protected void setUp() throws Exception {
        super.setUp();
        app1 = new File(getClass().getResource("/app1").toURI());
        File baseDir = new File(app1, "../../tomcat").getCanonicalFile();
        setupTomcat(baseDir, new TuscanyHost());
        engine.start();
    }

    protected void tearDown() throws Exception {
        engine.stop();
        super.tearDown();
    }

}
