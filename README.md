Welcome!

This project is aimed at creating a simple to use,
user friendly, cross platform, file encryption tool
for the masses. The tool has two modes, simple mode
advanced mode. Both modes require the selection of
a file and a password to encrypt the file. Those
are the only fields that the simple mode requires.
By default, the simple mode encrypts with
AES-CBC-PKS5Padding with a 128 bit key. The
advanced mode offers a few more options, such as the
ability to encrypt triple des (DESede) along with a
stronger version of AES (256 bit key). This mode also
adds the key strength selection field and the Algo
type field.

#### Contributing

Fork as you will but match style

#### Downloads

* [Releases](https://github.com/AWildBeard/FileEncryptor/releases)

#### Usage
In order to use this program, you will need a
Java runtime installed. They can be found on the
Oracle website, but come installed on most Windows
machines.

Once you have a JRE installed simple double click the
.jar file found on the releases page above and it
should launch just fine.

##### For Linux Users
If your system will not graphically launch the .jar
file, use: 

```java -jar /path/to/fileEncryptor.jar```

to run the .jar file.

You too will need a JRE to run this program however.
For Debian based distros, there should be a 
openjdk-8-jre package in your distros repos that 
you will need to install in addition to the 
openjfx package. For example:

```sudo apt install openjdk-8-jre openjfx```

Tested on Elementary OS.

Arch Linux users I recommend the Oracle JRE as it comes
with JavaFX installed in the JRE and there are proven
speed improvements in the Oracle JRE over the OPENJDK's.
Arch users may find the Oracle JRE in the AUR

```pacman -S jre```

Be aware that if the output of

```java -version```

does not look **SIMILAR** to this

```
   java version "1.8.0_121"
   Java(TM) SE Runtime Environment (build 1.8.0_121-b13)
   Java HotSpot(TM) 64-Bit Server VM (build 25.121-b13, mixed mode)
```

you are most likely still using whatever JRE or JDK you
had installed before.