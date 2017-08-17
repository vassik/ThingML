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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.thingml.xtext.constraints.ThingMLHelpers;
import org.thingml.xtext.thingML.CompositeState;
import org.thingml.xtext.thingML.Event;
import org.thingml.xtext.thingML.Handler;
import org.thingml.xtext.thingML.InternalTransition;
import org.thingml.xtext.thingML.Message;
import org.thingml.xtext.thingML.Port;
import org.thingml.xtext.thingML.Property;
import org.thingml.xtext.thingML.ReceiveMessage;
import org.thingml.xtext.thingML.State;
import org.thingml.xtext.thingML.StateContainer;
import org.thingml.xtext.thingML.Transition;

/**
 * Created by ffl on 10.05.2016.
 */
public class StateHelper {


	public static List<State> allStates(State self) {
		if (self instanceof CompositeState) {
			return CompositeStateHelper.allContainedStates((CompositeState) self);
		} else {
			return Collections.singletonList((State) self);
		}
	}

	public static List<State> allStatesIncludingSessions(State self) {
		if (self instanceof CompositeState) {
			return CompositeStateHelper.allContainedStatesIncludingSessions((CompositeState) self);
		} else {
			return Collections.singletonList((State) self);
		}
	}


	public static List<State> allStatesWithEntry(State self) {
		final List<State> result = new ArrayList<State>();
		for (State s : allStates(self)) {
			if (s.getEntry() != null)
				result.add(s);
		}
		return result;
	}


	public static List<State> allStatesWithExit(State self) {
		final List<State> result = new ArrayList<State>();
		for (State s : allStates(self)) {
			if (s.getExit() != null)
				result.add(s);
		}
		return result;
	}


	public static List<State> allContainingStates(State self) {
		return ThingMLHelpers.allContainingStates(self);
	}


	public static List<Property> allProperties(State self) {
		return ThingMLHelpers.allProperties(self);
	}


	public static List<State> allValidTargetStates(State self) {
		return ThingMLHelpers.allValidTargetStates(self);
	}


	public static List<Handler> allHandlers(State self, Port p, Message m) {
		Map<Port, Map<Message, List<Handler>>> handlers = allMessageHandlers(self);
		if (!handlers.containsKey(p) || !handlers.get(p).containsKey(m))
			return new ArrayList<Handler>();
		else
			return handlers.get(p).get(m);
	}


	public static List<Handler> allHandlersIncludingSessions(State self, Port p, Message m) {
		Map<Port, Map<Message, List<Handler>>> handlers = allMessageHandlersIncludingSessions(self);
		if (!handlers.containsKey(p) || !handlers.get(p).containsKey(m))
			return new ArrayList<Handler>();
		else
			return handlers.get(p).get(m);
	}


	public static Map<Port, Map<Message, List<Handler>>> allMessageHandlersSC(StateContainer self) {
		Map<Port, Map<Message, List<Handler>>> result = new HashMap<Port, Map<Message, List<Handler>>>();
		for(State s1 : self.getSubstate()) {
			for (State s : allStates(s1)) {//FIXME: avoid code duplicatio with method below and rather merge the results provided by method below
				//println("Processisng state " + s.getName)
				List<Handler> handlers = new ArrayList<Handler>();
				for (Transition t : s.getOutgoing()) {
					handlers.add(t);
				}
				for (InternalTransition i : s.getInternal()) {
					handlers.add(i);
				}
				for (Handler t : handlers) {
					//println("  Processisng handler " + t + " Event = " + t.getEvent)
					for (Event e : t.getEvent()) {
						if (e instanceof ReceiveMessage) {
							ReceiveMessage rm = (ReceiveMessage) e;
							Map<Message, List<Handler>> phdlrs = result.get(rm.getPort());
							if (phdlrs == null) {
								phdlrs = new HashMap<Message, List<Handler>>();
								result.put(rm.getPort(), phdlrs);
							}
							List<Handler> hdlrs = phdlrs.get(rm.getMessage());
							if (hdlrs == null) {
								hdlrs = new ArrayList<Handler>();
								phdlrs.put(rm.getMessage(), hdlrs);
							}
							hdlrs.add(t);
						}
					}
				}
			}
		}
		return result;
	}

	public static Map<Port, Map<Message, List<Handler>>> allMessageHandlers(State self) {
		Map<Port, Map<Message, List<Handler>>> result = new HashMap<Port, Map<Message, List<Handler>>>();
		for (State s : allStates(self)) {
			List<Handler> handlers = new ArrayList<Handler>();
			for (Transition t : s.getOutgoing()) {
				handlers.add(t);
			}
			for (InternalTransition i : s.getInternal()) {
				handlers.add(i);
			}
			for (Handler t : handlers) {
				for (Event e : t.getEvent()) {
					if (e instanceof ReceiveMessage) {
						ReceiveMessage rm = (ReceiveMessage) e;
						Map<Message, List<Handler>> phdlrs = result.get(rm.getPort());
						if (phdlrs == null) {
							phdlrs = new HashMap<Message, List<Handler>>();
							result.put(rm.getPort(), phdlrs);
						}
						List<Handler> hdlrs = phdlrs.get(rm.getMessage());
						if (hdlrs == null) {
							hdlrs = new ArrayList<Handler>();
							phdlrs.put(rm.getMessage(), hdlrs);
						}
						hdlrs.add(t);
					}
				}
			}
		}
		return result;
	}


	/**
	 * Add a new handler to the list of current found handler.
	 *
	 * @param handlers Set of all current handler
	 * @param rm       Message to add
	 * @param t
	 */
	private static void addMessageToHandlers(Map<Port, Map<Message, List<Handler>>> handlers, ReceiveMessage rm, Handler t) {
		Map<Message, List<Handler>> phdlrs = handlers.get(rm.getPort());
		if (phdlrs == null) {
			phdlrs = new HashMap<>();
			handlers.put(rm.getPort(), phdlrs);
		}
		List<Handler> hdlrs = phdlrs.get(rm.getMessage());
		if (hdlrs == null) {
			hdlrs = new ArrayList<>();
			phdlrs.put(rm.getMessage(), hdlrs);
		}
		if (t != null)
			hdlrs.add(t);
	}

	public static Map<Port, Map<Message, List<Handler>>> allMessageHandlersIncludingSessions(State self) {
		Map<Port, Map<Message, List<Handler>>> result = new HashMap<Port, Map<Message, List<Handler>>>();
		for (State s : allStatesIncludingSessions(self)) {
			//println("Processisng state " + s.getName)
			List<Handler> handlers = new ArrayList<Handler>();
			for (Transition t : s.getOutgoing()) {
				handlers.add(t);
			}
			for (InternalTransition i : s.getInternal()) {
				handlers.add(i);
			}
			for (Handler t : handlers) {
				//println("  Processisng handler " + t + " Event = " + t.getEvent)
				for (Event e : t.getEvent()) {
					if (e instanceof ReceiveMessage) {
						ReceiveMessage rm = (ReceiveMessage) e;
						Map<Message, List<Handler>> phdlrs = result.get(rm.getPort());
						if (phdlrs == null) {
							phdlrs = new HashMap<Message, List<Handler>>();
							result.put(rm.getPort(), phdlrs);
						}
						List<Handler> hdlrs = phdlrs.get(rm.getMessage());
						if (hdlrs == null) {
							hdlrs = new ArrayList<Handler>();
							phdlrs.put(rm.getMessage(), hdlrs);
						}
						hdlrs.add(t);
					}
				}
			}
		}
		/*
        //add stream handlers if not present
        if (self instanceof CompositeState) {
            for (Stream s : ThingMLElementHelper.findContainingThing(self).getStreams()) {
                ReceiveMessage rMsg;

                if (s.getInput() instanceof SimpleSource) {
                    rMsg = ((SimpleSource) s.getInput()).getMessage();
                    addMessageToHandlers(result, rMsg, null);
                } else if (s.getInput() instanceof JoinSources) {
                    for (Source source : ((JoinSources) s.getInput()).getSources()) {
                        if (source instanceof SimpleSource) {
                            rMsg = ((SimpleSource) source).getMessage();
                            addMessageToHandlers(result, rMsg, null);
                        }
                    }
                } else if (s.getInput() instanceof MergeSources) {
                    for (Source source : ((MergeSources) s.getInput()).getSources()) {
                        if (source instanceof SimpleSource) {
                            rMsg = ((SimpleSource) source).getMessage();
                            addMessageToHandlers(result, rMsg, null);
                        }

                    }
                }

            }
        }
		 */
		return result;
	}


	public static boolean canHandle(State self, Port p, Message m) {
		Map<Port, Map<Message, List<Handler>>> handlers = allMessageHandlers(self);
		if (!handlers.containsKey(p))
			return false;
		else
			return handlers.get(p).containsKey(m);
	}

	public static boolean hasEmptyHandlers(State self) {
		return !allEmptyHandlers(self).isEmpty();
	}


	public static boolean canHandleIncludingSessions(State self, Port p, Message m) {
		Map<Port, Map<Message, List<Handler>>> handlers = allMessageHandlersIncludingSessions(self);
		if (!handlers.containsKey(p))
			return false;
		else
			return handlers.get(p).containsKey(m);
	}

	public static boolean hasEmptyHandlersIncludingSessions(State self) {
		return !allEmptyHandlersIncludingSessions(self).isEmpty();
	}

	public static List<Handler> allEmptyHandlersSC(StateContainer self) {
		final List<Handler> result = new ArrayList<Handler>();
		for(State s1 : self.getSubstate()){
			for (State s : allStates(s1)) {
				List<Handler> handlers = new ArrayList<Handler>();
				for (Transition t : s.getOutgoing()) {
					handlers.add(t);
				}
				for (InternalTransition i : s.getInternal()) {
					handlers.add(i);
				}
				for (Handler t : handlers) {
					if (t.getEvent().isEmpty()) {
						result.add(t);
					}
				}
			}
		}
		return result;
	}

	public static List<Handler> allEmptyHandlers(State self) {
		final List<Handler> result = new ArrayList<Handler>();
		for (State s : allStates(self)) {
			List<Handler> handlers = new ArrayList<Handler>();
			for (Transition t : s.getOutgoing()) {
				handlers.add(t);
			}
			for (InternalTransition i : s.getInternal()) {
				handlers.add(i);
			}
			for (Handler t : handlers) {
				if (t.getEvent().isEmpty()) {
					result.add(t);
				}
			}
		}
		return result;
	}


	public static List<Handler> allEmptyHandlersIncludingSessions(State self) {
		final List<Handler> result = new ArrayList<Handler>();
		for (State s : allStatesIncludingSessions(self)) {
			List<Handler> handlers = new ArrayList<Handler>();
			for (Transition t : s.getOutgoing()) {
				handlers.add(t);
			}
			for (InternalTransition i : s.getInternal()) {
				handlers.add(i);
			}
			for (Handler t : handlers) {
				if (t.getEvent().isEmpty()) {
					result.add(t);
				}
			}
		}
		return result;
	}

}
