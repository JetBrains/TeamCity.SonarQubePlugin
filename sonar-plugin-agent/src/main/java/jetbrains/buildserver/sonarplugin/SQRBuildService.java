package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor;
import jetbrains.buildServer.agent.runner.*;
import jetbrains.buildServer.runner.JavaRunnerConstants;
import jetbrains.buildServer.util.OSType;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildserver.sonarplugin.sqrunner.tool.SonarQubeScannerConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

import static jetbrains.buildServer.util.OSType.WINDOWS;

/**
 * Created by Andrey Titov on 4/3/14.
 *
 * SonarQube Runner wrapper process.
 */
public class SQRBuildService extends CommandLineBuildService {
    private static final String BUNDLED_SQR_RUNNER_PATH = "sonar-qube-runner";
    private static final String SQR_RUNNER_PATH_PROPERTY = "teamcity.tool.sonarquberunner";

    @NotNull
    private final PluginDescriptor myPluginDescriptor;
    @NotNull
    private final SonarProcessListener mySonarProcessListener;
    @NotNull
    private final OSType myOsType;
    @NotNull
    private final SQArgsComposer mySQArgsComposer;

    public SQRBuildService(@NotNull final PluginDescriptor pluginDescriptor,
                           @NotNull final SonarProcessListener sonarProcessListener,
                           @NotNull final OSType osType) {
        myPluginDescriptor = pluginDescriptor;
        mySonarProcessListener = sonarProcessListener;
        myOsType = osType;
        mySQArgsComposer = new SQScannerArgsComposer(osType);
    }

    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        final List<String> programArgs = composeSQRArgs(
                getRunnerContext().getRunnerParameters(),
                getBuild().getSharedConfigParameters()
        );

        final String jdkHome = getRunnerContext().getRunnerParameters().get(JavaRunnerConstants.TARGET_JDK_HOME);
        if (jdkHome != null) {
            getRunnerContext().addEnvironmentVariable("JAVA_HOME", jdkHome);
        }

        final String executablePath = getExecutablePath();
        final SimpleProgramCommandLine cmd = new SimpleProgramCommandLine(getRunnerContext(), executablePath, programArgs);

        getLogger().message("Starting SQR in " + executablePath);
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
        final Map<String, String> allParameters = new HashMap<String, String>(runnerParameters);
        allParameters.putAll(sharedConfigParameters);
        final SQRParametersAccessor accessor = new SQRParametersAccessor(allParameters);

        final List<String> res = mySQArgsComposer.composeArgs(accessor, new JavaSonarQubeKeysProvider());

        final Set<String> collectedReports = mySonarProcessListener.getCollectedReports();
        if (!collectedReports.isEmpty() && (accessor.getAdditionalParameters() == null || !accessor.getAdditionalParameters().contains("-Dsonar.junit.reportsPath"))) {
            addSQRArg(res, "-Dsonar.dynamicAnalysis", "reuseReports", myOsType);
            addSQRArg(res, "-Dsonar.junit.reportsPath", collectReportsPath(collectedReports, accessor.getProjectModules()), myOsType);
        }

        final String jacocoExecFilePath = sharedConfigParameters.get("teamcity.jacoco.coverage.datafile");
        if (jacocoExecFilePath != null) {
            final File file = new File(jacocoExecFilePath);
            if (file.exists() && file.isFile() && file.canRead()) {
                addSQRArg(res, "-Dsonar.java.coveragePlugin", "jacoco", myOsType);
                addSQRArg(res, "-Dsonar.jacoco.reportPath", jacocoExecFilePath, myOsType);
            }
        }

        return res;
    }

    protected static String getProjectKey(String projectKey) {
        if (!StringUtil.isEmpty(projectKey)) {
            projectKey = projectKey.replaceAll("[^\\w\\-.:]", "_");
        }
        return projectKey;
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
     * @param osType
     */
    protected static void addSQRArg(@NotNull final List<String> argList, @NotNull final String key, @Nullable final String value, @NotNull final OSType osType) {
        if (!Util.isEmpty(value)) {
            final String paramValue = key + "=" + value;
            argList.add(osType == WINDOWS ? StringUtil.doubleQuote(StringUtil.escapeQuotes(paramValue)) : paramValue);
        }
    }

    /**
     * @return Classpath for SonarQube Runner
     * @throws SQRJarException
     */
    @NotNull
    private String getClasspath() throws SQRJarException {
        final File pluginJar[] = getSQRJar(myPluginDescriptor.getPluginRoot());
        final StringBuilder builder = new StringBuilder();
        for (final File file : pluginJar) {
            builder.append(file.getAbsolutePath()).append(File.pathSeparatorChar);
        }
        return builder.substring(0, builder.length() - 1);
    }

    @NotNull
    private String getExecutablePath() throws RunBuildException {
        File sqrRoot = myPluginDescriptor.getPluginRoot();
        final String path = getRunnerContext().getConfigParameters().get(SQR_RUNNER_PATH_PROPERTY);
        File exec;

        String execName = myOsType == WINDOWS ? "sonar-runner.bat" : "sonar-runner";

        if (path != null) {
            exec = new File(path + File.separatorChar + "bin" + File.separatorChar + execName);
        } else {
            exec = new File(sqrRoot, BUNDLED_SQR_RUNNER_PATH + File.separatorChar + "bin" + File.separatorChar + execName);
        }
        if (!exec.exists()) {
            throw new RunBuildException("SonarQube executable doesn't exist: " + exec.getAbsolutePath());
        }
        if (!exec.isFile()) {
            throw new RunBuildException("SonarQube executable is not a file: " + exec.getAbsolutePath());
        }
        return exec.getAbsolutePath();
    }

    /**
     * @param sqrRoot SQR root directory
     * @return SonarQube Runner jar location
     * @throws SQRJarException
     */
    @NotNull
    private File[] getSQRJar(@NotNull final File sqrRoot) throws SQRJarException {
        final String path = getRunnerContext().getConfigParameters().get(SQR_RUNNER_PATH_PROPERTY);
        File baseDir;
        if (path != null) {
            baseDir = new File(path);
        } else {
            baseDir = new File(sqrRoot, BUNDLED_SQR_RUNNER_PATH);
        }
        final File libPath = new File(baseDir, "lib");
        if (!libPath.exists()) {
            throw new SQRJarException("SonarQube Runner lib path doesn't exist: " + libPath.getAbsolutePath());
        } else if (!libPath.isDirectory()) {
            throw new SQRJarException("SonarQube Runner lib path is not a directory: " + libPath.getAbsolutePath());
        } else if (!libPath.canRead()) {
            throw new SQRJarException("Cannot read SonarQube Runner lib path: " + libPath.getAbsolutePath());
        }
        final File[] jars = libPath.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith("jar");
            }
        });
        if (jars.length == 0) {
            throw new SQRJarException("No JAR files found in lib path: " + libPath);
        }
        return jars;
    }
}
