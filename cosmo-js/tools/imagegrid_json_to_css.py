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
import sys

def usage():
    return """Usage:
    Turn the imagegrid json into a stylesheet:
        %s imagegrid_json_filename
""" % (sys.argv[0])

def json_to_css(filename):
    json_file = open(filename)
    json = eval(json_file.read())
    
    output = license()
    for name, values in json.items():
        name = name[0].upper() + name[1:]
        height = values['height'] 
        width = values['width']
        left = 0 - ((values['column'] - 1) * 45)
        top = 0 - ((values['row'] - 1) * 45)
        image_url = "images/image_grid.png"
        output += ".cosmo%s{height: %dpx; width: %dpx; background-position: %dpx %dpx; background-image: url(%s);}\n" % (
            name, height, width, left, top, image_url)
    print output
        

def license():
        return """/*
 * Copyright 2007 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
"""

if __name__ == "__main__":

    from optparse import OptionParser
    parser = OptionParser()
    
    (options, args) = parser.parse_args()
    if len(args) < 1: 
        exit(usage())
    
    filename = args[0]
    json_to_css(filename)
    
