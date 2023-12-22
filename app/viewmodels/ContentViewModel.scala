package viewmodels

import models.ContentModel

case class ContentViewModel(
    file: Option[ContentModel],
    fileList: Option[List[ContentModel]],
    repoName: String,
    path: String,
    login: String
)
