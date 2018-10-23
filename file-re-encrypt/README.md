# File Re Encrypt
This program takes an EGA identifier for dataset or files. Then accesses the files through FIRE (Either Direct or fuse mount) decrypts the file and generates a new AES encrypted file with a random new password that is stored on a database.
The process checks the md5 consistency of the process matching the unencrypted md5 with the one presented in the system. 
## Usage
```bash
java -jar file-re-encrypt.jar --egaId=EGAF00000414876 --spring.config.location="file-re-encrypt.properties"
```
This will run the re-encryption process with the file ```EGAF00000414876``` using the configuration from ```file-re-encrypt.properties``` aditionally we can also store the log of the execution with the parameter ```--logging.file=test.log```
## Configuration parameters
The configuration file holds the database connection data, also the following parameters can be configured:
- file-re-encrypt.config.staging-path (path in staging area where files will be stored)
- file-re-encrypt.config.relative-path (relative path from staging area)
- file-re-encrypt.config.gpg-key-path (path to the gpg key to decrypt)
- file-re-encrypt.config.random-key-size (size in bytes for the new random password key)
- file-re-encrypt.config.override (true if we want to override a file with the same name in the staging path otherwise file will be skip)
- file-re-encrypt.config.insert-profiler (true if we want to insert file into long term archive)
- file-re-encrypt.fire.mount-path (path to fire fuse layer)
- file-re-encrypt.fire.url (url to fire metadata server)
- file-re-encrypt.fire.user (user)
- file-re-encrypt.fire.key (password)
- file-re-encrypt.fire.use-direct (true to use fire direct, otherwise we will use fuse mount path)
