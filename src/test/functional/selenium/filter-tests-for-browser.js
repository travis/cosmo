/*Copyright 2006, Open Source Applications Foundation

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.*/

function filterTestsForBrowser() {
    var testTables = document.getElementsByTagName("table");
    for (var i = 0; i < testTables.length; i++)
    {
        filterTestTableForBrowser(testTables[i]);
    }
}

function filterTestTableForBrowser(testTable)
{
    for(rowNum = testTable.rows.length - 1; rowNum >= 0; rowNum--)
    {
        var row = testTable.rows[rowNum];
        var filterString = row.getAttribute("if");
        if (filterString && !eval(filterString))
        {
          testTable.deleteRow(rowNum)
        }
    }
}

window.onload=filterTestsForBrowser;