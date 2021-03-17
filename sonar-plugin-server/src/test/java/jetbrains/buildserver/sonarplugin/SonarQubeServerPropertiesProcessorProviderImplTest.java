package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.BDDAssertions.then;

public class SonarQubeServerPropertiesProcessorProviderImplTest {
    @Test
    public void testProcessor() {
        final PropertiesProcessor processor = new SonarQubeServerPropertiesProcessorProviderImpl().getRunnerPropertiesProcessor();

        {
            final Collection<InvalidProperty> process = processor.process(Collections.emptyMap());
            then(process).hasSize(1);
            then(process.iterator().next().getPropertyName()).isEqualTo(Constants.SQS_CHOOSER);
        }

        {
            final Collection<InvalidProperty> process = processor.process(Collections.singletonMap(Constants.SONAR_SERVER_ID, ""));
            then(process).isEmpty();
        }
    }
}