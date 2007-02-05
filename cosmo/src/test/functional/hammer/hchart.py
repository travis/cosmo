#! /usr/bin/env python

# 
# Copyright 2005-2006 Open Source Applications Foundation
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at 
# 
# 	http://www.apache.org/licenses/LICENSE-2.0
# 	
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 
# Author:       Mike Taylor
# Contributors: 
#

"""
Hammer Log Chart Utility

Charts the output from Hammer.py's logs

Usage: python hchart.py [options] logfile

Options:
  --format=[ps,png,pdf,x11,svg]
  --output=<output filename>

Note: format defaults to png and the output filename defaults to
      the logfile.<format>

Example:
  hchart.py --format=pdf test_run.log

"""

import os.path, string, datetime
from pychart import *

values = []
data   = []

class ts_coord(category_coord.T):
    def get_tics(self, min, max, interval):
          # get current tics and then walk thru and only
          # include tics that are on second change boundaries

        tics = category_coord.T.get_tics(self, min, max, interval)
        newtics = []

        s = 99 # set to a bogus value to force first entry to have a tic

        for item in tics:
            if s != item.second:
                newtics.append(item)
                s = item.second

        return newtics

def graph(values):
    # cycle thru values and graph each data point
    # each item in values is a tuple: (timestamp, gets, puts)

    def format_date(v):
        return '/a90{}' + v.strftime('%b %d %H:%M:%S')

    theme.reinitialize()

    tick1 = tick_mark.Circle(size=2)
    tick2 = tick_mark.Square(size=2)

    xaxis = axis.X(format=format_date, label='timestamp')
    yaxis = axis.Y(label='response time')

    ar = area.T(x_axis=xaxis, y_axis=yaxis, 
                x_coord=ts_coord(values, 0), size=(800, 200))

    ar.add_plot(line_plot.T(label='gets', data=values, ycol=1,
                            line_style=line_style.black_dash1,
                            tick_mark=tick1),
                line_plot.T(label='puts', data=values, ycol=2,
                            line_style=line_style.red_dash1,
                            tick_mark=tick2)
                )

    ar.draw()

    return True

def load(filename):
    #
    # the file is assumed to have the following tab seperated format:
    #
    # line 1        - test summary
    # line 2..n     - test values
    # line n+1..n+7 - test details
    #
    # example:
    #
    # 11/22/2005 09:00:45.000000<tab>Starting test with 10 threads, 100 iterations, 10000 bytes per file
    # 11/22/2005 09:00:45.000000<tab>PUT<tab>0.050
    # 11/22/2005 09:00:45.000000<tab>GET<tab>0.000
    # 11/22/2005 09:01:59.000000<tab>10 tests passed
    # 11/22/2005 09:01:59.000000<tab>Success rate is 100%
    # 11/22/2005 09:01:59.000000<tab>Total Elapsed Time: 74 seconds
    # 11/22/2005 09:01:59.000000<tab>Total data transfered: 19531 KB
    # 11/22/2005 09:01:59.000000<tab>Throughput: 261 KB/sec
    # 11/22/2005 09:01:59.000000<tab>Ran 10 threads through 100 iterations.
    # 11/22/2005 09:01:59.000000<tab>Test completed: PASSED.
    #

    lines  = file(filename).readlines()

    header  = lines[0]
    trailer = lines[-7:]

    for line in lines[1:-7]:
        try:
            timestamp, operation, responsetime  = string.split(line[:-1], '\t')
            datestring, timestring = string.split(timestamp, ' ')

            if operation.find('ERROR') == -1:
                yr, mn, dy  = map(int, string.split(datestring, '-'))
                h, m, s     = string.split(timestring, ':')
                s, ms       = string.split(s, '.')
                h, m, s, ms = map(int, [h, m, s, ms])

                dt = datetime.datetime(yr, mn, dy, h, m, s, ms)

                value = (dt, operation, float(responsetime))
                values.append(value)
        except:
            pass
            
    for item in values:
        t, operation, responsetime = item

        y1 = 0.0
        y2 = 0.0

        if operation == 'GET':
            value = (t, responsetime, None)
        else:
            value = (t, None, responsetime)

        data.append(value)


filename = ''
argv     = theme.get_options()

theme.scale_factor = 4
theme.use_color = True

if len(argv) == 1:
    filename = argv[0]

if os.path.isfile(filename):
    if theme.output_format is None:
        theme.output_format = 'png'

    if theme.output_file is None or len(theme.output_file) == 0:
        theme.output_file = "%s.%s" % (filename, theme.output_format)

        print "Output file not specified, using %s" % theme.output_file

    load(filename)

    if len(data) > 0:
        graph(data)
    else:
        print "no data to chart"
else:
    print "Unable to locate data file %s" % filename
