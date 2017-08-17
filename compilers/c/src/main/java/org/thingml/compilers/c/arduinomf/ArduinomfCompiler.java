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
package org.thingml.compilers.c.arduinomf;

import org.thingml.compilers.ThingMLCompiler;
import org.thingml.compilers.c.CCfgMainGenerator;
import org.thingml.compilers.c.CCompilerContext;
import org.thingml.compilers.c.CThingImplCompiler;
import org.thingml.compilers.c.arduino.ArduinoChecker;
import org.thingml.compilers.c.arduino.CThingActionCompilerArduino;
import org.thingml.compilers.configuration.CfgBuildCompiler;
import org.thingml.compilers.utils.OpaqueThingMLCompiler;
import org.thingml.utilities.logging.Logger;
import org.thingml.xtext.constraints.ThingMLHelpers;
import org.thingml.xtext.helpers.ConfigurationHelper;
import org.thingml.xtext.thingML.Configuration;
import org.thingml.xtext.thingML.Thing;

/**
 * Created by ffl on 25.11.14.
 */
public class ArduinomfCompiler extends OpaqueThingMLCompiler {

    public ArduinomfCompiler() {
            super(new CThingActionCompilerArduino(), new CThingApiCompilerArduinomf(), new CCfgMainGenerator(),
                    new CfgBuildCompiler(), new CThingImplCompiler());
            this.checker = new ArduinoChecker(this.getID(), null);
    }

    
    
    @Override
    public ThingMLCompiler clone() {
        return new ArduinomfCompiler();
    }

    @Override
    public String getID() {
        return "arduinomf";
    }
    @Override
    public String getName() {
        return "C/C++ for Arduino (AVR Microcontrollers)";
    }

    public String getDescription() {
        return "Generates C/C++ code for Arduino or other AVR microcontrollers (AVR-GCC compiler).";
    }

    @Override
    public void do_call_compiler(Configuration cfg, Logger log, String... options) {

        CCompilerContext ctx = new CCompilerContextArduinomf(this);
        processDebug(cfg);
        ctx.setCurrentConfiguration(cfg);
        //ctx.setOutputDirectory(new File(ctx.getOutputDirectory(), cfg.getName()));

        //Checks

        this.checker.do_check(cfg, false);
        //this.checker.printReport(log);

        // GENERATE A MODULE FOR EACH THING
        for (Thing thing : ConfigurationHelper.allThings(cfg)) {
            ctx.setConcreteThing(thing);

            // GENERATE HEADER
            ctx.getCompiler().getThingApiCompiler().generatePublicAPI(thing, ctx);

            // GENERATE IMPL
            ctx.getCompiler().getThingImplCompiler().generateImplementation(thing, ctx);
            ctx.clearConcreteThing();
        }

        // GENERATE A MODULE FOR THE CONFIGURATION (+ its dependencies)
        getMainCompiler().generateMainAndInit(cfg, ThingMLHelpers.findContainingModel(cfg), ctx);

        // WRITE THE GENERATED CODE
        ctx.writeGeneratedCodeToFiles();

    }
    
}
