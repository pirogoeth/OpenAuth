#!/usr/bin/env bash

# ==============================================================================================================
# configurable options

name="OpenAuth"
basedir="/home/pirogoeth/OpenAuth"
javac_includes="inc/craftbukkit.jar:inc/permissions.jar:inc/bukkit.jar:inc/worldedit.jar:inc/pex.jar:inc/JSONAPI.jar"
javac_src="src/me/maiome/openauth/*/*.java src/me/maiome/openauth/*/*/*.java src/net/eisental/common/*/*.java"

# configuration ENDS
# ==============================================================================================================


_WD=`pwd`
cd ${basedir}

version=`cat src/plugin.yml | grep version | awk '{print $2}'`
hashtag=`git log -n 1 | grep commit | awk '{ print $2 }' | cut -b 1-7`

while getopts "vVhHo:?" flag
    do
        case $flag in
            V) echo "Building for ${name}, version ${version}."
               exit 1
            ;;
            H) echo "${name} commit tag ${hashtag}"
               exit 1
            ;;
            h) echo "Adding git committag to archive name."
               export tagname="YES"
            ;;
            v) echo "Being verbose..."
               export verbose="YES"
            ;;
            o) echo "Output directory is now: ${OPTARG}"
               export outdir=${OPTARG}
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
    echo "Cleaned up logfiles!"
    cd ${_WD}
}

trap cleanup EXIT

echo "[${name}(${version}-${hashtag})] building.]"

javac -Xlint:depreciated -Xstdout compile_log.txt -sourcepath src/ -g -cp ${javac_includes} ${javac_src}

errors=`cat "./compile_log.txt" | tail -n 1`
errors_t=`echo ${errors} | tr -d "[[:space:]]"`
end=`tail -n -1 ./compile_log.txt | cut -b 1-5`

if ! [ "${end}" == "Note:" ] || (test -z "${errors}" && ! test -z "${errors_t}") ; then
    echo "$(cat compile_log.txt)"
    exit 1
fi

if [ "${verbose}" == "YES" ] ; then
    echo "$(cat compile_log.txt)"
fi

echo "[${name}(${version}-${hashtag})] packing.]"

if [ "${tagname}" == "YES" ] ; then
    OUTFILENAME="${name}-${version}-${hashtag}.jar"
else
    OUTFILENAME="${name}-${version}.jar"
fi

jar cvf ${OUTFILENAME} -C src/ . 2>&1 1>archive_log.txt

if [ "${verbose}" == "YES" ] ; then
    echo "$(cat archive_log.txt)"
fi

if [ ! -z $outdir ] ; then
    mv ${OUTFILENAME} ${outdir}
elif [ `pwd` != ${_WD} ] && [ -z $outdir ] ; then
    mv ${OUTFILENAME} ${_WD}
fi

echo "Successfully built ${name} ${version}-${hashtag}!"