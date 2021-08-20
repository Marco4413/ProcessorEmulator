
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
require "singleton"

# A plugin can contain multiple files and require them
require_relative "Processor.rb"
require_relative "localization/Translations.rb"

class Plugin < PEMU::Plugin
protected
	@@MIN_PEMU_VERSION = "1.12.0"
	@@MAX_PEMU_VERSION = "1.12.99"
public
	include Singleton

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

	def pemu_compatible?()
		return PEMU::version_in_range? PEMU::get_version, @@MIN_PEMU_VERSION, @@MAX_PEMU_VERSION
	end

	# This method is called when the plugin is being loaded (The last plugin is still loaded)
	# @return [Boolean] Whether or not the plugin successfully loaded
	# @raise [Java::JavaLang::Exception] This method may throw resulting in a failed load
	def onLoad()
		if !self.pemu_compatible? then
			raise PEMU::format_string PEMU::get_from_translation(Translations.instance.current_translation, "plugins.incompatible"), @@MIN_PEMU_VERSION, @@MAX_PEMU_VERSION
			return false
		end

		# "puts" could also be used but it doesn't support all Java characters
		PEMU::debug_log(
			PEMU::format_string(
				PEMU::get_from_translation(Translations.instance.current_translation, "plugins.loaded"),
				"Ruby"
			)
		)
		PEMU::debug_log
		return true
	end

	# This method is called when the plugin is being unloaded (The new plugin is already loaded)
	def onUnload()
		PEMU::debug_log(
			PEMU::format_string(
				PEMU::get_from_translation(Translations.instance.current_translation, "plugins.unloaded"),
				"Ruby"
			)
		)
		PEMU::debug_log
	end

	# This method returns a pretty name for this plugin, this is only called when the plugin is loaded
	#  else info from the plugin.info file will be used instead.
	# If this method throws an error the default name will be used instead
	# @return [String] A pretty name for this plugin
	def toString()
		return super.to_s
	end
end

return Plugin.instance
