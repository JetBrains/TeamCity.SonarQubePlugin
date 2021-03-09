package jetbrains.buildserver.sonarplugin;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.testng.annotations.DataProvider;

public class DataProviders {
    @DataProvider
    private static Object[][] getFileSystemAndRoot() {
        return new Object[][]{
                {Jimfs.newFileSystem(Configuration.windows()), "C:\\"},
                {Jimfs.newFileSystem(Configuration.unix()), "/"}
//                , {Jimfs.newFileSystem(Configuration.osX()), "/"}
        };
    }
}
