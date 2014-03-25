/**
 * Copyright (C) 2014 SINTEF <franck.fleurey@sintef.no>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingml.utils.esper;

import org.thingml.utils.esper.eventtypes.SensorData;

import com.espertech.esper.client.*;


public class EsperSensorDataEngine {
	Configuration config;
	EPAdministrator admin;
	EPRuntime rt;
	
	public EsperSensorDataEngine() {
		config = new Configuration();
		config.addEventType("SensorDataEvent", SensorData.class.getName());
		
	    EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEPEngine",config);
	    
	    rt = cep.getEPRuntime();
	    
	    admin = cep.getEPAdministrator();
	}
	
	
	public void addPattern(String pattern, EsperListener listener){
		Listener l = new Listener(listener);
		
		try{
			EPStatement st = admin.createPattern(pattern);
			st.addListener(l);
		}
		catch(EPException ex){
			ex.printStackTrace();
		}
	}

	public void addStatement(String statement, EsperListener listener){
		Listener l = new Listener(listener);
		
		try{
			EPStatement st = admin.createEPL(statement);
			st.addListener(l);
		}
		catch(EPException ex){
			ex.printStackTrace();
		}
		catch(IllegalStateException ex){
			ex.printStackTrace();
		}
	}
	
	// test
	public static void generateExampleEvents(EPRuntime rt, Double val){
		SensorData data = new SensorData("test", val, "degC", (long) 12344566);
		System.out.println("Sending data");
		try{
			rt.sendEvent(data);
		}
		catch(EPException ex){
			ex.printStackTrace();
		}
	}
	
	public void addEvent(SensorData event){
		try{
			rt.sendEvent(event);
		}
		catch(EPException ex){
			ex.printStackTrace();
		}
		
	}
	
	
	
	
	

	
}
