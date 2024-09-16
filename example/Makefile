# Makefile for FamilyForth's example project
# Requires ca65 and ld65 to build and 6502_tester to test
SRC_DIR				:=		src
ASM_DIR				:=		build/asm
CONFIG_DIR			:=		cfg
BUILD_DIR			:=		build/objects
DIST_DIR			:=		dist
TEST_DIR			:=		test
TEST_OK_DIR			:=		$(TEST_DIR)/ok
TEST_EXEC_DIR		:=		../../6502_tester
COVERAGE_DIR		:=		coverage

PROJECT				:=		example
TARGET				:=		$(DIST_DIR)/$(PROJECT).nes
DEBUG				:=		$(DIST_DIR)/$(PROJECT).dbg
SOURCES				:=		$(shell find $(ASM_DIR) -type f -name '*.asm')
OBJECTS				:=		$(SOURCES:$(ASM_DIR)/%.asm=$(BUILD_DIR)/%.o)
NES_CFG				:=		$(CONFIG_DIR)/example.cfg

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

.PHONY : all prepare build test clean

all : prepare build test

prepare :
	mkdir -p $(ASM_DIR)
	mkdir -p $(BUILD_DIR)
	java -jar ../target/famiforth-1.0-SNAPSHOT.jar --in src/pong.f --out build/asm ; 
	@echo "Assembly file generated."

build : $(TARGET)

$(TARGET) : $(OBJECTS) $(NES_CFG)
	mkdir -p $(DIST_DIR)
	$(LD) $(LDFLAGS) -o $(TARGET) --dbgfile $(DEBUG) --config $(NES_CFG) --obj $(OBJECTS)

$(BUILD_DIR)/%.o : $(ASM_DIR)/%.asm
	mkdir -p $(BUILD_DIR)
	$(AS) $(ASFLAGS) -o $@ $<

test_prepare : $(COVERAGE_DIR)
	-rm $(COVARAGE)

test : $(TEST_EXEC) test_prepare $(TEST_IDS)
	@echo "All tests passed."

$(TEST_EXEC) :
	make -C $(TEST_EXEC_DIR)

$(COVERAGE_DIR) :
	mkdir -p $(COVERAGE_DIR)

$(TEST_OK_DIR)/%.test : $(TEST_OK_DIR)/%.test.json
	$(TEST_EXEC) $(TEST_FLAGS) $(TEST_OK_FLAGS) -t $<

clean :
	-rm -r $(ASM_DIR)
	-rm -r $(BUILD_DIR)