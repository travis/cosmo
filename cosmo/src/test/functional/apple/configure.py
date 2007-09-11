#!/usr/bin/env python
import sys, os, shutil

if __name__ == "__main__":
    root_dir = os.path.abspath(os.path.dirname(__file__))
    shutil.copyfile(os.path.join(root_dir, 'serverinfo.xml'), 
                    os.path.join(root_dir, 'CalDAVTester', 'trunk', 'scripts', 'server', 'serverinfo.xml'),
                    )
    print 'Complete'
    
