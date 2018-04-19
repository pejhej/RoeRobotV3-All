#!/bin/bash

echo "Setting up Java..."
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get -y install oracle-java8-installer

JAVA_FOLDER=java-8-oracle
JAVA_PATH=/usr/lib/jvm/${JAVA_FOLDER}
export JAVA_HOME=${JAVA_PATH}
export PATH=$PATH:${JAVA_PATH}/bin
update-alternatives --config javac
update-alternatives --config java
echo "JAVA_HOME=${JAVA_PATH}" >> ~/.profile
echo "export PATH=$PATH" >> ~/.profile
echo "DONE with Java setup"

echo "Installing missing packages"
sudo apt-get remove netbeans
sudo apt-get install ant git cmake ffmpeg libavcodec-dev libavformat-dev libavresample-dev libavutil-dev libswscale-dev libtiff5-dev python-numpy libpng16-16 libpng16-dev
echo "DONE installing packages"

echo "Copying OpenCV library files..."
sudo cp *.jar ${JAVA_PATH}/lib
sudo cp *.so ${JAVA_PATH}/lib/arm
echo "DONE Copying OpenCV library files"

echo "Setting up Netbeans and examples..."
cd ..
unzip netbeans-8.1-201510222201-javase.zip
ln -s /home/odroid/netbeans/bin/netbeans /home/odroid/Desktop/netbeans
unzip odroid-examples.zip
echo "DONE"
