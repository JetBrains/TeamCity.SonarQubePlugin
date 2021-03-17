package jetbrains.buildserver.sonarplugin.util;

import java.util.Collections;
import java.util.List;

public final class Executable {
    public final String myExecutable;
    public final List<String> myArguments;

    public Executable(final String executable, final List<String> arguments) {
        myExecutable = executable;
        myArguments = Collections.unmodifiableList(arguments);
    }
}
