import sbt._

class TasteProject(info: ProjectInfo) extends DefaultProject(info) {
  val slf4jVersion      = "1.6.1"
  val luceneVersion     = "3.1.0"
  val scalaTestVersion  = "1.3"
  val ioVersion         = "2.0.1"
  val utilVersion       = "1.8.9"
  
  val io = "commons-io" % "commons-io" % ioVersion
  val codec = "commons-codec" % "commons-codec" % "1.5"
  
  val thrift = "thrift" % "libthrift" % "0.5.0"
  
  val util = "com.twitter" % "util-core" % utilVersion
  val utilC = "com.twitter" % "util-reflect" % utilVersion
  
  val lucene  = "org.apache.lucene" % "lucene-core" % luceneVersion
  val luceneA = "org.apache.lucene" % "lucene-analyzers" % luceneVersion
  val luceneQ = "org.apache.lucene" % "lucene-queryparser" % luceneVersion
    
  val sfl4japi = "org.slf4j" % "slf4j-api" % slf4jVersion
  val sfl4jlog4j = "org.slf4j" % "slf4j-log4j12" % slf4jVersion
  val log4j = "log4j" % "log4j" % "1.2.9"
  
  val scalaTest = "org.scalatest" % "scalatest" % "1.3" % "test"
  val mockito = "org.mockito" % "mockito-all" % "1.8.5" % "test"

  // val repo1 = "repo1" at 
  //     "http://repo1.maven.org/maven2/"
  val twitter = "Twitter" at
      "http://maven.twttr.com"
  val scalaToolsSnapshots = "Scala Tools Repository" at
      "http://nexus.scala-tools.org/content/repositories/snapshots/"
  val sonatypeNexusSnapshots = "Sonatype Nexus Snapshots" at
      "https://oss.sonatype.org/content/repositories/snapshots"
  val sonatypeNexusReleases = "Sonatype Nexus Releases" at 
      "https://oss.sonatype.org/content/repositories/releases"
  val fuseSourceSnapshots = "FuseSource Snapshot Repository" at 
      "http://repo.fusesource.com/nexus/content/repositories/snapshots"
  val clojars = "Clojars" at 
      "http://clojars.org/repo/"
      
  def thriftSources = (mainSourcePath / "thrift" ##) ** "*.thrift"
  def thriftBin = "thrift"
  
  def compileThriftAction(lang: String) = task {
    import Process._
    outputPath.asFile.mkdirs()
    val tasks = thriftSources.getPaths.map { path =>
      execTask { "%s --gen %s -o %s %s".format(thriftBin, lang, outputPath.absolutePath, path) }
    }
    if (tasks.isEmpty) None else tasks.reduceLeft { _ && _ }.run
  } describedAs("Compile thrift into %s".format(lang))
  
  lazy val compileThriftJava = compileThriftAction("java")

  override def compileAction = super.compileAction dependsOn(compileThriftJava)
  
  def generatedJavaDirectoryName   = "gen-java"
  def generatedJavaPath   = outputPath / generatedJavaDirectoryName

  override def mainSourceRoots = super.mainSourceRoots +++ (outputPath / generatedJavaDirectoryName ##)

  lazy val cleanGenerated = cleanTask(generatedJavaPath) describedAs "Clean generated source folders"

  override def cleanAction = super.cleanAction dependsOn(cleanGenerated)
}