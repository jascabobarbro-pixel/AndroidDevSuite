/**
 * Android Development Suite - GitHub API Client
 * منصة تطوير أندرويد الشاملة
 * 
 * GitHub API integration for:
 * - Repository browsing
 * - File downloading
 * - Gist management
 * - User authentication
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.data.remote.github

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// =====================================================
// DATA MODELS
// =====================================================

/**
 * GitHub User model.
 */
@JsonClass(generateAdapter = true)
data class GitHubUser(
    @Json(name = "id") val id: Long,
    @Json(name = "login") val login: String,
    @Json(name = "avatar_url") val avatarUrl: String,
    @Json(name = "html_url") val htmlUrl: String,
    @Json(name = "name") val name: String?,
    @Json(name = "email") val email: String?,
    @Json(name = "bio") val bio: String?,
    @Json(name = "public_repos") val publicRepos: Int,
    @Json(name = "followers") val followers: Int,
    @Json(name = "following") val following: Int
)

/**
 * GitHub Repository model.
 */
@JsonClass(generateAdapter = true)
data class GitHubRepository(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "description") val description: String?,
    @Json(name = "html_url") val htmlUrl: String,
    @Json(name = "clone_url") val cloneUrl: String,
    @Json(name = "ssh_url") val sshUrl: String,
    @Json(name = "language") val language: String?,
    @Json(name = "stargazers_count") val stars: Int,
    @Json(name = "forks_count") val forks: Int,
    @Json(name = "open_issues_count") val openIssues: Int,
    @Json(name = "default_branch") val defaultBranch: String,
    @Json(name = "owner") val owner: GitHubUser,
    @Json(name = "private") val isPrivate: Boolean,
    @Json(name = "fork") val isFork: Boolean
)

/**
 * GitHub Content (File/Directory) model.
 */
@JsonClass(generateAdapter = true)
data class GitHubContent(
    @Json(name = "name") val name: String,
    @Json(name = "path") val path: String,
    @Json(name = "sha") val sha: String,
    @Json(name = "size") val size: Long,
    @Json(name = "type") val type: String, // "file" or "dir"
    @Json(name = "content") val content: String?, // Base64 encoded
    @Json(name = "encoding") val encoding: String?,
    @Json(name = "download_url") val downloadUrl: String?,
    @Json(name = "html_url") val htmlUrl: String
)

/**
 * GitHub Release model.
 */
@JsonClass(generateAdapter = true)
data class GitHubRelease(
    @Json(name = "id") val id: Long,
    @Json(name = "tag_name") val tagName: String,
    @Json(name = "name") val name: String,
    @Json(name = "body") val body: String?,
    @Json(name = "html_url") val htmlUrl: String,
    @Json(name = "assets") val assets: List<ReleaseAsset>,
    @Json(name = "prerelease") val isPrerelease: Boolean,
    @Json(name = "draft") val isDraft: Boolean,
    @Json(name = "published_at") val publishedAt: String
)

/**
 * Release asset model.
 */
@JsonClass(generateAdapter = true)
data class ReleaseAsset(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "content_type") val contentType: String,
    @Json(name = "size") val size: Long,
    @Json(name = "download_count") val downloadCount: Int,
    @Json(name = "browser_download_url") val downloadUrl: String
)

/**
 * GitHub API response wrapper.
 */
sealed class GitHubResult<out T> {
    data class Success<T>(val data: T) : GitHubResult<T>()
    data class Error(val message: String, val code: Int = 0) : GitHubResult<T>()
    data class RateLimited(val resetTime: Long) : GitHubResult<T>()
}

// =====================================================
// RETROFIT API INTERFACE
// =====================================================

/**
 * GitHub REST API v3 interface.
 */
interface GitHubApiService {
    
    // User endpoints
    @GET("user")
    suspend fun getCurrentUser(): GitHubUser
    
    @GET("users/{username}")
    suspend fun getUser(@Path("username") username: String): GitHubUser
    
    // Repository endpoints
    @GET("user/repos")
    suspend fun getUserRepos(
        @Query("visibility") visibility: String? = null,
        @Query("sort") sort: String = "updated",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1
    ): List<GitHubRepository>
    
    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GitHubRepository
    
    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getContents(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String = "",
        @Query("ref") ref: String? = null
    ): List<GitHubContent>
    
    @GET("repos/{owner}/{repo}/releases")
    suspend fun getReleases(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 30
    ): List<GitHubRelease>
    
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GitHubRelease
    
    // Search endpoints
    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("sort") sort: String = "stars",
        @Query("order") order: String = "desc",
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): SearchResult
}

/**
 * Search results model.
 */
@JsonClass(generateAdapter = true)
data class SearchResult(
    @Json(name = "total_count") val totalCount: Int,
    @Json(name = "incomplete_results") val incompleteResults: Boolean,
    @Json(name = "items") val items: List<GitHubRepository>
)

// =====================================================
// API CLIENT
// =====================================================

/**
 * GitHub API Client - Singleton for GitHub operations.
 */
@Singleton
class GitHubApiClient @Inject constructor() {
    
    private var accessToken: String? = null
    private var username: String? = null
    private var password: String? = null
    
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                
                accessToken?.let { token ->
                    request.addHeader("Authorization", "Bearer $token")
                } ?: run {
                    username?.let { user ->
                        password?.let { pass ->
                            request.addHeader("Authorization", Credentials.basic(user, pass))
                        }
                    }
                }
                
                request.addHeader("Accept", "application/vnd.github.v3+json")
                request.addHeader("X-GitHub-Api-Version", "2022-11-28")
                
                chain.proceed(request.build())
            }
            .build()
    }
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }
    
    private val apiService: GitHubApiService by lazy {
        retrofit.create(GitHubApiService::class.java)
    }
    
    fun setAccessToken(token: String) {
        this.accessToken = token
        Timber.d("GitHub access token set")
    }
    
    fun setBasicAuth(username: String, password: String) {
        this.username = username
        this.password = password
        this.accessToken = null
        Timber.d("GitHub basic auth set for user: $username")
    }
    
    fun clearAuth() {
        accessToken = null
        username = null
        password = null
        Timber.d("GitHub authentication cleared")
    }
    
    suspend fun getCurrentUser(): GitHubResult<GitHubUser> = withContext(Dispatchers.IO) {
        try {
            val user = apiService.getCurrentUser()
            Timber.d("Got user: ${user.login}")
            GitHubResult.Success(user)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get current user")
            GitHubResult.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun getUserRepositories(
        visibility: String? = null,
        page: Int = 1
    ): GitHubResult<List<GitHubRepository>> = withContext(Dispatchers.IO) {
        try {
            val repos = apiService.getUserRepos(visibility = visibility, page = page)
            Timber.d("Got ${repos.size} repositories")
            GitHubResult.Success(repos)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get repositories")
            GitHubResult.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun getRepository(
        owner: String,
        repo: String
    ): GitHubResult<GitHubRepository> = withContext(Dispatchers.IO) {
        try {
            val repository = apiService.getRepository(owner, repo)
            Timber.d("Got repository: ${repository.fullName}")
            GitHubResult.Success(repository)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get repository: $owner/$repo")
            GitHubResult.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun getContents(
        owner: String,
        repo: String,
        path: String = "",
        branch: String? = null
    ): GitHubResult<List<GitHubContent>> = withContext(Dispatchers.IO) {
        try {
            val contents = apiService.getContents(owner, repo, path, branch)
            Timber.d("Got ${contents.size} items at path: $path")
            GitHubResult.Success(contents)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get contents: $owner/$repo/$path")
            GitHubResult.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun getReleases(
        owner: String,
        repo: String
    ): GitHubResult<List<GitHubRelease>> = withContext(Dispatchers.IO) {
        try {
            val releases = apiService.getReleases(owner, repo)
            Timber.d("Got ${releases.size} releases for $owner/$repo")
            GitHubResult.Success(releases)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get releases")
            GitHubResult.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun getLatestRelease(
        owner: String,
        repo: String
    ): GitHubResult<GitHubRelease> = withContext(Dispatchers.IO) {
        try {
            val release = apiService.getLatestRelease(owner, repo)
            Timber.d("Got latest release: ${release.tagName}")
            GitHubResult.Success(release)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get latest release")
            GitHubResult.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun searchRepositories(
        query: String,
        sort: String = "stars",
        page: Int = 1
    ): GitHubResult<SearchResult> = withContext(Dispatchers.IO) {
        try {
            val results = apiService.searchRepositories(query, sort, page = page)
            Timber.d("Search found ${results.totalCount} repositories")
            GitHubResult.Success(results)
        } catch (e: Exception) {
            Timber.e(e, "Search failed: $query")
            GitHubResult.Error(e.message ?: "Unknown error")
        }
    }
}
