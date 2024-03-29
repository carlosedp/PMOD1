SHELL = bash
# Project name
project = pmod1

generated_files = generated
CHISELPARAMS = --target:fpga --emission-options=disableMemRandomization,disableRegisterRandomization

# Targets
chisel: check-board-vars  ## Generates Verilog code from Chisel sources using Mill
	scripts/mill $(project).run -board ${BOARD} $(CHISELPARAMS) -td $(generated_files)

chisel_tests:
	scripts/mill $(project).test

check: chisel_tests ## Run Chisel tests using Mill

fmt: ## Formats code using scalafmt and scalafix
	scripts/mill lint

check-board-vars:
	@test -n "$(BOARD)" || (echo "Set BOARD variable to one of the supported boards: " ; cat pmod1.core|grep "\-board" |cut -d '-' -f 2|sed s/\"//g | sed s/board\ //g |tr -s '\n' ','| sed 's/,$$/\n/'; echo "Eg. make chisel BOARD=ulx3s"; echo; exit 1)

clean:   ## Clean all generated files
	@./scripts/mill clean
	@rm -rf obj_dir test_run_dir target
	@rm -rf $(generated_files)
	@rm -rf out

help:
	@echo "Makefile targets:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = "[:##]"}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$4}'
	@echo ""

.PHONY: chisel clean prog help
.DEFAULT_GOAL := help
