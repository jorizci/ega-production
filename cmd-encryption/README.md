# File command encrypt

This application retrieves an archived file in Fire, decrypts and encrypts into the `output-format` defined in the 
command line

##Commands
- fire-path (fire route to the file)
- output-format (which format will be used as output `PLAIN`, `AES_CTR_256_EGA`, `AES_CBC_OPENSSL`)
- password-file (path to file with the password)
- output-path (where to store the result file)
- output-password-file (path to file with the password for the result file)

##Application configuration

```properties
## FIRE direct
cmd-re-encrypt.fire.mount-path=
cmd-re-encrypt.fire.url=
cmd-re-encrypt.fire.user=
cmd-re-encrypt.fire.key=
## Logging
logging.level.root=WARN
logging.level.org.springframework=WARN
logging.level.uk.ac.ebi.ega.cmd=DEBUG
logging.level.uk.ac.ebi.ega.encryption.core=DEBUG
logging.level.uk.ac.ebi.ega.fire=DEBUG
```
Logging levels can be maintained, but fire properties need to be set in order to access the files.

##Usage example
This command will download s_CHC1065T_sorted.bam file and decrypt it into output directory

`java -jar ./target/cmd-encryption-1.0-SNAPSHOT.jar --fire-path="EGAR00001015731/s_CHC1065T_sorted.bam.gpg" 
--output-format=PLAIN --password-file="~/pgp_password" --output-path="~/output/" 
--output-password-file="~/pgp_password"  --spring.config.location="~/cmd-encryption.properties"`