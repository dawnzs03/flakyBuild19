/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.packaging.test;

import org.elasticsearch.packaging.util.Archives;
import org.elasticsearch.packaging.util.Distribution;
import org.elasticsearch.packaging.util.FileUtils;
import org.elasticsearch.packaging.util.ServerUtils;
import org.elasticsearch.packaging.util.Shell;
import org.junit.BeforeClass;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.elasticsearch.packaging.util.Archives.installArchive;
import static org.elasticsearch.packaging.util.Archives.verifyArchiveInstallation;
import static org.elasticsearch.packaging.util.FileExistenceMatchers.fileExists;
import static org.elasticsearch.packaging.util.FileUtils.getCurrentVersion;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.not;
import static org.junit.Assume.assumeTrue;

public class ArchiveGenerateInitialCredentialsTests extends PackagingTestCase {

    private static final Pattern PASSWORD_REGEX = Pattern.compile(
        "Password for the elastic user \\(reset with `bin/elasticsearch-reset-password -u elastic`\\): (.+)$",
        Pattern.MULTILINE
    );
    private static final Pattern TOKEN_REGEX = Pattern.compile(
        "Copy the following enrollment token and paste it into Kibana in your browser \\(valid for the next 30 minutes\\):\n(.+)$",
        Pattern.MULTILINE
    );
    private static final Pattern FINGERPRINT_REGEX = Pattern.compile("HTTP CA certificate SHA-256 fingerprint:\n(.+)$", Pattern.MULTILINE);

    private static final String OUTPUT_MATCH = "Configure other nodes to join this cluster*";

    @BeforeClass
    public static void filterDistros() {
        assumeTrue("archives only", distribution.isArchive());
    }

    public void test10Install() throws Exception {
        installation = installArchive(sh, distribution());
        verifyArchiveInstallation(installation, distribution());
    }

    public void test20NoAutoGenerationWhenAutoConfigurationDisabled() throws Exception {
        /* Windows issue awaits fix: https://github.com/elastic/elasticsearch/issues/49340 */
        assumeTrue("expect command isn't on Windows", distribution.platform != Distribution.Platform.WINDOWS);
        ServerUtils.disableSecurityAutoConfiguration(installation);
        Shell.Result result = awaitElasticsearchStartupWithResult(runElasticsearchStartCommand(null, false, true));
        assertThat(parseElasticPassword(result.stdout()), nullValue());
        assertThat(parseKibanaToken(result.stdout()), nullValue());
        assertThat(parseFingerprint(result.stdout()), nullValue());
        stopElasticsearch();
        FileUtils.rm(installation.data);
    }

    public void test30NoAutogenerationWhenDaemonized() throws Exception {
        /* Windows issue awaits fix: https://github.com/elastic/elasticsearch/issues/49340 */
        assumeTrue("expect command isn't on Windows", distribution.platform != Distribution.Platform.WINDOWS);
        ServerUtils.enableSecurityAutoConfiguration(installation);
        awaitElasticsearchStartup(runElasticsearchStartCommand(null, true, true));
        assertThat(installation.logs.resolve("elasticsearch.log"), fileExists());
        String logfile = FileUtils.slurp(installation.logs.resolve("elasticsearch.log"));
        assertThat(logfile, not(containsString("Password for the elastic user is")));
        assertThat(logfile, not(containsString("Enrollment token for kibana (valid for the next 30 minutes) :")));
        stopElasticsearch();
    }

    @AwaitsFix(bugUrl = "https://github.com/elastic/elasticsearch/issues/84407")
    public void test40VerifyAutogeneratedCredentials() throws Exception {
        /* Windows issue awaits fix: https://github.com/elastic/elasticsearch/issues/49340 */
        assumeTrue("expect command isn't on Windows", distribution.platform != Distribution.Platform.WINDOWS);
        // We use a different dir so that we don't have to clean up existing configuration and data directories
        // For this and the following tests `installation` points to the new installation
        installation = installArchive(sh, distribution(), getRootTempDir().resolve("elasticsearch-node1"), getCurrentVersion(), true);
        verifyArchiveInstallation(installation, distribution());
        Shell.Result result = awaitElasticsearchStartupWithResult(
            Archives.startElasticsearchWithTty(installation, sh, null, List.of(), OUTPUT_MATCH, false)
        );
        assertThat(parseElasticPassword(result.stdout()), notNullValue());
        assertThat(parseKibanaToken(result.stdout()), notNullValue());
        assertThat(parseFingerprint(result.stdout()), notNullValue());
        String response = makeRequestAsElastic("https://localhost:9200", parseElasticPassword(result.stdout()));
        assertThat(response, containsString("You Know, for Search"));
        stopElasticsearch();
    }

    @AwaitsFix(bugUrl = "https://github.com/elastic/elasticsearch/issues/84407")
    public void test50CredentialAutogenerationOnlyOnce() throws Exception {
        /* Windows issue awaits fix: https://github.com/elastic/elasticsearch/issues/49340 */
        assumeTrue("expect command isn't on Windows", distribution.platform != Distribution.Platform.WINDOWS);
        stopElasticsearch();
        Shell.Result result = awaitElasticsearchStartupWithResult(runElasticsearchStartCommand(null, false, true));
        assertThat(parseElasticPassword(result.stdout()), nullValue());
        assertThat(parseKibanaToken(result.stdout()), nullValue());
        assertThat(parseFingerprint(result.stdout()), nullValue());
    }

    private String parseElasticPassword(String output) {
        Matcher matcher = PASSWORD_REGEX.matcher(output);
        assertNotNull(matcher);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    private String parseKibanaToken(String output) {
        Matcher matcher = TOKEN_REGEX.matcher(output);
        assertNotNull(matcher);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    private String parseFingerprint(String output) {
        Matcher matcher = FINGERPRINT_REGEX.matcher(output);
        assertNotNull(matcher);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
}
