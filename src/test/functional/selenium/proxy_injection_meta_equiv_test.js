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

// this fragment was making PI mode inject.  Oops -- should only inject HTML containing 66
//#DWR-START#
var s1="<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN \" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-"; var s2="1\" />\n  <title>Insert</title>\n</head>\n<body>n<p><strong>DWR tests passed</strong></p>\n\n</body>\n</html>\n";
var s0=s1+s2;
//#DWR-END#
