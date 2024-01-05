package models

case class gitCUDParameters(CUD: String,
                            login: String,
                            repoName: String,
                            message: String,
                            fileName: String,
                            content: Option[String],
                            path: String)
