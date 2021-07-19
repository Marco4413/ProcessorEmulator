

# If you don't have java added to your system's path
#  then put the path to the Java Runtime executable here
JAVA_PATH = "java"


'''
Author: [hds536jhmk](https://github.com/hds536jhmk)
Description: This Python script can be used to verify PEMU programs from the command line
LICENSE: MIT

Copyright (c) 2021 [hds536jhmk](https://github.com/hds536jhmk/ProcessorEmulator)

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.
'''

from sys import argv
from os import path
from subprocess import Popen, PIPE

import re
from dataclasses import dataclass

@dataclass
class PEMUCompileError:
    fileName: str
    errorName: str
    errorDescription: str
    line: str
    character: str

    def __str__(self) -> str:
        return "PEMU Compilation Error ->\nFile: \t{}\nName: \t{}\nDesc: \t{}\nLine: \t{}\nChar: \t{}\n".format(
            self.fileName, self.errorName, self.errorDescription, self.line, self.character
        )

def parseError(err: str) -> PEMUCompileError:
    errorLines = err.split("\n")
    for line in errorLines:
        match = re.match(r"'(.+)': (.+ Error) \((.+):(.+)\): (.+)", line, re.DOTALL)
        if match:
            return PEMUCompileError(
                *match.group(1, 2, 5, 3, 4)
            )
    return None

def getUsage() -> str:
    return "Usage: {} <PEMU JAR FILE> <PEMU PROGRAM TO VERIFY> <PROCESSOR BITS>".format(path.basename(argv[0]))

def printUsage():
    print(getUsage())

REQUIRED_ARGUMENTS = 3
VERSION = "v1.1.0"
SUPPORTED_PEMU_VERSION = "v1.9.1+"

def main():
    print("Welcome to PEMU's Language Utility™ {}".format(VERSION))
    print("Supported PEMU Version {}".format(SUPPORTED_PEMU_VERSION))
    print("This program will verify your PEMU code on the command line and format errors and output!")
    print()

    # Subtracting 1 because the first element is the script's path
    #  and it's not an actual argument
    argCount = len(argv) - 1
    if argCount < REQUIRED_ARGUMENTS:
        printUsage()
        return
    elif argCount > REQUIRED_ARGUMENTS:
        print("Too many arguments, only {} needed, got {}".format(REQUIRED_ARGUMENTS, argCount))
        printUsage()
        return

    # variable "_" holds the script's path
    [_, applicationPath, programPath, bits] = argv

    try:
        int(bits)
    except:
        print("<PROCESSOR BITS> must be a number!")
        printUsage()
        return

    process = None
    try:
        process = Popen(
            [JAVA_PATH, "-jar", applicationPath, "-cl", "-sw", "-v", "-p", programPath, "-b", bits],
            stdout=PIPE, encoding="UTF-8", text=True
        )
    except:
        print("Java Runtime couldn't be found, check if its executable was added to the system's path, install it or edit the variable \"JAVA_PATH\" found at the top of this script's source.")
        return

    (output, err) = process.communicate()
    exit_code = process.wait()

    if exit_code != 0:
        printUsage()
        return

    parsedError = parseError(output)
    print(
        str(parsedError is None and output or parsedError).strip(" \t\n")
    )



if __name__ == "__main__":
    main()
