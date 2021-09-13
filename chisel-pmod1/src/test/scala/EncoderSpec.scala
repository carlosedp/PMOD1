import chisel3._
import chiseltest._
import org.scalatest._
import flatspec._
import matchers.should._

class EncoderSpec extends AnyFlatSpec with ChiselScalatestTester with Matchers {
  val countValue = 8
  "Encoder switch" should "click" in {
    test(new Encoder(countValue, 20000)) { c =>
      c.io.SwOut.expect(false.B)
      c.io.SwIn.poke(true.B)
      c.clock.step(100)
      c.io.SwOut.expect(true.B)
    }
  }
}
