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
package org.thingml.xtext.helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.thingml.xtext.constraints.ThingMLHelpers;
import org.thingml.xtext.thingML.CompositeState;
import org.thingml.xtext.thingML.Property;
import org.thingml.xtext.thingML.Region;
import org.thingml.xtext.thingML.Session;
import org.thingml.xtext.thingML.State;
import org.thingml.xtext.thingML.StateContainer;
import org.thingml.xtext.thingML.Type;

/**
 * Created by ffl on 10.05.2016.
 */
public class CompositeStateHelper {

    public static List<State> allContainedStates(CompositeState self) {
        final List<State> result = new ArrayList<State>();
        for(StateContainer r : allContainedRegions(self)) {
            if (r instanceof State && !(r instanceof Session)) {
            	boolean found = false;
            	for(State s : result) {
            		if(EcoreUtil.equals(r, s)) {
            			found = true;
            			break;
            		}
            	}
            	if(!found)
            		result.add((State)r);
            }
            for(State s : r.getSubstate()) {
                if (! (s instanceof Region)) {
                	boolean found = false;
                	for(State rs : result) {
                		if(EcoreUtil.equals(s, rs)) {
                			found = true;
                			break;
                		}
                	}
                	if(!found)
                		result.add(s);
                }
            }
        }
        return result;
    }

    public static List<State> allContainedStatesIncludingSessions(CompositeState self) {
        final List<State> result = new ArrayList<State>();
        for(StateContainer r : allContainedRegionsAndSessions(self)) {
            if (r instanceof State) {
                result.add((State)r);
            }
            for(State s : r.getSubstate()) {
                if (! (s instanceof Region)) {
                    result.add(s);
                }
            }
        }
        return result;
    }


    public static List<StateContainer> allContainedRegions(CompositeState self) {
    	List<StateContainer> result = new ArrayList<StateContainer>();
        result.add(self);
        result.addAll(ThingMLHelpers.<StateContainer>allContainedElementsOfType(self, CompositeState.class));
        result.addAll(ThingMLHelpers.<StateContainer>allContainedElementsOfType(self, Region.class));
        return result;
    }


    public static List<StateContainer> allContainedRegionsAndSessions(CompositeState self) {
        List<StateContainer> result = new ArrayList<StateContainer>();
        result.add(self);
        result.addAll(ThingMLHelpers.<StateContainer>allContainedElementsOfType(self, StateContainer.class));
        return result;
    }


    public static List<Session> allContainedSessions(CompositeState self) {
    	List<Session> result = new ArrayList<Session>();
        result.addAll(ThingMLHelpers.<Session>allContainedElementsOfType(self, Session.class));
        return result;
    }

    public static List<Property> allContainedProperties(CompositeState self) {
        List<Property> result = new ArrayList<Property>();
        for(State s : allContainedStates(self)) {
            result.addAll(s.getProperties());
        }
        return result;
    }

    public static List<CompositeState> allContainedCompositeStates(CompositeState self) {
        List<CompositeState> result = new ArrayList<CompositeState>();
        for(State s : allContainedStates(self)) {
            if (s instanceof CompositeState && !(s instanceof Session)) {
                result.add((CompositeState)s);
            }
        }
        return result;
    }


    public static List<State> allContainedSimpleStates(CompositeState self) {
        final List<State> result = allContainedStates(self);
        result.removeAll(allContainedCompositeStates(self));
        return result;
    }


    public static Set<CompositeState> allContainedCompositeStatesIncludingSessions(CompositeState self) {
    	Set<CompositeState> result = new HashSet<CompositeState>();
        for(State s : allContainedStatesIncludingSessions(self)) {
            if (s instanceof CompositeState) {
                result.add((CompositeState)s);
            }
        }
        return result;
    }


    public static List<State> allContainedSimpleStatesIncludingSessions(CompositeState self) {
        final List<State> result = allContainedStatesIncludingSessions(self);
        result.removeAll(allContainedCompositeStatesIncludingSessions(self));
        return result;
    }


    public static Set<Type> allUsedTypes(CompositeState self) {
        Set<Type> result = new HashSet<Type>();
        for(Property p : allContainedProperties(self)) {
            result.add(p.getTypeRef().getType());
        }
        return result;
    }

    public static boolean hasSeveralRegions(CompositeState self) {
        return self.getRegion().size() > 0;
    }

}
