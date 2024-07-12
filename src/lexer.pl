#!/usr/bin/perl
use strict;
use warnings;
use 5.12.1;

if (@ARGV != 1) {
    die "Usage: $0 <filename>\n";
}

my $sourcefile = $ARGV[0];

# Open the file for reading
open(my $fh, '<', $sourcefile) or die "Could not open source file '$sourcefile' $!";

# Read through the file line by line
while(my $line = <$fh>)  {   
	chomp;
    my @words = split;
    foreach(@words){
		# Replace each word with a token
		# If a integer don't change it
		# If a single quoted char, convert to hex
		# If a resevered special character, tokenize
		# Else uppercase
	}
}

# Close the file
close($fh);

