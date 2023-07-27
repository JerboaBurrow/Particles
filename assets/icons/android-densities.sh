#! /bin/bash

FILE=""
BASE=64

while [[ $# -gt 0 ]]; do
  case $1 in
    -f|--file)
      FILE=$2
      shift # past argument
      shift # past value
      ;;
    -b|--base)
      BASE=$2
      shift
      shift
      ;;
    -*|--*)
      echo "Unknown option $1"
      exit 1
      ;;
    *)
      POSITIONAL_ARGS+=("$1") # save positional arg
      shift # past argument
      ;;
  esac
done

declare -A files

files[75]=ldpi
files[100]=mdpi
files[150]=hdpi
files[200]=xhdpi
files[300]=xxhdpi
files[400]=xxxhdpi

mkdir ${FILE}
for i in {75,100,150,200,300,400}
do
	inkscape -w $(($i*BASE/100)) -h $(($i*BASE/100)) -e ${FILE}/${FILE}-${files[$i]}.png ${FILE}.svg
done
