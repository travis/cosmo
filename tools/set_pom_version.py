#!/usr/bin/env python
import os
import sys
import shutil
import getopt

parent_pom = ["./pom.xml"] 
cosmo_pom = ["./cosmo/pom.xml"]    
set_parent_poms = ["./jsonrpc-java-js/pom.xml",
                   "./olson/pom.xml",
                   "./migration/pom.xml",
                   "./snarf/pom.xml"]
dojo_poms = ["./dojo/pom.xml"]

def usage():
    print "Usage"

def get_new_versions():
    version = None
    dojo_version = None
    try:
        opts, args = getopt.getopt(sys.argv[1:], "v:d:")
    except getopt.GetoptError:
        # print help information and exit:
        usage()
        sys.exit(2)
        
    for o,a in opts:
        if o == "-v":
            version = a
        if o == "-d":
            dojo_version = a
    return version, dojo_version

def get_old_versions():
    

if __name__ == "__main__":
    
    required_files = parent_pom + cosmo_pom + set_parent_poms + dojo_poms

    for file_path in required_files:
        if not os.path.exists(file_path):
            print file_path + " does not exist."
            sys.exit(1)
            
    version, dojo_version = get_versions()
    
    if version == None or dojo_version == None:
        usage()
        sys.exit(1)

    
    

