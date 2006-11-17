#!/usr/bin/perl -w

####
# generate a javascript file that has function names extracted from the input.
# does not exclude function names that may be in input comments.
###
print "jum.TEST_FUNCTION_NAMES = [\n";
my $ctr = 0;
while(<>) {
    if (m/function (test_[_\w]+)/) {
	print ',' if $ctr++ > 0; 
        print "'$1'\n";
    }
}
print "]; // $ctr tests\n";
