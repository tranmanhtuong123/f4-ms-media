package com.mycompany.myapp;

import com.mycompany.myapp.config.ApplicationProperties;
import com.mycompany.myapp.config.CRLFLogConverter;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import tech.jhipster.config.DefaultProfileUtil;
import tech.jhipster.config.JHipsterConstants;

@SpringBootApplication
@EnableConfigurationProperties({ LiquibaseProperties.class, ApplicationProperties.class })
public class MsMediaApp {

    private static final Logger LOG = LoggerFactory.getLogger(MsMediaApp.class);

    private final Environment env;

    public MsMediaApp(Environment env) {
        super();
        this.env = env;
    }

    /**
     * Initializes msMedia.
     * <p>
     * Spring profiles can be configured with a program argument
     * --spring.profiles.active=your-active-profile
     * <p>
     * You can find more information on how profiles work with JHipster on <a href=
     * "https://www.jhipster.tech/profiles/">https://www.jhipster.tech/profiles/</a>.
     */
    @PostConstruct
    public void initApplication() {
        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) &&
                activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_PRODUCTION)) {
            LOG.error(
                    "You have misconfigured your application! It should not run "
                            + "with both the 'dev' and 'prod' profiles at the same time.");
        }
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) &&
                activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_CLOUD)) {
            LOG.error(
                    "You have misconfigured your application! It should not "
                            + "run with both the 'dev' and 'cloud' profiles at the same time.");
        }
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(key -> "https")
                .orElse("http");
        String applicationName = env.getProperty("spring.application.name");
        String serverPort = env.getProperty("server.port");
        String contextPath = Optional.ofNullable(env.getProperty("server.servlet.context-path"))
                .filter(StringUtils::isNotBlank)
                .orElse("/");
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            LOG.warn("The host name could not be determined, using `localhost` as fallback");
        }
        LOG.info(
                CRLFLogConverter.CRLF_SAFE_MARKER,
                """

                        ----------------------------------------------------------
                        \tApplication '{}' is running! Access URLs:
                        \tLocal: \t\t{}://localhost:{}{}
                        \tExternal: \t{}://{}:{}{}
                        \tProfile(s): \t{}
                        ----------------------------------------------------------""",
                applicationName,
                protocol,
                serverPort,
                contextPath,
                protocol,
                hostAddress,
                serverPort,
                contextPath,
                env.getActiveProfiles().length == 0 ? env.getDefaultProfiles() : env.getActiveProfiles());

        String configServerStatus = env.getProperty("configserver.status");
        if (configServerStatus == null) {
            configServerStatus = "Not found or not setup for this application";
        }
        LOG.info(
                CRLFLogConverter.CRLF_SAFE_MARKER,
                "\n----------------------------------------------------------\n\t" +
                        "Config Server: \t{}\n----------------------------------------------------------",
                configServerStatus);
    }

    /**
     * Main method, used to run the application.
     *
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        // Initial Spring setup
        SpringApplication app = new SpringApplication(MsMediaApp.class);
        DefaultProfileUtil.addDefaultProfile(app);
        // Load additional configuration for dev environment
        app.setAdditionalProfiles("dev");
        System.setProperty("spring.config.additional-location", "classpath:/config/consul-config-dev.yml");
        String remoteHost = System.getProperty("ssh.remote.host");
        String remotePortStr = System.getProperty("ssh.remote.port");
        int remotePort = remotePortStr != null ? Integer.parseInt(remotePortStr) : 8803;
        System.setProperty("SSH_REMOTE_PORT", String.valueOf(remotePort));
        String localPortStr = System.getProperty("ssh.local.port");
        int localPort = localPortStr != null ? Integer.parseInt(localPortStr) : 8080;
        String user = System.getProperty("ssh.user");
        String enableSshForwardingStr = System.getProperty("ssh.forwarding.enabled");
        if (enableSshForwardingStr == null) {
            enableSshForwardingStr = System.getenv().get("SSH_FORWARDING_ENABLED");
        }
        boolean enableSshForwarding = enableSshForwardingStr != null ? 
            Boolean.parseBoolean(enableSshForwardingStr) : true;
        // Set up SSH tunnel before starting the application if enabled
        if (enableSshForwarding) {
            LOG.info("Establishing SSH tunnel before application startup...");
            setupSshPortForwarding(remoteHost, remotePort, localPort, user);
            // Give the SSH connection some time to establish
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // Now start the application
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    /**
     * Setup SSH remote port forwarding.
     * This creates a tunnel from a remote server port to a local port.
     * 
     * @param remoteHost The remote host to connect to
     * @param remotePort The remote port to forward from
     * @param localPort  The local port to forward to
     * @param user       The SSH user for authentication
     */
    private static void setupSshPortForwarding(String remoteHost, int remotePort, int localPort, String user) {
        try {
            String sshCommand = String.format("ssh -f -N -R %d:localhost:%d %s@%s",
                    remotePort, localPort, user, remoteHost);
            LOG.info("Starting SSH port forwarding: {}", sshCommand);

            Process process = Runtime.getRuntime().exec(sshCommand);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                LOG.info("SSH port forwarding established successfully");
            } else {
                LOG.error("SSH port forwarding failed with exit code: {}", exitCode);
            }
        } catch (IOException | InterruptedException e) {
            LOG.error("Error setting up SSH port forwarding", e);
            Thread.currentThread().interrupt();
        }
    }

    // /**
    // * Main method, used to run the application.
    // *
    // * @param args the command line arguments.
    // */
    // public static void main(String[] args) {
    // SpringApplication app = new SpringApplication(MsMediaApp.class);
    // DefaultProfileUtil.addDefaultProfile(app);
    // Environment env = app.run(args).getEnvironment();
    // logApplicationStartup(env);
    // }
}
