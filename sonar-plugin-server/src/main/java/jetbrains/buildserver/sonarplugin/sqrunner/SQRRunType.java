package jetbrains.buildserver.sonarplugin.sqrunner;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildserver.sonarplugin.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created by linfar on 4/2/14.
 *
 * RunType for SonarQubeRunner - cmd tool collecting and pushing data for SonarQube Server
 */
public class SQRRunType extends RunType {

    public static final String EDIT_SQRRUN_PARAMS_JSP_PATH = "editSQRRunParams.jsp";
    public static final String VIEW_SQRRUN_PARAMS_JSP_PATH = "viewSQRRunParams.jsp";

    @NotNull
    @Override
    public String getType() {
        return Constants.RUNNER_TYPE;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "SonarQube Runner";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Runner for SonarQube Runner";
    }

    @Nullable
    @Override
    public PropertiesProcessor getRunnerPropertiesProcessor() {
        return null;
    }

    @Nullable
    @Override
    public String getEditRunnerParamsJspFilePath() {
        return EDIT_SQRRUN_PARAMS_JSP_PATH;
    }

    @Nullable
    @Override
    public String getViewRunnerParamsJspFilePath() {
        return VIEW_SQRRUN_PARAMS_JSP_PATH;
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultRunnerProperties() {
        return null;
    }
}
