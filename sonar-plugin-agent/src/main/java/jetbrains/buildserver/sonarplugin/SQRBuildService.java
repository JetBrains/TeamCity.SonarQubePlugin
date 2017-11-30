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
    @NotNull
    private final SonarProcessListener mySonarProcessListener;
    @NotNull
    private final OSType myOsType;

    public SQRBuildService(@NotNull final SonarProcessListener sonarProcessListener,
                           @NotNull final OSType osType) {
        mySonarProcessListener = sonarProcessListener;
        myOsType = osType;
    }

    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        final String jdkHome = getRunnerContext().getRunnerParameters().get(JavaRunnerConstants.TARGET_JDK_HOME);
        if (jdkHome != null) {
            getRunnerContext().addEnvironmentVariable("JAVA_HOME", jdkHome);
        }

        final String sonarScannerRoot = getSonarScannerRoot();
        if (sonarScannerRoot == null) {
            throw new RunBuildException("No SonarQube Scanner selected");
        }
        final List<String> programArgs = composeSQRArgs(getRunnerContext().getRunnerParameters(), getBuild().getSharedConfigParameters(), sonarScannerRoot);

        final boolean useScanner = isUseScannerMain(sonarScannerRoot);
        final JavaCommandLineBuilder builder = new JavaCommandLineBuilder();

        final ProgramCommandLine build = builder.withClassPath(getClasspath())
                .withMainClass(getMainClass(useScanner))
                .withJavaHome(jdkHome)
                .withBaseDir(getBuild().getCheckoutDirectory().getAbsolutePath())
                .withEnvVariables(getRunnerContext().getBuildParameters().getEnvironmentVariables())
                .withJvmArgs(JavaRunnerUtil.extractJvmArgs(getRunnerContext().getRunnerParameters()))
                .withClassPath(getClasspath())
                .withProgramArgs(programArgs)
                .withWorkingDir(getRunnerContext().getWorkingDirectory().getAbsolutePath()).build();

        getLogger().message("Starting SQS from " + sonarScannerRoot);
        for (String str : build.getArguments()) {
            getLogger().message(str);
        }

        return build;
    }

    @NotNull
    private String getMainClass(boolean useScanner) {
        return useScanner ? "org.sonarsource.scanner.cli.Main" : "org.sonar.runner.Main";
    }

    private boolean isUseScannerMain(String sonarScannerRoot) {
        return sonarScannerRoot.endsWith("scanner");
    }

    /**
     * Composes SonarQube Runner arguments.
     * @param runnerParameters Parameters to compose arguments from
     * @param sharedConfigParameters Shared config parameters to compose arguments from
     * @param sonarScannerRoot
     * @return List of arguments to be passed to the SQR
     */
    private List<String> composeSQRArgs(@NotNull final Map<String, String> runnerParameters,
                                        @NotNull final Map<String, String> sharedConfigParameters,
                                        @NotNull final String sonarScannerRoot) {
        final List<String> res = new LinkedList<String>();
        final Map<String, String> allParameters = new HashMap<String, String>(runnerParameters);
        allParameters.putAll(sharedConfigParameters);
        final SQRParametersAccessor accessor = new SQRParametersAccessor(allParameters);
        addSQRArg(res, "-Dproject.home", ".", myOsType);
        addSQRArg(res, "-Dsonar.host.url", accessor.getHostUrl(), myOsType);
        addSQRArg(res, "-Dsonar.jdbc.url", accessor.getJDBCUrl(), myOsType);
        addSQRArg(res, "-Dsonar.jdbc.username", accessor.getJDBCUsername(), myOsType);
        addSQRArg(res, "-Dsonar.jdbc.password", accessor.getJDBCPassword(), myOsType);
        addSQRArg(res, "-Dsonar.projectKey", getProjectKey(accessor.getProjectKey()), myOsType);
        addSQRArg(res, "-Dsonar.projectName", accessor.getProjectName(), myOsType);
        addSQRArg(res, "-Dsonar.projectVersion", accessor.getProjectVersion(), myOsType);
        addSQRArg(res, "-Dsonar.sources", accessor.getProjectSources(), myOsType);
        addSQRArg(res, "-Dsonar.tests", accessor.getProjectTests(), myOsType);
        addSQRArg(res, "-Dsonar.binaries", accessor.getProjectBinaries(), myOsType);
        addSQRArg(res, "-Dsonar.java.binaries", accessor.getProjectBinaries(), myOsType);
        addSQRArg(res, "-Dsonar.modules", accessor.getProjectModules(), myOsType);
        addSQRArg(res, "-Dsonar.password", accessor.getPassword(), myOsType);
        addSQRArg(res, "-Dsonar.login", accessor.getLogin(), myOsType);
        res.add("-Dscanner.home=" + sonarScannerRoot);
        final String additionalParameters = accessor.getAdditionalParameters();
        if (additionalParameters != null) {
            res.addAll(Arrays.asList(additionalParameters.split("\\n")));
        }

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
        final File pluginJar[] = getSQRJar(new File(getSonarScannerRoot()));
        final StringBuilder builder = new StringBuilder();
        for (final File file : pluginJar) {
            builder.append(file.getAbsolutePath()).append(File.pathSeparatorChar);
        }
        return builder.substring(0, builder.length() - 1);
    }

    @NotNull
    private String getExecutablePath() throws RunBuildException {
        final String path = getSonarScannerRoot();

        final String execName = myOsType == WINDOWS ? "sonar-runner.bat" : "sonar-runner";

        final File exec = new File(path + File.separatorChar + "bin" + File.separatorChar + execName);

        if (!exec.exists()) {
            throw new RunBuildException("SonarQube executable doesn't exist: " + exec.getAbsolutePath());
        }
        if (!exec.isFile()) {
            throw new RunBuildException("SonarQube executable is not a file: " + exec.getAbsolutePath());
        }
        return exec.getAbsolutePath();
    }

    private String getSonarScannerRoot() {
        final String explicitPath = getRunnerContext().getConfigParameters().get(SonarQubeScannerConstants.SONAR_QUBE_SCANNER_VERSION_PARAMETER);

        return explicitPath != null ? explicitPath : getRunnerContext().getRunnerParameters().get(SonarQubeScannerConstants.SONAR_QUBE_SCANNER_VERSION_PARAMETER);
    }

    /**
     * @param sqrRoot SQR root directory
     * @return SonarQube Runner jar location
     * @throws SQRJarException
     */
    @NotNull
    private File[] getSQRJar(@NotNull final File sqrRoot) throws SQRJarException {
        final File libPath = new File(sqrRoot, "lib");
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
