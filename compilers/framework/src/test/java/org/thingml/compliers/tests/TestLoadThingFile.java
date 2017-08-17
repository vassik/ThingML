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
package org.thingml.compliers.tests;

import static org.junit.Assert.assertFalse;

import java.io.File;

import org.junit.Test;
import org.thingml.compilers.ThingMLCompiler;
import org.thingml.xtext.thingML.ThingMLModel;

public class TestLoadThingFile extends LoadModelTestsCommon {

	@Test
	public void test() {
		// Get the .thingml file from resources
		File test = new File(this.getClass().getResource("/SimpleFlatModel.thingml").getFile());
		
		// Load the model
		ThingMLModel model = ThingMLCompiler.loadModel(test);
		assertFalse("Loaded model is not null", model == null);
		
		// Check that the model is correct
		checkSimpleModel(model);
	}
}
