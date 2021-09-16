import chisel3._
import chisel3.util.log2Ceil
import com.carlosedp.scalautils.ParseArguments

// Blinking LED top layer
class Toplevel(board: String, invReset: Boolean = true) extends Module {
  val io = IO(new Bundle {
    // Board LEDs
    val led0 = Output(Bool())
    val led1 = Output(Bool())
    val led2 = Output(Bool())
    val led3 = Output(Bool())
    val led4 = Output(Bool())
    val led6 = Output(Bool())

    // Rotary Encoder Pins
    val PMOD1_A0 = Input(Bool())
    val PMOD1_A1 = Input(Bool())
    val PMOD1_A2 = Input(Bool())

    // RingLED Pins
    val PMOD1_A3 = Output(Bool())
    val PMOD1_B0 = Output(Bool())
    val PMOD1_B1 = Output(Bool())
    val PMOD1_B2 = Output(Bool())
    val PMOD1_B3 = Output(Bool())
  })

  // Instantiate PLL module based on board
  val pll      = Module(new PLL0(board))
  val pllClock = 25000000
  pll.io.clki := clock
  // Define if reset should be inverted based on board switch
  val customReset = Wire(Bool())
  customReset := (if (invReset) ~reset.asBool() else reset)

  withClockAndReset(pll.io.clko, customReset) {
    // Instantiate the Blink module using 25Mhz from PLL output as heartbeat
    val bl = Module(new Blinky(pllClock, startOn = true))
    io.led0 := bl.io.led0

    // Instantiate the Encoder module with a count up to 8
    val countValue = 8
    val enc        = Module(new Encoder(countValue, pllClock))

    // Connect the encoder Switch
    enc.io.SwIn := io.PMOD1_A2
    io.led1 := enc.io.SwOut

    // Connect the rotary encoder
    enc.io.EncA := io.PMOD1_A0
    enc.io.EncB := io.PMOD1_A1
    // Use the rotary encoder counter to update the board LEDs
    val LEDs = Reg(UInt(log2Ceil(countValue).W))
    io.led2 := LEDs(0)
    io.led3 := LEDs(1)
    io.led4 := LEDs(2)
    // Indicate if the encoder is turning clockwise(on) or counter-clockwise(off)
    io.led6 := enc.io.CW
    LEDs := enc.io.Count

    // Instantiate and connect the Ring LED
    val ring = Module(new RingLed(pllClock))
    io.PMOD1_B0 := ring.io.Ser
    io.PMOD1_B1 := ring.io.RClk
    io.PMOD1_B2 := ring.io.SRClk
    io.PMOD1_A3 := ring.io.BankA
    io.PMOD1_B3 := ring.io.BankB
  }
}

// The Main object extending App to generate the Verilog code.
object Toplevel extends App {

  // Parse command line arguments and extract required parameters
  // pass the input arguments and a list of parameters to be extracted
  // The funcion will return the parameters map and the remaining non-extracted args
  val (params, chiselargs) = ParseArguments(args, List("board", "invreset"))
  val board: String =
    params.getOrElse("board", throw new IllegalArgumentException("The '-board' argument should be informed."))
  val invReset: Boolean =
    params.getOrElse("invreset", "true").toBoolean

  // Generate Verilog
  (new chisel3.stage.ChiselStage).emitVerilog(
    new Toplevel(board, invReset),
    chiselargs
  )
}
