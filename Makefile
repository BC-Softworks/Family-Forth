# Makefile for FamilyForth's generated assembly code
# Requires ca65 and ld65 to build and 6502_tester to test
SRC_DIR				:=		lib
ASM_DIR				:=		build/asm
CONFIG_DIR			:=		cfg
BUILD_DIR			:=		build/objects
DIST_DIR			:=		dist
TEST_DIR			:=		src/test/lib
TEST_OK_DIR			:=		$(TEST_DIR)/ok
TEST_EXEC_DIR		:=		../6502_tester
COVERAGE_DIR		:=		coverage

PROJECT				:=		familyforth
TARGET				:=		$(DIST_DIR)/$(PROJECT).nes
DEBUG				:=		$(DIST_DIR)/$(PROJECT).dbg
SOURCES				:=		$(shell find $(ASM_DIR) -type f -name '*.asm')
OBJECTS				:=		$(SOURCES:$(ASM_DIR)/%.asm=$(BUILD_DIR)/%.o)
NES_CFG				:=		$(CONFIG_DIR)/nes.cfg

TEST_OK				:=		$(shell find $(TEST_OK_DIR) -type f -name '*.test.json' | sort)
TEST_IDS			:=		$(TEST_OK:%.test.json=%.test)

COVARAGE			:=		$(COVERAGE_DIR)/lcov.info
COVERAGE_SEGMENTS	:=		"CODE"

AS					:=		ca65
ASFLAGS				:=		--cpu 6502 --target nes --debug-info
LD					:=		ld65
LDFLAGS				:=		
TEST_EXEC			:=		$(TEST_EXEC_DIR)/6502_tester

TEST_FLAGS			:=		--debug=$(DEBUG) --coverage=$(COVARAGE) --segment=$(COVERAGE_SEGMENTS)
TEST_OK_FLAGS		:=		--quiet-summary --quiet-ok

.PHONY : all build test prepare clean

all : build

build : $(TARGET)

$(TARGET) : $(OBJECTS) $(NES_CFG)
	mkdir -p $(DIST_DIR)
	$(LD) $(LDFLAGS) -o $(TARGET) --dbgfile $(DEBUG) --config $(NES_CFG) --obj $(OBJECTS)

$(BUILD_DIR)/%.o : $(ASM_DIR)/%.asm
	mkdir -p $(BUILD_DIR)
	$(AS) $(ASFLAGS) -o $@ $<

build_prepare : 
	mvn package
	java -jar target/famiforthh-1.0-SNAPSHOT.jar 
	@echo "Assembly file generated."

test : $(TEST_EXEC) test_prepare $(TEST_IDS)
	@echo "All tests passed."

$(TEST_EXEC) :
	make -C $(TEST_EXEC_DIR)

test_prepare : $(COVERAGE_DIR)
	-rm $(COVARAGE)

$(COVERAGE_DIR) :
	mkdir -p $(COVERAGE_DIR)

$(TEST_OK_DIR)/%.test : $(TEST_OK_DIR)/%.test.json
	$(TEST_EXEC) $(TEST_FLAGS) $(TEST_OK_FLAGS) -t $<

clean :
	-rm -r $(ASM_DIR)
	-rm -r $(BUILD_DIR)