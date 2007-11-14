dojo.provide("test.performance.topics");
dojo.require("dojo.event.topic");

test.performance.topics = {
    manyTopics: function(publishIterations, doSomething){
        var a = function(){doSomething("a")}
        var b = function(){doSomething("b")}
        var c = function(){doSomething("c")}
        var d = function(){doSomething("d")}
        var e = function(){doSomething("e")}
        var f = function(){doSomething("f")}
        var g = function(){doSomething("g")}
        var h = function(){doSomething("h")}
        var i = function(){doSomething("i")}
        var j = function(){doSomething("j")}
        dojo.event.topic.subscribe("a", a);
        dojo.event.topic.subscribe("b", b);
        dojo.event.topic.subscribe("c", c);
        dojo.event.topic.subscribe("d", d);
        dojo.event.topic.subscribe("e", e);
        dojo.event.topic.subscribe("f", f);
        dojo.event.topic.subscribe("g", g);
        dojo.event.topic.subscribe("h", h);
        dojo.event.topic.subscribe("i", i);
        dojo.event.topic.subscribe("j", j);

        var time = this._timePublish(publishIterations, "a");

        dojo.event.topic.destroy("a");
        dojo.event.topic.destroy("b");
        dojo.event.topic.destroy("c");
        dojo.event.topic.destroy("d");
        dojo.event.topic.destroy("e");
        dojo.event.topic.destroy("f");
        dojo.event.topic.destroy("g");
        dojo.event.topic.destroy("h");
        dojo.event.topic.destroy("i");
        dojo.event.topic.destroy("j");
        
        return time;
    },
    
    oneTopic: function(publishIterations, doSomething){
        var func = function (action){
            switch(action){
            case "a": doSomething("a"); break;
            case "b": doSomething("b"); break;
            case "c": doSomething("c"); break;
            case "d": doSomething("d"); break;
            case "e": doSomething("e"); break;
            case "f": doSomething("f"); break;
            case "g": doSomething("g"); break;
            case "h": doSomething("h"); break;
            case "i": doSomething("i"); break;
            case "j": doSomething("j"); break;
            }
        }
        dojo.event.topic.subscribe("func", func);
        
        var time = this._timePublish(publishIterations, "func", "a");
        
        dojo.event.topic.destroy("func");

        return time;
    },

    oneTopicHash: function(publishIterations, doSomething){
        var funcHash = {
            a: function(){doSomething("a")},
            b: function(){doSomething("b")},
            c: function(){doSomething("c")},
            d: function(){doSomething("d")},
            e: function(){doSomething("e")},
            f: function(){doSomething("f")},
            g: function(){doSomething("g")},
            h: function(){doSomething("h")},
            i: function(){doSomething("i")},
            j: function(){doSomething("j")}
        }
        var func = function(action){
            funcHash[action]();
        }
        dojo.event.topic.subscribe("func", func);

        var time = this._timePublish(publishIterations, "func", "a");
        
        dojo.event.topic.destroy("func");

        return time;
    },
    
    _timePublish: function(iterations, topic, args){
        var start = new Date();
        for (var i = 0; i < iterations; i++){
            dojo.event.topic.publish(topic, args);
        }
        var end = new Date();
        return end.getTime() - start.getTime();
    }
};

function doSomething(x){
    var y = x;
}


var runs = 50;
var total = 0;
for (var i = 0; i < runs; i++){
//    total += test.performance.topics.manyTopics(1000, doSomething);
//    total += test.performance.topics.oneTopic(1000, doSomething);
    total += test.performance.topics.oneTopicHash(1000, doSomething);
}
var ave = total / runs;
dojo.debug(ave)

