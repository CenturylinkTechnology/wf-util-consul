/**
 * 
 */
package com.ctlts.wfaas.util.consul;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.ctlts.wfaas.adapter.consul.ConsulAdapter;
import com.ctlts.wfaas.adapter.consul.VaultAdapter;

/**
 * @author mramach
 *
 */
@Component
@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private Environment env;
    
    @Autowired
    private ApplicationContext ctx;
    
    @Bean
    public ConsulAdapter consulAdapter() {
        
        ConsulAdapter adapter = new ConsulAdapter();
        
        if(env.containsProperty("consul.endpoint")) {
            adapter.setConsulEndpoint(env.getProperty("consul.endpoint"));
        }
        
        return adapter;
        
    }
    
    @Bean
    public VaultAdapter vaultAdapter() {
        
        VaultAdapter adapter = new VaultAdapter();
        
        if(env.containsProperty("vault.endpoint")) {
            adapter.setVaultEndpoint(env.getProperty("vault.endpoint"));
        }
        
        return adapter;
        
    }
    
    @Bean(name = "consul-list")
    public ConsulListCommand consulListCommand() {
        return new ConsulListCommand();
    }
    
    @Bean(name = "consul-read")
    public ConsulReadCommand consulReadCommand() {
        return new ConsulReadCommand();
    }

    @Bean(name = "consul-write")
    public ConsulWriteCommand consulWriteCommand() {
        return new ConsulWriteCommand();
    }
    
    @Bean(name = "vault-write")
    public VaultWriteCommand vaultWriteCommand() {
        return new VaultWriteCommand();
    }
    
    @Override
    public void run(String... args) throws Exception {
        ctx.getBean(args[0], Command.class).execute(args);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        new SpringApplicationBuilder(Application.class)
            .showBanner(false)
                .run(args);
        
    }
    
    static interface Command {

        void execute(String[] args);
        
    }
    
    static class ConsulListCommand implements Command {

        @Autowired
        private ConsulAdapter consulAdapter;
        
        @Override
        public void execute(String[] args) {
            
            String path = args[args.length - 1];
            
            boolean strip = Arrays.stream(args)
                    .filter(e -> "-strip".equals(e)).findFirst().isPresent();
            
            consulAdapter.getProperties(path).entrySet().stream().forEach(e -> {
                
                String value = strip ? e.getKey().replace(path, "") : e.getKey();
                
                System.out.println(value);
                
            });
            
        }
        
    }
    
    static class ConsulReadCommand implements Command {

        @Autowired
        private ConsulAdapter consulAdapter;
        
        @Override
        public void execute(String[] args) {
            
            consulAdapter.getProperties(args[1]).entrySet().stream().forEach(e -> {
                
                System.out.println(String.format("%s=%s", e.getKey(), e.getValue().replace(System.getProperty("line.separator"), 
                        String.format("\\%s", System.getProperty("line.separator")))));
                
            });
            
        }
        
    }
    
    static class ConsulWriteCommand implements Command {

        @Autowired
        private ConsulAdapter consulAdapter;
        
        @Override
        public void execute(String[] args) {

            Path cwd = Paths.get(System.getProperty("user.dir"));
            Path path = Paths.get(args[1]);

            if(!path.isAbsolute()) {
                path = cwd.resolve(path);
            }
            
            Properties properties = new Properties();
            
            try {
            
                properties.load(Files.newInputStream(path));
                
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            
            properties.entrySet().forEach(e -> {
                
                consulAdapter.setProperty(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
                
            });
            
        }
        
    }
    
    static class VaultWriteCommand implements Command {

        @Autowired
        private VaultAdapter vaultAdapter;
        
        @Override
        public void execute(String[] args) {

            String token = args[1];
            String mount = args[2];
            
            Path cwd = Paths.get(System.getProperty("user.dir"));
            Path path = Paths.get(args[3]);

            if(!path.isAbsolute()) {
                path = cwd.resolve(path);
            }
            
            Properties properties = new Properties();
            
            try {
            
                properties.load(Files.newInputStream(path));
                
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            
            properties.entrySet().forEach(e -> {
                
                vaultAdapter.setValue(token, mount + "/" + String.valueOf(e.getKey()), String.valueOf(e.getValue()));
                
            });
            
        }
        
    }

}
