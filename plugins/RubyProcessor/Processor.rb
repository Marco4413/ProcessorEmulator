
# Copyright (c) 2021 [hds536jhmk](https://github.com/hds536jhmk/ProcessorEmulator)
# 
# Permission is hereby granted, free of charge, to any person
# obtaining a copy of this software and associated documentation
# files (the "Software"), to deal in the Software without
# restriction, including without limitation the rights to use,
# copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the
# Software is furnished to do so, subject to the following
# conditions:
# 
# The above copyright notice and this permission notice shall be
# included in all copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
# OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
# NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
# HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
# WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
# FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR

require "PEMUJRubyPluginAPI"
require_relative "Utils.rb"

# This class is a copy-paste of PEMU::Processor (But written in Ruby):
# https://github.com/hds536jhmk/ProcessorEmulator/blob/master/src/main/java/io/github/hds/pemu/processor/Processor.java
class Processor
	protected
	attr_accessor :is_running
	attr_accessor :register_words
	attr_accessor :reserved_stack_elements
	attr_accessor :flags_words
	attr_accessor :memory
	attr_accessor :clock
	attr_accessor :instruction_set
	attr_accessor :history
	attr_accessor :registers
	attr_accessor :flags
	attr_accessor :char_pressed
	attr_accessor :key_pressed
	attr_accessor :start_timestamp
	attr_accessor :is_paused
	attr_accessor :stepping

	public
	# Implementing interface PEMU::IProcessor
	include PEMU::IProcessor

	def initialize(config)
		self.is_running = false
		self.register_words = 2
		self.reserved_stack_elements = 1
		self.flags_words = 1

		self.memory = PEMU::Memory.new(
			config.memory_size,
			PEMU::Word.getClosestWord(config.bits)
		)

		self.clock = PEMU::Clock.new config.clock_frequency

		self.instruction_set = config.instruction_set
		self.history = PEMU::InstructionHistory.new

		self.registers = PEMU::RegisterHolder.new(
			PEMU::MemoryRegister.new(getProgramAddress() , "Instruction Pointer", self.memory, 0),
			PEMU::MemoryRegister.new(self.memory.size - 1, "Stack Pointer"      , self.memory, 1)
		)

		self.flags = PEMU::FlagHolder.new(
			PEMU::MemoryFlag.new(false, "Zero Flag" , self.memory, self.register_words, 0),
			PEMU::MemoryFlag.new(false, "Carry Flag", self.memory, self.register_words, 1)
		)

		self.char_pressed = "\0"
		self.key_pressed  = PEMU::KeyEvent::VK_UNDEFINED
		self.start_timestamp = 0
		self.is_paused = false
		self.stepping  = false
	end

	def self.getDummyProcessor(config)
		return PEMU::DummyProcessor.new(
			config,
			[ PEMU::DummyMemoryRegister.new("Instruction Pointer"), PEMU::DummyMemoryRegister.new("Stack Pointer") ],
			[ PEMU::DummyMemoryFlag.new("Zero Flag"), PEMU::DummyMemoryFlag.new("Carry Flag") ]
		)
	end

	def getFlags()
		return self.flags.toArray
	end

	def getRegisters()
		return self.registers.to_a
	end

	def getFlag(shortName)
		return self.flags.getFlag shortName
	end

	def getRegister(shortName)
		return self.registers.getRegister shortName
	end

	def getMemory()
		return self.memory
	end

	def getClock()
		return self.clock
	end

	def getInstructionSet()
		return self.instruction_set
	end

	def getKeyPressed()
		return self.key_pressed
	end

	def setKeyPressed(key)
		self.key_pressed = key
	end

	def getCharPressed()
		return self.char_pressed
	end

	def setCharPressed(char)
		self.char_pressed = char
	end

	def getInfo()
        return "\tClock:\t#{ PEMU::StringUtils.getEngNotation(self.clock.getFrequency(), "Hz") }\n"\
               "\tMemory:\t#{ self.memory.getSize() }x#{ self.memory.getWord().TOTAL_BYTES } Bytes\n"\
               "\tInstructions:\t#{ self.instruction_set.getSize() }\n";
	end

	def getInstructionHistory()
		return self.history
	end

	def getTimeRunning()
		return -1 if !self.is_running
		return get_time_millis() - self.start_timestamp
	end

	def loadProgram(program)
		return "Couldn't load program because there's not enough space!" if program.length > self.memory.getSize() - getReservedWords()
		self.memory.setValuesAt(getProgramAddress(), program)
		return nil
	end

	def getProgramAddress()
		return self.register_words + self.flags_words
	end

	def getReservedWords()
		return self.register_words + self.flags_words + self.reserved_stack_elements
	end

	def isRunning()
		return self.is_running
	end

	def run()
		return if self.is_running

		self.start_timestamp = get_time_millis()
		self.is_running = true
		while self.is_running do
			if self.clock.update() && (self.stepping || !self.is_paused) then
				self.stepping = false

				ip = self.registers.getRegister("IP")

				if ip.getValue() >= self.memory.getSize() then
					stop()
				else
					currentIP = ip.getValue()
					instruction = self.instruction_set.getInstruction(self.memory.getValueAt(currentIP))
					raise PEMU::InstructionError.new("Unknown", "Unknown Instruction", currentIP) if instruction == nil
					self.history.put(currentIP.to_i, instruction.getKeyword())

					ip.setValue(currentIP + instruction.getWords())
					begin
						instruction.execute(
							self, instruction.getArgumentsCount() == 0 ? [] : self.memory.getValuesAt(currentIP + 1, instruction.getArgumentsCount())
						)
					rescue Exception => err
						raise PEMU::InstructionError.new(instruction.getKeyword(), err.to_s, currentIP)
					end
				end
			end
		end
	end

	def stop()
		self.is_running = false
	end

	def isPaused()
		return self.is_paused
	end

	def pause()
		self.is_paused = true
	end

	def resume()
		self.is_paused = false
	end

	def step()
		self.stepping = true
	end
end
