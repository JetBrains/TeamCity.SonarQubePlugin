package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.JavaCommandLineBuilder;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.runner.JavaRunnerConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Created by linfar on 4/3/14.
 */
public class SQRBuildService extends CommandLineBuildService {
    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        JavaCommandLineBuilder builder = new JavaCommandLineBuilder();
        builder.setJavaHome(getRunnerContext().getRunnerParameters().get(JavaRunnerConstants.TARGET_JDK_HOME));
        builder.setWorkingDir(getBuild().getCheckoutDirectory().getAbsolutePath());

        //builder.setSystemProperties(Map<String, String>);
        //builder.setEnvVariables(Map<String, String>);

//        builder.setJvmArgs(JavaRunnerUtil.extractJvmArgs(getRunnerParameters()));
        builder.setClassPath(getClasspath());
        builder.setMainClass("org.sonar.runner.Main");
//        builder.setProgramArgs(Arrays.asList(getProgramParameters()));
//        builder.setWorkingDir(getWorkingDirectory().getAbsolutePath());

        return builder.build();
    }

    private String getClasspath() {
        return "";
    }
}
