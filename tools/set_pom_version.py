#!/usr/bin/env python
import os, os.path
import sys
import shutil
import getopt
import re

parent_pom = "pom.xml" 
dojo_pom = "dojo/pom.xml"
set_cosmo_poms = [parent_pom,
                  "jsonrpc-java-js/pom.xml",
                   "olson/pom.xml",
                   "migration/pom.xml",
                   "snarf/pom.xml",
                   "dojo/pom.xml",
                   "cosmo/pom.xml"]
set_dojo_poms = [dojo_pom,
                 "cosmo/pom.xml"]
base_dir = "./"

def add_base(path):
    return os.path.join(base_dir, path)

def usage():
    print "Usage"

def get_new_versions():
    version = None
    dojo_version = None
    try:
        opts, args = getopt.getopt(sys.argv[1:], "v:d:b:")
    except getopt.GetoptError:
        # print help information and exit:
        usage()
        sys.exit(2)
        
    for o,a in opts:
        if o == "-v":
            version = a
        if o == "-d":
            dojo_version = a
        if o == "-b":
            global base_dir
            base_dir = a
    return version, dojo_version

def get_old_versions():
    try:
        old_version = re.search("<project>.*<groupId>org\.osaf\.cosmo</groupId>.*<version>(.*)</version>.*<name>.*</project>", open(add_base(parent_pom)).read(), re.DOTALL).group(1)    
        old_dojo_version = re.search("<project>.*<artifactId>dojo</artifactId>.*<version>(.*)</version>.*<name>.*</project>", open(add_base(dojo_pom)).read(), re.DOTALL).group(1)    
        return old_version, old_dojo_version

    except AttributeError, e:
        print "Could not figure out old version number."

if __name__ == "__main__":
    
    required_files = [parent_pom] + [dojo_pom] + set_cosmo_poms + set_dojo_poms

    for file_path in required_files:
        if not os.path.exists(file_path):
            print file_path + " does not exist."
            sys.exit(1)
            
    version, dojo_version = get_new_versions()
    old_version, old_dojo_version = get_old_versions()

    print version
    print dojo_version
    print old_version
    print old_dojo_version
    
    if version == None or dojo_version == None:
        usage()
        sys.exit(1)

    

    
    

