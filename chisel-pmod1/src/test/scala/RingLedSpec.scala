import chisel3.iotesters._
import org.scalatest.flatspec._
import org.scalatest.matchers.should._

// Generate VCD output
class RingLedVCDSpec extends AnyFlatSpec with Matchers {
  "WaveformCounter" should "pass" in {
    Driver.execute(
      Array("--generate-vcd-output", "on"),
      () => new RingLed(500)
    ) { c =>
      new WaveformCounterTester(c, 10)
    } should be(true)
  }
}

class WaveformCounterTester(dut: RingLed, cycles: Int) extends PeekPokeTester(dut) {
  for (_ <- 0 until cycles) {
    step(1000)
  }
}
