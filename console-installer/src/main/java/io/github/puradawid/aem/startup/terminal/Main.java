package io.github.puradawid.aem.startup.terminal;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import java.io.IOException;

public class Main {

    private static CommandLineParser commandLineParser = new DefaultParser();

    private static Options options = new Options()
        .addOption(Option.builder("h").desc("prints this help").longOpt("help").hasArg(false).optionalArg(true).build())
        .addOption(Option.builder("f").hasArgs().longOpt("file").desc("multiple packages to install").build())
        .addOption(Option.builder("H").hasArg().longOpt("host").desc("host name").build())
        .addOption(Option.builder("p").hasArg().longOpt("port").desc("port number").build())
        .addOption(Option.builder("debug").desc("Turn on debugging log").build());

    private static HelpFormatter helpFormatter = new HelpFormatter();

    public static void main(String[] args) throws ParseException, InterruptedException {
        CommandLine commandLine = commandLineParser.parse(options, args);
        if (commandLine.hasOption("debug")) {
            LogManager.getLogger("io.github.puradawid.aem").setLevel(Level.DEBUG);
        }
        if (hasAllNeededParams(commandLine)) {
            helpFormatter.printHelp(
                "java -jar install.jar",
                "Install AEM package and wait for result",
                options,
                "https://github.io/puradawid/startup-servlet",
                true
            );
        } else {
            // based on arguments
            try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                Communication connection = new Communication(
                    new Communication.Instance(commandLine.getOptionValue("host"),
                        Integer.parseInt(commandLine.getOptionValue("port")), false),
                    new Communication.User("admin", "admin"), httpClient);
                ZipPackage zipPackage = new ZipPackage(commandLine.getOptionValue("file"));

                InstallationProcess process = new InstallationProcess(zipPackage, connection);
                if (process.get()) {
                    System.out.println("ZIP is installed");
                    System.exit(0);
                } else {
                    System.out.println("ZIP is not installed");
                    System.exit(-1);
                }
            } catch (IOException ex) {
                System.out.println("There is a problem with registering http client. No rights there?");
                ex.printStackTrace();
                System.exit(-2);
            }
        }
    }

    private static boolean hasAllNeededParams(CommandLine cmd) {
        return cmd.hasOption("h") || (!cmd.hasOption("f") || !cmd.hasOption("H") || !cmd.hasOption("p"));
    }
}