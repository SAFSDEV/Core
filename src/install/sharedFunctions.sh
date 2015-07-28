#sharedFunctions.sh

#This script contains some functions can be used in other script.
#To include it to other script,just add . ./sharedVariables.sh in
#other script. See UninstallSAFS.sh,Uninstall-IOS-SAFS.sh,
#Setup-IOS-SAFS.sh and SetupSAFS.sh.

#27 AUG, 2010  LeiWang
#17 AUG, 2011  DharmeshPatel Added Mac/iOS support.

#This function will output the jdk string to stdout
#jdk string will be something like 1.4.2 or 1.5.11 etc.
getJavaVersion()
{
  #As sometimes java -version will print message to stderr, so use
  #2>&1 to redirect stderr to stdout
  local version=`java -version 2>&1 | head -1 | sed 's/[a-zA-Z "]*//g'`
  echo $version
}

getJavaMajorVersion()
{
  echo $(getJavaVersion) | awk -F. '{print $1}'
}

getJavaMinorVersion()
{
  echo $(getJavaVersion) | awk -F. '{print $2}'
}

#Need to be finished, we can use it to detect the used port by other STAF instance
getSSLPort()
{
  local usedPorts=`netstat -a | grep 65 | awk '{print $7}'`
  for port in $usedPorts
  do
    echo $port
  done
}

#=== params ====
#$1 string
#$2 from
#$3 to
#===============
#return: substring got from string between index from and index to
#==============
getSubString(){
  if ( test $# -ne 3 )
  then
    echo "Usage: getSubString aString from to"
    return 1
  else
    expr substr $1 $2 $3
    return 0
  fi
}

#=== params ====
#$1 string
#===============
#return: the length of string
#==============
getStringLength(){
  if ( test $# -ne 1 ); then
    echo "Usage: getStringLength aString"
    return 1
  else
    #expr length $1
     echo ${#1}
    return 0
  fi
}

#=== params ====
#$1 string
#===============
#return: the last character of string
#==============
getLastCharFromString(){
  if ( test $# -ne 1 ); then
    echo "Usage: getLastCharFromString aString"
    return 1
  else
    local length=$(getStringLength $1)
    echo $1 | cut -c $length
    return 0
  fi
}

#=== params ====
#$1 string
#===============
#return: If the string has a "/" at the last position, remove it.
#        But if string contains ONLY one "/", we should not remove it.
#==============
removeLastPathSepCharFromString(){
  if ( test $# -ne 1 ); then
    echo "Usage: removeLastPathSepCharFromString aString"
    return 1
  else
    local length=$(getStringLength $1)
    local index=`expr $length - 1`
    local lastchar=`expr substr $1 $length $length`
    if ( test $lastchar = "/" ) && ( test ! $length = 1 ); then
      expr substr $1 1 $index
    else
      echo $1
    fi
    return 0
  fi
}


#=== params ====
#$1 file1
#$2 file2
#===============
#porpose: Compare the last modified date of two files
#return:
#        "newer" if file1 is newer than file2
#        "same"  if file1 is the same old than file2
#        "older" if file2 is older than file2
#==============
compareFileLastModify(){
  if ( test $# -ne 2 ); then
    echo "Usage: compareFileLastModify file1 file2"
    return 1
  else
    if ( test -e $1 ) && ( test -e $2 ) ; then
      local file1LastModify=`stat -c%Y $1`
      local file2LastModify=`stat -c%Y $2`
      #echo "file1LastModify: $file1LastModify ; file2LastModify: $file2LastModify"
      if ( test $file1LastModify -gt $file2LastModify ); then
        echo "newer"
      elif ( test $file1LastModify -eq $file2LastModify ); then
        echo "same"
      else
        echo "older"
      fi
    else
      #one of file doesn't exist or none of them exist
      return 1
    fi
  fi
}
