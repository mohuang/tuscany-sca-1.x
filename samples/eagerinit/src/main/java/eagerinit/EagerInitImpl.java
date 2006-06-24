/**
 *
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package eagerinit;

// TODO import org.apache.tuscany.core.system.annotation.Monitor;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Service;

/**
 * This class implements the Eager Init service.
 */
@Service(EagerInitService.class)
public class EagerInitImpl implements EagerInitService {

    {
        System.out.println("Hello World, from a constructor");
    }

    @Init(eager = true)
    public void init() throws Exception {
        System.out.println("Hello World, init");
    }

    public String getGreetings(String name) {
        return "Hello " + name;
    }

}
