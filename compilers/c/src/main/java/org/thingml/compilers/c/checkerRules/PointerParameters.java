/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingml.compilers.c.checkerRules;

import java.util.LinkedList;
import java.util.List;

import org.thingml.xtext.constraints.ThingMLHelpers;
import org.thingml.xtext.helpers.AnnotatedElementHelper;
import org.thingml.xtext.helpers.ConfigurationHelper;
import org.thingml.xtext.thingML.Configuration;
import org.thingml.xtext.thingML.Message;
import org.thingml.xtext.thingML.Parameter;
import org.thingml.xtext.thingML.Port;
import org.thingml.xtext.thingML.Thing;
import org.thingml.xtext.validation.Checker;
import org.thingml.xtext.validation.Rule;

/**
 *
 * @author sintef
 */
public class PointerParameters extends Rule {

	@Override
    public Checker.InfoType getHighestLevel() {
        return Checker.InfoType.ERROR;
    }

    @Override
    public String getName() {
        return "Pointer Parameters";
    }

    @Override
    public String getDescription() {
        return "Check that each no pointer type is used as a parameter of a message sent asynchrounously ";
    }

    @Override
    public void check(Configuration cfg, Checker checker) {
        for (Thing t : ConfigurationHelper.allThings(cfg)) {
            for (Port p : ThingMLHelpers.allPorts(t)) {
                if (!AnnotatedElementHelper.isDefined(p, "sync_send", "true")) {
                    List<Message> messages = new LinkedList<Message>();
                    messages.addAll(p.getReceives());
                    messages.addAll(p.getSends());
                    for (Message m : messages) {
                        for (Parameter pt : m.getParameters()) {
                            if (AnnotatedElementHelper.isDefined(pt.getTypeRef().getType(), "c_byte_size", "*")) {
                                checker.addError("C", "Message including pointer parameters sent/received asynchronously.", m);
                            }
                            if (pt.getTypeRef().isIsArray()) {
                                checker.addError("C", "Message including array parameters sent/received asynchronously.", m);
                            }
                        }
                    }
                }
            }
        }
    }

}
