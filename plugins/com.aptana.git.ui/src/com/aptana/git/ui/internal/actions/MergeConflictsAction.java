/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.git.ui.internal.actions;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.ui.history.FileRevisionTypedElement;
import org.eclipse.team.ui.synchronize.SaveableCompareEditorInput;

import com.aptana.git.core.GitPlugin;
import com.aptana.git.core.model.ChangedFile;
import com.aptana.git.core.model.GitCommit;
import com.aptana.git.core.model.GitRepository;
import com.aptana.git.ui.GitUIPlugin;
import com.aptana.git.ui.actions.GitAction;
import com.aptana.git.ui.internal.history.GitCompareFileRevisionEditorInput;

@SuppressWarnings("restriction")
public class MergeConflictsAction extends GitAction
{

	@Override
	public void run()
	{
		IResource[] resources = getSelectedResources();
		if (resources == null || resources.length != 1)
			return;
		IResource blah = resources[0];
		if (blah.getType() != IResource.FILE)
			return;
		GitRepository repo = getGitRepositoryManager().getAttached(blah.getProject());
		if (repo == null)
			return;
		String name = repo.getChangedFileForResource(blah).getPath();
		IFile file = (IFile) blah;
		try
		{
			IPath copyPath = file.getFullPath().addFileExtension("conflict");
			IFile copy = ResourcesPlugin.getWorkspace().getRoot().getFile(copyPath);
			if (!copy.exists())
			{
				// We create a copy of the failed merged file with the markers as generated by Git
				file.copy(copyPath, true, new NullProgressMonitor());
				// Then we replace the working file's contents by the contents pre-merge
				final IFileRevision baseFile = GitPlugin.revisionForCommit(new GitCommit(repo, ":2"), name);
				IStorage storage = baseFile.getStorage(new NullProgressMonitor());
				file.setContents(storage.getContents(), true, true, new NullProgressMonitor());
			}
		}
		catch (CoreException e)
		{
			GitUIPlugin.logError(e);
			return;
		}
		// Now we use the pre-merge file and compare against the merging version.
		ITypedElement base = SaveableCompareEditorInput.createFileElement(file);
		final IFileRevision nextFile = GitPlugin.revisionForCommit(new GitCommit(repo, ":3"), name);
		final ITypedElement next = new FileRevisionTypedElement(nextFile);
		final GitCompareFileRevisionEditorInput in = new GitCompareFileRevisionEditorInput(base, next, null);
		CompareUI.openCompareEditor(in);
	}

	@Override
	public boolean isEnabled()
	{
		IResource[] resources = getSelectedResources();
		if (resources == null || resources.length != 1)
			return false;
		IResource blah = resources[0];
		if (blah.getType() != IResource.FILE)
			return false;
		GitRepository repo = getGitRepositoryManager().getAttached(blah.getProject());
		if (repo == null)
			return false;
		ChangedFile file = repo.getChangedFileForResource(blah);
		if (file == null)
			return false;
		return file.hasUnmergedChanges();
	}
}
