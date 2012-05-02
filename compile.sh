#!/usr/bin/env bash

version=`cat src/plugin.yml | grep version | awk '{print $2}'`
hashtag=`git log -n 1 | grep commit | awk '{ print $2 }' | cut -b 1-6`

while getopts "vVhH" flag
    do
        case $flag in
            V) echo "Building for OpenAuth, version ${version}."
               exit 1
            ;;
            H) echo "OpenAuth commit tag ${hashtag}"
               exit 1
            ;;
            h) echo "Adding git committag to archive name."
               export tagname="YES"
            ;;
            v) echo "Being verbose..."
               export verbose="YES"
            ;;
        esac
    done

echo "[OpenAuth(${version}-${hashtag})] building.]"

javac -Xlint:depreciated -Xstdout compile_log.txt -sourcepath src/ -g -cp inc/craftbukkit.jar:inc/permissions.jar:inc/bukkit.jar:inc/worldedit.jar:inc/pex.jar \
    src/me/maiome/openauth/*/*.java src/me/maiome/openauth/*/*/*.java \
    src/net/eisental/common/*/*.java

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

echo "[OpenAuth(${version}-${hashtag})] packing.]"

if [ "${tagname}" == "YES" ] ; then
    jar cvf "OpenAuth-${version}-${hashtag}.jar" -C src/ . 2>&1 1>archive_log.txt
else
    jar cvf "OpenAuth-${version}.jar" -C src/ . 2>&1 1>archive_log.txt
fi

if [ "${verbose}" == "YES" ] ; then
    echo "$(cat archive_log.txt)"
fi

rm ./*_log.txt

echo "Successfully built OpenAuth ${version}-${hashtag}!"