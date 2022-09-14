import chisel3._
import chiseltest._
import org.scalatest._
import flatspec._
import matchers.should._

class EncoderSpec extends AnyFlatSpec with ChiselScalatestTester with Matchers {
  val countValue = 8
  "Encoder switch" should "click and unclick" in {
    test(new Encoder(countValue, 20000)) { c =>
      c.io.SwOut.expect(false.B)
      c.io.SwIn.poke(true.B)
      c.clock.step(100)
      c.io.SwOut.expect(true.B)
      c.io.SwIn.poke(false.B)
      c.clock.step(100)
      c.io.SwOut.expect(false.B)
    }
  }

  "Rotary Encoder" should "pass turning right one click" in {
    test(new Encoder(countValue, 20000)) { c =>
      c.io.CW.expect(false.B)
      c.io.Count.expect(0.U)
      c.clock.step()
      // One click right
      c.io.EncA.poke(true.B)
      c.clock.step(10)
      c.io.EncB.poke(true.B)
      c.clock.step(10)
      c.io.EncA.poke(false.B)
      c.clock.step(10)
      c.io.EncB.poke(false.B)
      c.clock.step(100)

      c.io.CW.expect(true.B)
      c.io.Count.expect(1.U)
    }
  }
  it should "pass turning left one click" in {
    test(new Encoder(countValue, 20000)) { c =>
      c.io.CW.expect(false.B)
      c.io.Count.expect(0.U)
      c.clock.step()
      c.io.EncB.poke(true.B)
      c.clock.step(10)
      c.io.EncA.poke(true.B)
      c.clock.step(10)
      c.io.EncB.poke(false.B)
      c.clock.step(10)
      c.io.EncA.poke(false.B)
      c.clock.step(100)
      c.io.CW.expect(false.B)
      c.io.Count.expect(7.U)
    }
  }
  it should "pass turning right two clicks" in {
    test(new Encoder(countValue, 20000)) { c =>
      c.io.CW.expect(false.B)
      c.io.Count.expect(0.U)
      c.clock.step()
      // One click right
      c.io.EncA.poke(true.B)
      c.clock.step(10)
      c.io.EncB.poke(true.B)
      c.clock.step(10)
      c.io.EncA.poke(false.B)
      c.clock.step(10)
      c.io.EncB.poke(false.B)
      c.clock.step(100)

      c.io.CW.expect(true.B)
      c.io.Count.expect(1.U)
      // Second click right
      c.io.EncA.poke(true.B)
      c.clock.step(10)
      c.io.EncB.poke(true.B)
      c.clock.step(10)
      c.io.EncA.poke(false.B)
      c.clock.step(10)
      c.io.EncB.poke(false.B)
      c.clock.step(100)
      c.io.CW.expect(true.B)
      c.io.Count.expect(2.U)
    }
  }
  it should "pass turning right two clicks and back one" in {
    test(new Encoder(countValue, 20000)) { c =>
      c.io.CW.expect(false.B)
      c.io.Count.expect(0.U)
      c.clock.step()
      // One click right
      c.io.EncA.poke(true.B)
      c.clock.step(10)
      c.io.EncB.poke(true.B)
      c.clock.step(10)
      c.io.EncA.poke(false.B)
      c.clock.step(10)
      c.io.EncB.poke(false.B)
      c.clock.step(100)

      c.io.CW.expect(true.B)
      c.io.Count.expect(1.U)
      // Second click right
      c.io.EncA.poke(true.B)
      c.clock.step(10)
      c.io.EncB.poke(true.B)
      c.clock.step(10)
      c.io.EncA.poke(false.B)
      c.clock.step(10)
      c.io.EncB.poke(false.B)
      c.clock.step(100)
      c.io.CW.expect(true.B)
      c.io.Count.expect(2.U)

      // Back one click
      c.io.EncB.poke(true.B)
      c.clock.step(10)
      c.io.EncA.poke(true.B)
      c.clock.step(10)
      c.io.EncB.poke(false.B)
      c.clock.step(10)
      c.io.EncA.poke(false.B)
      c.clock.step(100)
      c.io.CW.expect(false.B)
      c.io.Count.expect(1.U)
    }
  }
}
