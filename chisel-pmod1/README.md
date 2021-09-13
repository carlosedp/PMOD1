# PMOD1 - Rotary Encoder and RingLed PMOD

This is a project built with Chisel HDL for the PMOD1, a Rotary Encoder and RingLED PMOD.

The Chisel sources uses a `-board` parameter to define which board is used and generate the proper PLL and build. To support this, an utility module was created.

## Building

The project provides two building methods. The first and recommended is using [Fusesoc](https://github.com/olofk/fusesoc), a package manager that handles all board backend files and configuration. It also makes adding support to new boards and vendors much easier.

A second method is using the built-in Makefile that generates the output Verilog code that can be imported into the board EDA manually.

### Fusesoc build and generation

<details>
  <summary>Installing FuseSOC</summary>

To install Fusesoc (requires Python3 and pip3):

```sh
pip3 install --upgrade --user fusesoc
```

Check if it's working:

```sh
$ fusesoc --version
1.12.0
```

If the terminal reports an error about the command not being found check that the directory `~/.local/bin` is in your command search path (`export PATH=~/.local/bin:$PATH`).

</details>

Fusesoc allows multiple boards from different vendors to be supported by the project. It uses chisel-generator to generate Verilog from Scala sources and calls the correct board EDA backend to create it's project files.

For example, to generate the programming files for the **ULX3s** board based on Lattice ECP5:

```sh
mkdir fusesoc-pmod1 && cd fusesoc-pmod1

fusesoc library add fusesoc-cores https://github.com/fusesoc/fusesoc-cores

# Since this project is not a standalone repo (but a folder in an umbrella repo)
# We clone it locally and add the library as a local dir.
git clone https://github.com/carlosedp/PMOD1
fusesoc library add pmod1 $(pwd)/PMOD1/chisel-pmod1

fusesoc run --target=ulx3s_85 carlosedp:demo:pmod1

# This requires all FPGA Toolchain applications to be installed locally, keep reading for a portable solution using containers
...
# Output bitstream will be on build/carlosedp_demo_chiselblinky_0/ulx3s_85-trellis
❯ ll build/carlosedp_demo_chiselblinky_0/ulx3s_85-trellis
total 2.7M
-rw-r--r-- 1 cdepaula staff  774 Apr  7 18:53 carlosedp_demo_chiselblinky_0.eda.yml
-rw-r--r-- 1 cdepaula staff  545 Apr  7 18:53 carlosedp_demo_chiselblinky_0.tcl
-rw-r--r-- 1 cdepaula staff  435 Apr  7 18:53 carlosedp_demo_chiselblinky_0.mk
-rw-r--r-- 1 cdepaula staff  608 Apr  7 18:53 Makefile
-rw-r--r-- 1 cdepaula staff 9.5K Apr  7 18:53 carlosedp_demo_chiselblinky_0.blif
-rw-r--r-- 1 cdepaula staff 655K Apr  7 18:53 carlosedp_demo_chiselblinky_0.json
-rw-r--r-- 1 cdepaula staff  44K Apr  7 18:53 carlosedp_demo_chiselblinky_0.edif
-rw-r--r-- 1 cdepaula staff  45K Apr  7 18:53 yosys.log
-rw-r--r-- 1 cdepaula staff 8.7K Apr  7 18:53 next.log
-rw-r--r-- 1 cdepaula staff 1.9M Apr  7 18:53 carlosedp_demo_chiselblinky_0.bit
```

Just program it to your FPGA with `OpenOCD` or in ULX3S case, [`ujprog`](https://github.com/f32c/tools/tree/master/ujprog)

### Building on containers

The standard build process uses locally installed tools like Java (for Chisel generation), Yosys, NextPNR, Vivado and others. Fusesoc also supports building the complete workflow by using containers thru a command launcher.

To use it:

```sh
# Download the command wrapper
wget https://gist.github.com/carlosedp/c0e29d55e48309a48961f2e3939acfe9/raw/bfeb1cfe2e188c1d5ced0b09aabc9902fdfda6aa/runme.py
chmod +x runme.py

# Run fusesoc with the wrapper as an environment var
EDALIZE_LAUNCHER=$(realpath ./runme.py) fusesoc run --target=ulx3s_85 carlosedp:demo:pmod1

# The output files will be on the local ./build dir like before
# Programming instructions will be printed-out.
```

### Adding support to new boards

<details>
  <summary>Click to expand</summary>

Support for new boards can be added in the `pmod1.core` file and programming instructions in the `proginfo/buildconfig.yaml` together with a board template text.

Three sections are required:

#### Fileset

Filesets lists the dependency from the chisel-generator, that outputs Verilog from Chisel (Scala) code. It also contains the static files used for each board like constraints and programming config that must be copied to the output project dir and used by EDA. The programming info text template is also added.

```yaml
  ulx3s-85:
    depend: ["fusesoc:utils:generators:0.1.6"]
    files:
      - constraints/ecp5-ulx3s.lpf: { file_type: LPF }
      - openocd/ft231x.cfg: { file_type: user }
      - openocd/LFE5U-85F.cfg: { file_type: user }
      - proginfo/ulx3s-template.txt: { file_type: user }
```

#### Generate

The generator section contains the Chisel generator parameters. It has the arguments to be passed to Chisel (the board), the project name and the output files created by the generator to be used by the EDA.

```yaml
  ulx3s:
    generator: chisel
    parameters:
      extraargs: "-board ulx3s"
      chiselproject: toplevel
      copy_core: true
      output:
        files:
          - generated/Toplevel.v: { file_type: verilogSource }
          - generated/pll_ulx3s.v: { file_type: verilogSource }
```

#### Target

Finally the target section has the board information to be passed to the EDA tools. Parameters like the package/die or extra parameters to synthesis or PnR. This is highly dependent of the EDA backend. It's name is the one passed on the `--target=` param on FuseSoc. It also references the fileset and generate configs.

```yaml
  ulx3s_85:
    default_tool: trellis
    description: ULX3S 85k version
    filesets: [ulx3s-85, proginfo]
    generate: [ulx3s]
    hooks:
      post_run: [ulx3s-85f]
    tools:
      diamond:
        part: LFE5U-85F-6BG381C
      trellis:
        nextpnr_options: [--package, CABGA381, --85k]
    toplevel: Toplevel
```

#### Post-run script

If you desire to add a programming information text output after generating the bitstream files, add the board to the `scripts` section (and to it's target hooks) calling the proginfo.py with a board identifier that must match the `boardconfig.yaml` file in the `proginfo` dir.

```yaml
  artya7-35t:
    cmd : [python3, proginfo.py, artya7-35t]
```

The `boardconfig.yaml` file must contain the files names used by each board and a corresponding template `.txt` file that will contain the output text. This will be printed after bitstream generation.

</details>
