#!\\ronco\public\dnt\tools\Perl5.6\bin\perl

#####################################################
# Perl.exe should be in the System PATH.
# Ex: PATH=...;\\ronco\public\dnt\tools\Perl5.6\bin;
#
# System Environment Variable PERLLIB should minimally contain:
# PERLLIB=C:\STAF\bin;C:\STAF\bin\perl56
#
# Execute the Perl sample from the C:\SAFS\samples\perl directory.
#
# perl perlTest.pl
#
###########################################

print "Hi there!\n";

use PLSTAF;

$rc = STAF::Register("PerlClient");
if ($rc != $STAF::kOk){
    print "Error registering with STAF, RC: $STAF::RC\n";
    return $rc;
}

print "\nMy STAF Handle: $STAF::Handle\n";

$rc = STAF::Submit("local", "service", "list");

if ($rc != $STAF::kOk){
    print "Error submitting SERVICE request, RC: $STAF::RC\n";
    if (length($STAF::Result) != 0){
        print "Additional error info: $STAF::Result\n";
    }
    return $rc;
}

print "\nSTAF Service List:\n\n$STAF::Result\n";

$rc = STAF::UnRegister();
if ($rc != $STAF::kOk){
    print "Error UnRegistering with STAF, RC: $STAF::RC\n";
    return $rc;
}

