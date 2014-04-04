package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.JavaCommandLineBuilder;
import jetbrains.buildServer.agent.runner.JavaRunnerUtil;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.runner.JavaRunnerConstants;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by linfar on 4/3/14.
 */
public class SQRBuildService extends CommandLineBuildService {
    private static final Logger LOG = Logger.getLogger(SQRBuildService.class.getName());

    private static final String SQR_JAR_NAME = "sonar-runner-dist-2.3.jar";
    private static final String SQR_JAR_PATH = "sonar-qube-runner" + File.separatorChar + "lib";
    private PluginDescriptor myPluginDescriptor;

    public SQRBuildService(final @NotNull PluginDescriptor pluginDescriptor) {
        myPluginDescriptor = pluginDescriptor;
    }

    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        JavaCommandLineBuilder builder = new JavaCommandLineBuilder();
        builder.setJavaHome(getRunnerContext().getRunnerParameters().get(JavaRunnerConstants.TARGET_JDK_HOME));
        builder.setWorkingDir(getBuild().getCheckoutDirectory().getAbsolutePath());

        //builder.setSystemProperties(Map<String, String>);
        //builder.setEnvVariables(Map<String, String>);

        builder.setJvmArgs(JavaRunnerUtil.extractJvmArgs(getRunnerContext().getRunnerParameters()));
        builder.setClassPath(getClasspath());

        builder.setMainClass("org.sonar.runner.Main");
        builder.setProgramArgs(composeSQRArgs());
        builder.setWorkingDir(getRunnerContext().getWorkingDirectory().getAbsolutePath());

        final ProgramCommandLine cmd = builder.build();

        getLogger().message("Starting SQR");
        for (String str : cmd.getArguments()) {
            getLogger().message(str);
        }

        return cmd;
    }

    private List<String> composeSQRArgs() {
        LinkedList<String> res = new LinkedList<String>();
        res.add("-Dsonar.projectKey=sonar-plugin");
        res.add("-Dsonar.projectName=sonar-plugin");
        res.add("-Dsonar.projectVersion=1.0.1");
        res.add("-Dsonar.sources=src");
        res.add("-Dsonar.modules=sonar-plugin-agent,sonar-plugin-common,sonar-plugin-server");
        return res;
    }

    private String getClasspath() throws SQRJarException {
        File pluginJar = getSQRJar(myPluginDescriptor.getPluginRoot());
        return pluginJar.getAbsolutePath();
    }

    private File getSQRJar(File sqrRoot) throws SQRJarException {
        File pluginJar = new File(sqrRoot, SQR_JAR_PATH + File.separatorChar + SQR_JAR_NAME);
        if (!pluginJar.exists()) {
            throw new SQRJarException("SonarQube Runner jar doesn't exist on path: " + pluginJar.getAbsolutePath());
        } else if (!pluginJar.isFile()) {
            throw new SQRJarException("SonarQube Runner jar is not a file on path: " + pluginJar.getAbsolutePath());
        } else if (!pluginJar.canRead()) {
            throw new SQRJarException("Cannot read SonarQube Runner jar on path: " + pluginJar.getAbsolutePath());
        }
        return pluginJar;
    }
}
