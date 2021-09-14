import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest._

import flatspec._

// Generate VCD output
class RingLedVCDSpec extends AnyFlatSpec with ChiselScalatestTester {
  "Waveform" should "pass" in {
    test(new RingLed(500)).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
      c.clock.setTimeout(0)
      c.clock.step(10000)
    }
  }
}
