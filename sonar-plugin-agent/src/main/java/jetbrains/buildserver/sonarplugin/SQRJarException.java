package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.RunBuildException;

/**
 * Created by linfar on 4/3/14.
 */
public class SQRJarException extends RunBuildException {
    public SQRJarException(String s) {
        super(s);
    }
}
