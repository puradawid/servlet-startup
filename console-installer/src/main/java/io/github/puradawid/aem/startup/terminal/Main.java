package io.github.puradawid.aem.startup.terminal;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.impl.client.HttpClientBuilder;

public class Main {

    private static CommandLineParser commandLineParser = new DefaultParser();

    private static Options options = new Options()
        .addOption(Option.builder("h").desc("prints this help").longOpt("help").hasArg(false).optionalArg(true).build())
        .addOption(Option.builder("f").hasArgs().longOpt("file").desc("multiple packages to install").build())
        .addOption(Option.builder("H").hasArg().longOpt("host").desc("host name").build())
        .addOption(Option.builder("p").hasArg().longOpt("port").desc("port number").build());

    private static HelpFormatter helpFormatter = new HelpFormatter();

    public static void main(String[] args) throws ParseException, InterruptedException {
        CommandLine commandLine = commandLineParser.parse(options, args);
        if (commandLine.hasOption("h")) {
            helpFormatter.printHelp(
                "java -jar install.jar",
                "Install AEM package and wait for result",
                options,
                "https://github.io/puradawid/startup-servlet",
                true
            );
        } else {
            // based on arguments
            Communication connection = new Communication(
                new Communication.Instance(commandLine.getOptionValue("host"), Integer.parseInt(commandLine.getOptionValue("port")), false),
                new Communication.User("admin", "admin"), HttpClientBuilder.create().build());
            ZipPackage zipPackage = new ZipPackage(commandLine.getOptionValue("file"));

            if (zipPackage.validate() && connection.established()) {
                int number = connection.pendingPackages();
                connection.install(zipPackage);

                while (connection.pendingPackages() > number) {
                    Thread.sleep(500);
                }

            } else {
                System.out.println("ZIP is not installed");
                System.exit(1);
            }
        }
    }
}