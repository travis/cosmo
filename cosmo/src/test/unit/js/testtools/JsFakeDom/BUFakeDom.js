/**
@file BUFakeDom.js

This is an implementation of most of the W3 DOM APIs. 

It is intended for use in testing ECMAScript libraries outside of a browser environment.

This file is standalone, with no dependencies on other files.

As a side-effect of loading this file, the global variable "document" will become
an instance of BUFakeDocument, if the variable "document" is not already defined.

<h3>Missing Functionality</h3>
The implementation is at least as complete as most browsers are today :) .

The main feature lacking is <code>document.write()</code> .

Here are the functions and attributes not yet implemented:
<ul>
<li>Node methods
<pre>
   We have not implemented these Node methods
      Node Node.cloneNode(Boolean deep)
      void Node.normalize()
</pre>

<li>Event objects (see http://www.w3.org/TR/DOM-Level-2-Events/events.html )
<pre>
   We do support:
      UIEvent, MouseEvent, HTMLEvent
   but not: 
      MutationEvent
</pre>

<li>attributes of HTML* classes
<pre>
   We have no special support for these attributes of HTMLDocument:
      title, referrer, domain, URL, body, images, applets, links, forms, anchors, cookie
   We have no special support for these attributes of HTMLElement:
      id, title, lang, dir, className
   Or for any of the other HTML* classes ( http://www.w3.org/TR/REC-DOM-Level-1/level-one-html.html ):
      HTMLHtmlElement, HTMLHeadElement, HTMLLinkELement, HTMLTitleElement, HTMLMetaElement, 
      HTMLBaseElement, HTMLIsIndexElement, HTMLStyleElement, HTMLBodyElement, ...
   Or for these tag-specific attributes ( http://www.w3.org/TR/REC-html40/struct/global.html#edef-BODY ):
      BODY.onload, BODY.onunload, ...
</pre>

<li>HTMLDocument methods
<pre>
   We do not support these methods of HTMLDocument:
      open(), close(), write(text), writeln(text)
</pre>
   
<li>BUFakeCharacterData
<pre>
   We need a BUFakeCharacterData subclass of BUFakeNode
   This can be a Comment, Text, or CDATA node.
   We need to support the special attributes of those 3 node types too.
   The methods are:
      String BUFakeCharacterData.substringData(long offset, long count)
      void BUFakeCharacterData.appendData(String arg)
      void BUFakeCharacterData.insertData(long offset, String arg)
      void BUFakeCharacterData.deleteData(long offset, long count)
      void BUFakeCharacterData.insertData(long offset, long count, String arg)
</pre>

<li>xml namespaces
<pre>
   None of the xml-ish methods (ending in "NS") are implemented, nor are the
   Node member variables namespaceURI, prefix, localName.
</pre>

<li>interfaces of Node types
<pre>
   We have no support for these interfaces:
      Entity, EntityReference, DocumentType, ProcessingInstruction, Notation, DocumentFragment
</pre>

<li>DOMImplementation
<pre>
   We have only very partial support for DOMImplementation.
</pre>
</ul>


<h3>Caveats and Bugs</h3>

This implements a HTML-style DOM. In particular, node names are uppercased when 
returned by API functions, regardless of their letter case when created.

All methods that return a NodeList in the specification are implemented here
to return an Array. These Arrays are not "live", unlike a DOM NodeList.
They also do not support the <code>item(number)</code> interface.

Node.attributes is implemented as an object which is both an Array (with numerical
index) and an associative array (with name index). It is "live". It does not
implement <code>item(number)</code> or any of the other methods of NamedNodeMap.

Note that while Document.getEementsByName does return an Array, we do not implement
support for duplicate uses of the same name.

We do not enforce that any member attributes are readonly.

We do not attempt to synchronize the values of aliased properties (for example,
Element nodeName and tagName, or Attr nodeName and name).


* @author Copyright 2003 Mark D. Anderson (mda@discerning.com)
* @author Licensed under the Academic Free License 1.2 http://www.opensource.org/licenses/academic.php
*/

/****************************************************************/
/**** begin burstlib definitions (so can be standalone) ****/
/****************************************************************/
// if not standalone, redundant with fix_dom.js
if (typeof Node == 'undefined') {
  var Node = {
    ELEMENT_NODE           : 1,
    ATTRIBUTE_NODE         : 2,
    TEXT_NODE              : 3,
    CDATA_SECTION_NODE     : 4,
    ENTITY_REFERENCE_NODE  : 5,
    ENTITY_NODE            : 6,
    PROCESSING_INSTRUCTION_NODE    : 7,
    COMMENT_NODE           : 8,
    DOCUMENT_NODE          : 9,
    DOCUMENT_TYPE_NODE     : 10,
    DOCUMENT_FRAGMENT_NODE : 11,
    NOTATION_NODE          : 12
  };
}
if (typeof DOMException == 'undefined') {
  var DOMException = {
      INDEX_SIZE_ERR                 : 1,
      DOMSTRING_SIZE_ERR             : 2,
      HIERARCHY_REQUEST_ERR          : 3,
      WRONG_DOCUMENT_ERR             : 4,
      INVALID_CHARACTER_ERR          : 5,
      NO_DATA_ALLOWED_ERR            : 6,
      NO_MODIFICATION_ALLOWED_ERR    : 7,
      NOT_FOUND_ERR                  : 8,
      NOT_SUPPORTED_ERR              : 9,
      INUSE_ATTRIBUTE_ERR            : 10,
      INVALID_STATE_ERR              : 11,
      SYNTAX_ERR                     : 12,
      INVALID_MODIFICATION_ERR       : 13,
      NAMESPACE_ERR                  : 14,
      INVALID_ACCESS_ERR             : 15
  };
}

if (typeof Event == 'undefined') {
  var Event = {
       CAPTURING_PHASE                : 1,
       AT_TARGET                      : 2,
       BUBBLING_PHASE                 : 3
  };
}


// debug function
if (typeof bu_debug == 'undefined') {
  var bu_debug = function() {}
}


// redundant copy of some utilities from burst.Alg, to make standalone 
if (typeof burst == 'undefined' || typeof burst.Alg == 'undefined') {
  var burst = {};
  burst.Alg = {};
  burst.Alg.find = function(arr, val) {
    for(var i=0;i<arr.length;++i) {if (arr[i] == val) return i;}
    return -1;
  }
  burst.Alg.for_each = function(arr, unary_func) {
   for(var i=0;i<arr.length;++i) unary_func(arr[i]);
  }
  burst.Alg.find_if = function(arr, unary_predicate) {
   for(var i=0;i<arr.length;++i) {if (unary_predicate(arr[i])) return i;}
   return -1;
  }
}

/****************************************************************/
/**** end burstlib definitions ****/
/****************************************************************/

function BUFakeEvent() {
  bu_debug("BUFakeEvent()");
// set in init*Event call
/*
  this.type;          // DOMString
  this.bubbles;       // boolean
  this.cancelable;    // boolean
*/

// set by dispatchEvent
/*
  this.target;        // EventTarget
  this.currentTarget; // EventTarget
  this.eventPhase;    // unsigned short
  this.timeStamp;     //  DOMTimeStamp
*/
}

// finish dispatching on this node but do no others
BUFakeEvent.prototype.stopPropagation = function() {this.stopped_ = true;};

// can be called by any listener to cancel the default action
BUFakeEvent.prototype.preventDefault = function() {if (this.cancelable) this.canceled_ = true;};

//this.initEvent(String eventTypeArg, Boolean canBubbleArg, Boolean cancelableArg);
BUFakeEvent.prototype.initEvent = function(eventTypeArg, canBubbleArg, cancelableArg) {
   bu_debug("initEvent(", eventTypeArg,',',canBubbleArg,',',cancelableArg,")");
   this.type = eventTypeArg;
   this.bubbles = canBubbleArg;
   this.cancelable = cancelableArg;
};

function BUFakeUIEvent() {
}
BUFakeUIEvent.prototype = new BUFakeEvent();
BUFakeUIEvent.prototype.constructor = BUFakeUIEvent;
BUFakeUIEvent.prototype.initUIEvent = function(
	/*DOMString*/ typeArg, 
        /*boolean*/ canBubbleArg, 
        /*boolean*/ cancelableArg, 
        /*views::AbstractView*/ viewArg, 
	/*long*/ detailArg) {
  this.initEvent(typeArg, canBubbleArg, cancelableArg);
  this.view = viewArg;
  this.detail = detailArg;
};

function BUFakeMouseEvent() {
}
BUFakeMouseEvent.prototype = new BUFakeUIEvent();
BUFakeMouseEvent.prototype.constructor = BUFakeMouseEvent;
BUFakeMouseEvent.prototype.initMouseEvent = function(
	 /*DOMString*/ typeArg, 
         /*boolean*/ canBubbleArg, 
         /*boolean*/ cancelableArg, 
         /*views::AbstractView*/ viewArg, 
         /*long*/ detailArg, 
	 /*long*/ screenXArg, 
         /*long*/ screenYArg, 
	 /*long*/ clientXArg, 
	 /*long*/ clientYArg, 
         /*boolean*/ ctrlKeyArg, 
         /*boolean*/ altKeyArg, 
         /*boolean*/ shiftKeyArg, 
         /*boolean*/ metaKeyArg, 
         /*unsigned short*/ buttonArg, 
         /*EventTarget*/ relatedTargetArg) {
  this.initUIEvent(typeArg, canBubbleArg, cancelableArg, viewArg, detailArg);
  this.screenX = screenXArg;
  this.screenY = screenYArg;
  this.clientX = clientXArg;
  this.clientY = clientYArg;
  this.ctrlKey = ctrlKeyArg;
  this.shiftKey = shiftKeyArg;
  this.altKey = altKeyArg;
  this.metaKey = metaKeyArg;
  this.button = buttonArg;
  this.relatedTarget = relatedTargetArg;
}



function BUFakeDOMException(ecode) {
  this.code = ecode;
  this.message = 'DOMException number ' + this.code;
  this.name = 'BUFakeDOMException';
}

// constructor for our Node base class
function BUFakeNode(nType, nName, nOwner) {
  if (arguments.length==0) {bu_debug('BUFakeNode()'); return;}
  bu_debug("BUFakeNode(", nType, ",", nName, ",", nOwner, ")");
  this.nodeType = nType;
  this.nodeName = (nName && nName.charAt(0) != '#') ? nOwner.norm_case_(nName) : nName;
  this.childNodes = [];
  this.firstChild = null;
  this.lastChild = null;
  this.nextSibling = null;
  this.previousSibling = null;
  this.parentNode = null;
  this.style = {};
  this.listeners_ = [];
  // both an Array (numerical index) and an associate array (name index)
  this.attributes = (nType == Node.ELEMENT_NODE ? [] : null);
  this.ownerDocument = nOwner;
}

// Internal method, to make the code clearer when we are indexing by name
BUFakeNode.prototype.attnodes_by_name_ = function() {return this.attributes}

// Attr removeAttributeNode(Attr attr)
BUFakeNode.prototype.removeAttributeNode = function(attr) {
  for(var i=0;i<attributes.length;++i) {
    if (attr === attributes[i]) break;
  }
  if (i == attributes.length) throw new BUFakeDOMException(DOMException.NOT_FOUND_ERR);
  this.ownerDocument.update_indexes_(attr.nodeName, attr.nodeValue, undefined, this);
  this.attributes.splice(i,1);
  delete this.attnodes_by_name_()[attr.nodeName];
  return attr;
}

// void removeAttribute(String attname)
BUFakeNode.prototype.removeAttribute = function(name) {
  name = this.ownerDocument.norm_case_(name);
  var node = this.attnodes_by_name_()[name];
  if (!node) return;
  this.removeAttributeNode(node);
}

BUFakeNode.prototype.find_listener_ = function(eventType, listener, useCapture) {
  return burst.Alg.find(this.listeners_, function(o) {o[0] == eventType && o[1] === listener && o[2] == useCapture});
}

// void EventTarget.addEventListener(String type, EventListener listener, Boolean useCapture)
BUFakeNode.prototype.addEventListener = function(eventType, listener, useCapture) {
  useCapture = !!useCapture;
  if (this.find_listener_(eventType, listener, useCapture) != -1) return;
  this.listeners_.push([eventType, listener, useCapture]);
}

// void EventTarget.removeEventListener(String type, EventListener listener, Boolean useCapture)
BUFakeNode.prototype.removeEventListener = function(eventType, listener, useCapture) {
  useCapture = !!useCapture;
  var existing_ind = this.find_listener_(eventType, listener, useCapture);
  if (existing_ind == -1) return;
  this.listeners_.splice(existing_ind, 1);
}

BUFakeNode.prototype.toString = function() {
  return 'BUFakeNode{' + 
      'nodeType: ' + this.nodeType + 
      ", nodeName: '" + (this.nodeName||'') + "'" +
      (this.attributes && this.attributes.length > 0 ? ", attributes: " + this.attributes : '') +
    '}';
}

// returns true if stopped
// An exception thrown by a listener is not supposed to stop propagation.
// For now however, we do no catch exceptions, for ease in debugging.
BUFakeNode.prototype.call_listeners_ = function(evt) {
  bu_debug("calling all listeners for event on node: " + this);
  var node = this;
  burst.Alg.for_each(this.listeners_, function(listen) {
    if (evt.type !== listen[0]) return;
    if (evt.eventPhase == Event.CAPTURING_PHASE && !listen[2]) return;
    if ((evt.eventPhase == Event.BUBBLING_PHASE || evt.eventPhase == Event.AT_TARGET) && listen[2]) return;
    bu_debug("calling listener for event on node: " + node);
    evt.currentTarget = node;
    //try {
      var ret = listen[1].call(node, evt);
      // returning false is the same as calling preventDefault()
      if (typeof ret == 'boolean' && !ret) evt.preventDefault();
    //} catch(e) {bu_warn("eating exception from event listener: " + e)}
  });
  bu_debug("returning with stopped_=", evt.stopped_, " after calling ", this.listeners_.length, " listeners");
  return evt.stopped_;
}

// boolean EventTarget.dispatchEvent(Event evt)
BUFakeNode.prototype.dispatchEvent = function(evt) {
  evt.target = this;
  evt.timeStamp = (new Date()).getTime();
  evt.eventPhase = Event.CAPTURING_PHASE;
  evt.canceled_ = false;
  evt.stopped_ = false;

  // do capturing
  var ancestors = [];
  for(var parent = this.parentNode; parent; parent = parent.parentNode) {ancestors.push(parent)}
  ancestors.reverse(); // document first
  // stop more capturing if stopped
  burst.Alg.find_if(ancestors, function(anc) {anc.call_listeners_(evt)});

  // stopPropagation() called
  if (evt.stopped_) return !evt.canceled_;

  // call the target
  evt.eventPhase = Event.AT_TARGET;
  var stopped = this.call_listeners_(evt);
  if (stopped) return !evt.canceled_;

  // bubble
  if (!evt.bubbles) return !evt.canceled_;

  evt.eventPhase = Event.BUBBLING_PHASE;
  ancestors.reverse(); // document last
  burst.Alg.find_if(ancestors, function(anc) {anc.call_listeners_(evt)});

  return !evt.canceled_;
}


// Attr setAttributeNode(Attr attr)
BUFakeNode.prototype.setAttributeNode = function(attr) {
  var name = attr.nodeName;
  // remove if already there
  if (name in this.attnodes_by_name_()) {
    this.removeAttributeNode(attr);
  }
  // add it
  this.attributes.push(this.attnodes_by_name_()[name] = attr);
  this.ownerDocument.update_indexes_(name, undefined, attr.nodeValue, this);  
}


// void setAttribute(String name, String value)
BUFakeNode.prototype.setAttribute = function(name, attval) {
  name = this.ownerDocument.norm_case_(name);
  if (name in this.attnodes_by_name_()) {
    this.ownerDocument.update_indexes_(name, this.attnodes_by_name_()[name], attval, this);
    this.attnodes_by_name_()[name][nodeValue] = attval;
  }
  else {
    this.attributes.push(this.attnodes_by_name_()[name] = this.ownerDocument.createAttribute(name, attval));
    this.ownerDocument.update_indexes_(name, undefined, attval, this);
  }
}

// Attr getAttributeNode(String name)
BUFakeNode.prototype.getAttributeNode = function(name) {
  name = this.ownerDocument.norm_case_(name);
  return this.attnodes_by_name_()[name];
}

// String getAttribute(String name)
BUFakeNode.prototype.getAttribute = function(name) {
  name = this.ownerDocument.norm_case_(name);
  return (name in this.attnodes_by_name_()) ? this.attnodes_by_name_()[name].nodeValue : null; 
}

// Boolean hasChildNodes()
BUFakeNode.prototype.hasChildNodes = function() {
  return this["childNodes"] && this.childNodes.length > 0;
}

// Boolean hasAttributes()
BUFakeNode.prototype.hasAttributes = function() {
  return this["attributes"] && this.attributes.length > 0;
}

// Boolean hasAttribute(String name)
BUFakeNode.prototype.hasAttribute = function(name) {
  name = this.ownerDocument.norm_case_(name);
  return (name in this.attnodes_by_name_());
}

// NodeList getElementsByTagName(String name)
BUFakeNode.prototype.getElementsByTagName = function(name, arr) {
  name = this.ownerDocument.norm_case_(name);
  if (!arr) arr = [];
  for(var child=this.firstChild; child; child=child.nextSibling) {
    if (child.nodeType == Node.ELEMENT_NODE && (name == '*' || child.nodeName == name)) arr.push(child);
    child.getElementsByTagName(name, arr);
  }
  return arr;
}

// Node BUFakeNode.removeChild(child)
BUFakeNode.prototype.removeChild = function(child) {
  var found_ind = burst.Alg.find(this.childNodes, child);
  if (found_ind == -1) throw new BUFakeDOMException(DOMException.NOT_FOUND_ERR);
  // linked list crap
  var prev = child.previousSibling;
  var next = child.nextSibling;
  if (prev) {
    prev.nextSibling = next;
    child.previousSibling = null;
  } else {
    this.firstChild = next;
  }
  if (next) {
    next.previousSibling = prev;
    child.nextSibling = null;
  } else {
  	this.lastChild = prev;
  }
  this.childNodes.splice(found_ind, 1);
  child.parentNode = null;
  if (child.nodeType == Node.ELEMENT_NODE) { 
    this.ownerDocument.update_indexes_('ID', child.getAttribute('id'), null, child);
    this.ownerDocument.update_indexes_('NAME', child.getAttribute('name'), null, child);
  }
  return child;
}


// returns new child
BUFakeNode.prototype.insertBefore = function(child, before_child) {
  var before_ind = burst.Alg.find(this.childNodes, before_child);
  if (before_ind == -1) throw new BUFakeDOMException(DOMException.NOT_FOUND_ERR);
  if (child.parentNode) child.parentNode.removeChild(child);
  
  child.nextSibling = before_child;
  var prev = before_child.previousSibling;
  before_child.previousSibling = child;
  child.previousSibling = prev;

  this.childNodes.splice(before_ind, 0, child);

  child.parentNode = this;

  if (child.nodeType == Node.ELEMENT_NODE) { 
    this.ownerDocument.update_indexes_('ID', null, child.getAttribute('id'), child);
    this.ownerDocument.update_indexes_('NAME', null, child.getAttribute('name'), child);
  }
  return child;
}

// returns new_child
BUFakeNode.prototype.replaceChild = function(new_child, existing_child) {
  var next_child = existing_child.nextSibling;
  this.removeChild(existing_child);
  if (next_child)
    return this.insertBefore(new_child, next_child);
  else
    return this.appendChild(new_child);
}


// Node appendChild(Node child)
// TODO: when child is a document fragment, append all its children instead. ditto for insertBefore
BUFakeNode.prototype.appendChild = function(child) {
  if (child.parentNode) child.parentNode.removeChild(child);
  if (this.childNodes.length > 0) {
     var prev = this.childNodes[this.childNodes.length - 1];
     prev.nextSibling = child;
     child.previousSibling = prev;
  } else {
     this.firstChild = child;
  }
  this.lastChild = child;
  this.childNodes.push(child);
  child.parentNode = this;

  if (child.nodeType == Node.ELEMENT_NODE) { 
    this.ownerDocument.update_indexes_('ID', null, child.getAttribute('id'), child);
    this.ownerDocument.update_indexes_('NAME', null, child.getAttribute('name'), child);
  }
  return child;
}

// constructor for our global document object
function BUFakeDocument() {
  BUFakeNode.call(this, Node.DOCUMENT_NODE, '#document', this); // am i my own ownerDocument?
  this.elements_by_id_ = {};
  this.elements_by_name_ = {};

  // this way they won't get warnings until they try to use it, as in node.ownerDocument.defaultView
  this.defaultView = null;

  // TODO finish the implementation object
  this.implementation = {};
  /*
  this.implementation.createDocument = function(nsuri, qname, doctype) {
	var doc = new BUFakeDocument();
	if (qname) {doc.documentElement = new BUFakeNode(qname)}
	return doc;
  };
  */
  this.implementation.hasFeature = function(feat, ver) {return false};
}

BUFakeDocument.prototype = new BUFakeNode();
BUFakeDocument.prototype.constructor = BUFakeDocument;

// internal method to implement case-sensitivity
BUFakeDocument.prototype.norm_case_ = function(name) {
  return name.toUpperCase();
}

// internal method to create minial html, head, body nodes
// It sets public member variables documentElement and body.
BUFakeDocument.prototype.create_minimal_content_ = function() {
  this.documentElement = this.appendChild(this.createElement('html'));
  /*this.head = */this.documentElement.appendChild(this.createElement('head'));
  this.body = this.documentElement.appendChild(this.createElement('body'));
}

// internal method to maintain ID and NAME indexes
// Instead of maintaining these indexes, we could have just implemented
//  getElementById and getElementsByName via iterators. 
BUFakeDocument.prototype.update_indexes_ = function(name, oldval, newval, element) {
   if (name == 'ID') {
      if (oldval) delete this.elements_by_id_[oldval];
      if (newval) this.elements_by_id_[newval] = element;
   }
   if (name == 'NAME') {
      if (oldval) delete this.elements_by_name_[oldval];
      if (newval) this.elements_by_name_[newval] = element;
   }
}

BUFakeDocument.prototype.createEvent = function(eventClass) {
  switch(eventClass) {
  case 'UIEvents':        return new BUFakeUIEvent();
  case 'MouseEvents':     return new BUFakeMouseEvent();
  case 'HTMLEvents':      return new BUFakeEvent();
  // level 2
  case 'MutationEvents':  
  // level 3
  case 'KeyEvents':  
			  throw Error("unimplemented: createEvent of " + eventClass);
  default:                throw Error("unexpected: createEvent of " + eventClass);
  }
}

BUFakeDocument.prototype.getElementById = function(idval) {return this.elements_by_id_[idval];}
// TODO: support more than one element with the same name
BUFakeDocument.prototype.getElementsByName = function(nameval) {return [this.elements_by_name_[nameval]];}


BUFakeDocument.prototype.createComment = function(data) {
    var node = new BUFakeNode(Node.COMMENT_NODE, '#comment', this);
    node.nodeValue = data;
    return node;
}

// we don't try to relate this to any Entity, let alone ensure they have the same child list
BUFakeDocument.prototype.createEntityReference = function(name) {
    var node = new BUFakeNode(Node.ENTITY_REFERENCE_NODE, name, this);
    // publicId, systemId, notationName
    return node;
}

BUFakeDocument.prototype.createProcessingInstruction = function(target, data) {
    var node = new BUFakeNode(Node.PROCESSING_INSTRUCTION_NODE, '#cdata-section', this);
    node.target = target;
    node.data = data;
    return node;
}

BUFakeDocument.prototype.createCDATASection = function(data) {
    var node = new BUFakeNode(Node.DOCUMENT_FRAGMENT_NODE, '#cdata-section', this);
    node.nodeValue = data;
    return node;
}


BUFakeDocument.prototype.createDocumentFragment = function() {
    var node = new BUFakeNode(Node.DOCUMENT_FRAGMENT_NODE, '#document-fragment', this);
    return node;
}

// Node createElement(nodeName)
BUFakeDocument.prototype.createElement = function(name) {
   var node = new BUFakeNode(Node.ELEMENT_NODE, name, this);
   node.tagName = node.nodeName; // redundant with nodeName
   return node;
};

// TODO: keep .data and .nodeValue in sync somehow despite both being writable directly
BUFakeDocument.prototype.createTextNode = function(text) {
   var node = new BUFakeNode(Node.TEXT_NODE, '#text', this);
   node.nodeValue = text;
   node.data = text;
   return node;
}

// TODO: make a BUFakeAttr subclass of BUFakeNode for these, instead.
// Note the non-standard second argument for our own convenience.
BUFakeDocument.prototype.createAttribute = function(attname, attval) {
  bu_debug("BUFakeDocument.createAttribute(", attname, ",", attval, ")");
   var node = new BUFakeNode(Node.ATTRIBUTE_NODE, attname, this);
   if (typeof attval != 'undefined') {
     node.nodeValue = attval;
     node.value = node.nodeValue; // redundant with nodeValue
   }
   node.name = node.nodeName; // redundant with nodeName
   return node;
}

// to support singleton semantics in getDocument
BUFakeNode.document = undefined;

// a namespace function to get a fake document object, creating if necessary.
// generally not needed if you are just relying on us to create the "document" global variable.
BUFakeNode.getDocument = function() {
  if(typeof BUFakeNode.document != 'undefined') return BUFakeNode.document;
  var doc = BUFakeNode.document = new BUFakeDocument();

  doc.create_minimal_content_();

  return doc;
}

// create "document" object
if (typeof document == 'undefined' || !document) {
  if (typeof print != 'undefined' && typeof load != 'undefined') print("(BUFakeDom.js) initializing fake DOM document");
  var document = BUFakeNode.getDocument();
}

