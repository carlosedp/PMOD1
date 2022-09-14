import mill._
import mill.scalalib._
import scalafmt._
import coursier.MavenRepository
import $ivy.`com.goyeau::mill-scalafix::0.2.10`
import com.goyeau.mill.scalafix.ScalafixModule

def mainClass = Some("Toplevel")

val defaultVersions = Map(
  "scala"            -> "2.13.8",
  "chisel3"          -> "3.5.4",
  "chiseltest"       -> "0.5.4",
  "scalatest"        -> "3.2.13",
  "organize-imports" -> "0.6.0",
  "scalautils"       -> "0.10.2"
)

trait HasChisel3 extends ScalaModule {
  override def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"edu.berkeley.cs::chisel3:${defaultVersions("chisel3")}",
    ivy"com.carlosedp::scalautils::${defaultVersions("scalautils")}"
  )
  override def scalacPluginIvyDeps = super.scalacPluginIvyDeps() ++ Agg(
    ivy"edu.berkeley.cs:::chisel3-plugin:${defaultVersions("chisel3")}"
  )
}

trait HasChiselTests extends ScalaModule {
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

trait ScalacOptions extends ScalaModule {
  override def scalacOptions = T {
    super.scalacOptions() ++ Seq(
      "-unchecked",
      "-deprecation",
      "-language:reflectiveCalls",
      "-feature",
      "-Xcheckinit",
      "-Xfatal-warnings",
      "-Ywarn-dead-code",
      "-Ywarn-unused",
      "-P:chiselplugin:genBundleElements"
    )
  }
}

object pmod1 extends ScalaModule with HasChisel3 with HasChiselTests with CodeQuality with ScalacOptions {
  // Add Snapshot repository
  def repositoriesTask = T.task { // Add snapshot repositories in case needed
    super.repositoriesTask() ++ Seq("oss", "s01.oss")
      .map(r => s"https://$r.sonatype.org/content/repositories/snapshots")
      .map(MavenRepository(_))
  }
  def scalaVersion = defaultVersions("scala")
}

// Toplevel commands
def lint(ev: eval.Evaluator) = T.command {
  mill.main.MainModule.evaluateTasks(
    ev,
    Seq("__.fix", "+", "mill.scalalib.scalafmt.ScalafmtModule/reformatAll", "__.sources"),
    mill.define.SelectMode.Separated
  )(identity)
}
def deps(ev: eval.Evaluator) = T.command {
  mill.scalalib.Dependency.showUpdates(ev)
}
