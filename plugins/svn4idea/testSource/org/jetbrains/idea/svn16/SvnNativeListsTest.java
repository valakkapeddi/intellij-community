/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.svn16;

import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.svn.SvnChangeProvider;
import org.jetbrains.idea.svn.SvnChangelistListener;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;

import java.util.Collection;
import java.util.List;

public class SvnNativeListsTest extends Svn16TestCase {
  private ChangeListManager myChangeListManager;
  private VcsDirtyScopeManager myDirtyScopeManager;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myChangeListManager = ChangeListManager.getInstance(myProject);
    myDirtyScopeManager = VcsDirtyScopeManager.getInstance(myProject);
  }

  @Override
  public void tearDown() throws Exception {
    final List<LocalChangeList> changeListList = myChangeListManager.getChangeLists();
    for (LocalChangeList list : changeListList) {
      if (SvnChangeProvider.ourDefaultListName.equals(list.getName())) continue;
      final Collection<Change> changes = list.getChanges();
      for (Change change : changes) {
        clearListForRevision(change.getBeforeRevision());
        clearListForRevision(change.getAfterRevision());
      }
    }

    super.tearDown();
  }

  private void clearListForRevision(final ContentRevision revision) throws SVNException {
    if (revision == null) return;
    SvnChangelistListener.removeFromList(myProject, revision.getFile().getIOFile());
  }

  @Test
  public void testAdd() throws Throwable {
    final LocalChangeList newL = myChangeListManager.addChangeList("newOne", null);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    myChangeListManager.setDefaultChangeList(newL);

    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    final VirtualFile file = createFileInCommand("a.txt", "old content");

    ensureAddedToNativeList();

    verify(runSvn("status"), "", "--- Changelist 'newOne':", "A a.txt");
  }

  private void ensureAddedToNativeList() {
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);  // first time new changes are detected and added to _IDEA_ changeslist
    myChangeListManager.ensureUpToDate(false);  // and on the same thread a request is put for files addition;
    // so stay here for 2nd cycle and wait for native addition completion
  }

  @Test
  public void testDeleted() throws Throwable {
    final LocalChangeList newL = myChangeListManager.addChangeList("newOne", null);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    myChangeListManager.setDefaultChangeList(newL);

    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    final VirtualFile file = createFileInCommand("a.txt", "old content");
    checkin();
    deleteFileInCommand(file);

    ensureAddedToNativeList();

    verify(runSvn("status"), "", "--- Changelist 'newOne':", "D a.txt");
  }

  @Test
  public void testEdit() throws Throwable {
    final LocalChangeList newL = myChangeListManager.addChangeList("newOne", null);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    myChangeListManager.setDefaultChangeList(newL);

    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    final VirtualFile file = createFileInCommand("a.txt", "old content");
    checkin();
    editFileInCommand(myProject, file, "111");

    ensureAddedToNativeList();

    verify(runSvn("status"), "", "--- Changelist 'newOne':", "M a.txt");
  }

  @Test
  public void testEditAndMove() throws Throwable {
    final LocalChangeList newL = myChangeListManager.addChangeList("newOne", null);

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    myChangeListManager.setDefaultChangeList(newL);

    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    final VirtualFile file = createFileInCommand("a.txt", "old content");
    checkin();
    editFileInCommand(myProject, file, "111");

    ensureAddedToNativeList();

    verify(runSvn("status"), "", "--- Changelist 'newOne':", "M a.txt");

    renameFileInCommand(file, "b.txt");
    /*A  +    b.txt

    --- Changelist 'newOne':
    D       a.txt
    */
    verify(runSvn("status"), "", "--- Changelist 'newOne':", "A + b.txt", "D a.txt");

    ensureAddedToNativeList();

    verify(runSvn("status"), "", "--- Changelist 'newOne':", "A + b.txt", "D a.txt");
  }

  @Test
  public void testMove() throws Throwable {
    final LocalChangeList newL = myChangeListManager.addChangeList("newOne", null);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    myChangeListManager.setDefaultChangeList(newL);

    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    final VirtualFile file = createFileInCommand("a.txt", "old content");
    checkin();

    renameFileInCommand(file, "b.txt");
    ensureAddedToNativeList();

    verify(runSvn("status"), "", "--- Changelist 'newOne':", "A + b.txt", "D a.txt");
  }

  @Test
  public void testMoveMove() throws Throwable {
    final LocalChangeList newL = myChangeListManager.addChangeList("newOne", null);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    myChangeListManager.setDefaultChangeList(newL);

    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    final VirtualFile file = createFileInCommand("a.txt", "old content");
    checkin();

    renameFileInCommand(file, "b.txt");
    ensureAddedToNativeList();

    verify(runSvn("status"), "", "--- Changelist 'newOne':", "A + b.txt", "D a.txt");

    renameFileInCommand(file, "c.txt");
    verify(runSvn("status"), "", "--- Changelist 'newOne':", "A + c.txt", "D a.txt");

    ensureAddedToNativeList();

    verify(runSvn("status"), "", "--- Changelist 'newOne':", "A + c.txt", "D a.txt");
  }
}
