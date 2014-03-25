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
package org.thingml.utils.esper.eventtypes;

public class SensorData {
	String sensor;
	Double val;
	String unit;
	Long time;
	
	
	public SensorData(String sensor, Double val, String unit, Long time) {
		this.sensor = sensor;
		this.val = val;
		this.unit = unit;
		this.time = time;
	}


	public String getSensor() {
		return sensor;
	}

	public Double getVal() {
		return val;
	}

	public String getUnit() {
		return unit;
	}

	public Long getTime() {
		return time;
	}

	@Override
	public String toString() {
		return "Sensor: " + sensor + " val: " + val.toString() + " unit: " + unit + " time: " + time.toString();
	}
}
