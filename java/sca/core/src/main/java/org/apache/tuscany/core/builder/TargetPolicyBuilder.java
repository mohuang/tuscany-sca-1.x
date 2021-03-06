/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.tuscany.core.builder;

import org.apache.tuscany.model.assembly.ConfiguredService;
import org.apache.tuscany.core.wire.WireTargetConfiguration;

/**
 * Implementations contribute {@link org.apache.tuscany.core.wire.Interceptor}s or {@link
 * org.apache.tuscany.core.wire.MessageHandler}s that handle target-side policy on a wire.  
 *
 * @version $$Rev$$ $$Date$$
 */
public interface TargetPolicyBuilder extends PolicyBuilder{

    public void build(ConfiguredService service, WireTargetConfiguration configuration) throws BuilderException;

}
