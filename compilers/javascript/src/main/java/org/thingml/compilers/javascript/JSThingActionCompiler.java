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
package org.thingml.compilers.javascript;

import org.thingml.compilers.Context;
import org.thingml.compilers.thing.common.CommonThingActionCompiler;
import org.thingml.xtext.constraints.ThingMLHelpers;
import org.thingml.xtext.constraints.Types;
import org.thingml.xtext.helpers.AnnotatedElementHelper;
import org.thingml.xtext.helpers.ConfigurationHelper;
import org.thingml.xtext.helpers.ThingHelper;
import org.thingml.xtext.helpers.ThingMLElementHelper;
import org.thingml.xtext.helpers.TyperHelper;
import org.thingml.xtext.thingML.ConfigPropertyAssign;
import org.thingml.xtext.thingML.Decrement;
import org.thingml.xtext.thingML.DivExpression;
import org.thingml.xtext.thingML.EnumLiteralRef;
import org.thingml.xtext.thingML.EqualsExpression;
import org.thingml.xtext.thingML.ErrorAction;
import org.thingml.xtext.thingML.EventReference;
import org.thingml.xtext.thingML.Expression;
import org.thingml.xtext.thingML.FunctionCallExpression;
import org.thingml.xtext.thingML.FunctionCallStatement;
import org.thingml.xtext.thingML.Increment;
import org.thingml.xtext.thingML.LocalVariable;
import org.thingml.xtext.thingML.Parameter;
import org.thingml.xtext.thingML.PrintAction;
import org.thingml.xtext.thingML.Property;
import org.thingml.xtext.thingML.PropertyReference;
import org.thingml.xtext.thingML.ReceiveMessage;
import org.thingml.xtext.thingML.SendAction;
import org.thingml.xtext.thingML.Session;
import org.thingml.xtext.thingML.StartSession;
import org.thingml.xtext.thingML.StringLiteral;
import org.thingml.xtext.thingML.Thing;
import org.thingml.xtext.thingML.Type;
import org.thingml.xtext.thingML.VariableAssignment;

/**
 * Created by bmori on 01.12.2014.
 */
public class JSThingActionCompiler extends CommonThingActionCompiler {

    /**
     * JS does not really have integers, and hence no proper integer division
     * @param expression
     * @param builder
     * @param ctx
     */
    @Override
    public void generate(DivExpression expression, StringBuilder builder, Context ctx) {
        final Type lhsType = TyperHelper.getBroadType(ctx.getCompiler().checker.typeChecker.computeTypeOf(expression.getLhs()));
        if(Types.INTEGER_TYPE.equals(lhsType)) {//integer division if LHS is integer
            builder.append("Math.floor(");
            generate(expression.getLhs(), builder, ctx);
            builder.append(" / ");
            generate(expression.getRhs(), builder, ctx);
            builder.append(")");
        } else {
            super.generate(expression, builder, ctx);
        }
    }

    @Override
    public void generate(VariableAssignment action, StringBuilder builder, Context ctx) {
        traceVariablePre(action, builder, ctx);
        if (action.getProperty().getTypeRef().getCardinality() != null && action.getIndex() != null) {//this is an array (and we want to affect just one index)
            for (Expression i : action.getIndex()) {
                if (action.getProperty() instanceof Property) {
                    builder.append(ctx.getContextAnnotation("thisRef"));
                }
                builder.append(ctx.getVariableName(action.getProperty()));
                StringBuilder tempBuilder = new StringBuilder();
                generate(i, tempBuilder, ctx);
                builder.append("[" + tempBuilder.toString() + "]");
                builder.append(" = ");
                cast(action.getProperty().getTypeRef().getType(), false, action.getExpression(), builder, ctx);
                builder.append(";\n");

            }
        } else {//simple variable or we re-affect the whole array
            if (action.getProperty() instanceof Property) {
                builder.append(ctx.getContextAnnotation("thisRef"));
            }
            builder.append(ctx.getVariableName(action.getProperty()));
            builder.append(" = ");
            cast(action.getProperty().getTypeRef().getType(), action.getProperty().getTypeRef().isIsArray(), action.getExpression(), builder, ctx);
            builder.append(";\n");
        }
        traceVariablePost(action, builder, ctx);
    }

    @Override
    public void traceVariablePre(VariableAssignment action, StringBuilder builder, Context ctx) {
        /*if (action.getProperty().eContainer() instanceof Thing) {
            builder.append("debug_" + ThingMLElementHelper.qname(action.getProperty(), "_") + "_var = this." + ThingMLElementHelper.qname(action.getProperty(), "_") + "_var;\n");
        }*/
    }

    @Override
    public void traceVariablePost(VariableAssignment action, StringBuilder builder, Context ctx) {
        if (action.getProperty().eContainer() instanceof Thing) {//we can only listen to properties of a Thing, not all local variables, etc
            builder.append("this.bus.emit('" + action.getProperty().getName() + "=', this." + ctx.getVariableName(action.getProperty()) + ");\n");
        }
    }

    @Override
    public void generate(SendAction action, StringBuilder builder, Context ctx) {
    	if(!AnnotatedElementHelper.isDefined(action.getPort(), "sync_send", "true")) {
            builder.append("setImmediate(() => ");
    	}
        builder.append("this.bus.emit(");
        builder.append("'" + action.getPort().getName() + "?" + action.getMessage().getName() + "'");
        for (Expression pa : action.getParameters()) {
            builder.append(", ");
            generate(pa, builder, ctx);
        }
        builder.append(")");
    	if(!AnnotatedElementHelper.isDefined(action.getPort(), "sync_send", "true")) {
            builder.append(")");
    	}
        builder.append(";\n");
    }

    @Override
    public void generate(StartSession action, StringBuilder builder, Context ctx) {
        Session session = action.getSession();
        builder.append("const " + session.getName() + " = new " + ctx.firstToUpper(ThingMLHelpers.findContainingThing(session).getName()) + "(\"" + session.getName() + "\", this");
        for (Property p : ThingHelper.allPropertiesInDepth(ThingMLHelpers.findContainingThing(session))) {
        	if (!AnnotatedElementHelper.isDefined(p, "private", "true") && p.eContainer() instanceof Thing) {
        		if (p.getTypeRef().isIsArray() || p.getTypeRef().getCardinality() != null) {
        			builder.append(", this." + ThingMLElementHelper.qname(p, "_") + "_var.slice(0)");
        		} else {
        			builder.append(", this." + ThingMLElementHelper.qname(p, "_") + "_var");
        		}
        	}
        }
        builder.append(", this.debug);\n");
        builder.append("this.forks.push(" + session.getName() + ");\n");
        builder.append(session.getName() + "._init();\n");
    }


    @Override
    public void generate(FunctionCallStatement action, StringBuilder builder, Context ctx) {
        builder.append("this." + action.getFunction().getName() + "(");
        int i = 0;
        for (Expression p : action.getParameters()) {
            if (i > 0)
                builder.append(", ");
            generate(p, builder, ctx);
            i++;
        }
        builder.append(");\n");
    }

    @Override
    public void generate(LocalVariable action, StringBuilder builder, Context ctx) {
        if (!action.isReadonly())
            builder.append("let ");
        else
            builder.append("const ");
        builder.append(ctx.getVariableName(action));
        if (action.getInit() != null) {
            builder.append(" = ");
            generate(action.getInit(), builder, ctx);
        } else {
            if (action.getTypeRef().getCardinality() != null) {
                builder.append(" = []");
            }
            if (action.isReadonly() && action.getTypeRef().getCardinality() == null)
                System.out.println("[ERROR] readonly variable " + action + " must be initialized");
        }
        builder.append(";\n");
    }

    @Override
    public void generate(ErrorAction action, StringBuilder builder, Context ctx) {
        builder.append("console.error(''+");
        generate(action.getMsg(), builder, ctx);
        builder.append(");\n");
    }

    @Override
    public void generate(PrintAction action, StringBuilder builder, Context ctx) {
        builder.append("console.log(''+");
        generate(action.getMsg(), builder, ctx);
        builder.append(");\n");
    }

    @Override
    public void generate(PropertyReference expression, StringBuilder builder, Context ctx) {
        /*if (AnnotatedElementHelper.isDefined(expression.getProperty(), "private", "true") || !(expression.getProperty().eContainer() instanceof Thing) || (expression.getProperty() instanceof Parameter) || (expression.getProperty() instanceof LocalVariable)) {
            builder.append("this." + ctx.getVariableName(expression.getProperty()));
        } else {*/
            if (expression.getProperty() instanceof Parameter || expression.getProperty() instanceof LocalVariable) {
                builder.append(ctx.getVariableName(expression.getProperty()));
            } else if (expression.getProperty() instanceof Property) {
                if (!ctx.getAtInitTimeLock()) {
                    if (ctx.currentInstance != null) {
                        Property p = (Property) expression.getProperty();
                        if (p.isReadonly()) {
                            boolean found = false;
                            for (ConfigPropertyAssign pa : ctx.getCurrentConfiguration().getPropassigns()) {
                                String tmp = ThingMLElementHelper.findContainingConfiguration(pa.getInstance()).getName() + "_" + pa.getInstance().getName();

                                if (ctx.currentInstance.getName().equals(tmp)) {
                                    if (pa.getProperty().getName().compareTo(p.getName()) == 0) {
                                        generate(pa.getInit(), builder, ctx);
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if (!found) {
                                generate(p.getInit(), builder, ctx);
                            }
                        } else {
                            builder.append("this." + ctx.getVariableName(expression.getProperty()));
                        }
                    } else {
                        builder.append("this." + ctx.getVariableName(expression.getProperty()));
                    }
                } else {
                    Property p = (Property) expression.getProperty();
                    Expression e = ConfigurationHelper.initExpressions(ctx.getCurrentConfiguration(), ctx.currentInstance, p).get(0);
                    generate(e, builder, ctx);
                }
            }
        //}
    }

    @Override
    public void generate(EnumLiteralRef expression, StringBuilder builder, Context ctx) {
        builder.append("Enum." + ctx.firstToUpper(expression.getEnum().getName()) + "_ENUM." + expression.getLiteral().getName().toUpperCase());
    }

    @Override
    public void generate(FunctionCallExpression expression, StringBuilder builder, Context ctx) {
        builder.append("this." + expression.getFunction().getName() + "(");
        int i = 0;
        for (Expression p : expression.getParameters()) {
            if (i > 0)
                builder.append(", ");
            generate(p, builder, ctx);
            i++;
        }
        builder.append(")");
    }

    @Override
    public void generate(EqualsExpression expression, StringBuilder builder, Context ctx) {
        generate(expression.getLhs(), builder, ctx);
        builder.append(" === ");
        generate(expression.getRhs(), builder, ctx);
    }

    @Override
    public void generate(StringLiteral expression, StringBuilder builder, Context ctx) {
        //builder.append("'" + CharacterEscaper.escapeEscapedCharacters(expression.getStringValue()) + "'");
    	builder.append("'" + expression.getStringValue() + "'");
    }
    
    @Override
    public void generate(EventReference expression, StringBuilder builder, Context ctx) {
        builder.append((((ReceiveMessage)expression.getReceiveMsg()).getMessage().getName()) + "." + expression.getParameter().getName());
    }
    
    @Override
    public void generate(Increment action, StringBuilder builder, Context ctx) {
    	if(action.getVar() instanceof Property) {
    		builder.append("this.");
    	}
    	super.generate(action, builder, ctx);
    }

    @Override
    public void generate(Decrement action, StringBuilder builder, Context ctx) {
    	if(action.getVar() instanceof Property) {
    		builder.append("this.");
    	}
    	super.generate(action, builder, ctx);
    }
}
