import sbt._

class MaltProject(info: ProjectInfo) extends DefaultProject(info) {
  val slf4jVersion      = "1.6.0"
  val luceneVersion     = "3.1.0"
  val scalaTestVersion  = "1.3"
  
  val lucene = "org.apache.lucene" % "lucene-core" % luceneVersion
  
  val sfl4japi = "org.slf4j" % "slf4j-api" % slf4jVersion % "runtime"
  val sfl4jnop = "org.slf4j" % "slf4j-nop" % slf4jVersion % "runtime"
  
  val scalaTest = "org.scalatest" % "scalatest" % "1.3"
  
  val scalaToolsSnapshots = "Scala Tools Repository" at
      "http://nexus.scala-tools.org/content/repositories/snapshots/"
  val sonatypeNexusSnapshots = "Sonatype Nexus Snapshots" at
      "https://oss.sonatype.org/content/repositories/snapshots"
  val sonatypeNexusReleases = "Sonatype Nexus Releases" at 
      "https://oss.sonatype.org/content/repositories/releases"
  val fuseSourceSnapshots = "FuseSource Snapshot Repository" at 
      "http://repo.fusesource.com/nexus/content/repositories/snapshots"
}