import chisel3._
import chisel3.util._

/**
 * The Encoder module from PMOD1 LED
 *
 * @constructor creates a new rotary encoder object.
 * @param io.EncA (Input) The input A from the encoder
 * @param io.EncB (Input) The input B from the encoder
 * @param io.SwIn (Input) The switch input from the encoder
 * @param io.SwOut (Output) The filtered and debounced output from the encoder switch
 * @param io.CW (Output) The clockwise output from the encoder, 1 if clockwise, 0 if counter-clockwise
 * @param io.Count (Output) The count output from the encoder from 0 to countLength
 */
class Encoder(countLength: Int = 256, freq: Int) extends Module {
  val io = IO(new Bundle {
    val EncA  = Input(Bool())
    val EncB  = Input(Bool())
    val SwIn  = Input(Bool())
    val SwOut = Output(Bool())
    val CW    = Output(Bool())
    val Count = Output(UInt(log2Ceil(countLength).W))
  })

  // Sync, debounce and filter encoder pins
  // Invert encoder inputs since they are pulled-up
  // Generate a tick every 2Khz
  val tick          = tickGen(freq, 2000)
  val Sw_Filtered   = fullFilterIO(io.SwIn, tick)
  val EncA_Rising   = risingEdge(fullFilterIO(~io.EncA, tick))
  val EncB_Filtered = fullFilterIO(~io.EncB, tick)

  // Generate a counter and direction for the encoder
  val count     = RegInit(0.U(log2Ceil(countLength).W))
  val direction = RegInit(false.B)
  when(EncA_Rising) {
    when(EncB_Filtered) {
      count := count - 1.U
      direction := false.B
    }.otherwise {
      count := count + 1.U
      direction := true.B
    }
  }

  io.SwOut := Sw_Filtered
  io.CW := direction
  io.Count := count

  // Utility Functions
  def tickGen(freq: Int, targetFreq: Int) = {
    val cnt    = ((freq + targetFreq / 2) / targetFreq - 1)
    val cntreg = RegInit(cnt.U(log2Ceil(cnt).W))
    cntreg := Mux(cntreg === 0.U, cnt.U, cntreg - 1.U)
    cntreg === 0.U
  }

  // Credits to Martin Schoeberl on functions below
  // https://github.com/schoeberl/chisel-examples/blob/master/src/main/scala/util/Debounce.scala
  def sync(v: Bool) = RegNext(RegNext(v))

  def risingEdge(din: Bool) = din & !RegNext(din)

  def fallingEdge(din: Bool) = !din & RegNext(din)

  def filter(v: Bool, t: Bool) = {
    val reg = RegInit(0.U(3.W))
    when(t) {
      reg := Cat(reg(1, 0), v)
    }
    (reg(2) & reg(1)) | (reg(2) & reg(0)) | (reg(1) & reg(0))
  }

  def fullFilterIO(din: Bool, tick: Bool) = {
    val din_sync = sync(din)
    val din_Deb  = Reg(Bool())
    when(tick)(din_Deb := din_sync)
    filter(din_Deb, tick)
  }
}
