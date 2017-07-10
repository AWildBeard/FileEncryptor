# Welcome!

![FileEncryptor](https://github.com/AWildBeard/resources/blob/master/FileEncryptor/resources/fileEncryptor.png?raw=true)

##### [Downloads](https://github.com/AWildBeard/FileEncryptor/releases)

### The project

This project is aimed at creating a simple to use,
user friendly, cross platform, file encryption tool
for the masses. 

##### Requirements

See the section for your system.

- [Windows](#windows)
- [Linux](#linux)
- [MacOS](#osx)

#### Usage

Out of the box, the tool is ready to
be used and encrypts files with strong AES 128bit encryptions.
Simply choose your files, enter a password, then press
the encrypt button.

#### Advanced Usage

For advanced users, there is an advanced 
section with more options to customize how the
encryption of your files is done. 

By default, the application encrypts/ decrypts with
AES-CBC-PKS5Padding with a 128 bit key. The
advanced section offers a few more options, such as the
ability to encrypt with 192 bit triple des (DESede) along with a
stronger version of AES (256 bit). 


#### Contributing

Please see [CONTRIBUTING.md](https://github.com/AWildBeard/FileEncryptor/blob/develop/CONTRIBUTING.md)

### <a name="windows"></a>Windows users

Please make sure you have Java installed. If you are unsure, go
to the 
[Java Website](https://www.java.com/en/download/)
and download the .exe for your system and install it. Once you have
done that, or are positive that your system has Java
installed, go to the 
[releases](https://github.com/AWildBeard/FileEncryptor/releases)
page and download the ".jar" file then double click it to
launch it.

### <a name="linux"></a>Linux Users

- Debian based distros:

Download the .deb file from the
[releases](https://github.com/AWildBeard/FileEncryptor/releases)
page, and install the .deb file by double clicking on it.
Please be aware that if your system does not already have
the required version of Java installed, it will
install it for you, but could take 10 or so minutes through the
Ubuntu software center (because its super slow). If you want it to install faster,
install the package from the command line.

- Arch Linux:

This project is packaged for the AUR using ALL open source technologies.
To get it:

```yaourt -S file-encryptor```

or/ if you use pacaur:

```pacaur -S file-encryptor```

- Red Hat based distros (Fedora):

Unfortunately due to the fact that neither Red Hat or Fedora package
JavaFX in any shape or form (at this time), you will not be able to use this program
without installing the official Oracle Java Runtime
(which also isn't packaged by Fedora or Red Hat).
Don't fret however
because the wonderful Fedora customization program Feddy offers to
install the Oracle Java Runtime. So go look up how to install
Feddy, then use that to install Oracle Java.

Once you have acomplished alll of this, go to the
[releases](https://github.com/AWildBeard/FileEncryptor/releases) 
page and download the ".jar" file then double click it to
launch it.

### <a name="osx"></a>MacOS

Please install Java from the 
[Java Website](https://www.java.com/en/download/)
and then download the ".jar" file from the
[releases](https://github.com/AWildBeard/FileEncryptor/releases) 
page. Run the ".jar" file to launch the program.
