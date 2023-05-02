package com.luminaryos.cli;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        OptionParser parser = new OptionParser();
        // Args
        parser.accepts("name", "Name of the plugin").withRequiredArg();
        parser.accepts("group-id", "GroupID of the plugin | E.G: me.example.plugin").withRequiredArg();
        parser.accepts("folder", "Folder to create the plugin in.").withRequiredArg();
        parser.allowsUnrecognizedOptions();
        OptionSet options = parser.parse(args);
        if(!options.has("name")) {
            System.out.println("[!] Missing name argument.");
            return;
        }
        if(!options.has("group-id")) {
            System.out.println("[!] Missing group-id argument.");
            return;
        }
        if(!options.has("folder")) {
            System.out.println("[!] Missing folder argument.");
            return;
        }
        try {
            createPlugin((String) options.valueOf("name"), (String) options.valueOf("group-id"), ((String) options.valueOf("folder")).replace("/", "").replace("\\", ""));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("One of the plugin files could not be created");
            deleteDirectory(new File(((String) options.valueOf("folder")).replace("/", "").replace("\\", "")));
        }
    }
    public static void createPlugin(String name, String groupid, String folder) throws IOException {
        if(!isValidGroupID(groupid)) {
            System.out.println("[!] Invalid Group ID!");
            return;
        }
        if(!new File(folder).exists()) {
            if(new File(folder).mkdir()) {
                System.out.println("[+] Successfully created folder");
            } else {
                System.out.println("[!] Folder creation failed failed..");
                return;
            }
        }
        File dirFolder = new File(folder + "/src/main/java/" + groupid.replace(".", "/"));
        if(dirFolder.mkdirs()) {
            if(new File(folder + "/pom.xml").createNewFile()) {
                System.out.println("[+] Created pom.xml");
                try {
                    writeToFile(folder + "/pom.xml",
                            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                    "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                                    "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                    "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                    "    <modelVersion>4.0.0</modelVersion>\n" +
                                    "    <groupId>" + groupid + "</groupId>\n" +
                                    "    <artifactId>" + name +"</artifactId>\n" +
                                    "    <version>1.0-SNAPSHOT</version>\n" +
                                    "    <properties>\n" +
                                    "        <maven.compiler.source>17</maven.compiler.source>\n" +
                                    "        <maven.compiler.target>17</maven.compiler.target>\n" +
                                    "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                                    "    </properties>\n" +
                                    "    <build>\n" +
                                    "        <plugins>\n" +
                                    "            <plugin>\n" +
                                    "                <groupId>org.apache.maven.plugins</groupId>\n" +
                                    "                <artifactId>maven-shade-plugin</artifactId>\n" +
                                    "                <version>3.2.4</version>\n" +
                                    "                <executions>\n" +
                                    "                    <execution>\n" +
                                    "                        <phase>package</phase>\n" +
                                    "                        <goals>\n" +
                                    "                            <goal>shade</goal>\n" +
                                    "                        </goals>\n" +
                                    "                        <configuration>\n" +
                                    "                            <createDependencyReducedPom>false</createDependencyReducedPom>\n" +
                                    "                        </configuration>\n" +
                                    "                    </execution>\n" +
                                    "                </executions>\n" +
                                    "            </plugin>\n" +
                                    "        </plugins>\n" +
                                    "    </build>\n" +
                                    "    <dependencies>\n" +
                                    "        <dependency>\n" +
                                    "            <groupId>com.luminary</groupId>\n" +
                                    "            <artifactId>luminaryos</artifactId>\n" +
                                    "            <version>LATEST</version>\n" +
                                    "        </dependency>\n" +
                                    "    </dependencies>\n" +
                                    "</project>"

                    );
                    System.out.println("[+] Written pom.xml");
                    String path = folder + "/src/main/java/" + groupid.replace(".", "/") + "/ExamplePlugin.java";
                    System.out.println("Main class path: " + path);
                    File main = new File(path);
                    if(main.createNewFile()) {
                        System.out.println("[+] Created main plugin java file.");
                    } else {
                        System.out.println("[!] Couldn't create plugin java file.");
                        return;
                    }
                    writeToFile(path,
                            "import com.luminary.os.plugin.Plugin;",
                            "",
                            "public class ExamplePlugin extends Plugin {",
                            "",
                            "\t@Override",
                            "\tpublic void onEnable() {",
                            "\t\tSystem.out.println(\"Hello from plugin\")",
                            "\t}",
                            "",
                            "\t@Override",
                            "\tpublic void onDisable() {",
                            "",
                            "\t}",
                            "",
                            "}"
                    );
                    System.out.println("Successfully created Plugin!");
                    System.exit(0);
                } catch (IOException e) {
                    System.out.println("[!] Error occurred, does the file already exist?");
                    e.printStackTrace();
                }
            } else {
                return;
            }
        } else {
            System.out.println("[!] Could not create folders.");
            return;
        }
    }
    public static boolean isValidGroupID(String groupID) {
        if (groupID == null || groupID.isEmpty()) {
            return false;
        }

        if (!groupID.matches("([A-Za-z]+(\\.[A-Za-z]+)+)")) {
            return false;
        }

        return true;
    }
    public static void writeToFile(String filename, String... lines) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (String line : lines) {
            writer.write(line);
            writer.newLine();
        }
        writer.close();
    }
    public static void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            Arrays.stream(Objects.requireNonNull(directory.listFiles())).forEach(Main::deleteDirectory);
        }
        directory.delete();
    }
}