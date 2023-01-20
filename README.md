# Github Exercise
 The GitHub API is documented here: https://docs.github.com/en/rest
 We will be connecting to this API to create a version of 'github'
 You should clone this repo and in terminal type sbt run, in your browser on localhost:9000 you should see a page with the title "Welcome to your version   of GitHub"
 
  For package structure use standard: view (html templates), controller (API endpoint entry), service (Orchestration, should be light weight seeing as    we're only calling one endpoint), connector (To speak to api.github), model (Domain model data representation)
 Using the GitHub API it’s a lot easier if upfront you start to use your own git credentials for authorisation (to avoid rate limits) -    https://developer.github.com/v3/auth/#basic-authentication 
 
## Task 1.
 Your mongdb should contain users which will have the following information: username: username, date account created, location,   number of followers, number following.
 Set up your mongo database and a model of the user it will accept.
 
 ## Task 2.
 Create a route that will return a user from the github API with the route:
 {url}/github/users/{username}
 When somebody accesses your app at the above url, you should display the user on a views page. 
 Use this connector to do this:
 https://api.github.com/users/{username}

## Task 3.
 Make routes for all the CRUD commands (Create Read Update Delete)
 
 ## Task 4.
  Make a route to add a user from the github API to your repository.
 
 ## Task 5.
  Make a  new route: /github/users/{userName}/repositories That will list the names of all public repositories for the provided username
  Make use of the github API: https://api.github.com/users/{username}/repos
  Once this page exists, add a link “Repositories” to the page created in Task 1, so that as a website user you can navigate from somebodies profile to    their list of repositories
 
## Task 6.
 Add a new route /github/users/:username/repos/:repoName
 On this page display the names of files & folders (as determined by an API call) for the selected usernames’ repository.
 
 ## Task 7.
  Stretch excercise 
  Make a new route GET /github/users/:username/repos/:repoName/*path
  Path will be path to file I.E app/controllers/HelloWorldController.scala, you can base64 encode path for ease
  If the path given resolves to a file, display the plaintext of the file on the page.
  If the path given is a folder, display the files & foldernames (In the same way it's displayed in the first part of Task 4)
## Task 8. 
 (Assuming task 7 stretch is complete)
 Link up the pages. I.E the first step of task 5 doesn't only list the files/folders but they are hyperlinks that will direct to the correct page in the  stretch exercise.
 For example /github/users/jxr227/repos/ROScratch  Will show various files and folders including the folder "exercises". If you click "exercises" you  will be directed to:
 /github/users/jxr227/repos/ROScratch/exercises
 
 Do the same on the /*path pages, so that you can, from the top level of a repository, browse all folders and files
