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
    
    @Bean(name = "list")
    public ListCommand listCommand() {
        return new ListCommand();
    }
    
    @Bean(name = "read")
    public ReadCommand readCommand() {
        return new ReadCommand();
    }

    @Bean(name = "write")
    public WriteCommand writeCommand() {
        return new WriteCommand();
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
    
    static class ListCommand implements Command {

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
    
    static class ReadCommand implements Command {

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
    
    static class WriteCommand implements Command {

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

}
