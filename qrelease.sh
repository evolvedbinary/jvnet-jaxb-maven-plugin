#!/usr/bin/env bash

echo "Performing a full clean build."
mvn clean install -Pall,sonatype-oss-release -DperformRelease
echo "Full clean build completed."

echo "Setting new version to: $1."
mvn versions:set -Pall -DnewVersion=$1
echo "Version was set to $1."
mvn versions:commit -Pall
echo "Version $1 committed."

echo "Performing a full clean build."
mvn clean install -Pall,sonatype-oss-release -DperformRelease
echo "Full clean build completed."

echo "Checking in version $1."
git commit -a -m "Version $1"
echo "Version $1 was checked in."

echo "Tagging version $1."
git tag -a $1 -m "Version $1"
echo "Version $1 was tagged."

echo "Pushing version $1."
git push origin main
git push --tags origin main
echo "Version $1 was pushed."

echo "Performing full clean deploy."
mvn -DperformRelease -Pall,sonatype-oss-release clean deploy
echo "Full clean deploy done."

echo "Setting new version to $2."
mvn versions:set -Pall -DnewVersion=$2
echo "Version was set to $2."
mvn versions:commit -Pall
echo "Version $2 was committed."

echo "Performing a full clean build."
mvn clean install -DperformRelease -Pall,sonatype-oss-release
echo "Full clean build completed."

echo "Checking in version $2."
git commit -a -m "Version $2"
echo "Version $2 was checked in."

echo "Pushing version $2."
git push origin main
git push --tags origin main
echo "Version $2 was pushed."
