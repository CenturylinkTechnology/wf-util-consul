# wf-consul-util

## Consul Bulk List

List all keys from Consul for a specified root directory.

	java -jar wfaas-util-consul-{version}.jar consul-list root/
	
### Strip Root Directory

	java -jar wfaas-util-consul-{version}.jar consul-list -strip root/

## Consul Bulk Read

Reads all key/value pairs from Consul for a specified root directory.

	java -jar wfaas-util-consul-{version}.jar consul-read root/
	
## Consul Bulk Write

Writes all values from a properties file to Consul.

	java -jar wfaas-util-consul-{version}.jar consul-write project.properties
	
## Consul Endpoint

Specify the environment variable `consul.endpoint` to set the target Consul Endpoint.

	java -Dconsul.endpoint=http://host:port -jar wfaas-util-consul-{version}.jar consul-read root/
	
## Vault Bulk Write

Writes all values from a properties file to Vault using the specified token and mount point.

	java -jar wfaas-util-consul-{version}.jar vault-write TOKEN secret project.properties
	
## Vault Endpoint

Specify the environment variable `vault.endpoint` to set the target Vault Endpoint.

	java -Dvault.endpoint=http://host:port -jar wfaas-util-consul-{version}.jar vault-write TOKEN secret project.properties
