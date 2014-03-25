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
package org.thingml.utils.xml;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class YrTransformer {
	
	public YrTransformer() {
	}
	
	public Document transformYrTimeFormat(Document doc){
		Xpath xpath = new Xpath();
		NodeList fcX = xpath.xPathNodeList("/weatherdata/forecast/tabular/time", doc);
		
		// transform timeformet "yyyy-MM-dd\'T\'hh:mm:ss" in milliseconds
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		for(int i = 0; i < fcX.getLength(); i++){
			try {
				Element el = (Element) fcX.item(i);
				
				Long from = format.parse(el.getAttribute("from")).getTime();
				Long to = format.parse(el.getAttribute("to")).getTime();
				el.setAttribute("from", from.toString());
				//System.out.println(from);
				el.setAttribute("to", to.toString());
				//System.out.println(to);

			} catch (ParseException e) {
				e.printStackTrace();
			}
			
		}
		
		return doc;
	}
	
}