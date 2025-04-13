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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.JSchException;

@SpringBootApplication
@EnableConfigurationProperties({ LiquibaseProperties.class, ApplicationProperties.class })
public class MsMediaApp {

    private static final Logger LOG = LoggerFactory.getLogger(MsMediaApp.class);
    private static Session sshSession;

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
     * Setup SSH remote port forwarding using JSch.
     * This creates a tunnel from a remote server port to a local port.
     * 
     * @param remoteHost The remote host to connect to
     * @param remotePort The remote port to forward from
     * @param localPort  The local port to forward to
     * @param user       The SSH user for authentication
     * @param password
     */
    private static void setupSshPortForwarding(String remoteHost, int remotePort, int localPort, String user,
            String password) {
        try {
            JSch jsch = new JSch();
            sshSession = jsch.getSession(user, remoteHost, 22);

            // Configure SSH session
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(config);

            // Set up password authentication
            sshSession.setPassword(password); // TODO: Move to configuration

            LOG.info("Establishing SSH connection to {}...", remoteHost);
            sshSession.connect();

            // Set up remote port forwarding
            sshSession.setPortForwardingR(remotePort, "localhost", localPort);
            LOG.info("SSH port forwarding established: remote port {} -> local port {}", remotePort, localPort);

        } catch (JSchException e) {
            LOG.error("Error setting up SSH port forwarding", e);
            if (sshSession != null && sshSession.isConnected()) {
                sshSession.disconnect();
            }
        }
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

        // Now start the application with all the configuration from YML
        Environment env = app.run(args).getEnvironment();

        // After Spring environment is loaded, get the configuration values
        String remoteHost = env.getProperty("ssh.remote.host");
        int remotePort = Integer.parseInt(env.getProperty("ssh.remote.port"));
        int localPort = Integer.parseInt(env.getProperty("server.port"));
        String user = env.getProperty("ssh.user");
        boolean enableSshForwarding = Boolean.parseBoolean(env.getProperty("ssh.forwarding.enabled"));
        String password = env.getProperty("ssh.password");
        // Ensure consul discovery port matches SSH remote port
        System.setProperty("spring.cloud.consul.discovery.port", String.valueOf(remotePort));

        // Set up SSH tunnel after application startup if enabled
        if (enableSshForwarding) {
            LOG.info("Establishing SSH tunnel after application startup...");
            setupSshPortForwarding(remoteHost, remotePort, localPort, user, password);
        }

        logApplicationStartup(env);
    }

    // Add shutdown hook to clean up SSH session
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (sshSession != null && sshSession.isConnected()) {
                sshSession.disconnect();
                LOG.info("SSH session closed");
            }
        }));
    }
}
