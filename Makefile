AVR_ARDUINO = lib/ArduinoCore-avr-master
AVR_UART = lib/avr-uart-master
USB = /dev/cu.usbserial-14130

.SUFFIXES: .arduinoCode .c .cpp .s .o .elf .hex .fuse
JAVA = /usr/bin/java
CC = avr-gcc
CPP = avr-g++
AS = avr-as
SIZE = avr-size
LD = avr-ld
OBJ_COPY = avr-objcopy

F_CPU = 16000000L
MCU = atmega328p
DUDE = avrdude

CPP_FLAGS = -Os -mmcu=$(MCU) -DF_CPU=$(F_CPU) -std=gnu++11 
CC_FLAGS = -Os -mmcu=$(MCU) -DF_CPU=$(F_CPU) -std=gnu99
AS_FLAGS = -mmcu=$(MCU)

CC_INCL = -I$(AVR_ARDUINO) -I$(AVR_ARDUINO)/cores/arduino -I$(AVR_ARDUINO)/variants/standard -I$(AVR_UART)

.c.o:
	$(CC) $(CC_FLAGS) $(CC_INCL) -c -o $@ $<

.c.s:
	$(CC) $(AS_FLAGS) $(CC_INCL) -S -O0 -o $@ $<

.cpp.o:
	$(CPP) $(CC_FLAGS) $(CC_INCL) -c -o $@ $<

.arduinoCode.s:
	$(JAVA) -cp classes fr.ubordeaux.arduinoCode.Main $< > $@ 2> $*.log

.s.o:
	$(AS) $(AS_FLAGS) -o $@ $<

.o.elf: 
	@make $(AVR_UART)/uart.o $(AVR_ARDUINO)/cores/arduino/wiring_digital.o $(AVR_ARDUINO)/cores/arduino/wiring.o $(AVR_ARDUINO)/cores/arduino/hooks.o src/arduinoCodeMain.o 
	$(CC) $(CC_FLAGS) -o $@ $< $(AVR_UART)/uart.o $(AVR_ARDUINO)/cores/arduino/wiring_digital.o $(AVR_ARDUINO)/cores/arduino/wiring.o $(AVR_ARDUINO)/cores/arduino/hooks.o src/arduinoCodeMain.o 

.elf.hex:
	$(SIZE) --format=avr --mcu=$(MCU) $<
	$(OBJ_COPY) -O ihex -R -eeprom $< $@

.hex.fuse:
	$(DUDE) -p $(MCU) -c arduino -P $(USB) -b115200 -D -U flash:w:$<:i > $@
	@rm $@



