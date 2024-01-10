package connectors

import akka.util.Timeout
import baseSpec.BaseSpec
import com.github.tomakehurst.wiremock._
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import models._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.test.Helpers.await
import play.api.test.Injecting

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class GitHubConnectorSpec extends BaseSpec with BeforeAndAfterEach with Injecting with GuiceOneAppPerSuite {
  val Port = 9000
  val Host = "localhost"
  val wireMockServer: WireMockServer = new WireMockServer(wireMockConfig().port(Port))

  implicit val defaultWsClient: WSClient = app.injector.instanceOf[WSClient]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val defaultConfig: Configuration = Configuration.from(Map("AUTHPASS" -> "a_github_auth_token"))

  implicit val timeout: Timeout = 2.minute

  val TestGitHubConnector = new GitHubConnector(defaultWsClient, defaultConfig)

  override def beforeEach(): Unit = {
    wireMockServer.start()
  }

  override def afterEach: Unit = {
    wireMockServer.stop()
  }

  val myUserProfile: UserModel = UserModel(
    login = "Vedant-N421",
    id = 147396311,
    url = "https://api.github.com/users/Vedant-N421",
    node_id = "U_kgDOCMkW1w",
    created_at = "2023-10-09T11:03:37Z",
    followers = 1,
    following = 0
  )

  val userResponse: String =
    """{
      |  "login": "Vedant-N421",
      |  "id": 147396311,
      |  "node_id": "U_kgDOCMkW1w",
      |  "avatar_url": "https://avatars.githubusercontent.com/u/147396311?v=4",
      |  "gravatar_id": "",
      |  "url": "https://api.github.com/users/Vedant-N421",
      |  "html_url": "https://github.com/Vedant-N421",
      |  "followers_url": "https://api.github.com/users/Vedant-N421/followers",
      |  "following_url": "https://api.github.com/users/Vedant-N421/following{/other_user}",
      |  "gists_url": "https://api.github.com/users/Vedant-N421/gists{/gist_id}",
      |  "starred_url": "https://api.github.com/users/Vedant-N421/starred{/owner}{/repo}",
      |  "subscriptions_url": "https://api.github.com/users/Vedant-N421/subscriptions",
      |  "organizations_url": "https://api.github.com/users/Vedant-N421/orgs",
      |  "repos_url": "https://api.github.com/users/Vedant-N421/repos",
      |  "events_url": "https://api.github.com/users/Vedant-N421/events{/privacy}",
      |  "received_events_url": "https://api.github.com/users/Vedant-N421/received_events",
      |  "type": "User",
      |  "site_admin": false,
      |  "name": null,
      |  "company": null,
      |  "blog": "",
      |  "location": null,
      |  "email": null,
      |  "hireable": null,
      |  "bio": null,
      |  "twitter_username": null,
      |  "public_repos": 4,
      |  "public_gists": 0,
      |  "followers": 0,
      |  "following": 0,
      |  "created_at": "2023-10-09T11:03:37Z",
      |  "updated_at": "2023-12-08T13:53:41Z"
      |}""".stripMargin

  val repositoryListResponse: String =
    """[
                                         |  {
                                         |    "id": 729130727,
                                         |    "node_id": "R_kgDOK3Wm5w",
                                         |    "name": "GithubProject",
                                         |    "full_name": "Vedant-N421/GithubProject",
                                         |    "private": false,
                                         |    "owner": {
                                         |      "login": "Vedant-N421",
                                         |      "id": 147396311,
                                         |      "node_id": "U_kgDOCMkW1w",
                                         |      "avatar_url": "https://avatars.githubusercontent.com/u/147396311?v=4",
                                         |      "gravatar_id": "",
                                         |      "url": "https://api.github.com/users/Vedant-N421",
                                         |      "html_url": "https://github.com/Vedant-N421",
                                         |      "followers_url": "https://api.github.com/users/Vedant-N421/followers",
                                         |      "following_url": "https://api.github.com/users/Vedant-N421/following{/other_user}",
                                         |      "gists_url": "https://api.github.com/users/Vedant-N421/gists{/gist_id}",
                                         |      "starred_url": "https://api.github.com/users/Vedant-N421/starred{/owner}{/repo}",
                                         |      "subscriptions_url": "https://api.github.com/users/Vedant-N421/subscriptions",
                                         |      "organizations_url": "https://api.github.com/users/Vedant-N421/orgs",
                                         |      "repos_url": "https://api.github.com/users/Vedant-N421/repos",
                                         |      "events_url": "https://api.github.com/users/Vedant-N421/events{/privacy}",
                                         |      "received_events_url": "https://api.github.com/users/Vedant-N421/received_events",
                                         |      "type": "User",
                                         |      "site_admin": false
                                         |    },
                                         |    "html_url": "https://github.com/Vedant-N421/GithubProject",
                                         |    "description": null,
                                         |    "fork": false,
                                         |    "url": "https://api.github.com/repos/Vedant-N421/GithubProject",
                                         |    "forks_url": "https://api.github.com/repos/Vedant-N421/GithubProject/forks",
                                         |    "keys_url": "https://api.github.com/repos/Vedant-N421/GithubProject/keys{/key_id}",
                                         |    "collaborators_url": "https://api.github.com/repos/Vedant-N421/GithubProject/collaborators{/collaborator}",
                                         |    "teams_url": "https://api.github.com/repos/Vedant-N421/GithubProject/teams",
                                         |    "hooks_url": "https://api.github.com/repos/Vedant-N421/GithubProject/hooks",
                                         |    "issue_events_url": "https://api.github.com/repos/Vedant-N421/GithubProject/issues/events{/number}",
                                         |    "events_url": "https://api.github.com/repos/Vedant-N421/GithubProject/events",
                                         |    "assignees_url": "https://api.github.com/repos/Vedant-N421/GithubProject/assignees{/user}",
                                         |    "branches_url": "https://api.github.com/repos/Vedant-N421/GithubProject/branches{/branch}",
                                         |    "tags_url": "https://api.github.com/repos/Vedant-N421/GithubProject/tags",
                                         |    "blobs_url": "https://api.github.com/repos/Vedant-N421/GithubProject/git/blobs{/sha}",
                                         |    "git_tags_url": "https://api.github.com/repos/Vedant-N421/GithubProject/git/tags{/sha}",
                                         |    "git_refs_url": "https://api.github.com/repos/Vedant-N421/GithubProject/git/refs{/sha}",
                                         |    "trees_url": "https://api.github.com/repos/Vedant-N421/GithubProject/git/trees{/sha}",
                                         |    "statuses_url": "https://api.github.com/repos/Vedant-N421/GithubProject/statuses/{sha}",
                                         |    "languages_url": "https://api.github.com/repos/Vedant-N421/GithubProject/languages",
                                         |    "stargazers_url": "https://api.github.com/repos/Vedant-N421/GithubProject/stargazers",
                                         |    "contributors_url": "https://api.github.com/repos/Vedant-N421/GithubProject/contributors",
                                         |    "subscribers_url": "https://api.github.com/repos/Vedant-N421/GithubProject/subscribers",
                                         |    "subscription_url": "https://api.github.com/repos/Vedant-N421/GithubProject/subscription",
                                         |    "commits_url": "https://api.github.com/repos/Vedant-N421/GithubProject/commits{/sha}",
                                         |    "git_commits_url": "https://api.github.com/repos/Vedant-N421/GithubProject/git/commits{/sha}",
                                         |    "comments_url": "https://api.github.com/repos/Vedant-N421/GithubProject/comments{/number}",
                                         |    "issue_comment_url": "https://api.github.com/repos/Vedant-N421/GithubProject/issues/comments{/number}",
                                         |    "contents_url": "https://api.github.com/repos/Vedant-N421/GithubProject/contents/{+path}",
                                         |    "compare_url": "https://api.github.com/repos/Vedant-N421/GithubProject/compare/{base}...{head}",
                                         |    "merges_url": "https://api.github.com/repos/Vedant-N421/GithubProject/merges",
                                         |    "archive_url": "https://api.github.com/repos/Vedant-N421/GithubProject/{archive_format}{/ref}",
                                         |    "downloads_url": "https://api.github.com/repos/Vedant-N421/GithubProject/downloads",
                                         |    "issues_url": "https://api.github.com/repos/Vedant-N421/GithubProject/issues{/number}",
                                         |    "pulls_url": "https://api.github.com/repos/Vedant-N421/GithubProject/pulls{/number}",
                                         |    "milestones_url": "https://api.github.com/repos/Vedant-N421/GithubProject/milestones{/number}",
                                         |    "notifications_url": "https://api.github.com/repos/Vedant-N421/GithubProject/notifications{?since,all,participating}",
                                         |    "labels_url": "https://api.github.com/repos/Vedant-N421/GithubProject/labels{/name}",
                                         |    "releases_url": "https://api.github.com/repos/Vedant-N421/GithubProject/releases{/id}",
                                         |    "deployments_url": "https://api.github.com/repos/Vedant-N421/GithubProject/deployments",
                                         |    "created_at": "2023-12-08T13:40:19Z",
                                         |    "updated_at": "2023-12-08T14:13:49Z",
                                         |    "pushed_at": "2024-01-05T16:23:55Z",
                                         |    "git_url": "git://github.com/Vedant-N421/GithubProject.git",
                                         |    "ssh_url": "git@github.com:Vedant-N421/GithubProject.git",
                                         |    "clone_url": "https://github.com/Vedant-N421/GithubProject.git",
                                         |    "svn_url": "https://github.com/Vedant-N421/GithubProject",
                                         |    "homepage": null,
                                         |    "size": 64,
                                         |    "stargazers_count": 0,
                                         |    "watchers_count": 0,
                                         |    "language": "Scala",
                                         |    "has_issues": true,
                                         |    "has_projects": true,
                                         |    "has_downloads": true,
                                         |    "has_wiki": true,
                                         |    "has_pages": false,
                                         |    "has_discussions": false,
                                         |    "forks_count": 0,
                                         |    "mirror_url": null,
                                         |    "archived": false,
                                         |    "disabled": false,
                                         |    "open_issues_count": 0,
                                         |    "license": null,
                                         |    "allow_forking": true,
                                         |    "is_template": false,
                                         |    "web_commit_signoff_required": false,
                                         |    "topics": [
                                         |
                                         |    ],
                                         |    "visibility": "public",
                                         |    "forks": 0,
                                         |    "open_issues": 0,
                                         |    "watchers": 0,
                                         |    "default_branch": "main"
                                         |  },
                                         |  {
                                         |    "id": 707668645,
                                         |    "node_id": "R_kgDOKi4qpQ",
                                         |    "name": "play-template",
                                         |    "full_name": "Vedant-N421/play-template",
                                         |    "private": false,
                                         |    "owner": {
                                         |      "login": "Vedant-N421",
                                         |      "id": 147396311,
                                         |      "node_id": "U_kgDOCMkW1w",
                                         |      "avatar_url": "https://avatars.githubusercontent.com/u/147396311?v=4",
                                         |      "gravatar_id": "",
                                         |      "url": "https://api.github.com/users/Vedant-N421",
                                         |      "html_url": "https://github.com/Vedant-N421",
                                         |      "followers_url": "https://api.github.com/users/Vedant-N421/followers",
                                         |      "following_url": "https://api.github.com/users/Vedant-N421/following{/other_user}",
                                         |      "gists_url": "https://api.github.com/users/Vedant-N421/gists{/gist_id}",
                                         |      "starred_url": "https://api.github.com/users/Vedant-N421/starred{/owner}{/repo}",
                                         |      "subscriptions_url": "https://api.github.com/users/Vedant-N421/subscriptions",
                                         |      "organizations_url": "https://api.github.com/users/Vedant-N421/orgs",
                                         |      "repos_url": "https://api.github.com/users/Vedant-N421/repos",
                                         |      "events_url": "https://api.github.com/users/Vedant-N421/events{/privacy}",
                                         |      "received_events_url": "https://api.github.com/users/Vedant-N421/received_events",
                                         |      "type": "User",
                                         |      "site_admin": false
                                         |    },
                                         |    "html_url": "https://github.com/Vedant-N421/play-template",
                                         |    "description": null,
                                         |    "fork": false,
                                         |    "url": "https://api.github.com/repos/Vedant-N421/play-template",
                                         |    "forks_url": "https://api.github.com/repos/Vedant-N421/play-template/forks",
                                         |    "keys_url": "https://api.github.com/repos/Vedant-N421/play-template/keys{/key_id}",
                                         |    "collaborators_url": "https://api.github.com/repos/Vedant-N421/play-template/collaborators{/collaborator}",
                                         |    "teams_url": "https://api.github.com/repos/Vedant-N421/play-template/teams",
                                         |    "hooks_url": "https://api.github.com/repos/Vedant-N421/play-template/hooks",
                                         |    "issue_events_url": "https://api.github.com/repos/Vedant-N421/play-template/issues/events{/number}",
                                         |    "events_url": "https://api.github.com/repos/Vedant-N421/play-template/events",
                                         |    "assignees_url": "https://api.github.com/repos/Vedant-N421/play-template/assignees{/user}",
                                         |    "branches_url": "https://api.github.com/repos/Vedant-N421/play-template/branches{/branch}",
                                         |    "tags_url": "https://api.github.com/repos/Vedant-N421/play-template/tags",
                                         |    "blobs_url": "https://api.github.com/repos/Vedant-N421/play-template/git/blobs{/sha}",
                                         |    "git_tags_url": "https://api.github.com/repos/Vedant-N421/play-template/git/tags{/sha}",
                                         |    "git_refs_url": "https://api.github.com/repos/Vedant-N421/play-template/git/refs{/sha}",
                                         |    "trees_url": "https://api.github.com/repos/Vedant-N421/play-template/git/trees{/sha}",
                                         |    "statuses_url": "https://api.github.com/repos/Vedant-N421/play-template/statuses/{sha}",
                                         |    "languages_url": "https://api.github.com/repos/Vedant-N421/play-template/languages",
                                         |    "stargazers_url": "https://api.github.com/repos/Vedant-N421/play-template/stargazers",
                                         |    "contributors_url": "https://api.github.com/repos/Vedant-N421/play-template/contributors",
                                         |    "subscribers_url": "https://api.github.com/repos/Vedant-N421/play-template/subscribers",
                                         |    "subscription_url": "https://api.github.com/repos/Vedant-N421/play-template/subscription",
                                         |    "commits_url": "https://api.github.com/repos/Vedant-N421/play-template/commits{/sha}",
                                         |    "git_commits_url": "https://api.github.com/repos/Vedant-N421/play-template/git/commits{/sha}",
                                         |    "comments_url": "https://api.github.com/repos/Vedant-N421/play-template/comments{/number}",
                                         |    "issue_comment_url": "https://api.github.com/repos/Vedant-N421/play-template/issues/comments{/number}",
                                         |    "contents_url": "https://api.github.com/repos/Vedant-N421/play-template/contents/{+path}",
                                         |    "compare_url": "https://api.github.com/repos/Vedant-N421/play-template/compare/{base}...{head}",
                                         |    "merges_url": "https://api.github.com/repos/Vedant-N421/play-template/merges",
                                         |    "archive_url": "https://api.github.com/repos/Vedant-N421/play-template/{archive_format}{/ref}",
                                         |    "downloads_url": "https://api.github.com/repos/Vedant-N421/play-template/downloads",
                                         |    "issues_url": "https://api.github.com/repos/Vedant-N421/play-template/issues{/number}",
                                         |    "pulls_url": "https://api.github.com/repos/Vedant-N421/play-template/pulls{/number}",
                                         |    "milestones_url": "https://api.github.com/repos/Vedant-N421/play-template/milestones{/number}",
                                         |    "notifications_url": "https://api.github.com/repos/Vedant-N421/play-template/notifications{?since,all,participating}",
                                         |    "labels_url": "https://api.github.com/repos/Vedant-N421/play-template/labels{/name}",
                                         |    "releases_url": "https://api.github.com/repos/Vedant-N421/play-template/releases{/id}",
                                         |    "deployments_url": "https://api.github.com/repos/Vedant-N421/play-template/deployments",
                                         |    "created_at": "2023-10-20T11:56:37Z",
                                         |    "updated_at": "2023-10-30T10:56:03Z",
                                         |    "pushed_at": "2023-12-08T11:14:37Z",
                                         |    "git_url": "git://github.com/Vedant-N421/play-template.git",
                                         |    "ssh_url": "git@github.com:Vedant-N421/play-template.git",
                                         |    "clone_url": "https://github.com/Vedant-N421/play-template.git",
                                         |    "svn_url": "https://github.com/Vedant-N421/play-template",
                                         |    "homepage": null,
                                         |    "size": 2827,
                                         |    "stargazers_count": 0,
                                         |    "watchers_count": 0,
                                         |    "language": "Scala",
                                         |    "has_issues": true,
                                         |    "has_projects": true,
                                         |    "has_downloads": true,
                                         |    "has_wiki": true,
                                         |    "has_pages": false,
                                         |    "has_discussions": false,
                                         |    "forks_count": 0,
                                         |    "mirror_url": null,
                                         |    "archived": false,
                                         |    "disabled": false,
                                         |    "open_issues_count": 0,
                                         |    "license": null,
                                         |    "allow_forking": true,
                                         |    "is_template": false,
                                         |    "web_commit_signoff_required": false,
                                         |    "topics": [
                                         |
                                         |    ],
                                         |    "visibility": "public",
                                         |    "forks": 0,
                                         |    "open_issues": 0,
                                         |    "watchers": 0,
                                         |    "default_branch": "main"
                                         |  },
                                         |  {
                                         |    "id": 706057005,
                                         |    "node_id": "R_kgDOKhWTLQ",
                                         |    "name": "Scala-Training",
                                         |    "full_name": "Vedant-N421/Scala-Training",
                                         |    "private": false,
                                         |    "owner": {
                                         |      "login": "Vedant-N421",
                                         |      "id": 147396311,
                                         |      "node_id": "U_kgDOCMkW1w",
                                         |      "avatar_url": "https://avatars.githubusercontent.com/u/147396311?v=4",
                                         |      "gravatar_id": "",
                                         |      "url": "https://api.github.com/users/Vedant-N421",
                                         |      "html_url": "https://github.com/Vedant-N421",
                                         |      "followers_url": "https://api.github.com/users/Vedant-N421/followers",
                                         |      "following_url": "https://api.github.com/users/Vedant-N421/following{/other_user}",
                                         |      "gists_url": "https://api.github.com/users/Vedant-N421/gists{/gist_id}",
                                         |      "starred_url": "https://api.github.com/users/Vedant-N421/starred{/owner}{/repo}",
                                         |      "subscriptions_url": "https://api.github.com/users/Vedant-N421/subscriptions",
                                         |      "organizations_url": "https://api.github.com/users/Vedant-N421/orgs",
                                         |      "repos_url": "https://api.github.com/users/Vedant-N421/repos",
                                         |      "events_url": "https://api.github.com/users/Vedant-N421/events{/privacy}",
                                         |      "received_events_url": "https://api.github.com/users/Vedant-N421/received_events",
                                         |      "type": "User",
                                         |      "site_admin": false
                                         |    },
                                         |    "html_url": "https://github.com/Vedant-N421/Scala-Training",
                                         |    "description": null,
                                         |    "fork": false,
                                         |    "url": "https://api.github.com/repos/Vedant-N421/Scala-Training",
                                         |    "forks_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/forks",
                                         |    "keys_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/keys{/key_id}",
                                         |    "collaborators_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/collaborators{/collaborator}",
                                         |    "teams_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/teams",
                                         |    "hooks_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/hooks",
                                         |    "issue_events_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/issues/events{/number}",
                                         |    "events_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/events",
                                         |    "assignees_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/assignees{/user}",
                                         |    "branches_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/branches{/branch}",
                                         |    "tags_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/tags",
                                         |    "blobs_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/git/blobs{/sha}",
                                         |    "git_tags_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/git/tags{/sha}",
                                         |    "git_refs_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/git/refs{/sha}",
                                         |    "trees_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/git/trees{/sha}",
                                         |    "statuses_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/statuses/{sha}",
                                         |    "languages_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/languages",
                                         |    "stargazers_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/stargazers",
                                         |    "contributors_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/contributors",
                                         |    "subscribers_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/subscribers",
                                         |    "subscription_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/subscription",
                                         |    "commits_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/commits{/sha}",
                                         |    "git_commits_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/git/commits{/sha}",
                                         |    "comments_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/comments{/number}",
                                         |    "issue_comment_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/issues/comments{/number}",
                                         |    "contents_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/contents/{+path}",
                                         |    "compare_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/compare/{base}...{head}",
                                         |    "merges_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/merges",
                                         |    "archive_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/{archive_format}{/ref}",
                                         |    "downloads_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/downloads",
                                         |    "issues_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/issues{/number}",
                                         |    "pulls_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/pulls{/number}",
                                         |    "milestones_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/milestones{/number}",
                                         |    "notifications_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/notifications{?since,all,participating}",
                                         |    "labels_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/labels{/name}",
                                         |    "releases_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/releases{/id}",
                                         |    "deployments_url": "https://api.github.com/repos/Vedant-N421/Scala-Training/deployments",
                                         |    "created_at": "2023-10-17T08:15:25Z",
                                         |    "updated_at": "2023-10-19T09:47:47Z",
                                         |    "pushed_at": "2023-10-25T13:57:53Z",
                                         |    "git_url": "git://github.com/Vedant-N421/Scala-Training.git",
                                         |    "ssh_url": "git@github.com:Vedant-N421/Scala-Training.git",
                                         |    "clone_url": "https://github.com/Vedant-N421/Scala-Training.git",
                                         |    "svn_url": "https://github.com/Vedant-N421/Scala-Training",
                                         |    "homepage": null,
                                         |    "size": 271,
                                         |    "stargazers_count": 0,
                                         |    "watchers_count": 0,
                                         |    "language": "Scala",
                                         |    "has_issues": true,
                                         |    "has_projects": true,
                                         |    "has_downloads": true,
                                         |    "has_wiki": false,
                                         |    "has_pages": false,
                                         |    "has_discussions": false,
                                         |    "forks_count": 0,
                                         |    "mirror_url": null,
                                         |    "archived": false,
                                         |    "disabled": false,
                                         |    "open_issues_count": 1,
                                         |    "license": null,
                                         |    "allow_forking": true,
                                         |    "is_template": false,
                                         |    "web_commit_signoff_required": false,
                                         |    "topics": [
                                         |
                                         |    ],
                                         |    "visibility": "public",
                                         |    "forks": 0,
                                         |    "open_issues": 1,
                                         |    "watchers": 0,
                                         |    "default_branch": "main"
                                         |  },
                                         |  {
                                         |    "id": 707137288,
                                         |    "node_id": "R_kgDOKiYPCA",
                                         |    "name": "ScalaUdemyTraining",
                                         |    "full_name": "Vedant-N421/ScalaUdemyTraining",
                                         |    "private": false,
                                         |    "owner": {
                                         |      "login": "Vedant-N421",
                                         |      "id": 147396311,
                                         |      "node_id": "U_kgDOCMkW1w",
                                         |      "avatar_url": "https://avatars.githubusercontent.com/u/147396311?v=4",
                                         |      "gravatar_id": "",
                                         |      "url": "https://api.github.com/users/Vedant-N421",
                                         |      "html_url": "https://github.com/Vedant-N421",
                                         |      "followers_url": "https://api.github.com/users/Vedant-N421/followers",
                                         |      "following_url": "https://api.github.com/users/Vedant-N421/following{/other_user}",
                                         |      "gists_url": "https://api.github.com/users/Vedant-N421/gists{/gist_id}",
                                         |      "starred_url": "https://api.github.com/users/Vedant-N421/starred{/owner}{/repo}",
                                         |      "subscriptions_url": "https://api.github.com/users/Vedant-N421/subscriptions",
                                         |      "organizations_url": "https://api.github.com/users/Vedant-N421/orgs",
                                         |      "repos_url": "https://api.github.com/users/Vedant-N421/repos",
                                         |      "events_url": "https://api.github.com/users/Vedant-N421/events{/privacy}",
                                         |      "received_events_url": "https://api.github.com/users/Vedant-N421/received_events",
                                         |      "type": "User",
                                         |      "site_admin": false
                                         |    },
                                         |    "html_url": "https://github.com/Vedant-N421/ScalaUdemyTraining",
                                         |    "description": null,
                                         |    "fork": false,
                                         |    "url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining",
                                         |    "forks_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/forks",
                                         |    "keys_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/keys{/key_id}",
                                         |    "collaborators_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/collaborators{/collaborator}",
                                         |    "teams_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/teams",
                                         |    "hooks_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/hooks",
                                         |    "issue_events_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/issues/events{/number}",
                                         |    "events_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/events",
                                         |    "assignees_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/assignees{/user}",
                                         |    "branches_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/branches{/branch}",
                                         |    "tags_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/tags",
                                         |    "blobs_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/blobs{/sha}",
                                         |    "git_tags_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/tags{/sha}",
                                         |    "git_refs_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/refs{/sha}",
                                         |    "trees_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/trees{/sha}",
                                         |    "statuses_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/statuses/{sha}",
                                         |    "languages_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/languages",
                                         |    "stargazers_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/stargazers",
                                         |    "contributors_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/contributors",
                                         |    "subscribers_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/subscribers",
                                         |    "subscription_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/subscription",
                                         |    "commits_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/commits{/sha}",
                                         |    "git_commits_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/commits{/sha}",
                                         |    "comments_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/comments{/number}",
                                         |    "issue_comment_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/issues/comments{/number}",
                                         |    "contents_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/contents/{+path}",
                                         |    "compare_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/compare/{base}...{head}",
                                         |    "merges_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/merges",
                                         |    "archive_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/{archive_format}{/ref}",
                                         |    "downloads_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/downloads",
                                         |    "issues_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/issues{/number}",
                                         |    "pulls_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/pulls{/number}",
                                         |    "milestones_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/milestones{/number}",
                                         |    "notifications_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/notifications{?since,all,participating}",
                                         |    "labels_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/labels{/name}",
                                         |    "releases_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/releases{/id}",
                                         |    "deployments_url": "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/deployments",
                                         |    "created_at": "2023-10-19T09:48:44Z",
                                         |    "updated_at": "2023-10-19T10:59:00Z",
                                         |    "pushed_at": "2024-01-05T22:03:59Z",
                                         |    "git_url": "git://github.com/Vedant-N421/ScalaUdemyTraining.git",
                                         |    "ssh_url": "git@github.com:Vedant-N421/ScalaUdemyTraining.git",
                                         |    "clone_url": "https://github.com/Vedant-N421/ScalaUdemyTraining.git",
                                         |    "svn_url": "https://github.com/Vedant-N421/ScalaUdemyTraining",
                                         |    "homepage": null,
                                         |    "size": 62,
                                         |    "stargazers_count": 0,
                                         |    "watchers_count": 0,
                                         |    "language": "Scala",
                                         |    "has_issues": true,
                                         |    "has_projects": true,
                                         |    "has_downloads": true,
                                         |    "has_wiki": true,
                                         |    "has_pages": false,
                                         |    "has_discussions": false,
                                         |    "forks_count": 0,
                                         |    "mirror_url": null,
                                         |    "archived": false,
                                         |    "disabled": false,
                                         |    "open_issues_count": 0,
                                         |    "license": null,
                                         |    "allow_forking": true,
                                         |    "is_template": false,
                                         |    "web_commit_signoff_required": false,
                                         |    "topics": [
                                         |
                                         |    ],
                                         |    "visibility": "public",
                                         |    "forks": 0,
                                         |    "open_issues": 0,
                                         |    "watchers": 0,
                                         |    "default_branch": "main"
                                         |  }
                                         |]""".stripMargin

  val contentsListResponse: String =
    """[
                                       |  {
                                       |    "name": ".bsp",
                                       |    "path": ".bsp",
                                       |    "sha": "a4a2d209682a84a9d55f63defca1d107c03a8c9e",
                                       |    "size": 0,
                                       |    "url": "https://api.github.com/repos/Vedant-N421/play-template/contents/.bsp?ref=main",
                                       |    "html_url": "https://github.com/Vedant-N421/play-template/tree/main/.bsp",
                                       |    "git_url": "https://api.github.com/repos/Vedant-N421/play-template/git/trees/a4a2d209682a84a9d55f63defca1d107c03a8c9e",
                                       |    "download_url": null,
                                       |    "type": "dir",
                                       |    "_links": {
                                       |      "self": "https://api.github.com/repos/Vedant-N421/play-template/contents/.bsp?ref=main",
                                       |      "git": "https://api.github.com/repos/Vedant-N421/play-template/git/trees/a4a2d209682a84a9d55f63defca1d107c03a8c9e",
                                       |      "html": "https://github.com/Vedant-N421/play-template/tree/main/.bsp"
                                       |    }
                                       |  },
                                       |  {
                                       |    "name": ".idea",
                                       |    "path": ".idea",
                                       |    "sha": "75636073e7b53a5b5c17fdabb73a48058ea2c5df",
                                       |    "size": 0,
                                       |    "url": "https://api.github.com/repos/Vedant-N421/play-template/contents/.idea?ref=main",
                                       |    "html_url": "https://github.com/Vedant-N421/play-template/tree/main/.idea",
                                       |    "git_url": "https://api.github.com/repos/Vedant-N421/play-template/git/trees/75636073e7b53a5b5c17fdabb73a48058ea2c5df",
                                       |    "download_url": null,
                                       |    "type": "dir",
                                       |    "_links": {
                                       |      "self": "https://api.github.com/repos/Vedant-N421/play-template/contents/.idea?ref=main",
                                       |      "git": "https://api.github.com/repos/Vedant-N421/play-template/git/trees/75636073e7b53a5b5c17fdabb73a48058ea2c5df",
                                       |      "html": "https://github.com/Vedant-N421/play-template/tree/main/.idea"
                                       |    }
                                       |  },
                                       |  {
                                       |    "name": ".scalafmt.conf",
                                       |    "path": ".scalafmt.conf",
                                       |    "sha": "333cbbd6488834da938c324e387c2471cc458581",
                                       |    "size": 247,
                                       |    "url": "https://api.github.com/repos/Vedant-N421/play-template/contents/.scalafmt.conf?ref=main",
                                       |    "html_url": "https://github.com/Vedant-N421/play-template/blob/main/.scalafmt.conf",
                                       |    "git_url": "https://api.github.com/repos/Vedant-N421/play-template/git/blobs/333cbbd6488834da938c324e387c2471cc458581",
                                       |    "download_url": "https://raw.githubusercontent.com/Vedant-N421/play-template/main/.scalafmt.conf",
                                       |    "type": "file",
                                       |    "_links": {
                                       |      "self": "https://api.github.com/repos/Vedant-N421/play-template/contents/.scalafmt.conf?ref=main",
                                       |      "git": "https://api.github.com/repos/Vedant-N421/play-template/git/blobs/333cbbd6488834da938c324e387c2471cc458581",
                                       |      "html": "https://github.com/Vedant-N421/play-template/blob/main/.scalafmt.conf"
                                       |    }
                                       |  },
                                       |  {
                                       |    "name": "app",
                                       |    "path": "app",
                                       |    "sha": "685ff18dc18adbcff5ff413038b73c1de287342e",
                                       |    "size": 0,
                                       |    "url": "https://api.github.com/repos/Vedant-N421/play-template/contents/app?ref=main",
                                       |    "html_url": "https://github.com/Vedant-N421/play-template/tree/main/app",
                                       |    "git_url": "https://api.github.com/repos/Vedant-N421/play-template/git/trees/685ff18dc18adbcff5ff413038b73c1de287342e",
                                       |    "download_url": null,
                                       |    "type": "dir",
                                       |    "_links": {
                                       |      "self": "https://api.github.com/repos/Vedant-N421/play-template/contents/app?ref=main",
                                       |      "git": "https://api.github.com/repos/Vedant-N421/play-template/git/trees/685ff18dc18adbcff5ff413038b73c1de287342e",
                                       |      "html": "https://github.com/Vedant-N421/play-template/tree/main/app"
                                       |    }
                                       |  },
                                       |  {
                                       |    "name": "build.sbt",
                                       |    "path": "build.sbt",
                                       |    "sha": "44f0aaf80163c885cc4cf5fdfc6f8422bfe3bd35",
                                       |    "size": 905,
                                       |    "url": "https://api.github.com/repos/Vedant-N421/play-template/contents/build.sbt?ref=main",
                                       |    "html_url": "https://github.com/Vedant-N421/play-template/blob/main/build.sbt",
                                       |    "git_url": "https://api.github.com/repos/Vedant-N421/play-template/git/blobs/44f0aaf80163c885cc4cf5fdfc6f8422bfe3bd35",
                                       |    "download_url": "https://raw.githubusercontent.com/Vedant-N421/play-template/main/build.sbt",
                                       |    "type": "file",
                                       |    "_links": {
                                       |      "self": "https://api.github.com/repos/Vedant-N421/play-template/contents/build.sbt?ref=main",
                                       |      "git": "https://api.github.com/repos/Vedant-N421/play-template/git/blobs/44f0aaf80163c885cc4cf5fdfc6f8422bfe3bd35",
                                       |      "html": "https://github.com/Vedant-N421/play-template/blob/main/build.sbt"
                                       |    }
                                       |  },
                                       |  {
                                       |    "name": "conf",
                                       |    "path": "conf",
                                       |    "sha": "64704f074b32ff1cafa460e3964d99e5751e1b87",
                                       |    "size": 0,
                                       |    "url": "https://api.github.com/repos/Vedant-N421/play-template/contents/conf?ref=main",
                                       |    "html_url": "https://github.com/Vedant-N421/play-template/tree/main/conf",
                                       |    "git_url": "https://api.github.com/repos/Vedant-N421/play-template/git/trees/64704f074b32ff1cafa460e3964d99e5751e1b87",
                                       |    "download_url": null,
                                       |    "type": "dir",
                                       |    "_links": {
                                       |      "self": "https://api.github.com/repos/Vedant-N421/play-template/contents/conf?ref=main",
                                       |      "git": "https://api.github.com/repos/Vedant-N421/play-template/git/trees/64704f074b32ff1cafa460e3964d99e5751e1b87",
                                       |      "html": "https://github.com/Vedant-N421/play-template/tree/main/conf"
                                       |    }
                                       |  },
                                       |  {
                                       |    "name": "project",
                                       |    "path": "project",
                                       |    "sha": "731aff107856d09dc1143f3615c5f2cfdee142fa",
                                       |    "size": 0,
                                       |    "url": "https://api.github.com/repos/Vedant-N421/play-template/contents/project?ref=main",
                                       |    "html_url": "https://github.com/Vedant-N421/play-template/tree/main/project",
                                       |    "git_url": "https://api.github.com/repos/Vedant-N421/play-template/git/trees/731aff107856d09dc1143f3615c5f2cfdee142fa",
                                       |    "download_url": null,
                                       |    "type": "dir",
                                       |    "_links": {
                                       |      "self": "https://api.github.com/repos/Vedant-N421/play-template/contents/project?ref=main",
                                       |      "git": "https://api.github.com/repos/Vedant-N421/play-template/git/trees/731aff107856d09dc1143f3615c5f2cfdee142fa",
                                       |      "html": "https://github.com/Vedant-N421/play-template/tree/main/project"
                                       |    }
                                       |  },
                                       |  {
                                       |    "name": "target",
                                       |    "path": "target",
                                       |    "sha": "15cb70a91d6c06275870df54e8df920b2b0fe8d1",
                                       |    "size": 0,
                                       |    "url": "https://api.github.com/repos/Vedant-N421/play-template/contents/target?ref=main",
                                       |    "html_url": "https://github.com/Vedant-N421/play-template/tree/main/target",
                                       |    "git_url": "https://api.github.com/repos/Vedant-N421/play-template/git/trees/15cb70a91d6c06275870df54e8df920b2b0fe8d1",
                                       |    "download_url": null,
                                       |    "type": "dir",
                                       |    "_links": {
                                       |      "self": "https://api.github.com/repos/Vedant-N421/play-template/contents/target?ref=main",
                                       |      "git": "https://api.github.com/repos/Vedant-N421/play-template/git/trees/15cb70a91d6c06275870df54e8df920b2b0fe8d1",
                                       |      "html": "https://github.com/Vedant-N421/play-template/tree/main/target"
                                       |    }
                                       |  },
                                       |  {
                                       |    "name": "test",
                                       |    "path": "test",
                                       |    "sha": "4f87ec21694a2e3215578e2f14684bb5021e95ec",
                                       |    "size": 0,
                                       |    "url": "https://api.github.com/repos/Vedant-N421/play-template/contents/test?ref=main",
                                       |    "html_url": "https://github.com/Vedant-N421/play-template/tree/main/test",
                                       |    "git_url": "https://api.github.com/repos/Vedant-N421/play-template/git/trees/4f87ec21694a2e3215578e2f14684bb5021e95ec",
                                       |    "download_url": null,
                                       |    "type": "dir",
                                       |    "_links": {
                                       |      "self": "https://api.github.com/repos/Vedant-N421/play-template/contents/test?ref=main",
                                       |      "git": "https://api.github.com/repos/Vedant-N421/play-template/git/trees/4f87ec21694a2e3215578e2f14684bb5021e95ec",
                                       |      "html": "https://github.com/Vedant-N421/play-template/tree/main/test"
                                       |    }
                                       |  }
                                       |]""".stripMargin

  val createFileResponse: String =
    """"{\"content\":{\"name\":\".gitignore\",\"path\":\".gitignore\",\"sha\":\"5a2e8bb632458f7417e65ffc4985f530ba086464\",\"size\":21,\"url\":\"https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/contents/.gitignore?ref=main\",\"html_url\":\"https://github.com/Vedant-N421/ScalaUdemyTraining/blob/main/.gitignore\",\"git_url\":\"https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/blobs/5a2e8bb632458f7417e65ffc4985f530ba086464\",\"download_url\":\"https://raw.githubusercontent.com/Vedant-N421/ScalaUdemyTraining/main/.gitignore\",\"type\":\"file\",\"_links\":{\"self\":\"https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/contents/.gitignore?ref=main\",\"git\":\"https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/blobs/5a2e8bb632458f7417e65ffc4985f530ba086464\",\"html\":\"https://github.com/Vedant-N421/ScalaUdemyTraining/blob/main/.gitignore\"}},\"commit\":{\"sha\":\"9c28e9e89801fe564a7171b9daa3d38c95b6d432\",\"node_id\":\"C_kwDOKiYPCNoAKDljMjhlOWU4OTgwMWZlNTY0YTcxNzFiOWRhYTNkMzhjOTViNmQ0MzI\",\"url\":\"https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/commits/9c28e9e89801fe564a7171b9daa3d38c95b6d432\",\"html_url\":\"https://github.com/Vedant-N421/ScalaUdemyTraining/commit/9c28e9e89801fe564a7171b9daa3d38c95b6d432\",\"author\":{\"name\":\"Vedant Nemane\",\"email\":\"vedant.nemane@mercator.group\",\"date\":\"2024-01-10T11:13:17Z\"},\"committer\":{\"name\":\"Vedant Nemane\",\"email\":\"vedant.nemane@mercator.group\",\"date\":\"2024-01-10T11:13:17Z\"},\"tree\":{\"sha\":\"4f02eeaed39e1a5ebba6990a75cad78ad51e26e1\",\"url\":\"https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/trees/4f02eeaed39e1a5ebba6990a75cad78ad51e26e1\"},\"message\":\"Trying\",\"parents\":[{\"sha\":\"e1fb463a62d87481457487731ac5d21161722eb5\",\"url\":\"https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/commits/e1fb463a62d87481457487731ac5d21161722eb5\",\"html_url\":\"https://github.com/Vedant-N421/ScalaUdemyTraining/commit/e1fb463a62d87481457487731ac5d21161722eb5\"}],\"verification\":{\"verified\":false,\"reason\":\"unsigned\",\"signature\":null,\"payload\":null}}}""""

  val updateFileResponse: String =
    """"{\"content\":{\"name\":\".gitignore\",\"path\":\".gitignore\",\"sha\":\"cab5f22da032dc6e45f322cadfe28526bc0d3363\",\"size\":20,\"url\":\"https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/contents/.gitignore?ref=main\",\"html_url\":\"https://github.com/Vedant-N421/ScalaUdemyTraining/blob/main/.gitignore\",\"git_url\":\"https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/blobs/cab5f22da032dc6e45f322cadfe28526bc0d3363\",\"download_url\":\"https://raw.githubusercontent.com/Vedant-N421/ScalaUdemyTraining/main/.gitignore\",\"type\":\"file\",\"_links\":{\"self\":\"https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/contents/.gitignore?ref=main\",\"git\":\"https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/blobs/cab5f22da032dc6e45f322cadfe28526bc0d3363\",\"html\":\"https://github.com/Vedant-N421/ScalaUdemyTraining/blob/main/.gitignore\"}},\"commit\":{\"sha\":\"692bdb15db63f05501e954dbdf78ebd212bb957c\",\"node_id\":\"C_kwDOKiYPCNoAKDY5MmJkYjE1ZGI2M2YwNTUwMWU5NTRkYmRmNzhlYmQyMTJiYjk1N2M\",\"url\":\"https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/commits/692bdb15db63f05501e954dbdf78ebd212bb957c\",\"html_url\":\"https://github.com/Vedant-N421/ScalaUdemyTraining/commit/692bdb15db63f05501e954dbdf78ebd212bb957c\",\"author\":{\"name\":\"Vedant Nemane\",\"email\":\"vedant.nemane@mercator.group\",\"date\":\"2024-01-10T15:35:38Z\"},\"committer\":{\"name\":\"Vedant Nemane\",\"email\":\"vedant.nemane@mercator.group\",\"date\":\"2024-01-10T15:35:38Z\"},\"tree\":{\"sha\":\"a2d8795f368f0ca2246bd5dda234e780109cd71e\",\"url\":\"https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/trees/a2d8795f368f0ca2246bd5dda234e780109cd71e\"},\"message\":\"updated with a form\",\"parents\":[{\"sha\":\"7f82799f4f9f35bdccaa09da2f3c2bbaffd01ec7\",\"url\":\"https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/commits/7f82799f4f9f35bdccaa09da2f3c2bbaffd01ec7\",\"html_url\":\"https://github.com/Vedant-N421/ScalaUdemyTraining/commit/7f82799f4f9f35bdccaa09da2f3c2bbaffd01ec7\"}],\"verification\":{\"verified\":false,\"reason\":\"unsigned\",\"signature\":null,\"payload\":null}}}""""

  val deletedFileResponse: String =
    """"{\"content\":null,\"commit\":{\"sha\":\"02c9c5bd937c33a0d29786c09ed3f4ae4ac78b2d\",\"node_id\":\"C_kwDOKiYPCNoAKDAyYzljNWJkOTM3YzMzYTBkMjk3ODZjMDllZDNmNGFlNGFjNzhiMmQ\",\"url\":\"https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/commits/02c9c5bd937c33a0d29786c09ed3f4ae4ac78b2d\",\"html_url\":\"https://github.com/Vedant-N421/ScalaUdemyTraining/commit/02c9c5bd937c33a0d29786c09ed3f4ae4ac78b2d\",\"author\":{\"name\":\"Vedant Nemane\",\"email\":\"vedant.nemane@mercator.group\",\"date\":\"2024-01-10T15:36:57Z\"},\"committer\":{\"name\":\"Vedant Nemane\",\"email\":\"vedant.nemane@mercator.group\",\"date\":\"2024-01-10T15:36:57Z\"},\"tree\":{\"sha\":\"f63e84725d7da41ef3b3d6ddd87f592eab80f617\",\"url\":\"https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/trees/f63e84725d7da41ef3b3d6ddd87f592eab80f617\"},\"message\":\"deleted and gone!\",\"parents\":[{\"sha\":\"692bdb15db63f05501e954dbdf78ebd212bb957c\",\"url\":\"https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/git/commits/692bdb15db63f05501e954dbdf78ebd212bb957c\",\"html_url\":\"https://github.com/Vedant-N421/ScalaUdemyTraining/commit/692bdb15db63f05501e954dbdf78ebd212bb957c\"}],\"verification\":{\"verified\":false,\"reason\":\"unsigned\",\"signature\":null,\"payload\":null}}}""""

  "A getuser stub request" should {
    "return a valid response" in {
      beforeEach()
      wireMockServer.stubFor(
        get(urlPathEqualTo(s"http://$Host:$Port/github/users/Vedant-N421"))
          .willReturn(
            aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(userResponse)
              .withStatus(200)
          )
      )

      val result = await(TestGitHubConnector.getUser("Vedant-N421"))

      result shouldBe Right(myUserProfile)

      afterEach()
    }
  }

  "A getrepos stub request" should {
    "return a valid response" in {
      beforeEach()
      wireMockServer.stubFor(
        get(urlPathEqualTo(s"http://$Host:$Port/github/users/Vedant-N421/repos"))
          .willReturn(
            aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(repositoryListResponse)
              .withStatus(200)
          )
      )

      val result = await(TestGitHubConnector.getRepos("Vedant-N421"))

      val condition = result match {
        case Left(_) => false
        case Right(y: List[RepoModel]) =>
          y.contains(
            RepoModel(729130727,
                      "GithubProject",
                      "https://api.github.com/repos/Vedant-N421/GithubProject",
                      Owner("Vedant-N421", "https://api.github.com/users/Vedant-N421"))
          )
      }

      condition shouldBe true

      afterEach()
    }
  }

  "A getcontents stub request" should {
    "return a valid response" in {
      beforeEach()
      wireMockServer.stubFor(
        get(urlPathEqualTo(s"http://$Host:$Port/github/users/Vedant-N421/repos/play-template/"))
          .willReturn(
            aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(contentsListResponse)
              .withStatus(200)
          )
      )

      val result = await(TestGitHubConnector.getContents("Vedant-N421", "play-template", ""))

      val condition = result match {
        case Left(_) => false
        case Right(y: List[ContentModel]) =>
          y.contains(
            ContentModel(
              "build.sbt",
              "build.sbt",
              "44f0aaf80163c885cc4cf5fdfc6f8422bfe3bd35",
              "https://api.github.com/repos/Vedant-N421/play-template/contents/build.sbt?ref=main",
              "file",
              None
            )
          )
      }

      condition shouldBe true

      afterEach()
    }
  }

  "A gitcud for create stub request" should {
    "return a valid response" in {
      beforeEach()
      wireMockServer.stubFor(
        get(urlPathEqualTo(s"http://$Host:$Port/github/users/Vedant-N421/ScalaUdemyTraining/createfile"))
          .willReturn(
            aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(createFileResponse)
              .withStatus(200)
          )
      )

      val result = await(
        TestGitHubConnector.gitCUD(
          gitCUDParameters("CREATE",
                           "Vedant-N421",
                           "ScalaUdemyTraining",
                           "Trying",
                           ".gitignore",
                           Some("something here insert"),
                           "")
        )
      )

      val condition = result match {
        case Left(error) => false
        case Right(created) => true
      }

      condition shouldBe true

      afterEach()
    }
  }

  "A gitcud for update stub request" should {
    "return a valid response" in {
      beforeEach()
      wireMockServer.stubFor(
        get(urlPathEqualTo(s"http://$Host:$Port/github/users/Vedant-N421/ScalaUdemyTraining/updatefile"))
          .willReturn(
            aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(updateFileResponse)
              .withStatus(200)
          )
      )

      val result = await(
        TestGitHubConnector.gitCUD(
          gitCUDParameters("UPDATE",
                           "Vedant-N421",
                           "ScalaUdemyTraining",
                           "updating",
                           ".gitignore",
                           Some("updating"),
                           "")
        )
      )

      val condition = result match {
        case Left(_) => false
        case Right(_) => true
      }

      condition shouldBe true

      afterEach()
    }
  }

  "A gitcud for delete stub request" should {
    "return a valid response" in {
      beforeEach()
      wireMockServer.stubFor(
        get(urlPathEqualTo(s"http://$Host:$Port/github/users/Vedant-N421/ScalaUdemyTraining/deletefile"))
          .willReturn(
            aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(deletedFileResponse)
              .withStatus(200)
          )
      )

      val result = await(
        TestGitHubConnector.gitCUD(
          gitCUDParameters("DELETE", "Vedant-N421", "ScalaUdemyTraining", "deleting", ".gitignore", None, "")
        )
      )

      val condition = result match {
        case Left(_) => false
        case Right(_) => true
      }

      condition shouldBe true

      afterEach()
    }
  }

}
