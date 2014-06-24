package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.RunBuildException;

/**
 * Created by Andrey Titov on 4/3/14.
 *
 * Exception on SQR jar file - nonexistent/nonreadable/...
 */
public class SQRJarException extends RunBuildException {
    public SQRJarException(String s) {
        super(s);
    }
}
