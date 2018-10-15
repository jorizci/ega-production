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
package uk.ac.ebi.ega.file.re.encryption.tracing;

import org.apache.commons.logging.Log;
import org.springframework.lang.Nullable;

public class CustomPerformanceMonitorInterceptor extends org.springframework.aop.interceptor.PerformanceMonitorInterceptor {

    public CustomPerformanceMonitorInterceptor(boolean useDynamicLogger) {
        super(useDynamicLogger);
    }

    @Override
    protected void writeToLog(Log logger, String message, @Nullable Throwable ex) {
        if (ex != null) {
            super.writeToLog(logger, message, ex);
        } else {
            logger.info(message);
        }
    }

    @Override
    protected boolean isLogEnabled(Log logger) {
        return logger.isInfoEnabled();
    }
}
