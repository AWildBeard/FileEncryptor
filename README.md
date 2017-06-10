## Welcome!

#### [Downloads](https://github.com/AWildBeard/FileEncryptor/releases)

![FileEncryptor](https://github.com/AWildBeard/resources/blob/master/FileEncryptor/resources/fileEncryptor.png?raw=true)

#### The project

This project is aimed at creating a simple to use,
user friendly, cross platform, file encryption tool
for the masses. 

#### Usage
Out of the box, the tool is ready to
be used and encrypts files with strong AES 128bit.
Simply choose your files, enter a password, then press
the encrypt button.
For advanced users, there is and advanced 
section with more options to customize how the
encryption of your files is done. 
By default, the application encrypts/ decrypts with
AES-CBC-PKS5Padding with a 128 bit key. The
advanced section offers a few more options, such as the
ability to encrypt triple des (DESede) along with a
stronger version of AES (256 bit key). 

In order to use this program, you will need a
Java runtime installed. They can be found on the
[Java Website](https://www.java.com/en/download/).

Once you have Java installed simply double click the
standalone .jar file found on the 
[releases](https://github.com/AWildBeard/FileEncryptor/releases) 
page above and it will launch just fine.

#### Contributing

Fork as you will but match style

#### For Linux Users

Debian based Linux distros:
If you are on debian, there is a packaged deb for you
on the [releases](https://github.com/AWildBeard/FileEncryptor/releases)
page.

Other Linux distros:
You will need to download the standalone .jar file 
from the [releases](https://github.com/AWildBeard/FileEncryptor/releases)
page and use that to launch the program.
If your system will not graphically
launch the .jar file, use from the command line: 

```java -jar /path/to/fileEncryptor.jar```

to run the .jar file.

You too will need Java installed to run this program 
however. For most distros, there should be a 
openjdk-8-jre package and openjfx package
in your distros repos that 
you will need to install. For example:

```sudo apt install openjdk-8-jre openjfx```

for a debian system.

Arch Linux users I recommend the Oracle JRE as it comes
with JavaFX installed in the JRE and there are proven
speed improvements in the Oracle JRE over the OPENJDK's.
Arch users may find the Oracle JRE in the AUR

```pacman -S jre```

#### Linux continued

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
