#!/usr/bin/perl -w

=head1 NAME

 src2code.pl - Output only code (no comments), and reduce white space

=head1 SYNOPSIS

 src2code.pl file1.js file2.js ... > file_code.js

or in a pipeline:

 cat file.js | src2code.pl > file_code.js

=head1 DESCRIPTION

This extracts the source code from one or more files. 

This utility should work with any source programming language, though it was
written for ECMAScript source files. 

The opposite utility to this is src2doc.pl, which extracts only comments.

We do not try to milk the last byte out of the script.
For example, we do not attempt replace all symbol names with shorter ones.

The 'gzip' transfer-encoding in HTTP is an excellent way to further reduce
bandwidth requirements when transfering script code to a browser.

By default, we attempt to preserve a certain amount of indenting (with single chars),
so that the output script is still somewhat readable.

Because ECMAScript doesn't allow multi-line quoted strings, for simplicity of
implementation we currently restrict ourselves
to reducing transformations which include a line ending or beginning.

=head1 TODO

Collapse internal runs of white space, and eliminate internal white space altogether,
when semantics are preserved. This will introduce reliance on the input being
ECMAScript, and so should be made optional.

Optionally preserve jscript conditional compilation statements hidden in comments: @cc_on @if @else @elif @end

=head1 SEE ALSO

Any simple web search such as reveals many programs that will compress javascript
source code, or purports to "obfuscate" it. 
See for example:

   http://www.google.com/search?q=javascript+compress
   http://dmoz.org/Computers/Programming/Languages/JavaScript/Tools/

This section contains brief reviews of a semi-random selection of them.

=head2 JSMIN

Douglous Crockford has JSMIN: http://www.crockford.com/javascript/jsmin.html
But:

 - it has no indicated license
 - it is written in C (so harder to start using than a perl script)
 - it is rather aggressive about white space (including EOL elimination),
   making the output hard to read
 - it does strive to be correct about quoted strings and regexp literals

=head2 compress.pl

Ted Schroyer has compress.pl: http://cvs.sourceforge.net/cgi-bin/viewcvs.cgi/netwindows/netWindows/winScripts/compress.pl

It is a small script written in Perl and licensed under the AFL.
However, it is rather careless about protecting the contents of quoted strings
(unlike Crockford's, which maintains an internal state machine).
It also is not using the '/m' flag when it should.

=head2 Crunchinator

Mike Hall has the "Crunchinator", see http://www.brainjar.com/js/crunch/

This is written in javascript, and is intended for use only javascript source.
It does deal with strings. It is overly aggressive about combining lines, doing so
even when it would break the code (because of implied semi-colons).
It does not deal correctly with regexp literals in javascript source code.
It is under a restrictive license: http://www.brainjar.com/terms.asp .

=head2 JSCruncher

The DomAPI project has http://www.domapi.com/jscruncher.cfm
This is a closed-source Windows executable. It claims to follow the same rules
as Crunchinator.

=head2 JSPack

Dan Steinman has http://www.dansteinman.com/jspack/
It is GPL, written in Perl.
Does nothing about quoted strings, and in general has little to recommend it.

=head2 Salstorm ESC

ESC is at http://www.saltstorm.net/depo/esc/
It is written in "JScript", and is under the GPL.
It supports variable name subsitution.

=head2 Semantic Designs ECMAScriptFormatter

This is a commercial product intended for source code formatting, but it
will "obfuscate" as well.

=head2 XenoCode 

See http://www.xenocode.com/en/

It works on .NET languages including JScript. Will do dead code elimination.


=head1 AUTHOR

Copyright 2003, Mark D. Anderson, mda@discerning.com.

This is free software; you can redistribute it and/or
modify it under the same terms as Perl itself.

=cut

my $in_files = 0;
my $in_lines = 0;
my $in_chars = 0;
my $blank_lines = 0;
my $out_lines = 0;
my $out_chars = 0;

# read all input files into a single big string
sub readall {
    $in_files = scalar(@ARGV) || 1;
    my $contents = '';
    while(<>) {
	$in_lines++;
	$contents .= $_;
	$in_chars += length($_);
    }
    return $contents;
}

# (currently unused.) read just the first named file into a string.
sub readarg {
    my $filename = $ARGV[0];
    open (JS, "<$filename") || die "can't open $filename: $!";
    local $/ = undef;
    my $contents = <JS>;
    close JS;
    return $contents;
}

# extract and print the code from the input
sub codeonly {
    my ($contents) = @_;
    $_ = $contents;

    # normalize EOL chars
    s/\r/\n/g;

    # Remove all C and C++ comments. You aren't expected to understand this.
    # See perlfaq 6.10, "How do I use a regular expression to strip C style comments from a file?"
    # I have correct the example there to avoid an 'uninitialized value' warning.
    # I have also added support for literal regexps.
    # TODO: should /*...*/ be replaced with empty string or single space? Currently it is empty string.
    s#/\*[^*]*\*+([^/*][^*]*\*+)*/|(?<!\\)//[^\n]*|("(\\.|[^"\\])*"|'(\\.|[^'\\])*'|/(\\.|[^\n/\\])*/|.[^/"'\\]*)#defined($2)?$2:''#gse;

    # eliminate all lines that are entirely white
    $blank_lines = s/\n\s*\n/\n/g;

    # eliminate all trailing white
    s/\s+$//gm;

    # normalize leading tabs to space
    s/^( *)\t/$1 /mg;

    # heuristically find the indent size being used
    my %lengths = ();
    while (m/^( +)/gm) {
	$lengths{length($1)}++;
    }
    my $indent = 1;
    my $maxcount = 0;
    while(my($k,$v) = each %lengths) {$indent = $k, $maxcount = $v if $v > $maxcount;}
    use Data::Dumper;
    # print STDERR "indent counts: ", Dumper(\%lengths);
    # print STDERR "Found indent=$indent\n";
    
    # replace all leading white space by a string of length divided by the indent size
    s#^(\ +)#substr($1,0,length($1)/$indent)#mexg;

    # print the line
    print $_;
    my @lines = split('\n');
    $out_lines = scalar(@lines);
    $out_chars = length($_);
}

sub main {
    my @opts = grep {m/^-/} @ARGV;
    @ARGV = grep {! m/^-/} @ARGV;
    my $opts = "@opts";
    $opt_quiet = 1 if $opts =~ m/-q/;
    codeonly(readall());
    print STDERR "Input $in_files files, $in_lines lines ($blank_lines blank), $in_chars chars. Output $out_lines lines, $out_chars chars.\n" unless $opt_quiet;
}



main();
