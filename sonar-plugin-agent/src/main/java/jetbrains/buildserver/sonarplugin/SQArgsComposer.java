package jetbrains.buildserver.sonarplugin;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SQArgsComposer {
    List<String> composeArgs(@NotNull final SQRParametersAccessor accessor,
                             @NotNull final SonarQubeKeysProvider sonarQubeKeysProvider);
}
