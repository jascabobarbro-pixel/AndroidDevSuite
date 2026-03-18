/**
 * Android Development Suite - Git Manager
 * منصة تطوير أندرويد الشاملة
 * 
 * Git version control management using JGit
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.git

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Status
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import timber.log.Timber
import java.io.File
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Git status data.
 */
data class GitStatus(
    val branch: String,
    val remote: String?,
    val ahead: Int,
    val behind: Int,
    val untracked: List<String>,
    val uncommitted: List<String>,
    val staged: List<String>,
    val conflicts: List<String>,
    val hasChanges: Boolean
)

/**
 * Git commit data.
 */
data class GitCommit(
    val hash: String,
    val shortHash: String,
    val message: String,
    val author: String,
    val email: String,
    val date: Date,
    val parents: List<String>
)

/**
 * Git branch data.
 */
data class GitBranch(
    val name: String,
    val isCurrent: Boolean,
    val isRemote: Boolean,
    val trackingBranch: String?
)

/**
 * Git operation result.
 */
sealed class GitResult<out T> {
    data class Success<T>(val data: T) : GitResult<T>()
    data class Error(val message: String, val exception: Exception? = null) : GitResult<Nothing>()
}

/**
 * Git Manager - Singleton for Git operations.
 * 
 * Features:
 * - Repository initialization
 * - Commit, push, pull operations
 * - Branch management
 * - Merge and rebase
 * - Conflict resolution
 * - Credential management
 */
@Singleton
class GitManager @Inject constructor(
    private val context: Context
) {
    private var git: Git? = null
    private var repository: Repository? = null
    
    private val _currentStatus = MutableStateFlow<GitStatus?>(null)
    val currentStatus: StateFlow<GitStatus?> = _currentStatus.asStateFlow()
    
    private var credentialsProvider: CredentialsProvider? = null
    
    /**
     * Initialize a new Git repository.
     */
    suspend fun initRepository(projectPath: File): GitResult<Repository> = withContext(Dispatchers.IO) {
        try {
            val gitDir = File(projectPath, ".git")
            
            if (gitDir.exists()) {
                // Open existing repository
                openRepository(projectPath)
            } else {
                // Create new repository
                git = Git.init()
                    .setDirectory(projectPath)
                    .setInitialBranch("main")
                    .call()
                
                repository = git?.repository
                Timber.i("Git repository initialized at: $projectPath")
                GitResult.Success(repository!!)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Git repository")
            GitResult.Error("Failed to initialize Git repository: ${e.message}", e)
        }
    }
    
    /**
     * Open an existing Git repository.
     */
    suspend fun openRepository(projectPath: File): GitResult<Repository> = withContext(Dispatchers.IO) {
        try {
            val repositoryBuilder = FileRepositoryBuilder()
            repository = repositoryBuilder
                .setGitDir(File(projectPath, ".git"))
                .readEnvironment()
                .findGitDir(projectPath)
                .build()
            
            git = Git(repository)
            Timber.i("Git repository opened at: $projectPath")
            GitResult.Success(repository!!)
        } catch (e: Exception) {
            Timber.e(e, "Failed to open Git repository")
            GitResult.Error("Failed to open Git repository: ${e.message}", e)
        }
    }
    
    /**
     * Close the current repository.
     */
    fun closeRepository() {
        git?.close()
        repository?.close()
        git = null
        repository = null
        _currentStatus.value = null
        Timber.d("Git repository closed")
    }
    
    /**
     * Clone a remote repository.
     */
    suspend fun cloneRepository(
        remoteUrl: String,
        localPath: File,
        credentials: Pair<String, String>? = null
    ): GitResult<Repository> = withContext(Dispatchers.IO) {
        try {
            val cloneCommand = Git.cloneRepository()
                .setURI(remoteUrl)
                .setDirectory(localPath)
            
            credentials?.let { (username, password) ->
                credentialsProvider = UsernamePasswordCredentialsProvider(username, password)
                cloneCommand.setCredentialsProvider(credentialsProvider)
            }
            
            git = cloneCommand.call()
            repository = git?.repository
            
            Timber.i("Repository cloned from: $remoteUrl")
            GitResult.Success(repository!!)
        } catch (e: Exception) {
            Timber.e(e, "Failed to clone repository")
            GitResult.Error("Failed to clone repository: ${e.message}", e)
        }
    }
    
    /**
     * Set credentials for remote operations.
     */
    fun setCredentials(username: String, password: String) {
        credentialsProvider = UsernamePasswordCredentialsProvider(username, password)
    }
    
    /**
     * Set credentials using personal access token.
     */
    fun setTokenAuth(token: String) {
        credentialsProvider = UsernamePasswordCredentialsProvider(token, "")
    }
    
    /**
     * Stage files for commit.
     */
    suspend fun stage(vararg filePatterns: String): GitResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val addCommand = git?.add() ?: return@withContext GitResult.Error("No repository open")
            
            filePatterns.forEach { pattern ->
                addCommand.addFilepattern(pattern)
            }
            addCommand.call()
            
            Timber.d("Staged files: ${filePatterns.joinToString()}")
            GitResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to stage files")
            GitResult.Error("Failed to stage files: ${e.message}", e)
        }
    }
    
    /**
     * Stage all changes.
     */
    suspend fun stageAll(): GitResult<Unit> = withContext(Dispatchers.IO) {
        return stage(".")
    }
    
    /**
     * Unstage files.
     */
    suspend fun unstage(vararg filePatterns: String): GitResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val resetCommand = git?.reset() ?: return@withContext GitResult.Error("No repository open")
            
            filePatterns.forEach { pattern ->
                resetCommand.addPath(pattern)
            }
            resetCommand.call()
            
            Timber.d("Unstaged files: ${filePatterns.joinToString()}")
            GitResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to unstage files")
            GitResult.Error("Failed to unstage files: ${e.message}", e)
        }
    }
    
    /**
     * Commit staged changes.
     */
    suspend fun commit(
        message: String,
        authorName: String? = null,
        authorEmail: String? = null
    ): GitResult<RevCommit> = withContext(Dispatchers.IO) {
        try {
            val commitCommand = git?.commit() ?: return@withContext GitResult.Error("No repository open")
            
            commitCommand.message = message
            
            if (authorName != null && authorEmail != null) {
                commitCommand.author = PersonIdent(authorName, authorEmail)
                commitCommand.committer = PersonIdent(authorName, authorEmail)
            }
            
            val commit = commitCommand.call()
            Timber.i("Committed: ${commit.shortMessage}")
            GitResult.Success(commit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to commit")
            GitResult.Error("Failed to commit: ${e.message}", e)
        }
    }
    
    /**
     * Push to remote.
     */
    suspend fun push(
        remote: String = "origin",
        branch: String? = null
    ): GitResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val pushCommand = git?.push() ?: return@withContext GitResult.Error("No repository open")
            
            pushCommand.remote = remote
            credentialsProvider?.let { pushCommand.setCredentialsProvider(it) }
            
            branch?.let { pushCommand.add(it) }
            
            pushCommand.call()
            Timber.i("Pushed to $remote")
            GitResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to push")
            GitResult.Error("Failed to push: ${e.message}", e)
        }
    }
    
    /**
     * Pull from remote.
     */
    suspend fun pull(
        remote: String = "origin",
        branch: String? = null
    ): GitResult<MergeResult> = withContext(Dispatchers.IO) {
        try {
            val pullCommand = git?.pull() ?: return@withContext GitResult.Error("No repository open")
            
            pullCommand.remote = remote
            credentialsProvider?.let { pullCommand.setCredentialsProvider(it) }
            
            branch?.let { pullCommand.remoteBranchName = it }
            
            val result = pullCommand.call()
            
            val mergeResult = MergeResult(
                success = result.isSuccessful,
                conflicts = if (result.mergeResult?.mergeStatus?.isSuccessful == false) {
                    result.mergeResult?.conflicts?.keys?.toList() ?: emptyList()
                } else emptyList(),
                message = result.mergeResult?.mergeStatus?.description ?: ""
            )
            
            Timber.i("Pulled from $remote: ${mergeResult.message}")
            GitResult.Success(mergeResult)
        } catch (e: Exception) {
            Timber.e(e, "Failed to pull")
            GitResult.Error("Failed to pull: ${e.message}", e)
        }
    }
    
    /**
     * Fetch from remote.
     */
    suspend fun fetch(
        remote: String = "origin"
    ): GitResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val fetchCommand = git?.fetch() ?: return@withContext GitResult.Error("No repository open")
            
            fetchCommand.remote = remote
            credentialsProvider?.let { fetchCommand.setCredentialsProvider(it) }
            
            fetchCommand.call()
            Timber.i("Fetched from $remote")
            GitResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch")
            GitResult.Error("Failed to fetch: ${e.message}", e)
        }
    }
    
    /**
     * Get current status.
     */
    suspend fun getStatus(): GitResult<GitStatus> = withContext(Dispatchers.IO) {
        try {
            val git = git ?: return@withContext GitResult.Error("No repository open")
            
            val status: Status = git.status().call()
            val branch = repository?.branch ?: "unknown"
            val remote = repository?.config?.getString("branch", branch, "remote")
            
            val gitStatus = GitStatus(
                branch = branch,
                remote = remote,
                ahead = 0, // Calculate from tracking info
                behind = 0,
                untracked = status.untracked.toList(),
                uncommitted = status.uncommittedChanges.toList(),
                staged = status.added.toList() + status.changed.toList(),
                conflicts = status.conflicting.toList(),
                hasChanges = !status.isClean
            )
            
            _currentStatus.value = gitStatus
            GitResult.Success(gitStatus)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get status")
            GitResult.Error("Failed to get status: ${e.message}", e)
        }
    }
    
    /**
     * Get commit history.
     */
    suspend fun getLog(limit: Int = 50): GitResult<List<GitCommit>> = withContext(Dispatchers.IO) {
        try {
            val git = git ?: return@withContext GitResult.Error("No repository open")
            
            val commits = git.log()
                .setMaxCount(limit)
                .call()
                .map { commit ->
                    GitCommit(
                        hash = commit.id.name,
                        shortHash = commit.id.abbreviate(7).name(),
                        message = commit.fullMessage,
                        author = commit.authorIdent.name,
                        email = commit.authorIdent.emailAddress,
                        date = Date(commit.commitTime * 1000L),
                        parents = commit.parents.map { it.id.name }
                    )
                }
            
            GitResult.Success(commits)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get log")
            GitResult.Error("Failed to get log: ${e.message}", e)
        }
    }
    
    /**
     * Get all branches.
     */
    suspend fun getBranches(): GitResult<List<GitBranch>> = withContext(Dispatchers.IO) {
        try {
            val git = git ?: return@withContext GitResult.Error("No repository open")
            val currentBranch = repository?.branch ?: ""
            
            val branches = git.branchList()
                .call()
                .map { ref ->
                    GitBranch(
                        name = ref.name.removePrefix("refs/heads/").removePrefix("refs/remotes/"),
                        isCurrent = ref.name.contains(currentBranch),
                        isRemote = ref.name.startsWith("refs/remotes/"),
                        trackingBranch = null
                    )
                }
            
            GitResult.Success(branches)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get branches")
            GitResult.Error("Failed to get branches: ${e.message}", e)
        }
    }
    
    /**
     * Create a new branch.
     */
    suspend fun createBranch(name: String, checkout: Boolean = true): GitResult<String> = withContext(Dispatchers.IO) {
        try {
            val git = git ?: return@withContext GitResult.Error("No repository open")
            
            val ref = git.branchCreate()
                .setName(name)
                .call()
            
            if (checkout) {
                git.checkout()
                    .setName(ref.name)
                    .call()
            }
            
            Timber.i("Created branch: $name")
            GitResult.Success(ref.name)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create branch")
            GitResult.Error("Failed to create branch: ${e.message}", e)
        }
    }
    
    /**
     * Checkout a branch.
     */
    suspend fun checkout(branchName: String): GitResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val git = git ?: return@withContext GitResult.Error("No repository open")
            
            git.checkout()
                .setName(branchName)
                .call()
            
            Timber.i("Checked out branch: $branchName")
            GitResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to checkout branch")
            GitResult.Error("Failed to checkout branch: ${e.message}", e)
        }
    }
    
    /**
     * Discard changes in working directory.
     */
    suspend fun discardChanges(vararg filePatterns: String): GitResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val git = git ?: return@withContext GitResult.Error("No repository open")
            
            val checkoutCommand = git.checkout()
            filePatterns.forEach { pattern ->
                checkoutCommand.addPath(pattern)
            }
            checkoutCommand.call()
            
            Timber.i("Discarded changes in: ${filePatterns.joinToString()}")
            GitResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to discard changes")
            GitResult.Error("Failed to discard changes: ${e.message}", e)
        }
    }
    
    /**
     * Add a remote.
     */
    suspend fun addRemote(name: String, url: String): GitResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val git = git ?: return@withContext GitResult.Error("No repository open")
            
            git.remoteAdd()
                .setName(name)
                .setUri(java.net.URI(url))
                .call()
            
            Timber.i("Added remote: $name -> $url")
            GitResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to add remote")
            GitResult.Error("Failed to add remote: ${e.message}", e)
        }
    }
}

/**
 * Merge result data.
 */
data class MergeResult(
    val success: Boolean,
    val conflicts: List<String>,
    val message: String
)
