import chisel3._
import chisel3.util._

/**
 * The RingLed component from PMOD1 LED
 *
 * @constructor creates a new RingLED object.
 * @param io.BankA (Output) Enables the bank A from the ring of LEDs. (0-7)
 * @param io.BankB (Output) Enables the bank B from the ring of LEDs. (8-15)
 * @param io.Ser (Output) Serial data output to the LED controller (DS).
 * @param io.RClk (Output) Register Latch (ST_CP)
 * @param io.SRClk (Output) Shift register clock (SH_CP).
 */
class RingLed(freq: Int) extends Module {
  val io = IO(new Bundle {
    val BankA = Output(Bool())
    val BankB = Output(Bool())
    val Ser   = Output(Bool())
    val RClk  = Output(Bool())
    val SRClk = Output(Bool())
  })

  val RClk       = RegInit(false.B)
  val BankA      = RegInit(false.B)
  val BankB      = RegInit(false.B)
  val bitsReg    = RegInit(7.U)
  val LEDs_out   = RegInit((1.U(8.W)))
  val LEDs_total = RegInit((1.U(16.W)))

  // Walk the LEDs that should be on and serialize the output
  // Run at input clock/500
  val tick = tickGen(freq, freq / 500)
  when(tick) {
    when(bitsReg =/= 0.U) {
      bitsReg := bitsReg - 1.U
      RClk := false.B
    }.elsewhen(bitsReg === 0.U) {
      bitsReg := 7.U
      RClk := true.B
    }
  }
  io.SRClk := tick
  io.Ser := LEDs_out(bitsReg)
  io.RClk := RClk

  // // Advance 1 count every second
  val (_, counterWrap) = Counter(true.B, freq)
  when(counterWrap) {
    LEDs_out := Mux(LEDs_out >= 128.U, 1.U, LEDs_out << 1.U)
    LEDs_total := Mux(LEDs_total >= 32768.U, 1.U, LEDs_total << 1.U)
  }

  // Alternate between Bank A and Bank B based on counter
  when(LEDs_total <= 128.U) { BankA := true.B; BankB := false.B }.otherwise { BankA := false.B; BankB := true.B }
  io.BankA := BankA
  io.BankB := BankB

  // Utility Functions
  def tickGen(freq: Int, targetFreq: Int) = {
    val cnt    = ((freq + targetFreq / 2) / targetFreq - 1)
    val cntreg = RegInit(cnt.U(log2Ceil(cnt).W))
    cntreg := Mux(cntreg === 0.U, cnt.U, cntreg - 1.U)
    cntreg === 0.U
  }
}
