package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;

import java.util.LinkedList;
import java.util.List;

public class SonarQubeServerPropertiesProcessorProviderImpl implements PropertiesProcessorProvider {
    @Override
    public PropertiesProcessor getRunnerPropertiesProcessor() {
        return properties -> {
            List<InvalidProperty> invalidProperties = new LinkedList<>();

            final String serverId = properties.get(Constants.SONAR_SERVER_ID);
            if (serverId == null) {
                invalidProperties.add(new InvalidProperty(Constants.SQS_CHOOSER, "Choose a SonarQube Server to send information to"));
            }

            return invalidProperties;
        };
    }
}
