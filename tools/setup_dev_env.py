#!/usr/local/bin/python
import os
import subprocess
import sys
from xml.etree import ElementTree as etree

def main():
    """
    * set up eclipse
      * run mvn eclipse:eclipse
      * change project name in .project
    * build app
    * build snarf
    * fix tomcat setup
    * 
    """
    initialize()    
    setup_snarf()
    setup_eclipse()
    setup_app()

def initialize():
    """
    set up some useful globals.
    """
    global version
    global scriptpath

    scriptpath =  os.path.abspath(os.path.dirname(sys.argv[0]))

    #get the version number from the pom.
    doc = etree.parse(get_pom_path());
    version = doc.find("version").text

def setup_snarf():
    print "Setting up snarf..."
    # cd into proper directory
    os.chdir(get_snarf_path())
    # run maven
    output = subprocess.Popen(["mvn", "assembly:directory"], stdout=subprocess.PIPE).communicate()[0]
    # edit the server.xml file
    doc = etree.parse(get_serverxml_path())
    context = _get_context_element(doc)
    context.set("docBase", get_cosmo_webapp_path())
    doc.write(get_serverxml_path())

def setup_eclipse():
    os.chdir(get_cosmo_app_path());
    output = subprocess.Popen(["mvn", "eclipse:eclipse"], stdout=subprocess.PIPE).communicate()[0]
    doc = etree.parse(get_dot_project_path())
    name = doc.find("name")
    name.text = "Cosmo "+ version
    doc.write(get_dot_project_path())
 
def setup_app():
    os.chdir(get_cosmo_app_path());
    output = subprocess.Popen(["mvn", "clean", "compile", "war:inplace"], stdout=subprocess.PIPE).communicate()[0]

def _get_context_element(doc):
    for context in doc.getiterator("Context"):
        if context.get("path") == "/chandler":
            return context

def get_script_path():
    return scriptpath

def get_cosmo_home_path():
    return get_script_path() + "/../"

def get_cosmo_app_path():
    return get_cosmo_home_path() + "cosmo/"

def get_cosmo_webapp_path():
    return os.path.abspath(get_cosmo_app_path() + "src/main/webapp")

def get_snarf_path():
    return get_cosmo_home_path() + "snarf/"

def get_pom_path():
    return get_cosmo_home_path() + "pom.xml";

def get_snarf_dist_path():
    dirname = "osaf-server-bundle-" + version + "/"
    return get_snarf_path() + "dist/" + dirname + dirname

def get_serverxml_path():
    return get_snarf_dist_path() + "tomcat/conf/server.xml"

def get_dot_project_path():
    return get_cosmo_app_path() + ".project"

if __name__ == "__main__":
    main()
