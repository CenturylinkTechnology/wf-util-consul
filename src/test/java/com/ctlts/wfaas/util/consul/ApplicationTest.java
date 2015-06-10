/**
 * 
 */
package com.ctlts.wfaas.util.consul;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import com.ctlts.wfaas.adapter.consul.ConsulAdapter;
import com.ctlts.wfaas.adapter.consul.VaultAdapter;
import com.ctlts.wfaas.util.consul.Application.Command;
import com.ctlts.wfaas.util.consul.Application.ConsulListCommand;
import com.ctlts.wfaas.util.consul.Application.ConsulReadCommand;
import com.ctlts.wfaas.util.consul.Application.ConsulWriteCommand;
import com.ctlts.wfaas.util.consul.Application.VaultWriteCommand;

/**
 * @author mramach
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationTest {

    @Mock
    private ConsulAdapter consulAdapter;
    @Mock
    private VaultAdapter vaultAdapter;
    @Mock
    private ApplicationContext ctx;
    @InjectMocks
	private Application app;
    @InjectMocks
    private ConsulListCommand consulListCommand;
    @InjectMocks
    private ConsulReadCommand consulReadCommand;
    @InjectMocks
    private ConsulWriteCommand consulWriteCommand;
    @InjectMocks
    private VaultWriteCommand vaultWriteCommand;

    @Test
    public void testList() throws Exception {
        
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf);
        System.setOut(out);
        
        when(ctx.getBean(eq("consul-list"), eq(Command.class))).thenReturn(consulListCommand);
        when(consulAdapter.getProperties(eq("root"))).thenReturn(Collections.singletonMap("property", "value"));
        
        app.run("consul-list", "root");

        assertEquals("Checking that the output buffer contains the expected output.", 
                String.format("property%s", System.getProperty("line.separator")), buf.toString());
        
    }
    
    @Test
    public void testList_Strip() throws Exception {
        
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf);
        System.setOut(out);
        
        when(ctx.getBean(eq("consul-list"), eq(Command.class))).thenReturn(consulListCommand);
        when(consulAdapter.getProperties(eq("root/"))).thenReturn(Collections.singletonMap("root/property", "value"));
        
        app.run("consul-list", "-strip", "root/");

        assertEquals("Checking that the output buffer contains the expected output.", 
                String.format("property%s", System.getProperty("line.separator")), buf.toString());
        
    }
    
    @Test
    public void testRead() throws Exception {
        
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf);
        System.setOut(out);
        
        when(ctx.getBean(eq("consul-read"), eq(Command.class))).thenReturn(consulReadCommand);
        when(consulAdapter.getProperties(eq("root"))).thenReturn(Collections.singletonMap("property", "value"));
        
        app.run("consul-read", "root");

        assertEquals("Checking that the output buffer contains the expected output.", 
                String.format("property=value%s", System.getProperty("line.separator")), buf.toString());
        
    }
    
    @Test
    public void testWrite() throws Exception {
        
        when(ctx.getBean(eq("consul-write"), eq(Command.class))).thenReturn(consulWriteCommand);
        
        app.run("consul-write", "src/test/resources/source.properties");

        verify(consulAdapter, atLeastOnce()).setProperty(eq("property"), eq("value"));
        
    }
    
    @Test
    public void testVaultWrite() throws Exception {
        
        when(ctx.getBean(eq("vault-write"), eq(Command.class))).thenReturn(vaultWriteCommand);
        
        app.run("vault-write", "token", "secret", "src/test/resources/source.vault.properties");

        verify(vaultAdapter, atLeastOnce()).setValue(eq("token"), eq("secret/property"), eq("value"));
        
    }
	
}
