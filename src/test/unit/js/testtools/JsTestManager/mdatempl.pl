#!/usr/bin/perl -w

=head1 NAME

 mdatempl.pl - substitute command-line bindings into a template

=head1 SYNOPSIS

 mdatempl.pl SYMBOL1="hello world" SOMELIST="a b c" template.html > file.html

=head1 DESCRIPTION

(Why even bother documenting this? The world needs another templating system like
a hole in the head.)

This performs these substitutions:

  - substitutes $FOO symbols anywhere in the file with the binding corresponding bound value

  - substitutes { ... } with the result of doing a perl eval of the contents

  - processes loops of the form:
      %FOR LOOPSYM LISTSYM
      ...
      %END
    by binding LOOPSYM for every space-separated value in LISTSYM. 

We don't deal with:
 
 - escaping (except for a hack to avoid processing anything in a style element)
 - nested loops
 - multi-line expressions
 - conditional expressions (like an %IF/%ELSEIF/%ELSE/%END)

=head1 AUTHOR

Copyright 2003, Mark D. Anderson, mda@discerning.com.

This is free software; you can redistribute it and/or
modify it under the same terms as Perl itself.

=cut

my $BINDINGS = {};
my $INFILE;
my $PACKAGE = 'SYMPACK';

my $DEBUG = 0;
sub dbg {
    print STDERR 'DBG: ', @_, "\n" if $DEBUG;
}

sub usage {
    my ($mess) = @_;
    print STDERR "Error: $mess\n";
    print STDERR "Usage: $0 SYM1=VAL1 SYM2=VAL2 ... filename\n";
    exit 1;
}

sub process_argv {
    foreach my $arg (@ARGV) {
	if ($arg =~ m/(.*)=(.*)/) {
	    $BINDINGS->{$1} = $2;
        }
	elsif (!$INFILE) {$INFILE = $arg;}
	else {usage("unexpected argument '$arg'");}
    }
    usage("no input file") if !$INFILE;
}

sub eval_expr {
    my ($bindings, $expr, $text) = @_;
    dbg("evaluating '$expr'");
    while(my($k,$v) = each %$bindings) {
	dbg("   binding $k to value '$v'");
	eval("package $PACKAGE; \$$k = '$v'");
    }
    my $warnings = '';
    local $SIG{__WARN__} = sub {$warnings .= $_[0]};
    my $result = eval("package $PACKAGE; $expr");
    die "expression '$expr' threw an exception: $@" if $@;
    die "expression '$expr' had warnings: $warnings" if $warnings;
    dbg("evaluated '$expr' to be '$result'");
    return $result;
}

sub lookup_binding {
    my ($bindings, $sym, $text) = @_;
    return $bindings->{$sym} if defined($bindings->{$sym});
    my $val = eval("package $PACKAGE; \$$sym");
    return $val if defined($val);
    die "no binding for symbol '$sym' in text '$text'";
}

sub subst_text {
    my ($bindings, $text) = @_;

    # {} expressions
    # do this first, because of dollars in expressions, and because of bindings
    $text =~ s/\{(.*?)\}/eval_expr($bindings,$1)/eg;

    # dollar symbols
    $text =~ s/\$(\w+)/lookup_binding($bindings,$1,$text)/eg;

    return $text;
}

sub subst_file {
    my ($bindings, $in_filename) = @_;
    open(IN, "<$in_filename") || die "can't open $in_filename for read: $!";
    my $in_exclude = 0;
    while(<IN>) {
	# we exclude things with a <style> section
        if (m,<style, && !m,</style,) {
	    $in_exclude = 1;
            print STDOUT $_;
        }
        elsif ($in_exclude) {
	    $in_exclude = 0 if m,</style,;
            print STDOUT $_;
        }

	# a %FOR loop
	elsif (m/^\s*\%FOR\s+(\w+)\s+(\w+)\s*$/) {
	    my ($sym, $array) = ($1,$2);
	    my $valstr = $bindings->{$array};
	    die "loop array symbol '$array' not bound" unless defined($valstr);
	    print STDERR "WARNING: loop array symbol '$array' is empty\n" unless $valstr;
	    my $looptext = '';
	    my $saw_end = 0;
	    while(<IN>) {
		$saw_end = 1, last if m/^\s*\%END/;
		$looptext .= $_;
	    }
	    die "never saw loop end before EOF, got text '$looptext'" unless $saw_end;
	    my @vals = split(' ',$valstr);
            dbg("processing loop of $sym over $array='$valstr'");
	    foreach my $val (@vals) {
		dbg("   binding loop symbol $sym='$val'");
		$bindings->{$sym} = $val;
		print STDOUT subst_text($bindings, $looptext);
	    }
	}

	# something else that needs substitutions, just do 
	else {
	    print STDOUT subst_text($bindings, $_);
	}
    }
    close IN;
    dbg("done processing file $in_filename");
}

process_argv();
subst_file($BINDINGS, $INFILE);

exit 0;
