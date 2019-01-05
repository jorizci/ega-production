/*
 *
 * Copyright 2018 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ega.cmd.encryption.options;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class CmdEncryptOptions {

    private static final Logger logger = LoggerFactory.getLogger(CmdEncryptOptions.class);

    private static final String FIRE_PATH = "fire-path";
    private static final String OUTPUT_PATH = "output-path";
    private static final String USE_FIRE_MOUNT = "use-fire-mount";
    private static final String OUTPUT_FORMAT = "output-format";
    private static final String RETRIES = "retries";
    private static final String PASSWORD_FILE = "password-file";
    private static final String OUTPUT_PASSWORD_FILE = "output-password-file";

    private String fireFilePath;

    private String outputPath;

    private boolean useFireMount;

    private OutputFormat outputFormat;

    private int retries;

    private String passwordFile;

    private String outputPasswordFile;

    public static Optional<CmdEncryptOptions> parse(String... parameters) throws IOException {
        OptionParser parser = new OptionParser();

        final ArgumentAcceptingOptionSpec<String> fireIdOptionSpec = parser
                .accepts(FIRE_PATH, "Fire path of the file").withRequiredArg().required()
                .ofType(String.class);
        final ArgumentAcceptingOptionSpec<String> outputPathOptionSpec = parser
                .accepts(OUTPUT_PATH, "Output path of the encrypted file").withRequiredArg().required()
                .ofType(String.class);
        final ArgumentAcceptingOptionSpec<OutputFormat> outputFormatOptionSpec = parser
                .accepts(OUTPUT_FORMAT, "Output file encryption format").withRequiredArg().required()
                .withValuesConvertedBy(new OutputFormatConverter())
                .describedAs(Arrays.toString(OutputFormat.values()))
                .ofType(OutputFormat.class);
        final OptionSpecBuilder useFireMountPathOptionSpec = parser
                .accepts(USE_FIRE_MOUNT, "Use Fire fs mount instead of Fire Direct");
        final ArgumentAcceptingOptionSpec<Integer> retriesOptionSpec = parser
                .accepts(RETRIES, "Number of retries in case of error").withRequiredArg()
                .ofType(Integer.class).defaultsTo(0);
        final ArgumentAcceptingOptionSpec<String> passwordFileOptionSpec = parser
                .accepts(PASSWORD_FILE, "Path to file with password to decrypt files")
                .withRequiredArg().required().ofType(String.class);
        final ArgumentAcceptingOptionSpec<String> outputPasswordFileOptionSpec = parser
                .accepts(OUTPUT_PASSWORD_FILE, "Path to file with password to encrypt files")
                .withRequiredArg().required().ofType(String.class);

        parser.allowsUnrecognizedOptions();

        try {
            final OptionSet optionSet = parser.parse(parameters);
            return Optional.of(new CmdEncryptOptions(
                    optionSet.valueOf(fireIdOptionSpec),
                    optionSet.valueOf(outputPathOptionSpec),
                    optionSet.valueOf(outputFormatOptionSpec),
                    optionSet.hasArgument(useFireMountPathOptionSpec),
                    optionSet.valueOf(retriesOptionSpec),
                    optionSet.valueOf(passwordFileOptionSpec),
                    optionSet.valueOf(outputPasswordFileOptionSpec)
            ));
        } catch (OptionException e) {
            parser.printHelpOn(System.out);
            return Optional.empty();
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    public CmdEncryptOptions(String fireFilePath, String outputPath, OutputFormat outputFormat, boolean useFireMount,
                             int retries, String passwordFile, String outputPasswordFile) {
        this.fireFilePath = fireFilePath;
        this.outputPath = outputPath;
        this.outputFormat = outputFormat;
        this.useFireMount = useFireMount;
        this.retries = retries;
        this.passwordFile = passwordFile;
        this.outputPasswordFile = passwordFile;
    }

    public String getFireFilePath() {
        return fireFilePath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public boolean isUseFireMount() {
        return useFireMount;
    }

    public OutputFormat getOutputFormat() {
        return outputFormat;
    }

    public int getRetries() {
        return retries;
    }

    public String getPasswordFile() {
        return passwordFile;
    }

    public String getOutputPasswordFile() {
        return outputPasswordFile;
    }

}
