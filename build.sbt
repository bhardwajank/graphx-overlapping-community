name := "graphx-overlapping-community"

version := "1.0"

scalaVersion := "2.10.4"

organization := "com.github.bhardwajank"

licenses += ("Apache License 2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))

sparkComponents += "graphx"

spName := "bhardwajank/graphx-overlapping-community"

sparkVersion := "1.5.2"

credentials += Credentials(Path.userHome / ".ivy2" / ".sbtcredentials")
enablePlugins(SiteScaladocPlugin)
siteSubdirName in SiteScaladoc := "api/latest"

ghpages.settings
git.remoteRepo := "git@github.com:bhardwajank/graphx-overlapping-community.git"
