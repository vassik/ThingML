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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;


import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Xpath {
	private XPath xPath;
	
	public Xpath(){
		xPath =  XPathFactory.newInstance().newXPath();
	}
	
	public String xPath(String expression,  Document source){
		String exp = expression;
		String res = null;
			try {
				res = xPath.compile(exp).evaluate(source);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
		}
		
		return res;
		
	}
	
	public Node xPathNode(String expression, Document source){
		Node res = null;
			try {
				res = (Node) xPath.compile(expression).evaluate(source, XPathConstants.NODE);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
		
		return res;	
	}
	
	public NodeList xPathNodeList(String expression, Document source){
		NodeList res = null;
			try {
				res = (NodeList) xPath.compile(expression).evaluate(source, XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
		
		return res;	
	}
}
