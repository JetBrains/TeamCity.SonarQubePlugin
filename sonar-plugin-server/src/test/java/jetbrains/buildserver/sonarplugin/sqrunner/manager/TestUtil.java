package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildServer.serverSide.SProject;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by linfar on 05.10.16.
 */
public final class TestUtil {
    private TestUtil() {}

    public static Projects createProjects(@NotNull final String rootId, @NotNull final String projectId) {
        final SProject root = mock(SProject.class);
        when(root.getProjectId()).thenReturn(rootId);
        when(root.getParentProject()).thenReturn(null);

        final SProject project = mock(SProject.class);
        when(project.getProjectId()).thenReturn(projectId);
        when(project.getParentProject()).thenReturn(root);

        when(root.getProjects()).thenReturn(Collections.singletonList(project));
        when(root.getOwnProjects()).thenReturn(Collections.singletonList(project));

        return new Projects(root, project);
    }

    public static class Projects {
        @NotNull
        public final SProject myRoot;
        @NotNull
        public final SProject myProject;

        public Projects(@NotNull final SProject root, @NotNull final SProject project) {
            myRoot = root;
            myProject = project;
        }
    }
}
