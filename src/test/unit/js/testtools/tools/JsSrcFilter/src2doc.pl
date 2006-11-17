#!/usr/bin/perl -w

=head1 NAME

 src2doc.pl - Extract comments in a source file, with optional fake code generation

=head1 SYNOPSIS

 src2doc.pl file.js > file.cpp

or in a pipeline:

 cat file.js | src2doc.pl > file.cpp

=head1 DESCRIPTION

This will extract comments from a source file. All comments are extracted, though
typically the ones of interest are the javadoc-style ones.

No attempt is made to parse non-comment content in the input file (though any
extension module may choose to).
This means that this utility could be used with a variety of source programming
languages (and the source programming language could use a variety of idioms).

Any input lines which are completely white space are passed through to the output
(regardless of whether inside or outside a comment).

This program may be used directly as a Doxygen filter.

=head2 Generated Output

In addition to normal comment extraction, it also looks for special comments
that start with "//:". These are interpreted as directives for code generation.

The idea is that this can be used to put code in the output that may not even be
the same programming language as the input (but may be processed by a javadoc-like
program in the output pipeline).

At the moment, the only output programming language supported is C++.
(This is in part because C++ is the default language expected by doxygen, and
currently doxygen only changes that language expectation according to file suffix,
which is awkward in a pipeline/filtering setup. Also, C++ supports more paradigms
than Java.)

We do document here what the output would be if we were generating Java.

The supported generation directives fall into these categories:

   namespace
   class
   interface
   global
   enum

Note that in some output languages, not all of these entities are available,
but we attempt to make meaningful approximations. For example, C++ has no "interface",
and Java has no "enum".

=head3 Namespace Directives

 example javascript input:

   /** this namespace does trigonometry */
   //:NSBEGIN Trigonemtry
   var Trigonometry = {};

   /** ratio of circumference to diameter */
   //:NSCONSTANT Number PI = 3.14159
   Trigonometry.PI = 3.14159;

   /** array of figures created */
   //:NSVAR Array figures
   Trigonometry.figures = [];

   /** calculate the area of a circle */
   //:NSFUNCTION Number calculateArea(Number radius)
   Trigonometry.calculateArea = function(radius) {return 2 * Trigonometry.PI * radius};

   //:NSEND

 another (equivalent) example javascript input, with identical comments:

    /** this namespace does trigonometry */
    //:NSBEGIN Trigonemtry
    var Trigonometry = {
      /** ratio of circumference to diameter */
      //:NSCONSTANT Number PI = 3.14159
      PI: 3.14159,
      /** array of figures created */
      //:NSVAR Array figures
      figures: [],
      /** calculate the area of a circle */
      //:NSFUNCTION Number calculateArea(Number radius)
      calculateArea: function(radius) {return 2 * Trigonometry.PI * radius}
    //:NSEND
    };

 example C++ output:

   /** this namespace does trigonometry */
   namespace Trigonometry {
      /** ratio of circumference to diameter */
     const Number PI = 3.14159;
      /** array of figures created */
     Array figures;
      /** calculate the area of a circle */
     Number calculateArea(Number radius) {}
   }

=over 4

=item namespace begin - NSBEGIN

   synopsis: //:NSBEGIN $token
   C++: namespace $token {
   java: public class $token {

=item namespace constant - NSCONSTANT

   synopsis:  //:NSCONSTANT $line
   C++: const $line;
   java: public static final $line;

=item namespace variable - NSVAR

   synopsis: //:NSVAR $line
   C++: $line;
   java: public static $line;

=item namespace function - NSFUNCTION

   synopsis: //:NSFUNCTION $line
   C++: $line {}
   java: $line {}

=item namespace end - NSEND

   synopsis: //:NSEND [$token]
   C++: }
   java: }

=back


=head3 Class Directives

 example javascript input (with no javadoc comments):

   //:CLBEGIN Foo extends Bar
   function Foo() {
      Bar.call(this);
   }
   Foo.prototype = new Bar();
   Foo.prototype.constructor = Foo;
   //:CLCONSTANT int CLASSICAL_PI = 3
   Foo.CLASSICAL_PI = 3;
   //:CLEND Foo

 example C++ output:

   class Foo : public Bar {public:
     static const int CLASSICAL_PI = 3;
   };

=over 4

=item class begin - CLBEGIN

    synopsis: //:CLBEGIN $classname [extends $superclass] [implements $interface1, $interface2]
    C++:  class $classname [: public $superclass, public $interface1, public $interface2] { public:
    java: public $classname [extends $superclass] [implements $interface1, $interface2] {

=item class begin - CLABEGIN

Begin an abstract base class. For C++ output, this is the same as CLBEGIN (since C++ automatically
determines that an ABC is one with a pure virtual method). For Java output, an extra 'abstract'
is added to the class declaration.

=item class constant - CLCONSTANT

    synopsis: //:CLCONSTANT $line
    C++: static const $line;
    java: public static final $line;

=item class instance variable - CLIVAR

    synopsis: //:CLIVAR  $line
    C++:  $line;

=item class static variable - CLCVAR

    synopsis: //:CLCVAR $line
    C++:  static $line;

=item class instance method - CLIMETHOD

    synopsis: //:CLIMETHOD $line
    C++: $line {}

=item class constructor method - CLCONSTRUCT

    synopsis: //:CLCONSTRUCT $line
    C++: $line {}

=item class static method - CLCMETHOD

    synopsis: //:CLCMETHOD $line
    C++: static $line {}

=item class abstract method - CLAMETHOD

For C++ output, this the same as IFMETHOD. For Java output, it is different because
interface methods and class abstract methods are declared differently.

    synopsis: //:CLAMETHOD $line
    C++: virtual $line = 0;
    java: abstract $line;

=back

=head3 Interface Directives

=over 4

=item interface begin - IFBEGIN

    synopsis: //:IFBEGIN $token
    C++:  class $token { public:
    java: interface $token {

=item interface constant - IFCONSTANT

    synopsis: //:IFCONSTANT $line
    C++: static const $line;
    java: static final $line;

=item interface method - IFMETHOD

    synopsis: //:IFMETHOD $line
    C++: virtual $line = 0;
    java: $line;

=item interface end - IFEND

    synopsis: //:IFEND
    C++:  };
    java: }

=back

=head3 Global Directives

=over 4

=item global constant - GLCONSTANT

    synopsis: //:GLCONSTANT $line
    C++: const $line;
    java: public static final $line;

=item global variable - GLVAR

    synopsis: //:GLVAR $line
    C++: $line;
    java: $line;

=item global function - GLFUNCTION

    synopsis: //:GLFUNCTION $line
    C++: $line {}

=back

=head3 Enum Directives

=over 4

=item enum begin - ENUMBEGIN

     synopsis: //:ENUMBEGIN $typename
     C++:  enum $typename {

=item enum value - ENUMVAL

     synopsis:  //:ENUMVAL $valname $value $comment
     C++: $valname [= $value], /**< $comment */

=item enum end - ENUMEND

     synopsis: //:ENUMEND
     C++: };

=back

=head2 Extension Modules

The utility will attempt to load a module named 'src2doc_extend.pm' (from the perl include path,
augmented by the directory this program is in). If available, it must define a global
variable $CMD_HOOK which is a sub taking parameters ($lang, $cmd, $args).
When called, it can read any number of input lines. It should return false if it
does not handle the command.

=head1 TODO

Currently only C++ output is implemented.

=head1 SEE ALSO

=head2 src2code.pl

The opposite side of this utility is src2code.pl, which extracts just the code,
not the comments.

=head2 Literate Programming

Purists will realize that these utilities do not actually constitute literate programming.
Knuth's "weave" (get documentation) and "tangle" (get code)
support re-ordering of code, and in general presumes a more intimate
relationship between documentation and executable artifacts.

=head2 js2doxy.pl

As part of his JsUnit project, Jorg Schaible implemented a perl program that converts javascript source
code to C++ suitable for doxygen processing: js2doxy.pl, http://jsunit.berlios.de/internal.html .

It generates C++ not from parsed comments (as we do), but instead by attempting
to parse the javascript source code itself.
If you write your code following his conventions, that can be more convenient.
However, it makes the tool javascript-specific, and limits the javascript idioms that can be used.
The tool also limits the set of javadoc tags that can be used.
It is also not set up to be used directly as a doxygen filter (src2doc.pl is).
Instead it supplies a largely superfluous 'jsdoc' wrapper.

=head2 JSDoc

An apparently abandoned sourceforge project at http://jsdoc.sourceforge.net/
it consists of 1-page perl program that attempts to emulate javadoc.

=head2 JSdoc

Located at http://www.stud.tu-ilmenau.de/~thla-in/scripts/JSdoc/ and is unrelated to the sourceforge JSDoc project.
It seems not to be released, just described.

=head2 jsdoc.js

There is a basic javascript program that like the perl jsdoc attempts
to implement javadoc: http://lxr.mozilla.org/mozilla/source/js/rhino/examples/jsdoc.js

=head1 AUTHOR

Copyright 2003, Mark D. Anderson, mda@discerning.com.

This is free software; you can redistribute it and/or
modify it under the same terms as Perl itself.

=cut

use FindBin;
use lib "$FindBin::Bin";
use lib ".";

use vars qw($CMD_HOOK);

eval {
    require src2doc_extend;
};
print STDERR "Could not find any extension module called src2doc_extend.pm: $@\n" if $@;

my $DEFAULT_LANG = 'cxx';
my $DO_RENAME = 0;

# generate a source line given the directive and the language it is in.
sub directive {
    my ($lang, $cmd, $args) = @_;

    if ($lang eq 'cxx') {
	directive_cxx($cmd, $args);
    }
    elsif ($lang eq 'java') {
	directive_java($cmd, $args);
    }
    else {
	die "unsupported output language '$lang'";
    }
}

# Generate C++ given our abstract commands. Given a line "//:NSBEGIN foo", $cmd is 'NSBEGIN' and $args is 'foo'.
sub directive_cxx {
    my ($cmd, $args) = @_;

    $args =~ s/^\s+//; $args =~ s/\s+$//;

    warn "arguments contain semi-colon: '$args'" if $args =~ m/\;/;
    warn "arguments do not contain parentheses: '$args'" if $args !~ m/\(/ && ($cmd =~ m/CONSTRUCT/ || $cmd =~ m/FUNCTION/ || $cmd =~ m/METHOD/);

    # namespace
    if    ($cmd eq 'NSBEGIN')    {print "namespace $args {\n";}
    elsif ($cmd eq 'NSCONSTANT') {print "const $args;\n";}
    elsif ($cmd eq 'NSVAR')      {print "$args;\n";}
    elsif ($cmd eq 'NSFUNCTION') {print "$args {}\n";}
    elsif ($cmd eq 'NSEND')      {print "}\n";}

    # interface and class
    elsif ($cmd eq 'IFBEGIN' || $cmd eq 'CLBEGIN' || $cmd eq 'CLABEGIN') {
	my @tokens = split(/[\s,]+/, $args);
	die "no arguments in '$args' for directive '$cmd'" if @tokens < 1;
	my $clname = shift @tokens;
	$clname =~ s/\./::/g;
	my $state = undef;
	my @extends = ();
	my @implements = ();
	for my $tok (@tokens) {
	    next unless $tok;
	    if ($tok eq 'extends') {$state = 'extends';}
	    elsif ($tok eq 'implements') {$state = 'implements';}
	    else {
		if (!$state) {die "no 'extends' or 'implements' prior to '$tok' in '$args'"}
		elsif ($state eq 'extends') {push(@extends,$tok);}
		elsif ($state eq 'implements') {push(@implements,$tok);}
		else {die "bad state '$state'"}
	    }
	}
	print "class $clname";
	my @all = (@extends, @implements);
	if (@all) {print ' : public ', join(", public ", @all);}
	print " { public:\n";
    }
    elsif ($cmd eq 'IFCONSTANT' || $cmd eq 'CLCONSTANT') 
                                 {print "static const $args;\n";}
    elsif ($cmd eq 'IFMETHOD')   {print "virtual $args = 0;\n";}
    elsif ($cmd eq 'CLAMETHOD')  {print "virtual $args = 0;\n";}
    elsif ($cmd eq 'CLCONSTANT') {print "static const $args;\n";}
    elsif ($cmd eq 'CLIVAR')     {print "$args;\n";}
    elsif ($cmd eq 'CLCVAR')     {print "static $args;\n";}
    elsif ($cmd eq 'CLIMETHOD' || $cmd eq 'CLCONSTRUCT')
                                 {print "$args {}\n";}
    elsif ($cmd eq 'CLCMETHOD')  {print "static $args {}\n";}
    elsif ($cmd eq 'IFEND' || $cmd eq 'CLEND') {print "};\n";}

    elsif ($cmd eq 'GLVAR')      {print "$args;\n";}
    elsif ($cmd eq 'GLCONSTANT') {print "const $args;\n";}
    elsif ($cmd eq 'GLFUNCTION') {print "$args {}\n";}

    else {
        my $handled;
	my $lang = 'cxx';
	$handled = &$CMD_HOOK($lang, $cmd, $args) if $CMD_HOOK;
	die "unknown directive '$cmd' (followed by: '$args')" if !$handled;
    }
}

# Generate Java given our abstract commands. Given a line "//:NSBEGIN foo", $cmd is 'NSBEGIN' and $args is 'foo'.
sub directive_java {
    my ($cmd, $args) = @_;

    $args =~ s/^\s+//; $args =~ s/\s+$//;

    # with doxygen and java, it won't document static functions and variables outside
    # of any package. So don't use ' static';
    my $glmod = '';

    warn "arguments contain semi-colon: '$args'" if $args =~ m/\;/;
    warn "arguments do not contain parentheses: '$args'" if $args !~ m/\(/ && ($cmd =~ m/CONSTRUCT/ || $cmd =~ m/FUNCTION/ || $cmd =~ m/METHOD/);

    $args =~ s/::/\./g;

    # namespace
    if    ($cmd eq 'NSBEGIN')    {
       my ($pack, $clname) = ($args =~ m/(.*)\.(.*)/);
       if (!$pack || !$clname) {
	   # warn "could not parse package and class out of NSBEGIN args: '$args'";
	   print "public abstract final class $args {\n";
       } 
       else {
	   print "package $pack;\npublic abstract final class $clname {\n";
       }
    }
    elsif ($cmd eq 'NSCONSTANT') {print "public static final $args;\n";}
    elsif ($cmd eq 'NSVAR')      {print "public static $args;\n";}
    elsif ($cmd eq 'NSFUNCTION') {print "public static $args {}\n";}
    elsif ($cmd eq 'NSEND')      {print "}\n";}

    # interface and class
    elsif ($cmd eq 'IFBEGIN')    {print "public interface $args {";}
    elsif ($cmd eq 'CLBEGIN')    {print "public class $args {";}
    elsif ($cmd eq 'CLABEGIN')   {print "public abstract class $args {";}
    elsif ($cmd eq 'IFCONSTANT' || $cmd eq 'CLCONSTANT') 
                                 {print "public static final $args;\n";}
    elsif ($cmd eq 'IFMETHOD')   {print "$args;\n";}
    elsif ($cmd eq 'CLAMETHOD')  {print "public abstract $args;\n";}
    elsif ($cmd eq 'CLCONSTANT') {print "public static final $args;\n";}
    elsif ($cmd eq 'CLIVAR')     {print "public $args;\n";}
    elsif ($cmd eq 'CLCVAR')     {print "public static $args;\n";}
    elsif ($cmd eq 'CLIMETHOD' || $cmd eq 'CLCONSTRUCT')
                                 {print "public $args {}\n";}
    elsif ($cmd eq 'CLCMETHOD')  {print "public static $args {}\n";}
    elsif ($cmd eq 'IFEND' || $cmd eq 'CLEND') {print "} // $args\n";}

    elsif ($cmd eq 'GLVAR')      {print "public$glmod $args;\n";}
    elsif ($cmd eq 'GLCONSTANT') {print "public$glmod final $args;\n";}
    elsif ($cmd eq 'GLFUNCTION') {print "public$glmod $args {}\n";}

    else {
        my $handled;
	my $lang = 'java';
	$handled = &$CMD_HOOK($lang, $cmd, $args) if $CMD_HOOK;
	die "unknown directive '$cmd' (followed by: '$args')" if !$handled;
    }
}

sub raw_cxx {
    my ($rest) = @_;
    print $rest, "\n";
}

sub raw_java {
    my ($rest) = @_;
    print $rest, "\n";
}

my $incomment = 0;

while($ARGV[0] =~ m/\-(.*)/) {
    my $opt = shift @ARGV;
    if ($opt eq '-java') {$DEFAULT_LANG = 'java';}
    elsif ($opt eq '-cxx') {$DEFAULT_LANG = 'cxx';}
    elsif ($opt eq '-rename') {$DO_RENAME = 1;}
    else {die "Unknown option $opt";}
}
while(<>) {
    if ($incomment) {
	# close */
	if (m,(.*\*/),) {print $1, "\n"; $incomment = 0;}
	# within /* and */ on other lines
	else {
	    # fix any @file directive for current suffix
	    if ($DO_RENAME) {
		s/(\@file \S*).js/$1.$DEFAULT_LANG/;
	    }
	    print $_;
	}
    }
    else {
	# special line to uncomment like "//CODE"
	if (m,//CODE\s(.*),) {print $1, "\n";}

	# raw C++
	elsif (m,//=c\+\+ (.*),i || m,//=cxx (.*),i) {raw_cxx($1);}

	# raw Java
	elsif (m,//=java (.*),i) {raw_java($1);}

	elsif (m,//:(\w+)\s(.*),) {directive($DEFAULT_LANG, $1, $2);}

	# // comment
	elsif (m,(//.*),) {print $1, "\n";}

	# /* */ open and close on same line
	elsif (m,(/\*.*?\*/),) {print $1, "\n";}

	# /* open
	elsif (m,(/\*.*),) {$incomment = 1; print $1, "\n";}

	# preserve entirely white lines
	elsif (m,^\s*$,) {print $_;}
    }
}
