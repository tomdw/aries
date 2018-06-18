/*
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

package org.apache.aries.component.dsl.internal;

/**
 * @author Carlos Sierra Andrés
 */
public class EffectsOSGi extends OSGiImpl<Void> {

    public EffectsOSGi(Runnable onAdding, Runnable onRemoving) {
        super((bundleContext, op) -> {
            onAdding.run();

            try {
                Runnable terminator = op.publish(null);

                return new OSGiResultImpl(
                    () -> {
                        try {
                            terminator.run();
                        }
                        finally {
                            try {
                                onRemoving.run();
                            }
                            catch (Exception e) {
                                //TODO: logging
                            }
                        }

                 });
            }
            catch (Exception e) {
                onRemoving.run();

                throw e;
            }
        });
    }
}
