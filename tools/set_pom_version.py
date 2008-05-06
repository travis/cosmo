#!/usr/bin/env python
# Copyright 2008 Open Source Applications Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
import os, os.path
import sys
import shutil
import getopt
import re

parent_pom = "pom.xml" 
set_cosmo_poms = [parent_pom,
                  "jsonrpc-java-js/pom.xml",
                  "olson/pom.xml",
                  "migration/pom.xml",
                  "snarf/pom.xml",
                  "cosmo-js/pom.xml",
                  "cosmo/pom.xml"]
base_dir = "./"

def add_base(path):
    return os.path.join(base_dir, path)

def usage():
    print """Usage:
    Print current version number:
        %s -n 
    Change version numbers:
        %s -v cosmo_version [-b base_directory]
""" % (sys.argv[0], sys.argv[0])

def get_new_versions():
    version = None
    dojo_version = None
    try:
        opts, args = getopt.getopt(sys.argv[1:], "v:d:b:n")
    except getopt.GetoptError:
        # print help information and exit:
        usage()
        sys.exit(2)
        
    for o,a in opts:
        if o == "-n":
            print get_old_versions()
            sys.exit(0)
        if o == "-v":
            version = a
        if o == "-b":
            global base_dir
            base_dir = a
    return version

def get_old_versions():
    try:
        old_version = re.search("<project>.*<groupId>org\.osaf\.cosmo</groupId>.*<version>(.*)</version>.*<name>.*</project>", open(add_base(parent_pom)).read(), re.DOTALL).group(1)    
        return old_version

    except AttributeError, e:
        print "Could not figure out old version number."

def replace_string(filename, find, replace):
    old_file = open(filename)
    content = old_file.read()
    old_file.close()
    
    new_file = open(filename, 'w')
    new_file.write(content.replace(find, replace))
    new_file.close()

if __name__ == "__main__":
    required_files = set_cosmo_poms
    for file_path in required_files:
        if not os.path.exists(file_path):
            print file_path + " does not exist."
            sys.exit(1)
            
    version = get_new_versions()
    old_version = get_old_versions()

    if version == None:
        print "Error: need version number."
        usage()
        sys.exit(1)

    for filename in set_cosmo_poms:
        replace_string(filename, old_version, version)
