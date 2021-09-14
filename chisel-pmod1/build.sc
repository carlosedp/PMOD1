import mill._
import mill.scalalib._
import scalafmt._
import coursier.MavenRepository
import $ivy.`com.goyeau::mill-scalafix:0.2.4`
import com.goyeau.mill.scalafix.ScalafixModule

def mainClass = Some("Toplevel")

val defaultVersions = Map(
  "scala"            -> "2.13.6",
  "chisel3"          -> "3.5-SNAPSHOT",
  "chiseltest"       -> "0.5-SNAPSHOT",
  "scalatest"        -> "3.2.9",
  "organize-imports" -> "0.5.0"
)
// val binCrossScalaVersions = Seq("2.12.10")

trait HasChisel3 extends ScalaModule {
  override def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"edu.berkeley.cs::chisel3:${defaultVersions("chisel3")}"
  )
  override def scalacPluginIvyDeps = super.scalacPluginIvyDeps() ++ Agg(
    ivy"edu.berkeley.cs:::chisel3-plugin:${defaultVersions("chisel3")}"
  )
}

trait HasChiselTests extends CrossSbtModule {
  object test extends Tests {
    override def ivyDeps = super.ivyDeps() ++ Agg(
      ivy"org.scalatest::scalatest:${defaultVersions("scalatest")}",
      ivy"edu.berkeley.cs::chiseltest:${defaultVersions("chiseltest")}"
    )

    def testFramework = "org.scalatest.tools.Framework"

    def testOne(args: String*) = T.command {
      super.runMain("org.scalatest.run", args: _*)
    }
  }
}

trait CodeQuality extends ScalafixModule with ScalafmtModule {
  def scalafixIvyDeps = Agg(ivy"com.github.liancheng::organize-imports:${defaultVersions("organize-imports")}")
}

trait Aliases extends mill.Module {
  def fmt() = T.command {
    toplevel.reformat()
    toplevel.fix()
  }
  def deps(ev: eval.Evaluator) = T.command {
    mill.scalalib.Dependency.updates(ev)
  }
}

trait ScalacOptions extends ScalaModule {
  override def scalacOptions = super.scalacOptions() ++ Seq(
    "-unchecked",
    "-deprecation",
    "-language:reflectiveCalls",
    "-feature",
    "-Xcheckinit",
    "-Xfatal-warnings",
    "-Ywarn-value-discard",
    "-Ywarn-dead-code",
    "-Ywarn-unused"
  )
}

object toplevel
    extends CrossSbtModule
    with HasChisel3
    with HasChiselTests
    with CodeQuality
    with Aliases
    with ScalacOptions {
  // Add Snapshot repository
  def repositories = super.repositories ++ Seq(
    MavenRepository("https://oss.sonatype.org/content/repositories/snapshots")
  )
  override def millSourcePath = super.millSourcePath
  def crossScalaVersion       = defaultVersions("scala")
}
