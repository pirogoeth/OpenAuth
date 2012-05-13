#!/usr/bin/env bash

# ==============================================================================================================
# configurable options

name="OpenAuth"
basedir="/home/pirogoeth/OpenAuth"
plugin_datafile="src/plugin.yml"
javac_includes="inc/craftbukkit.jar:inc/permissions.jar:inc/bukkit.jar:inc/worldedit.jar:inc/pex.jar:inc/JSONAPI.jar"
javac_src="src/me/maiome/openauth/*/*.java src/me/maiome/openauth/*/*/*.java src/net/eisental/common/*/*.java"

# configuration ENDS
# ==============================================================================================================
# bash colour codes
#
# green => 0;32
# yellow => 1;33
# red => 0;31
# no colour => 0

_bc_g='\033[1;32m'
_bc_y='\033[1;33m'
_bc_r='\033[0;31m'
_bc_nc='\033[0m'

# colour codes END
#===============================================================================================================
# context variables and statement

_WD=`pwd`
cd ${basedir}

version=`cat src/plugin.yml | grep "version" | awk '{print $2}'`
hashtag=`git log -n 1 | grep commit | awk '{ print $2 }' | cut -b 1-7`

# context vars/statement ENDS
#===============================================================================================================


while getopts "vhpo:VH?" flag
    do
        case $flag in
            V) echo -e "${_bc_y}Building for ${name}, version ${version}."
               exit 1
            ;;
            H) echo -e "${_bc_y}${name} committag ${hashtag}"
               exit 1
            ;;
            h) echo -e "${_bc_y}Adding git committag to archive name."
               export tagname="YES"
            ;;
            v) echo -e "${_bc_y}Being verbose..."
               export verbose="YES"
            ;;
            o) echo -e "${_bc_y}Output is now: ${OPTARG}"
               export outdir=${OPTARG}
            ;;
            p) echo -e "${_bc_y}Writing git committag to plugin.yml."
               export pct="YES"
            ;;
            \?) echo "Usage: `basename $0` [-HVhv?] [-o outfile]"
               exit
            ;;
            *) exit
            ;;
        esac
    done

function cleanup() {
    rm -f ./{archive,compile}_log.txt
    echo -e "${_bc_y}Cleaned up logfiles!${_bc_nc}"
    cd ${_WD}
}

trap cleanup EXIT

echo -en "${_bc_y}[${name}(${version}-${hashtag})] building.]${_bc_nc}"

javac -Xlint:depreciated -Xstdout compile_log.txt -sourcepath src/ -g -cp ${javac_includes} ${javac_src}

errors=`cat "./compile_log.txt" | tail -n 1`
errors_t=`echo ${errors} | tr -d "[[:space:]]"`
end=`tail -n -1 ./compile_log.txt | cut -b 1-5`

if ! [ "${end}" == "Note:" ] || (test -z "${errors}" && ! test -z "${errors_t}") ; then
    echo -e "           [ ${_bc_r} FAIL ${_bc_nc} ]"
    echo -e "${_bc_y}$(cat compile_log.txt)"
    exit 1
else
    echo -e "           [ ${_bc_g} OK ${_bc_nc} ]"
fi

if [ "${verbose}" == "YES" ] ; then
    echo -e "${_bc_y}$(cat compile_log.txt)"
fi

echo -en "${_bc_y}[${name}(${version}-${hashtag})] packing.]${_bc_nc}"

if [ "${tagname}" == "YES" ] ; then
    OUTFILENAME="${name}-${version}-${hashtag}.jar"
else
    OUTFILENAME="${name}-${version}.jar"
fi

if [ "${pct}" == "YES" ] ; then
    pd=`cat ${plugin_datafile}`
    _pd=`echo -en "${pd}"; echo -en "\nhashtag: ${hashtag}"`
    echo -n "${_pd}" >${plugin_datafile}
fi

jar cvf ${OUTFILENAME} -C src/ . 2>&1 1>archive_log.txt

echo -e "            [ ${_bc_g} OK ${_bc_nc} ]"

if [ "${pct}" == "YES" ] ; then
    echo -n "${pd}" >${plugin_datafile}
fi

if [ "${verbose}" == "YES" ] ; then
    echo "$(cat archive_log.txt)"
fi

if [ ! -z $outdir ] ; then
    mv ${OUTFILENAME} ${outdir}
elif [ `pwd` != ${_WD} ] && [ -z $outdir ] ; then
    mv ${OUTFILENAME} ${_WD}
fi

echo -e "${_bc_g}Successfully built ${name} ${version}-${hashtag}!${_bc_nc}"