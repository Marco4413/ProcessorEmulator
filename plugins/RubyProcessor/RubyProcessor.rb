
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
# OTHER DEALINGS IN THE SOFTWARE.

# NOTE: $stdin is set to nil, so stdin-based methods like "gets" don't work

# This file adds the module "PEMU" which gives easy access to commonly used classes
#  like PEMU::Processor
require "PEMUJRubyPluginAPI"

# A plugin can contain multiple files and require them
require_relative "Processor.rb"
require_relative "localization/Translations.rb"

class Plugin < PEMU::Plugin
	# Returns the id of this plugin, must be the same as specified in the plugin.info file
	# If more plugins with the same ID are found then only one is registered
	# @return [String] The id of this plugin
	def getID()
		return "io.github.hds.pemu:ruby_processor"
	end
	
	# Returns the name of this plugin, must be the same as specified in the plugin.info file
	# @return [String] The name of this plugin
	def getName()
		return "Ruby Processor"
	end

	# Returns the version of this plugin, must be the same as specified in the plugin.info file
	# @return [String] The version of this plugin
	def getVersion()
		return "1.1"
	end

	# This method is called when creating a new Processor that is needed to Compile a file
	# So it should be a light-weight Processor
	# If not implemented or nil is returned then "onCreateProcessor" will be called too
	# @param [PEMU::ProcessorConfig] config The config for the Processor
	# @return [PEMU::IDummyProcessor] A Dummy Processor
	def onCreateDummyProcessor(config)
		return Processor::getDummyProcessor config
	end
	
	# This method is called when creating a new Processor that needs to be ran
	# This is also called if the method "onCreateDummyProcessor" returns nil or is not implemented
	# @param [PEMU::ProcessorConfig] config The config for the Processor
	# @return [PEMU::IProcessor] A Processor
	def onCreateProcessor(config)
		return Processor.new config
	end

	# This method is called when the plugin is being loaded (The last plugin is still loaded)
	# @param [Java::JavaIo::StringWriter] stderr If any error occurs then the description of the error can be written here
	# @return [true, false] Whether or not the plugin successfully loaded, if false stderr should be populated with the description of the error
	def onLoad(stderr)
		# Consoles can be used to write stuff to the user
		# "puts" could also be used but it doesn't support all characters (like accented ones)
		PEMU::Console.Debug.println(
			PEMU::StringUtils.format(
				Translations.instance.current_translation.getOrDefault("plugins.loaded"),
				"Ruby"
			)
		)
		PEMU::Console.Debug.println
		return true
	end

	# This method is called when the plugin is being unloaded (The new plugin is already loaded)
	def onUnload()
		PEMU::Console.Debug.println(
			PEMU::StringUtils.format(
				Translations.instance.current_translation.getOrDefault("plugins.unloaded"),
				"Ruby"
			)
		)
		PEMU::Console.Debug.println
	end

	# This method returns a pretty name for this plugin, this is only called when the plugin is loaded
	#  else info from the plugin.info file will be used instead.
	# If this method throws an error the default name will be used instead
	# @return [String] A pretty name for this plugin
	def toString()
		return super.to_s
	end
end

return Plugin.new
