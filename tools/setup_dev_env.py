#!/usr/local/bin/python
import os
import subprocess
import sys
import shutil
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
    setup_app()
    setup_snarf()
    setup_eclipse()
   
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
    # copy over the context file
    shutil.copy(get_contextxml_path(), get_chandlerxml_path())
    # edit the chandler.xml file
    doc = etree.parse(get_chandlerxml_path())
    context = _get_context_element(doc)
    context.set("docBase", get_cosmo_webapp_path())
    doc.write(get_chandlerxml_path())

def setup_eclipse():
    os.chdir(get_cosmo_app_path());
    output = subprocess.Popen(["mvn", "eclipse:eclipse"], stdout=subprocess.PIPE).communicate()[0]
    doc = etree.parse(get_dot_project_path())
    name = doc.find("name")
    name.text = "Cosmo "+ version
    doc.write(get_dot_project_path())
 
def setup_app():
    os.chdir(get_cosmo_app_path());
    output = subprocess.Popen(["mvn", "clean", "compile", "war:inplace", "package"], stdout=subprocess.PIPE).communicate()[0]

def _get_context_element(doc):
    return doc.getiterator("Context")[0]

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

def get_dot_project_path():
    return get_cosmo_app_path() + ".project"

def get_contextxml_path():
    return get_cosmo_webapp_path() + "/META-INF/context.xml"

def get_chandlerxml_path():
    return get_snarf_dist_path() + "tomcat/conf/Catalina/localhost/chandler.xml"

if __name__ == "__main__":
    main()
