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

import org.thingml.xtext.constraints.Types;
import org.thingml.xtext.thingML.ObjectType;
import org.thingml.xtext.thingML.Type;

/**
 * Created by ffl on 10.05.2016.
 */
public class TyperHelper {


    public static Type getBroadType(Type self) {
        if (AnnotatedElementHelper.hasAnnotation(self, "type_checker")) {
            final String ty = AnnotatedElementHelper.annotation(self, "type_checker").get(0);
            if (ty.equals("Integer"))
                return Types.INTEGER_TYPE;
            else if (ty.equals("Real"))
                return Types.REAL_TYPE;
            else if (ty.equals("Boolean"))
                return Types.BOOLEAN_TYPE;
            else if (ty.equals("Character"))
                return Types.CHARACTER_TYPE;
            else if (ty.equals("String"))
                return Types.STRING_TYPE;
            else if (ty.equals("Object"))
                return Types.OBJECT_TYPE;
            else if (ty.equals("Error"))
                return Types.ERROR_TYPE;
            else if (ty.equals("Void"))
                return Types.VOID_TYPE;
            else
                return Types.ANY_TYPE;
        } else if (self instanceof ObjectType) {
            return Types.OBJECT_TYPE;
        }
        return Types.ANY_TYPE;
    }

    public static boolean isA(Type self, Type t) {
        if (getBroadType(t) == Types.ANY_TYPE)//anything is an Any
            return true;
        if (getBroadType(self) == getBroadType(t))
            return true;
        if (getBroadType(self) == Types.INTEGER_TYPE && getBroadType(t) == Types.REAL_TYPE) //an Integer is a Real
            return true;
        if (getBroadType(self) == Types.STRING_TYPE && getBroadType(t) == Types.OBJECT_TYPE)//a String is an Object
            return true;
        return false;
    }
}
