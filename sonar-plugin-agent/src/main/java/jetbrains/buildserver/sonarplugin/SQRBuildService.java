package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.JavaCommandLineBuilder;
import jetbrains.buildServer.agent.runner.JavaRunnerUtil;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.runner.JavaRunnerConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * Created by linfar on 4/3/14.
 */
public class SQRBuildService extends CommandLineBuildService {
    private static final String SQR_JAR_NAME = "sonar-runner-dist-2.3.jar";
    private static final String SQR_JAR_PATH = "sonar-qube-runner" + File.separatorChar + "lib";

    @NotNull
    private final PluginDescriptor myPluginDescriptor;

    public SQRBuildService(final @NotNull PluginDescriptor pluginDescriptor) {
        myPluginDescriptor = pluginDescriptor;
    }

    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        JavaCommandLineBuilder builder = new JavaCommandLineBuilder();
        builder.setJavaHome(getRunnerContext().getRunnerParameters().get(JavaRunnerConstants.TARGET_JDK_HOME));
        builder.setWorkingDir(getBuild().getCheckoutDirectory().getAbsolutePath());

        builder.setSystemProperties(Collections.<String, String>emptyMap());
        builder.setEnvVariables(Collections.<String, String>emptyMap());

        builder.setJvmArgs(JavaRunnerUtil.extractJvmArgs(getRunnerContext().getRunnerParameters()));
        builder.setClassPath(getClasspath());

        builder.setMainClass("org.sonar.runner.Main");
        builder.setProgramArgs(composeSQRArgs(getRunnerContext().getRunnerParameters(), getBuild()));
        builder.setWorkingDir(getRunnerContext().getWorkingDirectory().getAbsolutePath());

        final ProgramCommandLine cmd = builder.build();

        getLogger().message("Starting SQR");
        for (String str : cmd.getArguments()) {
            getLogger().message(str);
        }

        return cmd;
    }

    private List<String> composeSQRArgs(@NotNull final Map<String, String> runnerParameters, @NotNull final AgentRunningBuild build) {
        List<String> res = new LinkedList<String>();
        SQRParametersAccessor accessor = new SQRParametersAccessor(runnerParameters);
        addSQRArg(res, "-Dsonar.projectKey", accessor.getProjectKey());
        addSQRArg(res, "-Dsonar.projectName", accessor.getProjectName());
        addSQRArg(res, "-Dsonar.projectVersion", accessor.getProjectVersion());
        addSQRArg(res, "-Dsonar.sources", accessor.getProjectSources());
        addSQRArg(res, "-Dsonar.modules", accessor.getProjectModules());
        final String additionalParameters = accessor.getAdditionalParameters();
        if (additionalParameters != null) {
            res.addAll(Arrays.asList(additionalParameters.split(" ")));
        }
        String jacocoExecFilePath = build.getSharedConfigParameters().get("teamcity.jacoco.coverage.datafile");
        final File file = new File(jacocoExecFilePath);
        if (file.exists() && file.isFile() && file.canRead()) {
            addSQRArg(res, "-Dsonar.jacoco.reportPath", jacocoExecFilePath);
        }
        return res;
    }

    private static void addSQRArg(@NotNull final List<String> argList, @NotNull final String key, @Nullable final String value) {
        if (value != null) {
            argList.add(key + "=" + value);
        }
    }

    @NotNull
    private String getClasspath() throws SQRJarException {
        File pluginJar = getSQRJar(myPluginDescriptor.getPluginRoot());
        return pluginJar.getAbsolutePath();
    }

    @NotNull
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
