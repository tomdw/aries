/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.aries.tutorials.blueprint.greeter.server.blueprint;


import org.apache.aries.tutorials.blueprint.greeter.api.GreeterMessageService;


public class GreeterMessageServiceImpl implements GreeterMessageService{
    private String sender="<unset>";
    
    public String getGreetingMessage() {
        return "Hello World! from : "+sender;
    }
    
    public void setSender(String sender){
        this.sender=sender;
        if(this.sender==null)
            this.sender="";
    }

}
