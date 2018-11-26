# File Re Encrypt publisher
This program takes an EGA identifier for dataset. Then he retrieves the list of files that correspond to the dataset 
from audit and checks with the re-encryption database the status if it has been re-encrypted and if the file is 
consistent (md5-checks).

Then if any file is missing or didn't pass the consistency test, the application reports this information and stops 
the process. If all the files in a dataset have been validated properly, then we check with pro-filer if all the files
have been archived. If any of the files has not been archived, the process stops and reports the non archived files.

If all the files have been found on the long term archive we modify the file information on the database to point to 
the new re-encrypted file.
## Usage
```bash
java -jar file-re-encrypt-publisher.jar --egaId=EGAF00000414876 --spring.config.location="file-re-encrypt.properties"
```
This will run the re-encryption process with the file ```EGAF00000414876``` using the configuration from ```file-re-encrypt.properties``` aditionally we can also store the log of the execution with the parameter ```--logging.file=test.log```
## Configuration parameters
The configuration file holds the database connection data