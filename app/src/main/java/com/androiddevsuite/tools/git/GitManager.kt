/**
 * Android Development Suite - Git Manager
 * منصة تطوير أندرويد الشاملة
 * 
 * Git version control integration
 */
package com.androiddevsuite.tools.git

import android.content.Context
import com.androiddevsuite.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Git Manager - Handles all Git operations.
 */
@Singleton
class GitManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Initialize a new Git repository.
     */
    suspend fun initRepository(directory: File): Result<Git> = withContext(Dispatchers.IO) {
        try {
            val git = Git.init()
                .setDirectory(directory)
                .call()
            
            Timber.d("Initialized Git repository: ${directory.absolutePath}")
            Result.success(git)
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize repository")
            Result.failure(e)
        }
    }
    
    /**
     * Open existing repository.
     */
    suspend fun openRepository(directory: File): Result<Git> = withContext(Dispatchers.IO) {
        try {
            val repo = FileRepositoryBuilder()
                .findGitDir(directory)
                .build()
            
            val git = Git(repo)
            Result.success(git)
        } catch (e: Exception) {
            Timber.e(e, "Failed to open repository")
            Result.failure(e)
        }
    }
    
    /**
     * Clone a remote repository.
     */
    suspend fun cloneRepository(
        remoteUrl: String,
        localPath: File,
        username: String? = null,
        password: String? = null,
        branch: String? = null
    ): Result<Git> = withContext(Dispatchers.IO) {
        try {
            val cloneCommand = Git.cloneRepository()
                .setURI(remoteUrl)
                .setDirectory(localPath)
            
            if (branch != null) {
                cloneCommand.setBranch(branch)
            }
            
            if (username != null && password != null) {
                cloneCommand.setCredentialsProvider(
                    UsernamePasswordCredentialsProvider(username, password)
                )
            }
            
            val git = cloneCommand.call()
            
            Timber.d("Cloned repository: $remoteUrl -> ${localPath.absolutePath}")
            Result.success(git)
        } catch (e: Exception) {
            Timber.e(e, "Failed to clone repository: $remoteUrl")
            Result.failure(e)
        }
    }
    
    /**
     * Get repository status.
     */
    suspend fun getStatus(git: Git): Result<GitStatus> = withContext(Dispatchers.IO) {
        try {
            val status = git.status().call()
            
            val gitStatus = GitStatus(
                branch = git.repository.branch,
                ahead = 0, // Would need to compare with remote
                behind = 0,
                staged = status.added.map { it } + status.changed.map { it },
                unstaged = status.modified.map { it },
                untracked = status.untracked.map { it },
                conflicts = status.conflicting.map { it }
            )
            
            Result.success(gitStatus)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get status")
            Result.failure(e)
        }
    }
    
    /**
     * Add files to staging.
     */
    suspend fun add(git: Git, filePattern: String = "."): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            git.add()
                .addFilepattern(filePattern)
                .call()
            
            Timber.d("Added files: $filePattern")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to add files")
            Result.failure(e)
        }
    }
    
    /**
     * Commit changes.
     */
    suspend fun commit(
        git: Git,
        message: String,
        authorName: String? = null,
        authorEmail: String? = null
    ): Result<RevCommit> = withContext(Dispatchers.IO) {
        try {
            val commitCommand = git.commit().setMessage(message)
            
            if (authorName != null && authorEmail != null) {
                commitCommand.setAuthor(PersonIdent(authorName, authorEmail))
            }
            
            val commit = commitCommand.call()
            
            Timber.d("Committed: ${commit.id.name} - $message")
            Result.success(commit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to commit")
            Result.failure(e)
        }
    }
    
    /**
     * Push to remote.
     */
    suspend fun push(
        git: Git,
        username: String? = null,
        password: String? = null,
        remote: String = "origin"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val pushCommand = git.push().setRemote(remote)
            
            if (username != null && password != null) {
                pushCommand.setCredentialsProvider(
                    UsernamePasswordCredentialsProvider(username, password)
                )
            }
            
            pushCommand.call()
            
            Timber.d("Pushed to: $remote")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to push")
            Result.failure(e)
        }
    }
    
    /**
     * Pull from remote.
     */
    suspend fun pull(
        git: Git,
        username: String? = null,
        password: String? = null,
        remote: String = "origin"
    ): Result<MergeResult> = withContext(Dispatchers.IO) {
        try {
            val pullCommand = git.pull().setRemote(remote)
            
            if (username != null && password != null) {
                pullCommand.setCredentialsProvider(
                    UsernamePasswordCredentialsProvider(username, password)
                )
            }
            
            val result = pullCommand.call()
            
            Timber.d("Pulled from: $remote - ${result.mergeResult.mergeStatus}")
            Result.success(MergeResult(
                success = result.mergeResult.mergeStatus.isSuccessful,
                status = result.mergeResult.mergeStatus.name
            ))
        } catch (e: Exception) {
            Timber.e(e, "Failed to pull")
            Result.failure(e)
        }
    }
    
    /**
     * Create a new branch.
     */
    suspend fun createBranch(git: Git, branchName: String): Result<Ref> = withContext(Dispatchers.IO) {
        try {
            val ref = git.branchCreate()
                .setName(branchName)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                .call()
            
            Timber.d("Created branch: $branchName")
            Result.success(ref)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create branch: $branchName")
            Result.failure(e)
        }
    }
    
    /**
     * Checkout branch.
     */
    suspend fun checkout(git: Git, branchName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            git.checkout()
                .setName(branchName)
                .call()
            
            Timber.d("Checked out branch: $branchName")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to checkout branch: $branchName")
            Result.failure(e)
        }
    }
    
    /**
     * List all branches.
     */
    suspend fun listBranches(git: Git): Result<List<GitBranch>> = withContext(Dispatchers.IO) {
        try {
            val currentBranch = git.repository.branch
            val branches = git.branchList()
                .setListMode(ListBranchCommand.ListMode.ALL)
                .call()
                .map { ref ->
                    val name = ref.name.substringAfterLast('/')
                    GitBranch(
                        name = name,
                        isCurrent = name == currentBranch,
                        isRemote = ref.name.startsWith("refs/remotes/"),
                        trackingBranch = null,
                        lastCommitId = ref.objectId.name
                    )
                }
            
            Result.success(branches)
        } catch (e: Exception) {
            Timber.e(e, "Failed to list branches")
            Result.failure(e)
        }
    }
    
    /**
     * Get commit history.
     */
    suspend fun getCommitHistory(git: Git, maxCount: Int = 50): Result<List<GitCommit>> = withContext(Dispatchers.IO) {
        try {
            val commits = git.log()
                .setMaxCount(maxCount)
                .call()
                .map { commit ->
                    GitCommit(
                        id = commit.id.name,
                        message = commit.fullMessage,
                        author = commit.authorIdent.name,
                        email = commit.authorIdent.emailAddress,
                        timestamp = commit.commitTime.toLong() * 1000,
                        parentIds = commit.parents.map { it.id.name }
                    )
                }
            
            Result.success(commits)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get commit history")
            Result.failure(e)
        }
    }
    
    /**
     * Reset to commit.
     */
    suspend fun reset(git: Git, commitId: String, hard: Boolean = false): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val command = git.reset()
                .setRef(commitId)
            
            if (hard) {
                command.setMode(org.eclipse.jgit.api.ResetCommand.ResetType.HARD)
            } else {
                command.setMode(org.eclipse.jgit.api.ResetCommand.ResetType.SOFT)
            }
            
            command.call()
            
            Timber.d("Reset to: $commitId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to reset")
            Result.failure(e)
        }
    }
    
    /**
     * Add remote.
     */
    suspend fun addRemote(git: Git, name: String, url: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            git.remoteAdd()
                .setName(name)
                .setUri(java.net.URI(url))
                .call()
            
            Timber.d("Added remote: $name -> $url")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to add remote")
            Result.failure(e)
        }
    }
    
    /**
     * Stash changes.
     */
    suspend fun stash(git: Git, message: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val command = git.stashCreate()
            if (message != null) {
                command.setWorkingDirectoryMessage(message)
            }
            command.call()
            
            Timber.d("Stashed changes")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to stash")
            Result.failure(e)
        }
    }
    
    /**
     * Apply stash.
     */
    suspend fun stashPop(git: Git): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            git.stashApply().call()
            
            Timber.d("Applied stash")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to apply stash")
            Result.failure(e)
        }
    }
    
    /**
     * Close repository.
     */
    fun close(git: Git) {
        git.close()
        Timber.d("Closed Git repository")
    }
}

/**
 * Merge result.
 */
data class MergeResult(
    val success: Boolean,
    val status: String
)
