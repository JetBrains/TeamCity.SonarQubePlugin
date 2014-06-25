package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.RunBuildException;
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
 * Created by Andrey Titov on 4/3/14.
 *
 * SonarQube Runner wrapper process.
 */
public class SQRBuildService extends CommandLineBuildService {
    private static final String SQR_JAR_NAME = "sonar-runner-dist-2.3.jar";
    private static final String SQR_JAR_PATH = "sonar-qube-runner" + File.separatorChar + "lib";

    @NotNull
    private final PluginDescriptor myPluginDescriptor;
    @NotNull
    private final SonarProcessListener mySonarProcessListener;

    public SQRBuildService(@NotNull final PluginDescriptor pluginDescriptor,
                           @NotNull final SonarProcessListener sonarProcessListener) {
        myPluginDescriptor = pluginDescriptor;
        mySonarProcessListener = sonarProcessListener;
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
        builder.setProgramArgs(
                composeSQRArgs(
                        getRunnerContext().getRunnerParameters(),
                        getBuild().getSharedConfigParameters()
                ));
        builder.setWorkingDir(getRunnerContext().getWorkingDirectory().getAbsolutePath());

        final ProgramCommandLine cmd = builder.build();

        getLogger().message("Starting SQR");
        for (String str : cmd.getArguments()) {
            getLogger().message(str);
        }

        return cmd;
    }

    /**
     * Composes SonarQube Runner arguments.
     * @param runnerParameters Parameters to compose arguments from
     * @param sharedConfigParameters Shared config parameters to compose arguments from
     * @return List of arguments to be passed to the SQR
     */
    private List<String> composeSQRArgs(@NotNull final Map<String, String> runnerParameters,
                                        @NotNull final Map<String, String> sharedConfigParameters) {
        final List<String> res = new LinkedList<String>();
        SQRParametersAccessor accessor = new SQRParametersAccessor(runnerParameters);
        addSQRArg(res, "-Dsonar.host.url", accessor.getHostUrl());
        addSQRArg(res, "-Dsonar.jdbc.url", accessor.getJDBCUrl());
        addSQRArg(res, "-Dsonar.jdbc.username", accessor.getJDBCUsername());
        addSQRArg(res, "-Dsonar.jdbc.password", accessor.getJDBCPassword());
        addSQRArg(res, "-Dsonar.projectKey", accessor.getProjectKey());
        addSQRArg(res, "-Dsonar.projectName", accessor.getProjectName());
        addSQRArg(res, "-Dsonar.projectVersion", accessor.getProjectVersion());
        addSQRArg(res, "-Dsonar.sources", accessor.getProjectSources());
        addSQRArg(res, "-Dsonar.tests", accessor.getProjectTests());
        addSQRArg(res, "-Dsonar.binaries", accessor.getProjectBinaries());
        addSQRArg(res, "-Dsonar.modules", accessor.getProjectModules());
        final String additionalParameters = accessor.getAdditionalParameters();
        if (additionalParameters != null) {
            res.addAll(Arrays.asList(additionalParameters.split("\\n")));
        }

        final Set<String> collectedReports = mySonarProcessListener.getCollectedReports();
        if (!collectedReports.isEmpty() && (accessor.getAdditionalParameters() == null || !accessor.getAdditionalParameters().contains("-Dsonar.junit.reportsPath"))) {
            addSQRArg(res, "-Dsonar.dynamicAnalysis", "reuseReports");
            addSQRArg(res, "-Dsonar.junit.reportsPath", collectReportsPath(collectedReports, accessor.getProjectModules()));
        }

        final String jacocoExecFilePath = sharedConfigParameters.get("teamcity.jacoco.coverage.datafile");
        if (jacocoExecFilePath != null) {
            final File file = new File(jacocoExecFilePath);
            if (file.exists() && file.isFile() && file.canRead()) {
                addSQRArg(res, "-Dsonar.java.coveragePlugin", "jacoco");
                addSQRArg(res, "-Dsonar.jacoco.reportPath", jacocoExecFilePath);
            }
        }
        return res;
    }

    @Nullable
    private String collectReportsPath(Set<String> collectedReports, String projectModules) {
        StringBuilder sb = new StringBuilder();
        final String[] modules = projectModules != null ? projectModules.split(",") : new String[0];
        Set<String> filteredReports = new HashSet<String>();
        for (String report : collectedReports) {
            if (!new File(report).exists()) continue;
            for (String module : modules) {
                final int indexOf = report.indexOf(module);
                if (indexOf > 0) {
                    report = report.substring(indexOf + module.length() + 1);
                }
            }
            filteredReports.add(report);
        }

        for (String report : filteredReports) {
            sb.append(report).append(',');
            break; // At the moment sonar.junit.reportsPath doesn't accept several paths
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : null;
    }

    /**
     * Adds argument only if it's value is not null
     * @param argList Result list of arguments
     * @param key Argument key
     * @param value Argument value
     */
    protected static void addSQRArg(@NotNull final List<String> argList, @NotNull final String key, @Nullable final String value) {
        if (value != null) {
            argList.add(key + "=" + value);
        }
    }

    /**
     * @return Classpath for SonarQube Runner
     * @throws SQRJarException
     */
    @NotNull
    private String getClasspath() throws SQRJarException {
        File pluginJar = getSQRJar(myPluginDescriptor.getPluginRoot());
        return pluginJar.getAbsolutePath();
    }

    /**
     * @param sqrRoot SQR root directory
     * @return SonarQube Runner jar location
     * @throws SQRJarException
     */
    @NotNull
    private File getSQRJar(final @NotNull File sqrRoot) throws SQRJarException {
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
