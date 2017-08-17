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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingml.networkplugins.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.thingml.compilers.Context;
import org.thingml.compilers.spi.NetworkPlugin;
import org.thingml.compilers.spi.SerializationPlugin;
import org.thingml.xtext.helpers.AnnotatedElementHelper;
import org.thingml.xtext.thingML.Configuration;
import org.thingml.xtext.thingML.ExternalConnector;
import org.thingml.xtext.thingML.Message;
import org.thingml.xtext.thingML.Port;
import org.thingml.xtext.thingML.Protocol;
import org.thingml.xtext.thingML.RequiredPort;

public class JavaMQTTPlugin extends NetworkPlugin {

    public String getPluginID() {
        return "JavaMQTTPlugin";
    }

    public List<String> getSupportedProtocols() {
        List<String> res = new ArrayList<>();
        res.add("MQTT");
        res.add("mqtt");
        return res;
    }

    public List<String> getTargetedLanguages() {
        List<String> res = new ArrayList<>();
        res.add("java");
        return res;
    }

    private Set<Message> messages = new HashSet<Message>();

    private void clearMessages() {
        messages.clear();
    }

    private void addMessage(Message m) {
        boolean contains = false;
        for(Message msg : messages) {
            if (EcoreUtil.equals(msg, m)) {
                contains = true;
                break;
            }
        }
        if (!contains) {
            messages.add(m);
        }
    }

    private void updatePOM(Context ctx) {
        //Update POM.xml with JSSC Maven dependency
        try {
            final InputStream input = new FileInputStream(ctx.getOutputDirectory() + "/pom.xml");
            final List<String> packLines = IOUtils.readLines(input, Charset.forName("UTF-8"));
            String pom = "";
            for (String line : packLines) {
                pom += line + "\n";
            }
            input.close();
            pom = pom.replace("<!--DEP-->", "<dependency>\n" +
                    "    <groupId>org.fusesource.mqtt-client</groupId>\n" +
                    "    <artifactId>mqtt-client</artifactId>\n" +
                    "    <version>1.12</version>\n" +
                    "</dependency>\n<!--DEP-->");
            final File f = new File(ctx.getOutputDirectory() + "/pom.xml");
            final OutputStream output = new FileOutputStream(f);
            IOUtils.write(pom, output, Charset.forName("UTF-8"));
            IOUtils.closeQuietly(output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateNetworkLibrary(Configuration cfg, Context ctx, Set<Protocol> protocols) {
        boolean escape = false; //FIXME true/false depending on annotation

        updatePOM(ctx);
        StringBuilder builder = new StringBuilder();
        for (Protocol prot : protocols) {
            clearMessages();
            for (ThingPortMessage tpm : getMessagesSent(cfg, prot)) {
                addMessage(tpm.m);
            }
            SerializationPlugin sp = null;
            try {
                sp = ctx.getSerializationPlugin(prot);
            } catch (UnsupportedEncodingException uee) {
                System.err.println("Could not get serialization plugin... Expect some errors in the generated code");
                uee.printStackTrace();
                return;
            }
            StringBuilder temp = new StringBuilder();
            if (sp.getSupportedFormat().contains("Binary")) {
                temp.append("public Byte[] format(Event e){\n");
            } else {
                temp.append("public String format(Event e){\n");
            }
            int i = 0;
            for(Message m : messages) {
                if (i > 0)
                    temp.append("else ");
                temp.append("if (e.getType().equals(" +  m.getName().toUpperCase() + ")) {\n");
                temp.append("return ");
                if (sp.getSupportedFormat().contains("Binary")) {//FIXME
                    temp.append("JavaBinaryHelper.toObject(");
                    if (escape) {
                        temp.append("JavaBinaryHelper.unescape(");
                    }
                }
                temp.append("format((" + ctx.firstToUpper(m.getName()) + "MessageType." + ctx.firstToUpper(m.getName()) + "Message)e)\n");
                if (sp.getSupportedFormat().contains("Binary")) {//FIXME
                    temp.append(")");
                    if (escape) {
                        temp.append(")");
                    }
                }
                temp.append(";");
                temp.append("}\n");
                i++;
            }
            temp.append("return null;\n");
            temp.append("}\n");
            for (Message m : messages) {
                sp.generateSerialization(temp, prot.getName() + "BinaryProtocol", m);
            }
            clearMessages();
            builder = new StringBuilder();
            for (ThingPortMessage tpm : getMessagesReceived(cfg, prot)) {
                addMessage(tpm.m);
            }
            sp.generateParserBody(builder, prot.getName() + "BinaryProtocol", null, messages, null);
            final String result = builder.toString().replace("/*$SERIALIZERS$*/", temp.toString());
            try {
                final File folder = new File(ctx.getOutputDirectory() + "/src/main/java/org/thingml/generated/network");
                folder.mkdir();
                final File f = new File(ctx.getOutputDirectory() + "/src/main/java/org/thingml/generated/network/" + prot.getName() + "BinaryProtocol.java");
                final OutputStream output = new FileOutputStream(f);
                IOUtils.write(result, output, Charset.forName("UTF-8"));
                IOUtils.closeQuietly(output);
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SerialProtocol(ctx, prot, cfg).generate(escape);
        }
    }

    private class SerialProtocol {
        Context ctx;
        Protocol prot;
        Configuration cfg;

        private List<Port> ports = new ArrayList<Port>();

        public SerialProtocol(Context ctx, Protocol prot, Configuration cfg) {
            this.ctx = ctx;
            this.prot = prot;
            this.cfg = cfg;
        }

        private void addPort(Port p) {
            boolean contains = false;
            for (Port port : ports) {
                if (EcoreUtil.equals(port, p)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                ports.add(p);
            }
        }

        public void generate(boolean escape) {
            for (ThingPortMessage tpm : getMessagesSent(cfg, prot)) {
                addPort(tpm.p);
            }
            for (ThingPortMessage tpm : getMessagesReceived(cfg, prot)) {
                addPort(tpm.p);
            }
            String template = ctx.getTemplateByID("templates/JavaMQTTPlugin.java");
            template = template.replace("/*$SERIALIZER$*/", prot.getName() + "BinaryProtocol");
            StringBuilder parseBuilder = new StringBuilder();
            SerializationPlugin sp = null;
            try {
                sp = ctx.getSerializationPlugin(prot);
            } catch (UnsupportedEncodingException uee) {
                System.err.println("Could not get serialization plugin... Expect some errors in the generated code");
                uee.printStackTrace();
                return;
            }

            if (sp.getSupportedFormat().contains("Binary")) {//FIXME
                parseBuilder.append("final Event event = formatter.instantiate(JavaBinaryHelper.toObject(payload.toByteArray()));\n");
                if (escape) {
                    template = template.replace("/*$PUBLISH$*/", "connection.publish(this.pubtopic, JavaBinaryHelper.escape(JavaBinaryHelper.toPrimitive((Byte[]) payload)), QoS.AT_LEAST_ONCE, false, disconnectOnFailure);\n");
                } else {
                    template = template.replace("/*$PUBLISH$*/", "connection.publish(this.pubtopic, JavaBinaryHelper.toPrimitive((Byte[]) payload), QoS.AT_LEAST_ONCE, false, disconnectOnFailure);\n");
                }
            } else {
                parseBuilder.append("final Event event = formatter.instantiate(new String(payload.toByteArray(), java.nio.charset.StandardCharsets.UTF_8));\n");
                template = template.replace("/*$PUBLISH$*/", "connection.publish(new UTF8Buffer(this.pubtopic), new Buffer(((String)payload).getBytes()), QoS.AT_LEAST_ONCE, false, disconnectOnFailure);\n");
            }
            for(Port p : ports) {//FIXME
                parseBuilder.append("if (event != null) " + p.getName() + "_port.send(event);\n");
            }
            template = initPort(ctx, template);
            for (ExternalConnector conn : getExternalConnectors(cfg, prot)) {
                updateMain(ctx, cfg, conn);
            }
            template = template.replace("/*$PARSING CODE$*/", parseBuilder.toString());

            try {
                final File folder = new File(ctx.getOutputDirectory() + "/src/main/java/org/thingml/generated/network");
                folder.mkdir();
                final File f = new File(ctx.getOutputDirectory() + "/src/main/java/org/thingml/generated/network/MQTTJava.java");
                final OutputStream output = new FileOutputStream(f);
                IOUtils.write(template, output, Charset.forName("UTF-8"));
                IOUtils.closeQuietly(output);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String initPort(Context ctx, String template) {
            for (Port p : ports) {
                template = template.replace("/*$PORTS$*/", "/*$PORTS$*/\nprivate Port " + p.getName() + "_port;\npublic Port get" + ctx.firstToUpper(p.getName()) + "_port(){return " + p.getName() + "_port;}\n");
                String portType = "PortType.PROVIDED";
                if (p instanceof RequiredPort)
                    portType = "PortType.REQUIRED";

                template = template.replace("/*$INIT PORTS$*/", "/*$INIT PORTS$*/\n" + p.getName() + "_port = new Port(" + portType + ", \"" + p.getName() + "\", this);\n");
            }
            return template;
        }

        private void updateMain(Context ctx, Configuration cfg, ExternalConnector conn) {
            try {
                final InputStream input = new FileInputStream(ctx.getOutputDirectory() + "/src/main/java/org/thingml/generated/Main.java");
                final List<String> packLines = IOUtils.readLines(input);
                String main = "";
                for (String line : packLines) {
                    main += line + "\n";
                }
                input.close();
                final String url = "tcp://" + AnnotatedElementHelper.annotationOrElse(conn, "url", AnnotatedElementHelper.annotationOrElse(conn.getProtocol(), "url", "127.0.0.1:1883"));
                final String subtopic = AnnotatedElementHelper.annotationOrElse(conn.getProtocol(), "subscribe", "ThingML");
                final String pubtopic = AnnotatedElementHelper.annotationOrElse(conn.getProtocol(), "publish", "ThingML");

                main = main.replace("/*$NETWORK$*/", "/*$NETWORK$*/\nMQTTJava " + conn.getName() + "_" + conn.getProtocol().getName() + " = (MQTTJava) new MQTTJava(\"" + url + "\", \"" + pubtopic + "\", \"" + subtopic + "\").buildBehavior(null, null);\n");

                StringBuilder connBuilder = new StringBuilder();
                connBuilder.append(conn.getName() + "_" + conn.getProtocol().getName() + ".get" + ctx.firstToUpper(conn.getPort().getName()) + "_port().addListener(");
                connBuilder.append(ctx.getInstanceName(conn.getInst()) + ".get" + ctx.firstToUpper(conn.getPort().getName()) + "_port());\n");
                connBuilder.append(ctx.getInstanceName(conn.getInst()) + ".get" + ctx.firstToUpper(conn.getPort().getName()) + "_port().addListener(");
                connBuilder.append(conn.getName() + "_" + conn.getProtocol().getName() + ".get" + ctx.firstToUpper(conn.getPort().getName()) + "_port());\n");
                main = main.replace("/*$EXT CONNECTORS$*/", "/*$EXT CONNECTORS$*/\n" + connBuilder.toString());

                main = main.replace("/*$START$*/", "/*$START$*/\n" + conn.getName() + "_" + conn.getProtocol().getName() + ".init().start();\n");
                main = main.replace("/*$STOP$*/", "/*$STOP$*/\n" + conn.getName() + "_" + conn.getProtocol().getName() + ".stop();\n");

                final File f = new File(ctx.getOutputDirectory() + "/src/main/java/org/thingml/generated/Main.java");
                final OutputStream output = new FileOutputStream(f);
                IOUtils.write(main, output, Charset.forName("UTF-8"));
                IOUtils.closeQuietly(output);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
