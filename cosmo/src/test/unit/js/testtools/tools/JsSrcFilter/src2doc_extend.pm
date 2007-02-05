=head1 intro

This is a custom extension for src2doc.pl

It deals with these directives:

  CLCONSTANTS
  ENDCONSTANTS
  IPROPERTYDEFS
  CPROPERTYDEFS
  ENDPROPERTYDEFS

=head1 *CONSTANTS

An example of usage:

  /** Here is some documentation. */
  //:CLCONSTANTS String
  var FOO_A = 'A';
  var FOO_B = 'B';
  //:ENDCONSTANTS

from which we would generate:

  /** Here is some documentation. */
  static const String FOO_A = 'A';
  static const String FOO_B = 'B';

=head1 *PROPERTYDEFS

An example of usage:

  burst.Something.PROP_DEFS = [
  //:IPROPERTYDEFS
  new burst.reflect.PropertyDefString({
     name: 'propname',
     descript: 'description of propname'
  })
  //:ENDPROPERTYDEFS
  ];

Between IPROPERTYDEFS (or CPROPERTYDEFS) and ENDPROPERTYDEFS we
look on 1 or more lines for property declarations of the form

   burst.reflect.PropertyDefFrabble ({ ... })

where "Frabble" is one of the burst.reflect.PropertyDef subclass names.
The paren and bracket in the opening "({" or closing "})" can have
white space but cannot be split across lines.
The opening and closing combinations can be on different lines.

=cut

my $HOOK_DBG = undef;
sub hook_dbg {print STDERR "HOOKDBG: ", @_, "\n" if $HOOK_DBG}
sub hook_warning {print STDERR "HOOK WARNING: ", @_, "\n"}

sub unquote {
    my ($s) = @_;
    return $s if !$s;
    $s =~ s/^'(.*)'$/$1/;
    $s =~ s/^"(.*)"$/$1/;
    return $s;
}

sub print_prop_def {
    my ($lang, $cmd, $args, $typename, $att_string, $is_static) = @_;

    hook_dbg("property type is '$typename', att_string: $att_string");

    my $hash;

    # assume $att_string is a series of lines like: ^foo: "bar"$
    if (1) {
       $hash = {};
       while ($att_string =~ m/^\s*(\w+)\s*:\s*(.*?),?\s*$/mg) {
           $hash->{$1} = $2;
           hook_dbg("   hash{$1}=$2");
       }
   }

    # do it via eval
   elsif (undef) {
      # change ':' to '=>' so we can eval it as a perl hash
      $att_string =~ s/(\w+):/$1 =>/g;
      # change bare true and false to be quoted
      $att_string =~ s/(=>\s*)true/$1'true'/g;
      $att_string =~ s/(=>\s*)false/$1'false'/g;

      $hash = eval("{$atts}");
    }

    use Data::Dumper;
    hook_dbg("parsed atts: ", Dumper($hash));

    my $varname = $hash->{name} || die "no name in hash: ", Dumper($hash);
    $varname = unquote($varname);
    my $defaultValue = $hash->{defaultValue};
    $defaultValue = "'$defaultValue'" if $defaultValue && $typename eq 'String';
    my $desc = unquote($hash->{description});
    my $kind = $cmd eq 'IPROPERTYDEFS' ? 'Instance' : 'Class';

    print STDOUT "/**\n* $kind property" . ($desc ? ': ' . $desc : '') . "\n";
    print STDOUT "*\n* Must be one of: " . $hash->{enumArray} if $hash->{enumArray};
    print STDOUT "*\n* Must be one of: " . $hash->{enumMap} if $hash->{enumMap};
    print STDOUT "*\n* Default value is $defaultValue\n" if $defaultValue;
    print STDOUT "* No default.\n" if !$defaultValue;
    print STDOUT "*\n* This property is mandatory.\n" if $hash->{isMandatory};
    print STDOUT "*/\n";
    print STDOUT "public " if $lang eq 'java';
    print STDOUT "static " if $is_static;
    print STDOUT "$typename $varname;", "\n";
}

$CMD_HOOK = sub {
    my ($lang, $cmd, $args) = @_;
    hook_dbg("got command '$cmd'");

    if ($cmd eq 'CLCONSTANTS') {
        my $typename = $args;
        my $saw_end = 0;
	my $saw_comment = 0;
	my $modifier = ($lang eq 'java' ? 'public static final' : 'static const'); 
        while(<>) {
            hook_dbg("hook is parsing line:", $_);
            if (m,^\s*//$,) {
                hook_dbg('skipping C++ comment line');
            }
            elsif (m,^\s*/\*.*?\*/\s*$,) {
		$saw_comment = 1;
		hook_dbg('printing C comment line');
                print STDOUT $_;
            }
            elsif (m/ENDCONSTANTS/) {
              $saw_end = 1;
	      hook_dbg("hook saw ENDCONSTANTS");
	      last;
            }
            elsif (m/var\s+(.*\S)/) {
		my $rest = $1;
		$rest = "$rest;" unless $rest =~ m/;\s*$/;
		if (!$saw_comment) {
		    $rest =~ m/=\s*(.*\S)\s*;/;
		    print STDOUT "/** $1 */\n";
		}
		print STDOUT "$modifier $typename $rest\n";
		$saw_comment = 0;
            }
	    elsif (m/^\s*$/ || m,^\s*/\*, || m,^\s*\*/\s*$, || m,^\s*//,) {}
            else {hook_warning("hook not using non-white line: ", $_);}
        }
        if (!$saw_end) {die "saw eof before ENDCONSTANTS";}
        return 1;
    }

    # instance or class properties
    # loop through lines parsing 
    elsif ($cmd eq 'IPROPERTYDEFS' || $cmd eq 'CPROPERTYDEFS') {
        my $typename;
        my $att_string;
        my $indecl = 0;
	my $is_static = ($cmd eq 'CPROPERTYDEFS');
	while (<>) {
	    hook_dbg("hook is parsing line:", $_);

	    if (m,^\s*//$,) {
		hook_dbg('skipping C++ comment line');
	    }
	    elsif (m,^\s*/\*.*?\*/\s*$,) {
		hook_dbg('skipping C comment line');
            }
            elsif ($indecl) {
                if (m/(.*)\}\s*\)/) {
                    hook_dbg('found end of property declaration');
		    $att_string .= $1;
		    $indecl = 0;
		    print_prop_def($lang, $cmd, $args, $typename, $att_string, $is_static);
		}
                else {
		    hook_dbg("in property declaration");
		    $att_string .= $_;
                }
	    }
	    elsif (m/burst\.reflect\.PropertyDef(\w*)\s*\(\s*\{(.*)/) {
		$typename = $1;
		$att_string = $2;
		if ($att_string =~ m/(.*)\}\s*\)/) {
                    hook_dbg("start and end of property declaration in one line");
		    $att_strings = $1;
		    print_prop_def($lang, $cmd, $args, $typename, $att_string, $is_static);
                }
                else {
                    hook_dbg("start of property declaration");
                    $indecl = 1;
                }
	    }
	    elsif (m/ENDPROPERTYDEFS/) {
		hook_dbg("hook is not using last line and is returning");
		last;
	    }
	    else {
		if (m/^\s*$/ || m,^\s*/\*, || m,^\s*\*/\s*$, || m,^\s*//,) {}
		else {hook_warning("hook not using non-white line: ", $_);}
	    }
	}
        if ($indecl) {die "saw eof before end of burst.reflect.PropertyDef";}
        return 1;
    }
    else {die "unknown directive '$cmd' (followed by: '$args')";}
};

1;
