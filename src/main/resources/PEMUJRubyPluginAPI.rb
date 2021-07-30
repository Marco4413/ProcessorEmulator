
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

module PEMU

    include_package Java::IoGithubHdsPemuInstructions
    include_package Java::IoGithubHdsPemuMemoryFlags
    include_package Java::IoGithubHdsPemuMemoryRegisters
    include_package Java::IoGithubHdsPemuMemory
    include_package Java::IoGithubHdsPemuProcessor
    java_import     Java::IoGithubHdsPemuApp::Application
    java_import     Java::IoGithubHdsPemuApp::Console
    java_import     Java::IoGithubHdsPemuPlugins::IPlugin
    java_import     Java::IoGithubHdsPemuPlugins::Plugin
    java_import     Java::IoGithubHdsPemuUtils::MathUtils
    java_import     Java::IoGithubHdsPemuUtils::StringUtils
    java_import     Java::JavaAwtEvent::KeyEvent

end
